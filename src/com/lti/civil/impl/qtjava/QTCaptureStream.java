/*
 * Created on May 27, 2005
 */
package com.lti.civil.impl.qtjava;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.qd.QDConstants;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.image.CodecComponent;
import quicktime.std.image.DSequence;
import quicktime.std.image.ImageDescription;
import quicktime.std.image.Matrix;
import quicktime.std.image.QTImage;
import quicktime.std.sg.SGChannel;
import quicktime.std.sg.SGDataProc;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;
import quicktime.util.QTPointerRef;
import quicktime.util.RawEncodedImage;

import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.VideoFormat;
import com.lti.civil.impl.common.BufferedImageImage;
import com.lti.civil.impl.common.VideoFormatImpl;
import com.lti.civil.utility.LoggerSingleton;
import com.lti.utils.synchronization.CloseableThread;

// adapted from http://lists.apple.com/archives/quicktime-java/2005/Feb/msg00062.html
// then adapted from http://lists.apple.com/archives/QuickTime-java/2005/Nov/msg00036.html
/**
 *
 * @author Ken Larson
 */
public class QTCaptureStream implements CaptureStream
{
	// TODO: since we never copy the byte array, painting of controls with the data can have data mixed from 2 frames

	private static final Logger logger = LoggerSingleton.logger;

	private static final boolean ALLOC_NEW_IMAGE_EACH_FRAME = true;	// if false, we will re-use the buffer, but this causes the buffer to change while downstream code is still using it.

	private GrabberThread thread;
	private CaptureObserver observer;
	private final boolean bigEndian;

	public QTCaptureStream() throws QTException
	{
		super();
		bigEndian = System.getProperty("sun.cpu.endian").equals("big"); // TODO: there is some way to query this from quicktime/quickdraw directly
		logger.info("Big endian: " + bigEndian);

		initSequenceGrabber();
		enumVideoFormats();
	}

	private int frameCount = 0;
	// Data concerning the sequence grabber, its gWorld and its image size
	private SequenceGrabber sg;
	private QDRect cameraImageSize;
	private QDGraphics gWorld;
	// Data concerning building awt images from cameras gWorld
	private byte[] pixelData;
	private BufferedImage image;
	private SGVideoChannel vc;
	private int myCodec;
	private boolean sequenceGrabberInitialized;
	private boolean stopping;
	private boolean started;

	private void initSequenceGrabber() throws QTException
	{
		if (sequenceGrabberInitialized)
			return;
		QTSession.open();

		sg = new SequenceGrabber();

		QTException exception = null;
		final int RETRIES = 5;
		final int RETRY_INTERVAL_MS = 200;
        for (int i = 0; i < RETRIES; ++i)
        {
            try
            {
                vc = new SGVideoChannel(sg);
                break;
            }
            catch (QTException ex)
            {
                logger.info("Failed to initialize Video Channel" + (i < (RETRIES - 1) ? ", trying again" : ""));
                exception = ex;
                try
                {
                    Thread.sleep(RETRY_INTERVAL_MS);
                }
                catch(InterruptedException e)
                {
                	logger.log(Level.WARNING, "" + e, e);
                	return;	// TODO: what to do?
                }
            }
        }
        if (vc == null)
        {
            throw exception;
        }


		//cameraImageSize = new QDRect(320, 240);
		if (overrideVideoFormat != null)
			cameraImageSize = new QDRect(overrideVideoFormat.getWidth(), overrideVideoFormat.getHeight());
		else
		{
			cameraImageSize = vc.getSrcVideoBounds();
			logger.info("Camera image size reported as: " + cameraImageSize.getWidth() + "x" + cameraImageSize.getHeight());

			// this is a workaround found at http://rsb.info.nih.gov/ij/plugins/download/QuickTime_Capture.java
			// and other places for the isight, which gives the wrong resolution.
			// TODO: find a better way of identifying the isight.
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			if (cameraImageSize.getHeight()>screen.height-40) // iSight camera claims to 1600x1200!
			{	logger.warning("Camera image size reported as: " + cameraImageSize.getWidth() + "x" + cameraImageSize.getHeight() + "; resizing to 640x480");
				cameraImageSize.resize(640, 480);
			}


		}
		// On PPC (big endian) we use: k32ARGBPixelFormat
		// On Intel we use: k32ABGRPixelFormat
		// fails on PPC with DepthErrInvalid: k32ABGRPixelFormat, k32BGRAPixelFormat, k32RGBAPixelFormat
		gWorld = new QDGraphics(bigEndian ? QDConstants.k32ARGBPixelFormat : QDConstants.k32ABGRPixelFormat, cameraImageSize);	// set a specific pixel format so we can predictably convert to buffered image below.
		sg.setGWorld(gWorld, null);
		vc.setBounds(cameraImageSize);
		vc.setUsage(quicktime.std.StdQTConstants.seqGrabRecord);
		vc.setFrameRate(0);
		myCodec = quicktime.std.StdQTConstants.kComponentVideoCodecType;
		vc.setCompressorType(myCodec);
		sequenceGrabberInitialized = true;
	}

	private void disposeSequenceGrabber() throws QTException
	{
		if (!sequenceGrabberInitialized)
			return;
		try
		{
			if (vc != null)
				vc.disposeQTObject();

			if (sg != null)
			{	sg.stop();
				sg.disposeQTObject();
			}

			QTSession.close();
		}
		finally
		{
			sequenceGrabberInitialized = false;
		}
	}

	private void initBufferedImage()
	{
		pixelData = allocPixelData();
		image = allocBufferedImage(pixelData);
	}

	private byte[] allocPixelData()
	{
		//final int size = gWorld.getPixMap().getPixelData().getSize();
		final int intsPerRow = gWorld.getPixMap().getPixelData().getRowBytes() / 4;
		final int size = intsPerRow * cameraImageSize.getHeight();
		return new byte[size * 4];
	}

	private BufferedImage allocBufferedImage(byte[] bytes)
	{

		// Setting up the buffered image
		// bytesPerRow may be larger than needed for a single row.
		// for example, a Canon DV camera with 720 pixels across may have enough
		// space for 724 pixels.
		final int bytesPerRow = gWorld.getPixMap().getPixelData().getRowBytes();

		// using a byte[] instead of an int[] is more compatible with the allowed CIVIL output formats (always byte[]).

		final int w = cameraImageSize.getWidth();
		final int h = cameraImageSize.getHeight();

		// TODO: we don't need alpha...
		final DataBufferByte db = new DataBufferByte(new byte[][] {bytes}, bytes.length);

		final ComponentSampleModel sm
			= new ComponentSampleModel(
					DataBuffer.TYPE_BYTE, w, h, 4, bytesPerRow,
					// bigEndian ? ARGB : ABGR
					bigEndian ? new int[] {1, 2, 3, 0} : new int[] {3, 2, 1, 0}
					);
		final WritableRaster r = Raster.createWritableRaster(sm, db, new Point(0, 0));
		// construction borrowed from BufferedImage constructor, for BufferedImage.TYPE_4BYTE_ABGR
        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = {8, 8, 8, 8};
        //int[] bOffs = {3, 2, 1, 0};
        final ColorModel colorModel = new ComponentColorModel(cs, nBits, true, false,
                                             Transparency.TRANSLUCENT,
                                             DataBuffer.TYPE_BYTE);
        return new BufferedImage(colorModel, r, false, null);

	}


	private void startPreviewing() throws QTException
	{

		// Defining the data procedure which pushes the data into the image
		SGDataProc myDataProc = new SGDataProc()
		{
			DSequence ds = null;

			final Matrix idMatrix = new Matrix();

			byte[] rawData = new byte[QTImage.getMaxCompressionSize(gWorld, gWorld.getBounds(), 0, quicktime.std.StdQTConstants.codecLowQuality, myCodec,
					CodecComponent.anyCodec)];

			RawEncodedImage ri = null;

			public int execute(SGChannel chan, QTPointerRef dataToWrite, int offset, int chRefCon, int time, int writeType)
			{
				if (chan instanceof SGVideoChannel)
					try
					{
						if (!sequenceGrabberInitialized || !started || stopping)
							return 0;

						long timestamp = System.currentTimeMillis();

						ImageDescription id = vc.getImageDescription();
						if (rawData == null)
							rawData = new byte[dataToWrite.getSize()];
						RawEncodedImage ri = new RawEncodedImage(rawData);
						dataToWrite.copyToArray(0, rawData, 0, dataToWrite.getSize());
						if (ds == null)
						{
							ds = new DSequence(id, ri, gWorld, cameraImageSize, idMatrix, null, 0, quicktime.std.StdQTConstants.codecNormalQuality,
									CodecComponent.anyCodec);
						} else
						{
							ds.decompressFrameS(ri, quicktime.std.StdQTConstants.codecNormalQuality);
						}

						// allocate a new buffered image
						if (ALLOC_NEW_IMAGE_EACH_FRAME)
							initBufferedImage();

						// TODO: can we use gWorld.getPixMap().getPixelData().getBytes()? image is always black if we do so.
						gWorld.getPixMap().getPixelData().copyToArray(0, pixelData, 0, pixelData.length);

//						try
//						{
//							ImageIO.write(image, "PNG", new File("snap.png"));
//							++num;
//							if (num > 4)
//							System.exit(0);
//						}
//						catch (IOException e)
//						{	logger.log(Level.WARNING, "" + e, e);
//						}

						if (observer != null)
						{	observer.onNewImage(QTCaptureStream.this, new BufferedImageImage(image, timestamp));
						}

						return 0;

					} catch (Exception ex)
					{

						logger.log(Level.WARNING, "" + ex, ex);
						return 1;

					}
				else
					return 1;
			}

		};

		sg.setDataProc(myDataProc);

		// Preparing for output
		sg.setDataOutput(null, quicktime.std.StdQTConstants.seqGrabDontMakeMovie);
		sg.prepare(false, true);
		sg.startRecord();


	}
	//int num;

	private List<VideoFormat> videoFormats;
	public List<VideoFormat> enumVideoFormats()
	{
		if (videoFormats != null)
			return videoFormats;
		videoFormats = new ArrayList<VideoFormat>();
		videoFormats.add(new VideoFormatImpl(VideoFormat.RGB32, cameraImageSize.getWidth(), cameraImageSize.getHeight(), VideoFormat.FPS_UNKNOWN, VideoFormat.DATA_TYPE_BYTE_ARRAY));
		// just for fun, add one at quarter size:
		videoFormats.add(new VideoFormatImpl(VideoFormat.RGB32, cameraImageSize.getWidth() / 2, cameraImageSize.getHeight() / 2, VideoFormat.FPS_UNKNOWN, VideoFormat.DATA_TYPE_BYTE_ARRAY));
		return videoFormats;
	}

	private VideoFormat overrideVideoFormat;

	public void setVideoFormat(VideoFormat f) throws CaptureException
	{	overrideVideoFormat = f;
	}

	public VideoFormat getVideoFormat() throws CaptureException
	{	if (overrideVideoFormat != null)
			return overrideVideoFormat;
		return enumVideoFormats().get(0);
	}

	public void setObserver(CaptureObserver observer, int numBuffersHint)
	{	this.observer = observer;
	}
	
	public void setObserver(CaptureObserver observer) 
	{
		this.setObserver(observer, NUM_BUFFERS_HINT_DEFAULT);
	}

	public void start() throws CaptureException
	{
		if (started)
			return;

		if (thread != null)
		{	logger.log(Level.WARNING, "QTCaptureStream already started, start called without stop, ignoring");
			return;
		}
		try
		{

			initSequenceGrabber();

			initBufferedImage();

			startPreviewing();

		} catch (QTException e)
		{
			throw new CaptureException(e);
		}
        if (thread == null)
        {
	        thread = new GrabberThread();
	        thread.start();
        }
        started = true;


	}

	class GrabberThread extends CloseableThread
	{

		public GrabberThread()
		{
			super(Thread.currentThread().getThreadGroup(), "GrabberThread");
			setDaemon(true);
		}

		public void close()
		{
			//super.close();
			setClosing();	// don't interrupt
		}
		private final int taskingDelay = 25;
		public void run()
		{

			try
			{

				QTSession.open();
				while (!isClosing())
				{
					Thread.sleep(taskingDelay);
					sg.idleMore();
					sg.update(null);

				}

			}
			catch (InterruptedException ex)
			{

			}
			catch (Exception ex) {
				if (observer != null && !isClosing())
					observer.onError(QTCaptureStream.this, new CaptureException(ex));
			}

			finally
			{
				QTSession.close();
				setClosed();
			}




		}

	}

	public void stop() throws CaptureException
	{
		if (!started)
			return;

		stopping = true;

		if (thread != null)
			thread.close();


		if (thread != null)
		{
			try
			{
				logger.fine("Waiting for GrabberThread to complete...");
				thread.waitUntilClosed();
			} catch (InterruptedException e)
			{
				logger.log(Level.WARNING, "" + e, e);
				return;
			}
		}
		logger.fine("GrabberThread completed");
		thread = null;

		try
		{
			disposeSequenceGrabber();
		} catch (QTException e)
		{
			throw new CaptureException(e);
		}

		started = false;
		stopping = false;

	}

	public void dispose() throws CaptureException
	{
		stop();

	}
}

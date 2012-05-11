package com.lti.swtutils.image;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lti.swtutils.ModalShellRunner;
import com.lti.swtutils.SWTUtils;
import com.lti.utils.synchronization.MessageDrivenThread;
import com.lti.utils.synchronization.MessageDrivenThreadListener;
import com.lti.utils.synchronization.SynchronizedBoolean;
import com.lti.utils.synchronization.ThreadGroupMgr;

/**
 * 
 * @author Ken Larson
 *
 */
public class ImageControl extends Composite
{
	private static final Logger logger = Logger.getLogger(ImageControl.class.getName());

	private Label imageBox;
	private Canvas imageCanvas;

	private Point imageFrameSize = new Point(320, 240);
	private static final String NO_IMAGE_TEXT = "No Image";
	private static final boolean TRACE = false;
	
	// TODO: any images we need to dispose of?
	// TODO: why does the canvas not work on Linux?  Could it be related to the v4l impl?
	// not using the canvas is VERY slow.
	private static final boolean USE_CANVAS = true;

	private static final int NUM_COLUMNS = 1;
	
	private boolean scaleImageToFit = false;
	
	private static final boolean USE_GC_TO_SCALE = true;
	private static final boolean SCALE_EXACT = true;
	
	private boolean doubleLines = false;

	
	public boolean isDoubleLines()
	{
		return doubleLines;
	}


	public void setDoubleLines(boolean doubleLines)
	{
		this.doubleLines = doubleLines;
	}


	public ImageControl(Composite parent, int style)
	{
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		//gridLayout.makeColumnsEqualWidth = true;
		gridLayout.numColumns = NUM_COLUMNS;
		gridLayout.marginBottom = gridLayout.marginTop = gridLayout.marginLeft = gridLayout.marginRight = 0;
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		if (!USE_CANVAS)
		{
			imageBox = new Label(this, /*SWT.BORDER |*/ SWT.CENTER);
			final GridData gridData = new GridData(GridData.FILL_BOTH);
			gridData.horizontalSpan = NUM_COLUMNS;
			gridData.heightHint = imageFrameSize.y;
			gridData.widthHint = imageFrameSize.x;
			imageBox.setLayoutData(gridData);
			imageBox.setText(NO_IMAGE_TEXT);
		}
		else
		{
			imageCanvas = new Canvas(this, /*SWT.BORDER | */ /*| SWT.CENTER | */ SWT.NO_BACKGROUND);
			final GridData gridData = new GridData(GridData.FILL_BOTH);
			gridData.horizontalSpan = NUM_COLUMNS;
			gridData.heightHint = imageFrameSize.y;
			gridData.widthHint = imageFrameSize.x;
			imageCanvas.setLayoutData(gridData);
	
			imageCanvas.addPaintListener(new PaintListener() {
			      public void paintControl(PaintEvent e) {
				    	//logger.debug("paintControl");
				        // Draws the buffer image onto the canvas.
				    	if (image != null)
				    	{	
				    		final long now = System.currentTimeMillis();
				    		
				    		
			    			int drawX = 0;
				    		int drawY = 0;
				    		
				    		final int frameWidth = imageCanvas.getSize().x;
				    		final int frameHeight = imageCanvas.getSize().y;

				    		final int imageWidth;
				    		final int imageHeight;
				    		if (USE_GC_TO_SCALE && scaleImageToFit)
				    		{
				    			final Point scaledTo = calcScaledTo(image);	// TODO: if no scale, use faster algorithm below.
				    			
				    			imageWidth = scaledTo.x;
					    		imageHeight = scaledTo.y;
				    		}
				    		else
				    		{
				    			imageWidth = image.getBounds().width;
					    		imageHeight = image.getBounds().height * (doubleLines ? 2 : 1);
				    		}
				    		
				    		// if smaller than frame, center it:
				    		if (imageWidth < frameWidth)
				    			drawX = (frameWidth - imageWidth) / 2;
				    		if (imageHeight < frameHeight)
				    			drawY = (frameHeight - imageHeight) / 2;
				    		
				    		
				    		final long now2 = System.currentTimeMillis();
				    		if (USE_GC_TO_SCALE && (scaleImageToFit || doubleLines) && (imageWidth != image.getBounds().width || imageHeight != image.getBounds().height))
				    		{
				    			// TODO: see http://www.eclipse.org/articles/Article-SWT-images/graphics-resources.html
					    		// for scaling info.
					    		// GC.drawImage(Image image, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, int dstWidth, int dstHeight)
				    			e.gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, drawX, drawY, imageWidth, imageHeight);
				    		}
				    		else
				    		{
				    			e.gc.drawImage(image, drawX, drawY);
				    		}
				    		
				    		if (TRACE) 
							{
								final long after = System.currentTimeMillis();
								logger.debug("drawImage took: " + (after - now2));
							}
				    		
				    		// fill the areas around the image:
				    		// TODO: could we be cutting off 1 pixel here?
				    		// TODO: clipping?
				    		if (imageWidth < frameWidth)
				    		{	e.gc.fillRectangle(0, 0, drawX, frameHeight);
				    			e.gc.fillRectangle(drawX + imageWidth, 0, frameWidth - imageWidth, frameHeight);
				    		
				    		}
				    		if (imageHeight < frameHeight)
				    		{	e.gc.fillRectangle(0, 0, frameWidth, drawY);
				    			e.gc.fillRectangle(0, drawY + imageHeight, frameWidth, frameHeight - imageHeight);
				    		}
			    		
				    		
				    		if (TRACE) 
							{
								final long after = System.currentTimeMillis();
								logger.debug("paintControl took: " + (after - now));
							}
				    		
				    	}
				    	else
				    		e.gc.fillRectangle(e.gc.getClipping());
				    	// TODO: otherwise blank.
				        
				      }
				    });
			}

		imageProxyLoaderThread = new MessageDrivenThread(ThreadGroupMgr.getDefaultThreadGroup(), "MessageDrivenThread for ImageControl", new MessageDrivenThreadListener(){

			public void onMessage(MessageDrivenThread sender, Object o)
			{
				if (isDisposed())
					return;
				final ImageProxy p = (ImageProxy) o;
				final Image image = p.getImage();
				getDisplay().asyncExec(new Runnable(){

					public void run()
					{
						setImage(image);
					}});
			}});
		
		
	}
	

	public void addMouseListener(MouseListener ml)
	{		
		super.addMouseListener(ml);
		if (imageBox != null)
			imageBox.addMouseListener(ml);
		
		if (imageCanvas != null)
			imageCanvas.addMouseListener(ml);
	
	}
	
//	public void addDragSource(DragSourceListener list)
//	{
//		DndUtils.addDragSource(list, this);
//		
//		if (imageBox != null)
//			DndUtils.addDragSource(list, imageBox);
//		
//		if (imageCanvas != null)
//			DndUtils.addDragSource(list, imageCanvas);
//				
//	}
	
	
	private Image image;
	public void setImage(Image image)
	{
		if (this.image != null && this.image != image)
			SWTUtils.dispose(this.image);
		
		if (scaleImageToFit)
		{
			if (image == null)
			{
				this.scaledTo = new Point(0, 0);
			}
			else
			{
				this.scaledTo = calcScaledTo(image);
				if (this.scaledTo == null)
					this.scaledTo = new Point(0, 0);
			}
		}
		
		if (!USE_GC_TO_SCALE && (scaleImageToFit || doubleLines))
		{
			final Image unscaledImage = image;
			if (scaleImageToFit)
				image = scaleImageToFit(image);
			else
				image = doubleLines(image);
			
			if (unscaledImage != image && unscaledImage != null)
				SWTUtils.dispose(unscaledImage);
		}
		
		this.image = image;
		if (!USE_CANVAS)
		{
			if (image == null)
			{
				imageBox.setText(NO_IMAGE_TEXT);
			}
			else
			{
				final long now = System.currentTimeMillis();
				
				imageBox.setImage(image);
				//imageBox.update();
				
				if (TRACE) 
				{
					final long after = System.currentTimeMillis();
					logger.debug("setImage took: " + (after - now));
				}
			}
		}
		else
		{
			imageCanvas.redraw();
		}
		
		// calculate fps;
		long now = System.currentTimeMillis();
		if (lastImageTime > 0)
		{	long period = now - lastImageTime;
			double fps = 1000.0 / (double) period;
			fps = (double) ((long) (fps * 1000)) / 1000;
			this.fps = fps;
		}
		
		lastImageTime = now;
		
	}
	
	
	public boolean isScaleImageToFit()
	{
		return scaleImageToFit;
	}


	public void setScaleImageToFit(boolean scaleImageToFit)
	{
		this.scaleImageToFit = scaleImageToFit;
	}

	private Point scaledTo = new Point(0, 0);

	private Image scaleImageToFit(Image image)
	{
		final Point scaledTo = calcScaledTo(image);
		if (scaledTo == null)
			return null;

		
		if (image.getBounds().width == scaledTo.x && image.getBounds().height == scaledTo.x)
			return image;
		
		return new Image(getShell().getDisplay(), SWTUtils.getImageData(image).scaledTo(scaledTo.x, scaledTo.y));
		
	}
	
	private Image doubleLines(Image image)
	{
		if (image == null)
			return null;
	
		
		return new Image(getShell().getDisplay(), SWTUtils.getImageData(image).scaledTo(image.getBounds().width, image.getBounds().height * 2));
		
	}
	
	private Point calcScaledTo(Image image)
	{
		if (SCALE_EXACT)
			return calcScaledToExact(image);
		else
			return calcScaledToMuliple(image);
	}
	
	private Point calcScaledToExact(Image image)
	{
		if (image == null)
			return null;
		final Control frameControl = USE_CANVAS ? imageCanvas : imageBox;
		final Point frameControlSize = frameControl.getSize();
		final int frameWidth = frameControlSize.x;
		final int frameHeight = frameControlSize.y;
		
		final int imageWidth = image.getBounds().width;
		final int imageHeight = image.getBounds().height * (doubleLines ? 2 : 1);
	
		if (imageWidth == frameWidth && imageHeight == frameHeight)
			return frameControlSize;	// no need to scale
		
		if (frameWidth == 0 || frameHeight == 0)
		{	return null;	// nothing to display - no room.  TODO: should be blank, instead of prev image
		}
		final double xRatio = (double) imageWidth / (double) frameWidth;
		final double yRatio = (double) imageHeight / (double) frameHeight;
		final Point scaledTo = new Point(0, 0);
		if (xRatio == yRatio)
		{	scaledTo.x = frameWidth;
			scaledTo.y = frameHeight;
		}
		else if (xRatio > yRatio)	// if it is proportionally bigger in the x direction, then fill the x dimension, and the y dimension will have extra space.
		{	scaledTo.x = frameWidth;
			scaledTo.y = (int) (frameWidth * (double) imageHeight / (double) imageWidth);
		}
		else
		{	scaledTo.x = (int) (frameHeight * (double) imageWidth / (double) imageHeight);
			scaledTo.y = frameHeight;
		}
		
		return scaledTo;
		
		
	
	}
	
	private Point calcScaledToMuliple(Image image)
	{
		if (image == null)
			return null;
		final Control frameControl = USE_CANVAS ? imageCanvas : imageBox;
		final Point frameControlSize = frameControl.getSize();
		final int frameWidth = frameControlSize.x;
		final int frameHeight = frameControlSize.y;
		

		
		final int imageWidth = image.getBounds().width;
		final int imageHeight = image.getBounds().height * (doubleLines ? 2 : 1);
		
		if (imageWidth == frameWidth && imageHeight == frameHeight)
			return frameControlSize;	// no need to scale
		
		if (frameWidth == 0 || frameHeight == 0)
		{	return null;	// nothing to display - no room.  TODO: should be blank, instead of prev image
		}
		
		if (imageWidth <= frameWidth && imageHeight <= frameHeight)
		{
			return new Point(imageWidth, imageHeight);	// no need to scale
		
		// TODO: calc larger multiples
		}
		else
		{
			// shrink down
		
			for (int scale = 1; scale < 1000; ++scale)
			{
				int scaledWidth = imageWidth / scale;
				int scaledHeight = imageHeight / scale;
				if (scaledWidth <= 0 || scaledHeight <= 0)
					return null;	// no room
				
				if (scaledWidth <= frameWidth && scaledHeight <= frameHeight)
					return new Point(scaledWidth, scaledHeight);	
	
			}
			
			return null;	// no room
		}

		
	
	}
	
	public Point getScaledTo()
	{
		return scaledTo;
	}


	/**
	 * May be called from any thread.
	 * @param p
	 */
	public void queueImage(ImageProxy p)
	{
		try
		{
			imageProxyLoaderThread.post(p);
		} catch (InterruptedException e)
		{
			// ignore
		}
	}
	
	private MessageDrivenThread imageProxyLoaderThread;
	
	
	public void setImageSize(int x, int y)
	{

		if (doubleLines)
			y *= 2;
		
		if (x == imageFrameSize.x && y == imageFrameSize.y)
			return;
		imageFrameSize.y = y;
		imageFrameSize.x = x;
		if (!isScaleImageToFit())
		{
			final GridData gridData = new GridData(GridData.FILL_BOTH);
			gridData.horizontalSpan = NUM_COLUMNS;
			gridData.heightHint = imageFrameSize.y;
			gridData.widthHint = imageFrameSize.x;
			final Control frameControl = USE_CANVAS ? imageCanvas : imageBox;
			frameControl.setLayoutData(gridData);
			layout();
		}
		if (imageSizeChangedListener != null)
			imageSizeChangedListener.onImageSizeChanged(this);
		
		
		
	}
	
	public Point getImageSize()
	{	return imageFrameSize;
	}
	
	
	private ImageSizeChangedListener imageSizeChangedListener = new PackImageSizeChangedListener();
	

	private long lastImageTime;
	private double fps;
	
	private final SynchronizedBoolean disposing = new SynchronizedBoolean(false);
	



	public void dispose()
	{	
		imageProxyLoaderThread.close();
	}
	

	public double getFps()
	{
		return fps;
	}

	public static void runShell(Shell shell, Image image)
	{
		shell.setLayout(new FillLayout()); 
//		Composite c1 = new Composite(shell, 0);
//		c1.setLayout(new FillLayout());
		ImageControl c = new ImageControl(shell, 0);
		shell.setText("Image");
		c.setImage(image);
		shell.pack();
		ModalShellRunner.run(shell);

		c.dispose();
	}
	
	public static void run(Shell parent, Image image)
	{
		final Shell shell = new Shell(parent);
		runShell(shell, image);
	}
	public static void run(Display parent, Image image)
	{
		final Shell shell = new Shell(parent);
		runShell(shell, image);
	}
	
	public static void main(String[] args) throws Exception
	{
		final Display display = new Display();
		
//		final String path = "/home/ken/Dev/LTI/civil/image.jpeg";
//		final Image image = new Image(display, new FileInputStream(path));          
         
		run(display, null);
		display.dispose();
	}

	public void setImageSizeChangedListener(ImageSizeChangedListener imageSizeChangedListener)
	{
		this.imageSizeChangedListener = imageSizeChangedListener;
	}
	
	public void exportImageToJpegFile(String path)	throws IOException
	{
		if (image == null)
			throw new IOException("No image to export");
		final Image exportImage;
		if (doubleLines)
			exportImage = doubleLines(image);
		else
			exportImage = image;
		
		final ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] {SWTUtils.getImageData(exportImage)};
		imageLoader.save(path, SWT.IMAGE_JPEG);
		if (exportImage != image)
			SWTUtils.dispose(exportImage);
		
	}
}

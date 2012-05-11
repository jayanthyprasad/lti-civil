package com.lti.swtutils.image;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lti.swtutils.DefaultMessageBox;
import com.lti.utils.synchronization.SynchronizedBoolean;
import com.lti.utils.synchronization.SynchronizedObjectHolder;

/**
 * This class is intended to be subclassed to display video, where the individual
 * frames are in some subclass-specific FrameT class.
 * 
 * This base class handles issues like painting the images as fast as possible, and
 * dropping frames if the painting doesn't keep up.
 * 
 * @author Ken Larson
 *
 */
public abstract class AbstractVideoControl<FrameT> extends Composite
{
	private static final Logger logger = Logger.getLogger(AbstractVideoControl.class.getName());
	
	private final ImageControl imageControl;
	private final VideoToolbarControl videoToolbarControl;
	private final VideoStatusbarControl videoStatusbarControl;
	
	// TODO: any images we need to dispose of?

	private static final int NUM_COLUMNS = 1;
	
	public static final int DEFAULT = 0;
	
	public static final int NO_REPACK_ON_NEW_SIZE = 0x0001;
	public static final int SCALE_IMAGE_TO_FIT = 0x0002;
	public static final int DEFAULT_EMBED = NO_REPACK_ON_NEW_SIZE | SCALE_IMAGE_TO_FIT;
	
	private final int abstractVideoControlStyle;
	
	private static final boolean TRACE = false;

	public AbstractVideoControl(Composite parent, int style, int videoToolbarStyle, int numStatusBarComponents)
	{
		this(parent, style, videoToolbarStyle, numStatusBarComponents, DEFAULT);
	}
	
	public AbstractVideoControl(Composite parent, int style, int videoToolbarStyle, int numStatusBarComponents, int abstractVideoControlStyle)
	{
		super(parent, style);
		this.abstractVideoControlStyle = abstractVideoControlStyle;
		
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = gridLayout.marginWidth =0;
		gridLayout.marginTop = gridLayout.marginBottom = gridLayout.marginLeft = gridLayout.marginRight = 0;
		gridLayout.verticalSpacing = gridLayout.horizontalSpacing = 0;
		
		//gridLayout.makeColumnsEqualWidth = true;
		gridLayout.numColumns = NUM_COLUMNS;
		setLayout(gridLayout);
		imageControl = new ImageControl(this, SWT.NONE);
		{
			final GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
			imageControl.setLayoutData(gd);
		}
		
		imageControl.setScaleImageToFit((abstractVideoControlStyle & SCALE_IMAGE_TO_FIT) != 0);
		
	
		videoToolbarControl = new VideoToolbarControl(this, SWT.NONE, videoToolbarStyle);
		
	
		{
			videoStatusbarControl = new VideoStatusbarControl(this, SWT.NONE, numStatusBarComponents);
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			videoStatusbarControl.setLayoutData(gridData);
			
		}
		
		if ((abstractVideoControlStyle & NO_REPACK_ON_NEW_SIZE) != 0)
			imageControl.setImageSizeChangedListener(null);
//		setImageSize(320, 200);
//		setImage((Image) null);
		getDisplay().asyncExec(new InitRunnable());
	}
	
	public ImageControl getImageControl()
	{
		return imageControl;
	}

	public VideoToolbarControl getVideoToolbarControl()
	{
		return videoToolbarControl;
	}

	public VideoStatusbarControl getVideoStatusbarControl()
	{
		return videoStatusbarControl;
	}

	
	private class InitRunnable implements Runnable
	{
		public void run()
		{
			if (disposing.getValue())
				return;
			if (isDisposed())
				return;
			try
			{
				doInit();
			}
			catch (Exception e)
			{	showError(e);
			}
			
		}
	}
	
	private final SynchronizedBoolean disposing = new SynchronizedBoolean(false);
	
	public abstract void doInit() throws Exception;

	public abstract void doDispose() throws Exception;
	
	public final void dispose()
	{	try
		{	doDispose();	// TODO: is this ever getting called?
		}
		catch (Exception e)
		{	logger.warn(e, e);
		}
		super.dispose();
	}
	
	protected void showError(Exception e)
	{
		logger.warn(e, e);
		DefaultMessageBox.showError(getShell(), e);

	}
	
	// used to collapse multiple outstanding frames - only paint the most recent, in case
	// we can't keep up with the frame rate.
	private SynchronizedObjectHolder<FrameT> frameHolder = new SynchronizedObjectHolder<FrameT>();
	private FrameT lastDisplayedFrame;
	private SynchronizedBoolean frameRunnablePending = new SynchronizedBoolean(false);	// used to prevent lots of stacked up async execs of the frameRunnable.
	private final FrameRunnable frameRunnable = new FrameRunnable();
	
	protected void postFrame(FrameT frame)
	{
		if (disposing.getValue())
			return;
		if (isDisposed())
			return;
		//logger.debug("onNewImage " + sender + " " + image);
		//logger.debug("onNewImage image: format=" + image.getFormat() + " width=" + image.getWidth() + " height=" + image.getHeight() + " size=" + image.getBytes().length);

		//Image swtImage1 = new Image(getDisplay(), convertToSWT(bimg));
		frameHolder.setObject(frame);
		synchronized (frameRunnablePending)
		{
			if (!frameRunnablePending.getValue())
			{
				frameRunnablePending.setValue(true);
				getDisplay().asyncExec(frameRunnable);	// sync exec can cause deadlock on closing.
			}
		}
	}
	
	protected abstract Image processFrame(FrameT frame);
	protected abstract void processEOS();
	private boolean frameObjectsReused;	// if true, we don't check frame == lastDisplayedFrame (civil needs this)
	
	
	private class FrameRunnable implements Runnable
	{
		public FrameRunnable()
		{
			super();
			
		}
		public void run()
		{
			frameRunnablePending.setValue(false);
			
			if (disposing.getValue())
				return;
			if (isDisposed())
				return;
			
			
			
			final FrameT frame = frameHolder.getObject();
			
			
			if (frame == null)	// EOS
			{	
				processEOS();
				return;
			}
			if (!frameObjectsReused && frame == lastDisplayedFrame)	// prevent painting same image twice
				return;
			lastDisplayedFrame = frame;
			
			
			final long now = System.currentTimeMillis();
			
			final Image image = processFrame(frame);
			
			if (TRACE) 
			{
				final long after = System.currentTimeMillis();
				logger.debug("processFrame took: " + (after - now));
			}
			
			getImageControl().setImageSize(image.getBounds().width, image.getBounds().height);
			getImageControl().setImage(image);
			//getVideoStatusbarControl().setText(0, "FPS: " + getImageControl().getFps());
			if (getImageControl().isScaleImageToFit())
			{
				getVideoStatusbarControl().setText(1, "" + getImageControl().getImageSize().x + "x" + getImageControl().getImageSize().y + " @ " + getImageControl().getScaledTo().x + "x" + getImageControl().getScaledTo().y);
			}
			else
			{
				getVideoStatusbarControl().setText(1, "" + getImageControl().getImageSize().x + "x" + getImageControl().getImageSize().y);
			}
			
		}
	}


	public boolean isFrameObjectsReused()
	{
		return frameObjectsReused;
	}

	public void setFrameObjectsReused(boolean frameObjectsReused)
	{
		this.frameObjectsReused = frameObjectsReused;
	}



}

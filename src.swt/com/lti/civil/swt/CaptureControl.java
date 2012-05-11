/*
 * Created on Aug 29, 2004
 */
package com.lti.civil.swt;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;
import com.lti.civil.utility.LoggerSingleton;
import com.lti.swtutils.image.AbstractVideoControl;
import com.lti.swtutils.image.VideoToolbarControl;
import com.lti.swtutils.image.VideoToolbarControlAdapter;
import com.lti.utils.synchronization.SynchronizedBoolean;

/**
 * Sample control show video and take snapshots.
 * @author Ken Larson
 */
public class CaptureControl extends AbstractVideoControl<com.lti.civil.Image>
{
    private static final Logger logger = LoggerSingleton.logger;

    // TODO: any images we need to dispose of?

    public CaptureControl(Composite parent, int style, CaptureSystemFactory factory)
    {
        super(parent, style, VideoToolbarControl.SNAP, 2);
        setFrameObjectsReused(true);

        this.factory = factory;

        getVideoToolbarControl().setListener(new VideoToolbarControlAdapter(){

            @Override
            public void onSnap(VideoToolbarControl sender)
            {
                snapNext = true;
            }});
    }



    public void setImage(com.lti.civil.Image image)
    {
        if (image == null)
            getImageControl().setImage((Image) null);
        else
        {

            Image swtImage = SWTImageConverter.convertToSWTImage(getDisplay(), image);
            getImageControl().setImage(swtImage);
        }


    }

    protected Image processFrame(com.lti.civil.Image image)
    {
        final Image result = new Image(getDisplay(), SWTImageConverter.convertToSWTImageData(image));
        if (snapNext)
        {	snapNext = false;
            snap(image);
        }
        return result;
    }
    protected void processEOS()
    {
    }


    private CaptureSystem system;
    private CaptureStream captureStream;
    private final CaptureSystemFactory factory;
    private boolean snapNext;
    private final SynchronizedBoolean disposing = new SynchronizedBoolean(false);

    public void doInit() throws CaptureException
    {

        system = factory.createCaptureSystem();
        system.init();
        final List<CaptureDeviceInfo> list = system.getCaptureDeviceInfoList();
        String[] choices = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            final CaptureDeviceInfo info = list.get(i);
            choices[i] = info.getDescription();
        }

        String device = (String) SWTOptionPane.showInputDialog(getShell(),
                "Select Capture Device:", "Capture Device",
                SWTOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
        for (int i = 0; i < list.size(); i++)
        {
            final CaptureDeviceInfo info = list.get(i);
            if (info.getDescription().equals(device)) {
                logger.fine("Device ID " + i + ": " + info.getDeviceID());
                logger.fine("Description " + i + ": " + info.getDescription());
                captureStream = system.openCaptureDeviceStream(
                        info.getDeviceID());
                captureStream.setObserver(new MyCaptureObserver(), 1);
                captureStream.start();
                break;
            }
        }
    }

    public void doDispose() throws CaptureException
    {
        disposing.setValue(true);
        if (captureStream != null)
        {	logger.fine("disposeCapture: stopping capture stream...");
            captureStream.stop();
            logger.fine("disposeCapture: stopped capture stream.");
            captureStream.dispose();
            captureStream = null;
        }

        if (system != null)
            system.dispose();
        logger.fine("disposeCapture done.");
    }


    class MyCaptureObserver implements CaptureObserver
    {

        public void onError(CaptureStream sender, CaptureException e)
        {
            logger.log(Level.WARNING, "onError " + sender + ": "  + e, e);
        }


        public void onNewImage(CaptureStream sender, com.lti.civil.Image image)
        {
            try
            {
                postFrame(image);
            }
            catch (Throwable t)
            {	logger.log(Level.SEVERE, "" + t, t);
            }
        }

    }
    void snap(com.lti.civil.Image image)
    {
        if (listener != null)
            listener.onSnap(image);
    }

    private CaptureControlListener listener = new DefaultCaptureControlListener();

    public void setListener(CaptureControlListener listener)
    {	this.listener = listener;
    }
    public static void runShell(Shell shell, CaptureSystemFactory factory, CaptureControlListener listener) throws CaptureException
    {
        shell.setLayout(new FillLayout());
        CaptureControl c = new CaptureControl(shell, 0, factory);
        c.setListener(listener);
        shell.setText("Image Capture");
        shell.pack();
        shell.open();
        while (!shell.isDisposed())
        {	if (!shell.getDisplay().readAndDispatch())
                shell.getDisplay().sleep();
        }
        c.dispose();
    }

    public static void run(Shell parent, CaptureSystemFactory factory, CaptureControlListener listener) throws CaptureException
    {
        Shell shell = new Shell(parent);
        runShell(shell, factory, listener);
    }
    public static void run(Display parent, CaptureSystemFactory factory, CaptureControlListener listener) throws CaptureException
    {
        Shell shell = new Shell(parent);
        runShell(shell, factory, listener);
    }

    public static void main(String[] args) throws CaptureException
    {
        Display display = new Display();
        run(display, DefaultCaptureSystemFactorySingleton.instance(), new DefaultCaptureControlListener());
        display.dispose();
    }
}

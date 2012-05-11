package com.lti.civil.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;
import com.lti.civil.VideoFormat;
import com.lti.civil.awt.AWTImageConverter;
import com.lti.civil.utility.LoggerSingleton;
import com.lti.civil.utility.VideoFormatNames;

/**
 * Simple GUI to display captured video.
 * @author Ken Larson
 *
 */
public class CaptureFrame extends ImageFrame
{
    private static final Logger logger = LoggerSingleton.logger;

	public static void main(String[] args) throws CaptureException
	{
		new CaptureFrame(DefaultCaptureSystemFactorySingleton.instance()).run();

	}

	private CaptureSystem system;
	private CaptureStream captureStream;
	private final CaptureSystemFactory factory;
	private volatile boolean disposing = false;


	public CaptureFrame(CaptureSystemFactory factory)
	{
		super("LTI-CIVIL");
		this.factory = factory;

	}

	public void run() throws CaptureException
	{

		initCapture();

		if (captureStream == null)
			System.exit(0);

		setSize(captureStream.getVideoFormat().getWidth(), captureStream.getVideoFormat().getHeight());
		setLocation(200, 200);


		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				try
				{
					disposeCapture();
				} catch (CaptureException e1)
				{
					logger.log(Level.SEVERE, "" + e1, e1);
				}
				System.exit(0);
			}
		});



		setVisible(true);
		pack();

		startCapture();
	}

	private class Format {
		private VideoFormat format = null;

		private Format(VideoFormat format) {
			this.format = format;
		}

		public String toString() {
			return format.getWidth() + " x " + format.getHeight() + " (" + VideoFormatNames.formatTypeToString(format.getFormatType()) + ")";
		}
	}

	public void initCapture() throws CaptureException
	{

		system = factory.createCaptureSystem();
		system.init();
        final List<CaptureDeviceInfo> list = system.getCaptureDeviceInfoList();
        Vector<CaptureDevice> choices = new Vector<CaptureDevice>();
        for (int i = 0; i < list.size(); i++)
        {
            final CaptureDeviceInfo info = list.get(i);
            String[] outputs = info.getOutputNames();
            if (outputs.length <= 1) {
                choices.add(new CaptureDevice(info));
            } else {
            	for (int j = 0; j < outputs.length; j++) {
            		choices.add(new CaptureDevice(info, j));
            	}
            }
        }

        CaptureDevice device = (CaptureDevice) JOptionPane.showInputDialog(null,
                "Select Capture Device:", "Capture Device",
                JOptionPane.QUESTION_MESSAGE, null, choices.toArray(),
                choices.get(0));
        if (device == null) {
        	System.exit(0);
        }

        CaptureDeviceInfo info = device.getInfo();
        int output = device.getOutput();
        int input = 0;
        if (info.getOutputNames().length >= 1) {
        	String[] inputs = info.getInputNames(output);
        	if (inputs.length > 0) {
        		String inputName = (String) JOptionPane.showInputDialog(null,
                        "Select Input:", "Input",
                        JOptionPane.QUESTION_MESSAGE, null, inputs,
                        null);
        		if (inputName == null) {
        			System.exit(0);
        		}
        		for (int i = 0; i < inputs.length; i++) {
        			if (inputs[i].equals(inputName)) {
        				input = i;
        				break;
        			}
        		}
        	}
        }

        System.err.println(info.getDeviceID());
        logger.fine("Device ID: " + info.getDeviceID());
        logger.fine("Description: " + info.getDescription());
        captureStream = system.openCaptureDeviceStreamOutput(
                info.getDeviceID(), output, input);
        List<VideoFormat> formats = captureStream.enumVideoFormats();
        VideoFormat def = captureStream.getVideoFormat();
        Format[] formatChoices = new Format[formats.size()];
        Format defaultFormat = null;
        for (int j = 0; j < formats.size(); j++) {
        	VideoFormat format = formats.get(j);
        	formatChoices[j] = new Format(format);
        	if (format.equals(def)) {
        		defaultFormat = formatChoices[j];
        	}
        }

        Format format = (Format) JOptionPane.showInputDialog(null,
                "Select Format:", "Format",
                JOptionPane.QUESTION_MESSAGE, null, formatChoices, defaultFormat);
        if (format == null) {
        	System.exit(0);
        }
        captureStream.setVideoFormat(format.format);
        captureStream.setObserver(new MyCaptureObserver(), 10);
        captureStream.start();
	}

	public void startCapture() throws CaptureException
	{
		captureStream.start();
	}

	public void disposeCapture() throws CaptureException
	{
		disposing = true;

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
			if (disposing)
				return;
			try
			{
				setImage(AWTImageConverter.toBufferedImage(image));
			}
			catch (Throwable t)
			{	logger.log(Level.SEVERE, "" + t, t);
			}
		}

	}
}

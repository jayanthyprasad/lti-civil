package com.lti.swtutils.image;

import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.lti.swtutils.DefaultMessageBox;

/**
 * 
 * @author Ken Larson
 *
 */
public class JpegSaver
{

	public static String getJpegSaveAsPath(Shell shell)
	{
		final FileDialog dlg = new FileDialog(shell, SWT.SAVE);
		dlg.setFilterExtensions(new String[] {"*.jpg"}); //$NON-NLS-1$
		dlg.setFilterNames(new String[] {"JPEG files (*.jpg)"});	// TODO: externalize
		dlg.setFileName("Untitled.jpg");
		return dlg.open();	
	}
	
	public static void saveJpeg(byte[] data, String outputPath) throws IOException
	{
	
		final FileOutputStream fos = new FileOutputStream(outputPath);
		fos.write(data);
		fos.close();

	}
	
	public static boolean promptAndSaveJpeg(Shell shell, byte[] data)
	{
		final String path = getJpegSaveAsPath(shell);
		if (path == null)
			return false;
		try
		{
			saveJpeg(data, path);
			return true;
		} catch (IOException e)
		{
			DefaultMessageBox.showError(shell, e);
			return false;
		}
		
	}
}

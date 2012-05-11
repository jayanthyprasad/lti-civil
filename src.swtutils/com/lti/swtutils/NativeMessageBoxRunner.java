/*
 * Created on May 2, 2005
 */
package com.lti.swtutils;

import org.eclipse.swt.widgets.Shell;

import com.lti.swtutils.swt_rwt.MessageBoxResultListener;

/**
 * Uses SWT native message box.  This is the default implementation.
 * @author Ken Larson
 */
public class NativeMessageBoxRunner implements MessageBoxRunner
{
	public int run(Shell shell, String message, String title, int style)
	{
		org.eclipse.swt.widgets.MessageBox m = new org.eclipse.swt.widgets.MessageBox(shell, style);
		m.setMessage("" + message); //$NON-NLS-1$
		m.setText(title);
		return m.open();
	}
	
	public void run(Shell shell, String message, String title, int style, MessageBoxResultListener listener)
	{
		listener.onMessageBoxResult(run(shell, message, title, style));	// blocks - will not work with RWT.
		
	}
}

/*
 * Created on May 2, 2005
 */
package com.lti.swtutils;

import org.eclipse.swt.widgets.Shell;

import com.lti.swtutils.swt_rwt.MessageBoxResultListener;

/**
 * Allows us to swap out the code that shows the message box in DefaultMessageBox.
 * @author Ken Larson
 */
public interface MessageBoxRunner
{
	public int run(Shell shell, String message, String title, int style);
	public void run(Shell shell, String message, String title, int style, MessageBoxResultListener listener);
	
	
}

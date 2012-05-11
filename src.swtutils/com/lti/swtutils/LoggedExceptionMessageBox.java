package com.lti.swtutils;

import org.eclipse.swt.widgets.Shell;

/**
 * Displays a generic message in place of the actual exception.
 * Used for exceptions not known to be user friendly.
 * @author Ken Larson
 *
 */
public class LoggedExceptionMessageBox
{
	public static void showError(Shell control, Throwable e)
	{
		DefaultMessageBox.showError(control, "An error has occurred.  See the log for details.");	// TODO: externalize
	}
}

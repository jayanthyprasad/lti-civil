package com.lti.swtutils;

import org.eclipse.swt.widgets.Shell;

import com.lti.swtutils.swt_rwt.ModalShellDisposedListener;

/**
 * Boilerplate read and dispatch loop.  Useful because it can be swapped out by swt_rwt for different behavior.
 * @author Ken Larson
 *
 */
public class DispatchLoop
{
	public static void readAndDispatchLoop(Shell shell)
	{
		while (!shell.isDisposed())
		{
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
	}
	
	public static void readAndDispatchLoop(Shell shell, ModalShellDisposedListener listener)
	{
		while (!shell.isDisposed())
		{
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
		
		listener.onModalShellClosed(shell);
	}
}

/*
 * Created on Jul 31, 2004
 */

package com.lti.swtutils;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;

import com.lti.swtutils.swt_rwt.ModalShellDisposedListener;

/**
 * Encapsulates the default SWT readAndDispatch loop.
 * TODO: not really modal.
 * @author Ken Larson
 */
public final class ModalShellRunner
{
	private ModalShellRunner()
	{	super();
	}
	public static void run(Shell shell)
	{
		shell.open();
		DispatchLoop.readAndDispatchLoop(shell);

	}
	public static void run(Shell shell, ModalShellDisposedListener listener)
	{
		shell.open();
		DispatchLoop.readAndDispatchLoop(shell, listener);

	}
	
	/** Modeless */
	public static void open(final Shell shell, final ModalShellDisposedListener listener)
	{
		shell.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent arg0)
			{
				listener.onModalShellClosed(shell);
			}
			
		});
		shell.open();

	}
}
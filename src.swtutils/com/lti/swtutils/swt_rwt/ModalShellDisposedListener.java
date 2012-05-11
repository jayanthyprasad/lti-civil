package com.lti.swtutils.swt_rwt;

import org.eclipse.swt.widgets.Shell;

/**
 * 
 * Used to abstract away the fact that RWT cannot do blocking calls to running modal shells.
 * @author Ken Larson
 *
 */
public interface ModalShellDisposedListener
{
	public void onModalShellClosed(Shell shell);
}

package com.lti.swtutils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.lti.swtutils.swt_rwt.MessageBoxResultListener;

/**
 * Provides convenient way to automatically set title, and otherwise code message boxes
 * with a minimum of code.  Also allows the ability to swap out the message box code, for 
 * custom message boxes.
 * @author Ken Larson
 */
public final class DefaultMessageBox
{
	private DefaultMessageBox()
	{	super();
	}
	private static String messageBoxCaption = null;

	public static void setMessageBoxCaption(String messageBoxCaption)
	{	DefaultMessageBox.messageBoxCaption = messageBoxCaption;
	}

	public static String getMessageBoxCaption()
	{
		return messageBoxCaption;
	}
	public static String getMessageBoxCaptionFor(Shell messageBoxParent)
	{	if (messageBoxCaption != null)
			return messageBoxCaption;
		
		return messageBoxParent.getText();
//		return "";
	}

	private static MessageBoxRunner messageBoxRunner = new NativeMessageBoxRunner();

	public static MessageBoxRunner getMessageBoxRunner()
	{
		return messageBoxRunner;
	}
	public static void setMessageBoxRunner(MessageBoxRunner messageBoxRunner)
	{
		DefaultMessageBox.messageBoxRunner = messageBoxRunner;
	}
	
	private static int show(Shell shell, String message, String title, int style)
	{
		return messageBoxRunner.run(shell, message, title, style);
	}
	
	private static void show(Shell shell, String message, String title, int style, MessageBoxResultListener listener)
	{
		messageBoxRunner.run(shell, message, title, style, listener);
	}
	
	
	
	public static void showWarning(Composite control, Throwable e)
	{	
		
		showWarning(control.getShell(), e.getMessage());
	}

	public static void showWarning(Shell shell, Throwable e, String caption)
	{
		if (caption == null)
		{	showWarning(shell, e);
			return;
		}
		
		showWarning(shell, e.getMessage(), caption);
	}
	public static void showError(Composite control, Throwable e)
	{	
		
		showError(control.getShell(), e.getMessage());
	}

	public static void showError(Shell shell, Throwable e, String caption)
	{
		if (caption == null)
		{	showError(shell, e);
			return;
		}
		
		showError(shell, e.getMessage(), caption);
	}
	


	
	public static int show(String message)
	{
		String caption = getMessageBoxCaptionFor(null);
		return show(null, message, caption, SWT.OK);
	}
	public static void show(String message, MessageBoxResultListener listener)
	{
		String caption = getMessageBoxCaptionFor(null);
		show(null, message, caption, SWT.OK, listener);
	}
	
	public static int show(Shell shell, String message)
	{	
		String caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.OK);
	}
	public static void show(Shell shell, String message, MessageBoxResultListener listener)
	{	
		String caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK, listener);
	}
	
	public static int show(Shell shell, String message, String caption)
	{	
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.OK);
	}
	
	public static void show(Shell shell, String message, String caption, MessageBoxResultListener listener)
	{	
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK, listener);
	}



	public static void showInformation(Shell shell, String message)
	{
		String caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK | SWT.ICON_INFORMATION);
	}
	public static void showInformation(Shell shell, String message, String caption)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK | SWT.ICON_INFORMATION);
	}

	public static void showWarning(Shell shell, String message)
	{
		String caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK | SWT.ICON_WARNING);
	}
	public static void showWarning(Shell shell, String message, String caption)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK | SWT.ICON_WARNING);
	}

	public static void showError(Shell shell, String message)
	{
		String caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK | SWT.ICON_ERROR);
	}
	public static void showError(Shell shell, String message, String caption)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK | SWT.ICON_ERROR);
	}

	public static int showYesNoWarning(Shell shell, String message)
	{
		String caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.YES | SWT.NO | SWT.ICON_WARNING);
	}
	
	public static void showYesNoWarning(Shell shell, String message, MessageBoxResultListener listener)
	{
		String caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.YES | SWT.NO | SWT.ICON_WARNING, listener);
	}

	public static int showYesNoWarning(Shell shell, String message, String caption)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.YES | SWT.NO | SWT.ICON_WARNING);
	}

	public static int showYesNoQuestion(Shell shell, String message)
	{
		String caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
	}
	
	public static void showYesNoQuestion(Shell shell, String message, MessageBoxResultListener listener)
	{
		String caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.YES | SWT.NO | SWT.ICON_QUESTION, listener);
	}

	public static int showYesNoQuestion(Shell shell, String message, String caption)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
	}

	public static void showYesNoQuestion(Shell shell, String message, String caption, MessageBoxResultListener listener)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.YES | SWT.NO | SWT.ICON_QUESTION, listener);
	}
	
	public static int showYesNoCancelQuestion(Shell shell, String message)
	{
		String caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
	}
	
	public static void showYesNoCancelQuestion(Shell shell, String message, MessageBoxResultListener listener)
	{
		String caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION, listener);
	}

	public static int showYesNoCancelQuestion(Shell shell, String message, String caption)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
	}

	public static void showYesNoCancelQuestion(Shell shell, String message, String caption, MessageBoxResultListener listener)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION, listener);
	}
	
	
	public static int showYesNoCancelWarning(Shell shell, String message)
	{
		String caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING);
	}
	
	public static void showYesNoCancelWarning(Shell shell, String message, MessageBoxResultListener listener)
	{
		String caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING, listener);
	}

	public static int showYesNoCancelWarning(Shell shell, String message, String caption)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING);
	}
	
	public static void showYesNoCancelWarning(Shell shell, String message, String caption, MessageBoxResultListener listener)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING, listener);
	}

	public static int showOKCancelWarning(Shell shell, String message)
	{
		String caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING);
	}
	
	public static void showOKCancelWarning(Shell shell, String message, MessageBoxResultListener listener)
	{
		String caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING, listener);
	}

	public static int showOKCancelWarning(Shell shell, String message, String caption)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		return show(shell, message, caption, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING);
	}
	
	public static void showOKCancelWarning(Shell shell, String message, String caption, MessageBoxResultListener listener)
	{
		if (caption == null)
			caption = getMessageBoxCaptionFor(shell);
		show(shell, message, caption, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING, listener);
	}
}

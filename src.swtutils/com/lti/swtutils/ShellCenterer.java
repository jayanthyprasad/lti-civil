/*
 * Created on Jul 31, 2004
 */
package com.lti.swtutils;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;



/**
 * Centers a shell on its parent.
 * @author Ken Larson
 */
public final class ShellCenterer
{
	private ShellCenterer()
	{	super();
	}
	
	public static void center(Shell shell, Shell parent)
	{
		Rectangle parentBounds = parent.getBounds();
		final Display display = shell.getDisplay();

		Point size = shell.getSize();
		int newX = parentBounds.x + (parentBounds.width - size.x) / 2;
		int newY = parentBounds.y + (parentBounds.height - size.y) / 2;
		
		if (newX < SWTUtils.getClientArea(display).x)
		{
			newX = SWTUtils.getClientArea(display).x;
		}
		
		if (newY < SWTUtils.getClientArea(display).y)
		{
			newY = SWTUtils.getClientArea(display).y;
		}
		
		shell.setLocation(new Point(newX, newY));
		
	}
	
	public static void center(Shell shell, Display display)
	{
		final Rectangle bounds = display.getPrimaryMonitor().getBounds();
		final Rectangle rect = shell.getBounds();
		final int x = bounds.x + (bounds.width - rect.width) / 2;
		final int y = bounds.y + (bounds.height - rect.height) / 2;
		
		shell.setLocation(x, y);
	}	
}

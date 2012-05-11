package com.lti.swtutils.image;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * @author Ken Larson
 *
 */
public class VideoStatusbarControl extends Composite
{
	private final Label labelStatusLines[];

	public VideoStatusbarControl(Composite parent, int style, int num)
	{
		super(parent, style);
		
		final GridLayout gridLayout = new GridLayout(num, true);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		this.setLayout(gridLayout);
		
		labelStatusLines = new Label[num];
		
		for (int i = 0; i < num; ++i)
		{
			labelStatusLines[i] = new Label(this, SWT.BORDER | SWT.LEFT);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 1;
			
			if (i == num - 1)
				gd.grabExcessHorizontalSpace = true;
			labelStatusLines[i].setLayoutData(gd);
			
		}

	}
	
	public void setText(int index, String s)
	{
		labelStatusLines[index].setText(s);
	}

	public void addMouseListener(MouseListener ml)
	{
		super.addMouseListener(ml);
		
		for (Label l:labelStatusLines)
		{
			if (l != null)
			{
				l.addMouseListener(ml);
			}	
		}
	}
	
	
}

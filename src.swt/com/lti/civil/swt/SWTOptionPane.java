package com.lti.civil.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lti.swtutils.ModalShellRunner;
import com.lti.swtutils.ShellCenterer;

/**
 * SWT (partial) replacement for JOptionPane
 * @author Ken Larson
 *
 */
public class SWTOptionPane extends Composite
{
	public static final int QUESTION_MESSAGE = SWT.ICON_QUESTION;
	
	public static Object showInputDialog(Shell parent,
            Object message,
            String title,
            int messageType,
            Object icon, // not used
            Object[] selectionValues,
            Object initialSelectionValue)
	{
//        return JOptionPane.showInputDialog(null,
//                message, title,
//                messageType, null, selectionValues, initialSelectionValue);
        
		Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.CLOSE);
		shell.setText(title);
		shell.setLayout(new FillLayout());
		SWTOptionPane c = new SWTOptionPane(shell, SWT.NONE, "" + message, messageType | SWT.OK | SWT.CANCEL);
		for (int i = 0; i < selectionValues.length; ++i)
		{	Object o = selectionValues[i];
			c.combo.add("" + o);
			if (o == initialSelectionValue)
				c.combo.select(i);
		}
		shell.pack();
		if (parent != null)
			ShellCenterer.center(shell, parent);
		ModalShellRunner.run(shell);
		if (c.getResponse() == SWT.OK)
		{	return selectionValues[c.selectionIndex];
			
		}
		else
			return null;
	}
	
	private int response;
	private int selectionIndex = -1;
    
	private static final int[] styles = new int[] {SWT.ICON_ERROR, SWT.ICON_INFORMATION, SWT.ICON_QUESTION, SWT.ICON_WARNING, SWT.ICON_WORKING};

	private Combo combo;
	
	SWTOptionPane(Composite arg0, int arg1, String message, int style)
	{
		super(arg0, arg1);

		
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginTop = 12;
        gridLayout.marginLeft = gridLayout.marginRight = 12;
        gridLayout.verticalSpacing = 12;
        gridLayout.horizontalSpacing = 18;
        setLayout(gridLayout);

        Label labelIcon = new Label(this, SWT.NONE);
        labelIcon.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        for (int i = 0; i < styles.length; ++i)
        {
        	if ((style & styles[i]) != 0)
	        {	labelIcon.setImage(arg0.getDisplay().getSystemImage(styles[i]));
	        	break;
	        }
        }

        
        Label label = new Label(this, SWT.CENTER);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
       label.setText(message);
       
       combo = new Combo(this, SWT.READ_ONLY);
       combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

//        Label labelIcon2 = new Label(this, SWT.NONE);
//        labelIcon2.setLayoutData(new GridData(GridData.FILL_BOTH));
//        labelIcon2.setVisible(false);
//        if ((style & SWT.ICON_ERROR) != 0)
//        	labelIcon2.setImage(arg0.getDisplay().getSystemImage(SWT.ICON_ERROR));
        
        
        
        int numButtons = 0;
        if ((style & SWT.OK) != 0)	++numButtons;
        if ((style & SWT.YES) != 0)	++numButtons;
        if ((style & SWT.NO) != 0)	++numButtons;
        if ((style & SWT.CANCEL) != 0)	++numButtons;
        
        
        Composite buttonsPanel = new Composite(this, SWT.NONE);
        {	final GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        	gd.horizontalSpan = 3;
        	buttonsPanel.setLayoutData(gd);
        }
        GridLayout buttonsPanelGridLayout = new GridLayout();
        buttonsPanelGridLayout.numColumns = numButtons;
        buttonsPanelGridLayout.makeColumnsEqualWidth = true;
        buttonsPanel.setLayout(buttonsPanelGridLayout);
               
        final int WidthHints_BUTTON = 80;
        
        if ((style & SWT.OK) != 0)
        {	Button button = new Button(buttonsPanel, SWT.PUSH);
        	button.setText("OK");
        	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        	gd.widthHint = WidthHints_BUTTON;
        	button.setLayoutData(gd);
        	button.addSelectionListener(new ButtonSelectionAdapter(SWT.OK));
        }
        if ((style & SWT.YES) != 0)
        {	Button button = new Button(buttonsPanel, SWT.PUSH);
        	button.setText("Yes");
        	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        	gd.widthHint = WidthHints_BUTTON;
        	button.setLayoutData(gd);
        	button.addSelectionListener(new ButtonSelectionAdapter(SWT.YES));
        }              
        if ((style & SWT.NO) != 0)
        {	Button button = new Button(buttonsPanel, SWT.PUSH);
        	button.setText("No");
        	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        	gd.widthHint = WidthHints_BUTTON;
        	button.setLayoutData(gd);
        	button.addSelectionListener(new ButtonSelectionAdapter(SWT.NO));
        }              
        if ((style & SWT.CANCEL) != 0)
        {	Button button = new Button(buttonsPanel, SWT.PUSH);
        	button.setText("Cancel");
        	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        	gd.widthHint = WidthHints_BUTTON;
        	button.setLayoutData(gd);
        	button.addSelectionListener(new ButtonSelectionAdapter(SWT.CANCEL));
        }        

	}
	class ButtonSelectionAdapter extends SelectionAdapter
	{	final int response;
		public ButtonSelectionAdapter(final int response)
		{
			super();
			this.response = response;
		}
	    @Override
		public void widgetSelected(SelectionEvent e)
	    {
	       setResponse(response);
	    // TODO: if we wanted to do this right, we should prevent the user from selecting nothing.
	       if (response == SWT.OK)
	    	   selectionIndex = combo.getSelectionIndex();	
	       else
	    	   selectionIndex = -1;
	       getShell().close();
	    }       	
	}
	
	public int getResponse()
	{
		return response;
	}
	public void setResponse(int response)
	{
		this.response = response;
	}
}

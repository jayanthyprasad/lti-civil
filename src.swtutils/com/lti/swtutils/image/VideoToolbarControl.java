package com.lti.swtutils.image;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.lti.swtutils.ExceptionSafeSelectionAdapter;
import com.lti.swtutils.ResourceManager;
import com.lti.swtutils.SWTUtils;




/**
 * Control intended for live and/or playback video control.
 * @author Ken Larson
 *
 */
public class VideoToolbarControl extends Composite
{
	private static final Logger logger = Logger.getLogger(VideoToolbarControl.class.getName());

	// TODO: dispose tool button images.
	
	public static final int PLAY = 0x0001;
	public static final int PAUSE = 0x0002;
	public static final int FAST_FORWARD = 0x0004;
	public static final int REWIND = 0x0008;
	public static final int SEEK_BEGINNING = 0x0010;
	public static final int SEEK_END = 0x0020;
	public static final int SNAP = 0x0040;
	public static final int LIVE = 0x0080;
	public static final int LOOP = 0x0100;
	public static final int STOP = 0x0200;
	public static final int PTZ = 0x0400;
	
	public static final int ALL = PLAY | PAUSE | FAST_FORWARD | REWIND | SEEK_BEGINNING | SEEK_END | SNAP | LIVE | LOOP | STOP | PTZ;
	public static final int[] ALL_LIST = new int[] {PLAY , PAUSE , FAST_FORWARD , REWIND , SEEK_BEGINNING , SEEK_END , SNAP , LIVE, LOOP, STOP, PTZ};
	
	private final ToolBar toolBar;
	
	private final ToolItem buttonPlay;
	private final ToolItem buttonPause;
	private final ToolItem buttonFastForward;
	private final ToolItem buttonRewind;
	private final ToolItem buttonSeekBeginning;
	private final ToolItem buttonSeekEnd;
	private final ToolItem buttonSnap;
	private final ToolItem buttonLive;
	private final ToolItem buttonLoop;
	private final ToolItem buttonStop;
	private final ToolItem buttonPtz;

	private final int videoToolbarStyle;
	private VideoToolbarControlListener listener;
	
	private final boolean hasStyle(int flag)
	{	return (videoToolbarStyle & flag) != 0;
	}
	
	private int numButtons()
	{
		int count = 0;
		for (int i : ALL_LIST)
		{	if (hasStyle(i))
				++count;
		}
		return count;
	}
	
	public void setListener(VideoToolbarControlListener listener)
	{
		this.listener = listener;
	}

	public VideoToolbarControl(Composite parent, int style, int videoToolbarStyle)
	{
		super(parent, style);
		this.videoToolbarStyle = videoToolbarStyle;
		
		setLayout(new FillLayout());
		toolBar = new ToolBar(this, SWT.FLAT);
		
		if (hasStyle(SEEK_BEGINNING))
		{
			buttonSeekBeginning = new ToolItem(toolBar, SWT.NONE);
			buttonSeekBeginning.setToolTipText("Seek to Beginning");
			
			buttonSeekBeginning.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/control_seek_beginning_blue.png"));
			SWTUtils.setDisabledImage(buttonSeekBeginning, ResourceManager.getImage(VideoToolbarControl.class, "resources/control_seek_beginning.png"));
			buttonSeekBeginning.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onSeekBeginning(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonSeekBeginning = null;
		}
		
		if (hasStyle(REWIND))
		{
			buttonRewind = new ToolItem(toolBar, SWT.NONE);
			buttonRewind.setToolTipText("Rewind");
			buttonRewind.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/control_rewind_blue.png"));
			SWTUtils.setDisabledImage(buttonRewind, ResourceManager.getImage(VideoToolbarControl.class, "resources/control_rewind.png"));
			buttonRewind.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onRewind(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonRewind = null;
		}
		
		
		if (hasStyle(STOP))
		{
			buttonStop = new ToolItem(toolBar, SWT.NONE);
			buttonStop.setToolTipText("Stop");
			buttonStop.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/control_stop_blue.png"));
			SWTUtils.setDisabledImage(buttonStop, ResourceManager.getImage(VideoToolbarControl.class, "resources/control_stop.png"));
			buttonStop.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onStop(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonStop = null;
		}
		
		
		if (hasStyle(PAUSE))
		{
			buttonPause = new ToolItem(toolBar, SWT.NONE);
			buttonPause.setToolTipText("Pause");
			buttonPause.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/control_pause_blue.png"));
			SWTUtils.setDisabledImage(buttonPause, ResourceManager.getImage(VideoToolbarControl.class, "resources/control_pause.png"));
			buttonPause.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onPause(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonPause = null;
		}
		
		if (hasStyle(PLAY))
		{
			buttonPlay = new ToolItem(toolBar, SWT.NONE);
			buttonPlay.setToolTipText("Play");
			buttonPlay.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/control_play_blue.png"));
			SWTUtils.setDisabledImage(buttonPlay, ResourceManager.getImage(VideoToolbarControl.class, "resources/control_play.png"));
			buttonPlay.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onPlay(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonPlay = null;
		}
		
		if (hasStyle(FAST_FORWARD))
		{
			buttonFastForward = new ToolItem(toolBar, SWT.NONE);
			buttonFastForward.setToolTipText("Fast Forward");
			buttonFastForward.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/control_fastforward_blue.png"));
			SWTUtils.setDisabledImage(buttonFastForward, ResourceManager.getImage(VideoToolbarControl.class, "resources/control_fastforward.png"));
			buttonFastForward.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onFastForward(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonFastForward = null;
		}
		
		if (hasStyle(SEEK_END))
		{
			buttonSeekEnd = new ToolItem(toolBar, SWT.NONE);
			buttonSeekEnd.setToolTipText("Seek to End");
			buttonSeekEnd.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/control_seek_end_blue.png"));
			SWTUtils.setDisabledImage(buttonSeekEnd, ResourceManager.getImage(VideoToolbarControl.class, "resources/control_seek_end.png"));
			buttonSeekEnd.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onSeekEnd(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonSeekEnd = null;
		}
		
		if (hasStyle(LOOP))
		{
			buttonLoop = new ToolItem(toolBar, SWT.CHECK);
			buttonLoop.setToolTipText("Loop");
			buttonLoop.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/control_repeat_blue.png"));
			SWTUtils.setDisabledImage(buttonLoop, ResourceManager.getImage(VideoToolbarControl.class, "resources/control_repeat.png"));
			buttonLoop.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onLoop(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonLoop = null;
		}
		
		if (hasStyle(LIVE))
		{
			buttonLive = new ToolItem(toolBar, SWT.NONE);
			buttonLive.setText("Live");
			buttonLive.setToolTipText("Live");
			buttonLive.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onLive(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonLive = null;
		}
		
		if (hasStyle(SNAP))
		{
			buttonSnap = new ToolItem(toolBar, SWT.NONE);
			buttonSnap.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/save.png"));
			//buttonSnap.setText("Snap");
			buttonSnap.setToolTipText("Snap");
			buttonSnap.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onSnap(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonSnap = null;
		}
		
		if (hasStyle(PTZ))
		{
			buttonPtz = new ToolItem(toolBar, SWT.NONE);
			buttonPtz.setImage(ResourceManager.getImage(VideoToolbarControl.class, "resources/ptz.png"));
			//buttonPtz.setText("PTZ");
			buttonPtz.setToolTipText("Pan/Tilt/Zoom");
			buttonPtz.addSelectionListener(new ExceptionSafeSelectionAdapter(getShell(), logger, new SelectionAdapter()
					{
						@Override
						public void widgetSelected(SelectionEvent e)
						{
							if (listener != null)
								listener.onPtz(VideoToolbarControl.this);
						}	
	
					}));
		}
		else
		{	buttonPtz = null;
		}
		
		
	}
	
	public boolean isLoopChecked()
	{
		if (buttonLoop == null)
			return false;
		return buttonLoop.getSelection();
	}
	
	public void setLoopChecked(boolean value)
	{
		if (buttonLoop == null)
			return;
		buttonLoop.setSelection(value);
	}
}

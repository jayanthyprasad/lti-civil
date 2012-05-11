package com.lti.swtutils;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;


/**
 * 
 * @author Ken Larson
 *
 */
public class SWTUtils
{
	public static final int MODELESS = SWT.MODELESS;
	public static final int NO_BACKGROUND = SWT.NO_BACKGROUND;
	public static final int CR = SWT.CR;
	public static final int FULL_SELECTION = SWT.FULL_SELECTION;
	public static final int HIDE_SELECTION = SWT.HIDE_SELECTION;
	public static final int KeyUp = SWT.KeyUp;
	public static final int Selection = SWT.Selection;
	public static final int Show = SWT.Show;
	public static final int YES = SWT.YES;
	public static final int Close = SWT.Close;
	public static final int PRIMARY_MODAL = SWT.PRIMARY_MODAL;
	public static final int CANCEL = SWT.CANCEL;
	public static final int Traverse = SWT.Traverse;
	public static final int MouseWheel = SWT.MouseWheel;
	public static final int OK = SWT.OK;
	public static final int MENU = SWT.MENU;
	public static final int OPEN = SWT.OPEN;
	public static final int EMBEDDED = SWT.EMBEDDED;
	public static final int NO = SWT.NO;
	public static final int TRANSPARENCY_MASK = SWT.TRANSPARENCY_MASK;
	public static final int TRANSPARENCY_PIXEL = SWT.TRANSPARENCY_PIXEL;
	public static final int TRANSPARENCY_ALPHA = SWT.TRANSPARENCY_ALPHA;
	public static final int TRANSPARENCY_NONE = SWT.TRANSPARENCY_NONE;

	public static final int IMAGE_UNDEFINED = SWT.IMAGE_UNDEFINED;
	public static final int IMAGE_BMP = 0;
	public static final int IMAGE_BMP_RLE = 1;
	public static final int IMAGE_GIF = 2;
	public static final int IMAGE_ICO = 3;
	public static final int IMAGE_JPEG = 4;
	public static final int IMAGE_PNG = 5;
	public static final int IMAGE_TIFF = 6;
	public static final int IMAGE_OS2_BMP = 7;
	
	public static final int COLOR_WHITE = 1;
	public static final int COLOR_BLACK = 2;
	public static final int COLOR_RED = 3;
	public static final int COLOR_DARK_RED = 4;
	public static final int COLOR_GREEN = 5;
	public static final int COLOR_DARK_GREEN = 6;
	public static final int COLOR_YELLOW = 7;
	public static final int COLOR_DARK_YELLOW = 8;
	public static final int COLOR_BLUE = 9;
	public static final int COLOR_DARK_BLUE = 10;
	public static final int COLOR_MAGENTA = 11;
	public static final int COLOR_DARK_MAGENTA = 12;
	public static final int COLOR_CYAN = 13;
	public static final int COLOR_DARK_CYAN = 14;
	public static final int COLOR_GRAY = 15;
	public static final int COLOR_DARK_GRAY = 16;

	public static final int COLOR_WIDGET_DARK_SHADOW = 17;
	public static final int COLOR_WIDGET_NORMAL_SHADOW = 18;
	public static final int COLOR_WIDGET_LIGHT_SHADOW = 19;
	public static final int COLOR_WIDGET_HIGHLIGHT_SHADOW = 20;
	public static final int COLOR_WIDGET_FOREGROUND = 21;
	public static final int COLOR_WIDGET_BACKGROUND = 22;
	public static final int COLOR_WIDGET_BORDER = 23;
	public static final int COLOR_LIST_FOREGROUND = 24;
	public static final int COLOR_LIST_BACKGROUND = 25;
	public static final int COLOR_LIST_SELECTION = 26;
	public static final int COLOR_LIST_SELECTION_TEXT = 27;
	public static final int COLOR_INFO_FOREGROUND = 28;
	public static final int COLOR_INFO_BACKGROUND = 29;
	public static final int COLOR_TITLE_FOREGROUND = 30;
	public static final int COLOR_TITLE_BACKGROUND = 31;
	public static final int COLOR_TITLE_BACKGROUND_GRADIENT = 32;
	public static final int COLOR_TITLE_INACTIVE_FOREGROUND = 33;
	public static final int COLOR_TITLE_INACTIVE_BACKGROUND = 34;
	public static final int COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT = 35;

	public static final int LEFT_TO_RIGHT = 1 << 25;
	public static final int RIGHT_TO_LEFT = 1 << 26;

	public static final int CAP_FLAT = 1;
	public static final int CAP_ROUND = 2;
	public static final int CAP_SQUARE = 3;

	public static final int JOIN_MITER = 1;
	public static final int JOIN_ROUND = 2;
	public static final int JOIN_BEVEL = 3;
	public static final int LINE_SOLID = 1;
	public static final int LINE_DASH = 2;
	public static final int LINE_DOT = 3;
	public static final int LINE_DASHDOT = 4;
	public static final int LINE_DASHDOTDOT = 5;
	public static final int LINE_CUSTOM = 6;

	public static final int DRAW_TRANSPARENT = 1 << 0;
	public static final int DRAW_DELIMITER = 1 << 1;
	public static final int DRAW_TAB = 1 << 2;
	public static final int DRAW_MNEMONIC = 1 << 3;	
	
	public static final int OFF = 0;
	public static final int ON = 1;
	
	public static final int FILL_EVEN_ODD = 1;
	public static final int FILL_WINDING = 2;

	public static final int BITMAP = 0;
	public static final int ICON = 1;


	public static final int IMAGE_COPY = 0;
	public static final int IMAGE_DISABLE = 1;
	public static final int IMAGE_GRAY = 2;
	
	
	// determine whether SWT_RWT is in use.
	private static boolean swt_rwt = querySWT_RWT();
	public static boolean isSWT_RWT()
	{
		return swt_rwt;
	}
	
	/** Whether isSWT_RWT() and dynamic updates using syncExec and asyncExec are possible. */
	public static boolean isSWT_RWTDynamic()
	{
		return false;
	}
	
	/** Whether dynamic updates using syncExec and asyncExec are possible. */
	public static boolean isDynamic()
	{
		return true;
	}
	
	private static boolean querySWT_RWT()
	{
		try
		{
			Class widgetClass = org.eclipse.swt.widgets.Widget.class;
			for (Class i : widgetClass.getInterfaces())
			{	if (i.getName().equals("com.w4t.Adaptable"))
					return true;
			}
			
			return false;
		}
		catch (Throwable e)
		{	return false;
		}
	}
	
	public static void setImages(Shell shell, Image[] images)
	{
		shell.setImages(images);

	}
	

	
	public static final Image loadImage(Display display, String path)
	{	return new Image(display, path);
	}
	
	public static final Image loadImage(Display display, InputStream is)
	{	return new Image(display, is);
	}
	
	public static int getDefaultHelpAccelerator()
	{
		return SWT.F1;
	}
	
	public static void setAccelerator(MenuItem i, int value)
	{	i.setAccelerator(value);
	}
	
	public static void addMouseListener(Control control, MouseListener l)
	{	control.addMouseListener(l);
	}
	
	public static Rectangle getClientArea(Display d)
	{	return d.getClientArea();
	}
	
	public static void setDisabledImage(ToolItem item, Image image)
	{	item.setDisabledImage(image);
	}
	
	public static Tray getSystemTray(Display display)
	{	return display.getSystemTray();
	}
	
	public static void syncExec(Display display, Runnable r)
	{	display.syncExec(r);
	}
	
	public static void asyncExec(Display display, Runnable r)
	{	display.asyncExec(r);
	}
	
	public static void setWaitCursor(Composite c)
	{
		c.setCursor(ResourceManager.getCursor(SWT.CURSOR_WAIT));
	}
	
	public static void setArrowCursor(Composite c)
	{
		c.setCursor(ResourceManager.getCursor(SWT.CURSOR_ARROW));
	}
	
	public static void addListener(Widget w, int type, Listener listener)
	{
		w.addListener(type, listener);
	}
	
	public static void removeListener(Widget w, int type, Listener listener)
	{
		w.removeListener(type, listener);
	}
	
	public static void notifyListeners(Widget w, int type, Event e)
	{
		w.notifyListeners(type, e);
	}
	
	public static void setRedraw(Table t, boolean value)
	{	t.setRedraw(value);
	}
	
	public static void setResizable(TableColumn c, boolean value)
	{
		c.setResizable(value);
	}
	
	public static void addFilter(Display display, int type, Listener filter)
	{	display.addFilter(type, filter);
	}
	
	public static void removeFilter(Display display, int type, Listener filter)
	{	display.removeFilter(type, filter);
	}
	
	public static Item getItem(SelectionEvent e)
	{
		return (Item) e.item;
	}
	
	public static Widget getWidget(SelectionEvent e)
	{
		return e.widget;
	}
	
	public static void addModifyListener(Combo c, ModifyListener listener)
	{	c.addModifyListener(listener);
	}
	
	public static void removeModifyListener(Combo c, ModifyListener listener)
	{	c.removeModifyListener(listener);
	}
	
	public static void setGrayed(TreeItem item, boolean value)
	{	item.setGrayed(value);
	}
	
	public static void addMouseTrackListener(Control c, MouseTrackListener listener)
	{
		c.addMouseTrackListener(listener);
	}
	
	public static void addMouseMoveListener(Control c, MouseMoveListener listener)
	{
		c.addMouseMoveListener(listener);
	}
	
	public static Color getBackground(TableItem item)
	{	return item.getBackground();
	}
	
	public static void setImage(TableItem item, int index, Image image)
	{
		item.setImage(index, image);
	}
	
	public static Image getImage(TableItem item, int index)
	{	return item.getImage(index);
	}
	
	public static void dispose(Image image)
	{	image.dispose();
	}
	
	public static void dispose(Color color)
	{	color.dispose();
	}
	
	public static String getText(Combo c)
	{	return c.getText();
	}
	public static void setText(Combo c, String text)
	{	c.setText(text);
	}
	
	public static void setText(TableItem item, String[] strings)
	{
		item.setText(strings);
	}
	
	public static void addKeyListener(Control c, KeyListener listener)
	{	c.addKeyListener(listener);
	}
	
	public static void setChecked(TableItem item, boolean value)
	{
		item.setChecked(value);
	}
	
	public static boolean getChecked(TableItem item)
	{	return item.getChecked();
	}
	
	public static void setGrayed(TableItem item, boolean value)
	{
		item.setGrayed(value);
	}
	
	public static boolean getGrayed(TableItem item)
	{	return item.getGrayed();
	}
	
	public static void update(Control c)
	{	c.update();
	}
	
	public static void setLinesVisible(Tree t, boolean value)
	{	t.setLinesVisible(value);
	}
	
	public static void setHeaderVisible(Tree t, boolean value)
	{	t.setHeaderVisible(value);
	}
	
	public static Color newColor(Display display, int red, int green, int blue)
	{
		return new Color(display, red, green, blue);
	}
	
	public static Image newImage(Display display, ImageData imageData)
	{	return new Image(display, imageData);
	}
	
	public static Image newImage(Display display, int x, int y)
	{	return new Image(display, x, y);
	}
	
	public static void addSelectionListener(Text text, SelectionListener listener)
	{	text.addSelectionListener(listener);
	}
	
	public static ImageData getImageData(Image image)
	{	return image.getImageData();
	}
	
	public static int indexOf(Combo c, String s)
	{	return c.indexOf(s);
	}
	
	public static void forceActive(Shell s)
	{	s.forceActive();
	}
	
	public static void setDoIt(ShellEvent e, boolean value)
	{	e.doit = value;
	}
}


package com.lti.swtutils;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.lti.swtutils.SWTUtils;

/**
 * Utility class for managing OS resources associated with SWT controls such as
 * colors, fonts, images, etc.
 * 
 * IMPORTANT! Application code must explicitly invoke the <code>dispose()</code>
 * method to release the operating system resources managed by cached objects
 * when those objects and OS resources are no longer needed (e.g. on
 * application shutdown)
 * 
 * This class may be freely distributed as part of any application or plugin.
 * <p>
 * Copyright (c) 2003, Instantiations, Inc. <br>All Rights Reserved
 * 
 * @author scheglov_ke
 * @author Dan Rubel
 */
public class ResourceManager {

	/**
	 * Dispose of cached objects and their underlying OS resources. This should
	 * only be called when the cached objects are no longer needed (e.g. on
	 * application shutdown)
	 */
	public static void dispose() {
		disposeColors();
		disposeFonts();
		disposeImages();
		disposeCursors();
	}

	// Color support
	private static HashMap<RGB, Color> m_ColorMap = new HashMap<RGB, Color>();
	public static Color getColor(int systemColorID) {
		Display display = Display.getCurrent();
		return display.getSystemColor(systemColorID);
	}
	public static Color getColor(int r, int g, int b) {
		return getColor(new RGB(r, g, b));
	}
	public static Color getColor(RGB rgb) {
		Color color = (Color) m_ColorMap.get(rgb);
		if (color == null) {
			Display display = Display.getCurrent();
			color = new Color(display, rgb);
			m_ColorMap.put(rgb, color);
		}
		return color;
	}
	public static void disposeColors() {
		for (Iterator<Color> iter = m_ColorMap.values().iterator(); iter.hasNext();)
			 ((Color) iter.next()).dispose();
		m_ColorMap.clear();
	}

	// Image support
	private static HashMap<String, Image> m_ClassImageMap = new HashMap<String, Image>();
//	private static HashMap<ImageDescriptor, Image> m_DescriptorImageMap = new HashMap<ImageDescriptor, Image>();
	private static HashMap<Image, HashMap<Image, Image>> m_ImageToDecoratorMap = new HashMap<Image, HashMap<Image, Image>>();
	private static Image getImage(InputStream is) {
		Display display = Display.getCurrent();
		ImageData data = new ImageData(is);
		if (data.transparentPixel > 0)
			return new Image(display, data, data.getTransparencyMask());
		return new Image(display, data);
	}
//	public static Image getImage(String path) {
//		String key = ResourceManager.class.getName() + "|" + path; //$NON-NLS-1$
//		Image image = (Image) m_ClassImageMap.get(key);
//		if (image == null) {
//			try {
//				FileInputStream fis = new FileInputStream(path);
//				image = getImage(fis);
//				m_ClassImageMap.put(key, image);
//				fis.close();
//			} catch (IOException e) {
//				return null;
//			}
//		}
//		return image;
//	}
	public static Image getImage(Class clazz, String path) {
		String key = clazz.getName() + "|" + path; //$NON-NLS-1$
		Image image = (Image) m_ClassImageMap.get(key);
		if (image == null) {
			if (path.length() > 0 && path.charAt(0) == '/') {
				String newPath = path.substring(1, path.length());
				InputStream is = clazz.getClassLoader().getResourceAsStream(newPath);
				if (is == null)
					return null;
				image = getImage(is);
			} else {
				final InputStream is = clazz.getResourceAsStream(path);
				if (is == null)
					return null;
				image = getImage(clazz.getResourceAsStream(path));
			}
			m_ClassImageMap.put(key, image);
		}
		return image;
	}
//	public static ImageDescriptor getImageDescriptor(Class clazz, String path) {
//		return ImageDescriptor.createFromFile(clazz, path);
//	}
//	public static ImageDescriptor getImageDescriptor(String path) {
//		try {
//			return ImageDescriptor.createFromURL((new File(path)).toURL());
//		} catch (MalformedURLException e) {
//			return null;
//		}
//	}
//	public static Image getImage(ImageDescriptor descriptor) {
//		if (descriptor == null) return null;
//		Image image = (Image) m_DescriptorImageMap.get(descriptor);
//		if (image == null) {
//			image = descriptor.createImage();
//			m_DescriptorImageMap.put(descriptor, image);
//		}
//		return image;
//	}
//	public static Image decorateImage(Image baseImage, Image decorator) {
//		HashMap<Image, Image> decoratedMap = m_ImageToDecoratorMap.get(baseImage);
//		if (decoratedMap == null) {
//			decoratedMap = new HashMap<Image, Image>();
//			m_ImageToDecoratorMap.put(baseImage, decoratedMap);
//		}
//		Image result = (Image) decoratedMap.get(decorator);
//		if (result == null) {
//			ImageData bid = baseImage.getImageData();
//			ImageData did = decorator.getImageData();
//			result = new Image(Display.getCurrent(), bid.width, bid.height);
//			GC gc = new GC(result);
//			//
//			gc.drawImage(baseImage, 0, 0);
//			gc.drawImage(decorator, bid.width - did.width - 1, bid.height - did.height - 1);
//			//
//			gc.dispose();
//			decoratedMap.put(decorator, result);
//		}
//		return result;
//	}
	public static void disposeImages() {
		for (Iterator iter = m_ClassImageMap.values().iterator(); iter.hasNext();)
			SWTUtils.dispose(((Image) iter.next()));
		m_ClassImageMap.clear();
//		for (Iterator iter = m_DescriptorImageMap.values().iterator(); iter.hasNext();)
//			SWTUtils.dispose(((Image) iter.next()));
//		m_DescriptorImageMap.clear();
	}

//	// Plugin images support
//	private static HashMap<URL, Image> m_URLImageMap = new HashMap<URL, Image>();
//	public static Image getPluginImage(Object plugin, String name) {
//		try {
//			try {
//				URL url = getPluginImageURL(plugin, name);
//				if (m_URLImageMap.containsKey(url))
//					return (Image) m_URLImageMap.get(url);
//				InputStream is = url.openStream();
//				Image image;
//				try {
//					image = getImage(is);
//					m_URLImageMap.put(url, image);
//				} finally {
//					is.close();
//				}
//				return image;
//			} catch (Throwable e) {	// fall through and return null
//			}
//		} catch (Throwable e) {	// fall through and return null
//		}
//		return null;
//	}
//	public static ImageDescriptor getPluginImageDescriptor(Object plugin, String name) {
//		try {
//			try {
//				URL url = getPluginImageURL(plugin, name);
//				return ImageDescriptor.createFromURL(url);
//			} catch (Throwable e) {	// fall through and return null
//			}
//		} catch (Throwable e) {	// fall through and return null
//		}
//		return null;
//	}
//	private static URL getPluginImageURL(Object plugin, String name) throws Exception {
//		Class pluginClass = Class.forName("org.eclipse.core.runtime.Plugin"); //$NON-NLS-1$
//		Method getDescriptorMethod = pluginClass.getMethod("getDescriptor", (Class []) null); //$NON-NLS-1$
//		Class pluginDescriptorClass = Class.forName("org.eclipse.core.runtime.IPluginDescriptor"); //$NON-NLS-1$
//		Method getInstallURLMethod = pluginDescriptorClass.getMethod("getInstallURL", (Class []) null); //$NON-NLS-1$
//		//
//		Object pluginDescriptor = getDescriptorMethod.invoke(plugin, (Object []) null);
//		URL installURL = (URL) getInstallURLMethod.invoke(pluginDescriptor, (Object []) null);
//		URL url = new URL(installURL, name);
//		return url;
//	}
//	
	// Font support
	private static HashMap<String, Font> m_FontMap = new HashMap<String, Font>();
	private static HashMap<Font, Font> m_FontToBoldFontMap = new HashMap<Font, Font>();
	public static Font getFont(String name, int height, int style) {
		String fullName = name + "|" + height + "|" + style; //$NON-NLS-1$ //$NON-NLS-2$
		Font font = (Font) m_FontMap.get(fullName);
		if (font == null) {
			font = new Font(Display.getCurrent(), name, height, style);
			m_FontMap.put(fullName, font);
		}
		return font;
	}
	public static Font getBoldFont(Font baseFont) {
		Font font = (Font) m_FontToBoldFontMap.get(baseFont);
		if (font == null) {
			FontData fontDatas[] = baseFont.getFontData();
			FontData data = fontDatas[0];
			font = new Font(Display.getCurrent(), data.getName(), data.getHeight(), SWT.BOLD);
			m_FontToBoldFontMap.put(baseFont, font);
		}
		return font;
	}
	public static Font getBoldResizedFont(Font baseFont, int newSize) {
		Font font = (Font) m_FontToBoldFontMap.get(baseFont);
		if (font == null) {
			FontData fontDatas[] = baseFont.getFontData();
			FontData data = fontDatas[0];
			font = new Font(Display.getCurrent(), data.getName(), newSize, SWT.BOLD);
			m_FontToBoldFontMap.put(baseFont, font);
		}
		return font;
	}
	
	public static void disposeFonts() {
		for (Iterator iter = m_FontMap.values().iterator(); iter.hasNext();)
			 ((Font) iter.next()).dispose();
		m_FontMap.clear();
	}

//	// CoolBar support
//	public static void fixCoolBarSize(CoolBar bar) {
//		CoolItem[] items = bar.getItems();
//		// ensure that each item has control (at least empty one)
//		for (int i = 0; i < items.length; i++) {
//			CoolItem item = items[i];
//			if (item.getControl() == null)
//				item.setControl(new Canvas(bar, SWT.NONE) {
//				public Point computeSize(int wHint, int hHint, boolean changed) {
//					return new Point(20, 20);
//				}
//			});
//		}
//		// compute size for each item
//		for (int i = 0; i < items.length; i++) {
//			CoolItem item = items[i];
//			Control control = item.getControl();
//			control.pack();
//			Point size = control.getSize();
//			item.setSize(item.computeSize(size.x, size.y));
//		}
//	}

	// Cursor support
	private static HashMap<Integer, Cursor> m_IdToCursorMap = new HashMap<Integer, Cursor>();
	public static Cursor getCursor(int id) {
		Integer key = new Integer(id);
		Cursor cursor = (Cursor) m_IdToCursorMap.get(key);
		if (cursor == null) {
			cursor = new Cursor(Display.getDefault(), id);
			m_IdToCursorMap.put(key, cursor);
		}
		return cursor;
	}
	public static void disposeCursors() {
		for (Iterator iter = m_IdToCursorMap.values().iterator(); iter.hasNext();)
			 ((Cursor) iter.next()).dispose();
		m_IdToCursorMap.clear();
	}
}
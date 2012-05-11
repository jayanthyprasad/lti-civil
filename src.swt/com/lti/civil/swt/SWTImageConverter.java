/*
 * Created on Jun 1, 2005
 */
package com.lti.civil.swt;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

import com.lti.civil.VideoFormat;
import com.lti.civil.awt.AWTImageConverter;
import com.lti.civil.utility.LoggerSingleton;
import com.lti.swtutils.SWTAWTImageConverter;

/**
 *
 * @author Ken Larson
 */
public class SWTImageConverter
{
	private static final Logger logger = LoggerSingleton.logger;

	private static int getBytesPerPixel(int format)
	{
		switch (format)
		{	case com.lti.civil.VideoFormat.RGB24:
				return 3;
			case com.lti.civil.VideoFormat.RGB32:
				return 4;
			default:
				throw new RuntimeException();
		}
	}
	private static int getBitsPerPixel(int format)
	{	return getBytesPerPixel(format) * 8;
	}

	public static ImageData convertToSWTImageData(final com.lti.civil.Image image)
	{
		final VideoFormat format = image.getFormat();
		//PaletteData palette = new PaletteData(0xff0000, 0xff00, 0xff);
		PaletteData palette = new PaletteData(0xff, 0xff00, 0xff0000);
		// TODO: what is scanlinePad supposed to be?
		// TODO: test RGB32
		return new ImageData(
					format.getWidth(),
					format.getHeight(),
					getBitsPerPixel(format.getFormatType()),
					palette,
					1,
					(byte[]) image.getObject());

		// old, slower way:
//		final int w = image.getWidth();
//		final int h = image.getHeight();
//		final int bpp = getBytesPerPixel(image.getFormat());
//		ImageData data = new ImageData(w, h, 24, palette);
//		int off = 0;
//		byte[] bytes = image.getBytes();
//		for (int y = 0; y < h; ++y)
//		{
//			for (int x = 0; x < w; ++x)
//			{
//				final byte r = bytes[off + 0];
//				final byte g = bytes[off + 1];
//				final byte b = bytes[off + 2];
//				int pixel = 0;
//				pixel += uByteToInt(r);	// red
//				pixel *= 256;
//				pixel += uByteToInt(g); // green
//				pixel *= 256;
//				pixel += uByteToInt(b);	// blue
//				//int pixel = palette.getPixel(new RGB(uByteToInt(r), uByteToInt(g), uByteToInt(b)));
//				data.setPixel(x, y, pixel);
//				off += bpp;
//			}
//		}
//		return data;
	}

	public static Image convertToSWTImage(Display display, com.lti.civil.Image image)
	{
		return new Image(display, convertToSWTImageData(image));
	}

	public static ImageData convertToSWTImageData_old(com.lti.civil.Image image)
	{

		BufferedImage bimg = AWTImageConverter.toBufferedImage(image);
//		 TODO: we can avoid this, and go straight from our image to this.
		return SWTAWTImageConverter.convertToSWTImageData(bimg);

	}



}

/*
 * Created on Jun 1, 2005
 */
package com.lti.civil.awt;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import com.lti.civil.VideoFormat;

/**
 * Converts {@link com.lti.civil.Image} to BufferedImage.
 * @author Ken Larson
 */
public class AWTImageConverter
{

	// adapted from FMJ's BufferToImage class.

	/**
	 * Convert the given {@link com.lti.civil.Image} to a BufferedImage.
	 */
	public static BufferedImage toBufferedImage(final com.lti.civil.Image image)
	{
		// TODO: if it is a BufferedImageImage, we should just cast and return the wrapped image.
		final VideoFormat format = image.getFormat();
		final int w = format.getWidth();
		final int h = format.getHeight();

		if (format.getFormatType() == com.lti.civil.VideoFormat.RGB24 && format.getDataType() == VideoFormat.DATA_TYPE_BYTE_ARRAY)

		{

			byte[] bytes = (byte[]) image.getObject();
			// this is much faster than iterating through the pixels.
			// if we create a writable raster and then construct a buffered image,
			// no new array is created and no data is copied.
			// TODO: optimize other cases.
			final DataBufferByte db = new DataBufferByte(new byte[][] {bytes}, bytes.length, new int[]{image.getOffset()});
			final ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE, w, h, 3, w * 3, new int[] {2, 1, 0});
			final WritableRaster r = Raster.createWritableRaster(sm, db, new Point(0, 0));
			// construction borrowed from BufferedImage constructor, for BufferedImage.TYPE_3BYTE_BGR
            final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            int[] nBits = {8, 8, 8};
            //int[] bOffs = {2, 1, 0};
            final ColorModel colorModel = new ComponentColorModel(cs, nBits, false, false,
                                                 Transparency.OPAQUE,
                                                 DataBuffer.TYPE_BYTE);
            final BufferedImage bi = new BufferedImage(colorModel, r, false, null);
			return bi;
		}
		else if (format.getFormatType() == com.lti.civil.VideoFormat.RGB32 && format.getDataType() == VideoFormat.DATA_TYPE_BYTE_ARRAY)
		{

			byte[] bytes = (byte[]) image.getObject();
			final DataBufferByte db = new DataBufferByte(new byte[][] {bytes}, bytes.length, new int[]{image.getOffset()});
			final ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE, w, h, 4, w * 4, new int[] {2, 1, 0, 3});
			final WritableRaster r = Raster.createWritableRaster(sm, db, new Point(0, 0));
			// construction borrowed from BufferedImage constructor, for BufferedImage.TYPE_4BYTE_ABGR
            final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            int[] nBits = {8, 8, 8, 8};
            //int[] bOffs = {3, 2, 1, 0};
            final ColorModel colorModel = new ComponentColorModel(cs, nBits, true, false,
                                                 Transparency.TRANSLUCENT,
                                                 DataBuffer.TYPE_BYTE);
            final BufferedImage bi = new BufferedImage(colorModel, r, false, null);
			return bi;
		}
		else if (format.getFormatType() == com.lti.civil.VideoFormat.RGB32 && format.getDataType() == VideoFormat.DATA_TYPE_INT_ARRAY)
		{

			int[] data = (int[]) image.getObject();
			final DataBufferInt db = new DataBufferInt(data, w * h, image.getOffset());
			final SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, w, h, new int[]{0x00FF0000, 0x0000FF00, 0x000000FF});
			final WritableRaster r = Raster.createWritableRaster(sm, db, new Point(0, 0));
			final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
			final ColorModel colorModel = new DirectColorModel(cs, 24, 0x00FF0000, 0x0000FF00, 0x000000FF, 0x00000000, false, DataBuffer.TYPE_INT);
            final BufferedImage bi = new BufferedImage(colorModel, r, false, null);
			return bi;
		}
		else
		{	throw new IllegalArgumentException("Unable to convert non-rgb video (" + format.getFormatType() + ") to AWT");
		}
	}
}

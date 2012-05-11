package com.lti.civil.utility;

import com.lti.civil.VideoFormat;

public class VideoFormatNames {

	private VideoFormatNames() {

	}

	public static String formatTypeToString(int f)
	{
		switch (f)
		{
			case VideoFormat.RGB24:
				return "RGB24";
			case VideoFormat.RGB32:
				return "RGB32";
			case VideoFormat.RGB565:
				return "RGB565";
			case VideoFormat.RGB555:
				return "RGB555";
			case VideoFormat.ARGB1555:
				return "ARGB1555";
			case VideoFormat.ARGB32:
				return "ARGB32";
			case VideoFormat.UYVY:
				return "UYVY";
			case VideoFormat.YUYV:
				return "YUYV";
			case VideoFormat.YVYU:
				return "YVYU";
			case VideoFormat.YUY2:
				return "YUY2";
			case VideoFormat.YV12:
				return "YV12";
			case VideoFormat.I420:
				return "I420";
			case VideoFormat.IYUV:
				return "IYUV";
			case VideoFormat.NV12:
				return "NV12";
			case VideoFormat.Y411:
				return "Y411";
			case VideoFormat.YVU9:
				return "YVU9";
			case VideoFormat.MJPG:
				return "MJPG";
			case VideoFormat.DVSL:
				return "DVSL";
			case VideoFormat.DVSD:
				return "DVSD";
			case VideoFormat.DVHD:
				return "DVHD";
			default:
				return "" + f + " (unknown)";
		}
	}
}

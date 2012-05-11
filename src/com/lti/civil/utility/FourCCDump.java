package com.lti.civil.utility;

/**
 * Used to parse fourcc codes that are given as integers.
 * @author Ken Larson
 *
 */
public class FourCCDump
{
	public static void main(String[] args)
	{
		int i = 1448695129; //825770306; // 1196444237;
		System.out.println("Decimal: " + i);
		System.out.println("Hex: 0x" + Integer.toHexString(i));
		String s = new String(reverse(hexStringToByteArray(Integer.toHexString(i))));
		System.out.println("FourCC: " + s);
	}
	
	/**
	 * does not reverse in-place.  returns a new array.
	 */
	public static byte[] reverse(byte[] a)
	{	byte[] result = new byte[a.length];
		for (int i = 0; i < a.length; ++i)
		{	result[a.length - i - 1] = a[i];
		}
		return result;
	}	
	
	/**
	 * 
	 * @throws NumberFormatException
	 */
	public static byte[] hexStringToByteArray(String s)
	{
		byte[] array = new byte[s.length() / 2];
		for (int i = 0; i < array.length; ++i)
		{	array[i] = hexStringToByte(s.substring(i * 2, i * 2 + 2));
		}
		return array;
	}
	
	/**
	 * 
	 * @throws NumberFormatException
	 */
	public static byte hexStringToByte(String s)
	{	return (byte) Integer.parseInt(s, RADIX_16);
	}
	
	private static final int RADIX_16 = 16;
}

#include <stdio.h>
#include <stdlib.h>

#include "yuv2rgb.h"

#ifdef __cplusplus__
extern "C" {
#endif /* __cplusplus__ */

// YUV to RGB conversion added May 31 2005 KAL:
unsigned char clip(int value)
{	if (value <= 0)
		return 0;
	else if (value >= 255)
		return 255;
	else
		return (unsigned char) (value);
}

extern double round(double value);
/*
void yuv2rgb(unsigned char y, unsigned char u, unsigned char v, unsigned char *pr, unsigned char *pg, unsigned char *pb)
{
	float c, d, e;
	
	c = y - 16;
	d = u - 128;
	e = v - 128;

	*pr = clip( (int) round( 1.164383 * c                   + 1.596027 * e  ) );
	*pg = clip( (int) round( 1.164383 * c - (0.391762 * d) - (0.812968 * e) ) );
	*pb = clip( (int) round( 1.164383 * c +  2.017232 * d                   ) );

}
*/

/**
 * Converts a yuv into RGV, using only integer (no double).
 */
inline void yuv2rgb(unsigned char y, unsigned char u, unsigned char v, unsigned char *pr, unsigned char *pg, unsigned char *pb)
{
	int C = 298 * (y - 16);
	int D = u - 128;
	int E = v - 128;
	int r = ( C           + 409 * E + 128) >> 8;
	if (r < 0)
		r = 0;
	else if (r > 255)
		r = 255;
	int g = ( C - 100 * D - 208 * E + 128) >> 8;
	if (g < 0)
		g = 0;
	else if (g > 255)
		g = 255;
	int b = ( C + 516 * D           + 128) >> 8;
	if (b < 0)
		b = 0;
	else if (b > 255)
		b = 255;
	*pr = r;
	*pg = g;
	*pb = b;
}

/**
 * Converts a buffer, encoded in yuyv, into RGB 3 bytes buffer.
 */
unsigned char* yuyvToRGB(unsigned char * yuyv, int width, int height)
{
	unsigned char * buffer = (unsigned char*) malloc(width * height * 3);
	int index = 0, i;
	for (i=0; i < width * height * 2; i+= 4) {
		int y1 = (int) yuyv[i];
		int u = (int) yuyv[i+3];
		int y2 = (int) yuyv[i+2];
		int v = (int) yuyv[i+1];
		buffer[index++] = y1;
		buffer[index++] = u;
		buffer[index++] = v;
		buffer[index++] = y2;
		buffer[index++] = u;
		buffer[index++] = v;
	}
	unpackedyuv2rgb_rwbuf(buffer, width, height);
	return buffer;
}

/**
 * Converts an unpacked YUV buffer (3 bytes) into RGB24 buffer reusing the same buffer.
 * Input buffer contains YUV values (1 byte for Y, 1 for U, and 1 for V) in a raw
 * format. Each pixel has 3 bytes.
 */
void unpackedyuv2rgb_rwbuf(unsigned char *src, int width, int height) {
	int i=0;
	for (i=0; i < width * height * 3; i+= 3) {
		yuv2rgb(src[i] /* y */, src[i+1] /* u */, src[i+2] /* v */, &src[i], &src[i+1], &src[i+2]);					
	}
}


void yuv2rgb_buf(unsigned char *src, int width, int height, unsigned char *dst)
{
	int u_offset = width * height;
	int v_offset = u_offset + width * height / 4;	// TODO: handle rows not divisible by 2.
	int uv_width = width / 2;
	int i;
    for ( i = 0; i < ( width * height ); i++ )
    {
		int px = i % width;
		int py = i / width;
		int uv_x = px / 2; // x in u/v planes
		int uv_y = py / 2; // y in u/v planes
		unsigned char y, u, v;
		unsigned char r, g, b;
        y = src[i];

        u = src[u_offset + uv_width * uv_y + uv_x];
        v = src[v_offset + uv_width * uv_y + uv_x];
		yuv2rgb(y, u, v, &r, &g, &b);

		*dst++ = b;
		*dst++ = g;
		*dst++ = r;
	}

}

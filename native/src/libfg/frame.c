//==========================================================================
//
//  Project:        libfg - Frame Grabber interface for Linux
//
//  Module:         Frame implementation
//
//  Description:    Each frame captured by the FRAMEGRABBER returns a FRAME
//                  (defined here).  It contains the raw frame data, as well
//                  as information about the frame's size and format.
//
//  Author:         Gavin Baker <gavinb@antonym.org>
//
//  Homepage:       http://www.antonym.org/libfg
//
//--------------------------------------------------------------------------
//
//  libfg - Frame Grabber interface for Linux
//  Copyright (c) 2002 Gavin Baker
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
//  or obtain a copy from the GNU website at http://www.gnu.org/
//
//==========================================================================

#include "frame.h"

#include <stdio.h>
#include <stdlib.h>

#include <linux/videodev.h>

#include "yuv2rgb.h"

#ifdef __cplusplus__
extern "C" {
#endif /* __cplusplus__ */

//--------------------------------------------------------------------------

FRAME* frame_new( int width, int height, int format )
{
    FRAME* fr = (FRAME*) malloc( sizeof( FRAME ) );
    int size = 0;

    fr->width = width;
    fr->height = height;
    fr->format = format;

    size = frame_get_size( fr );

    fr->data = malloc( size );

    return fr;
}

//--------------------------------------------------------------------------

void frame_release( FRAME* fr )
{
    free( fr->data );
    free( fr );
}

//--------------------------------------------------------------------------


int frame_get_size( FRAME* fr )
{
    int pixels = fr->width * fr->height;
    int size = 0;

    switch ( fr->format )
    {
        case VIDEO_PALETTE_RGB24:
            // 3 bytes per pixel
            size = pixels * 3;
            break;
            
        case VIDEO_PALETTE_RGB32:
            // 4 bytes (1 int) per pixel
            size = pixels * 4;
            break;

        case VIDEO_PALETTE_RGB565:
            // 2 bytes (16 bits) per pixel
            size = pixels * 2;
            break;

        case VIDEO_PALETTE_RGB555:
            // 2 bytes (15 bits) per pixel
            size = pixels * 2;
            break;

        case VIDEO_PALETTE_YUV422:
            // 1 byte (8 bits) per pixel
            size = pixels;
            break;

		// May 30 2005 KAL:
        case VIDEO_PALETTE_YUV420P:
			size = pixels + 2 * (pixels / 4); // TODO: need to pad out rows if not divisible by 2!  also, if rows/cols not even, add 1!
			break;

        default:
            // Unsupported!
            fprintf( stderr, "frame_get_size(): Unsupported type!\n" );
            size = -1;
    }

    return size;
}

//--------------------------------------------------------------------------




int frame_save( FRAME* fr, const char* filename )
{
    int i = 0;
    int val = 0;
    FRAME_RGB rgb;
    FILE* fp = fopen( filename, "w" );

    if ( fp == NULL )
    {
        perror( "frame_save(): opening file for writing" );
        return -1;
    }

    // Write PNM header
    fprintf( fp, "P6\n" );
    fprintf( fp, "# Generated by a herd of rabid fleas\n" );
    fprintf( fp, "%d %d\n", fr->width, fr->height );

    switch ( fr->format )
    {
        case VIDEO_PALETTE_RGB24:

            // Max val
            fprintf( fp, "255\n" );

            // Write image data
            for ( i = 0; i < ( fr->width * fr->height ); i++ )
            {
                // 3 bytes per pixel
                rgb = ((FRAME_RGB*)(fr->data))[i];

                fprintf( fp, "%c%c%c",
                         rgb.blue,
                         rgb.green,
                         rgb.red );
            }
            break;

        case VIDEO_PALETTE_RGB32:

            // Max val
            fprintf( fp, "255\n" );

            // Write image data
            for ( i = 0; i < ( fr->width * fr->height ); i++ )
            {
                // Retrieve lower 24 bits of ARGB
                val = ((int*)(fr->data))[i] & 0x00ffffff;

                fprintf( fp, "%c%c%c",
                         ( val & 0x00ff0000 ) >> 16, // Blue
                         ( val & 0x0000ff00 ) >>  8, // Green
                         ( val & 0x000000ff )        // Red
                         );
            }
            break;

        case VIDEO_PALETTE_RGB565:
        case VIDEO_PALETTE_RGB555:

            // Max val
            fprintf( fp, "65535\n" );

            // Write image data
            for ( i = 0; i < ( fr->width * fr->height ); i++ )
            {
                // Retrieve 16-bit words
                val = ((short*)(fr->data))[i];

                fprintf( fp, "%c%c",
                         ( val & 0xff00 ) >> 8, // High
                         ( val & 0x00ff )       // Low
                         );
            }

            break;

        case VIDEO_PALETTE_YUV422:

            // Max val
            fprintf( fp, "255\n" );

            // Dump raw image data
            fwrite( fr->data,
                    1, // YUV422 has 1 byte per pixel
                    fr->width * fr->height,
                    fp );
            break;
		// VIDEO_PALETTE_YUV420P support added May 31 2005 KAL:
        case VIDEO_PALETTE_YUV420P:
            // Max val
            fprintf( fp, "255\n" );
			{		
				int u_offset = fr->width * fr->height;
				int v_offset = u_offset + fr->width * fr->height / 4;	// TODO: handle rows not divisible by 2.
				int uv_width = fr->width / 2;
	            for ( i = 0; i < ( fr->width * fr->height ); i++ )
    	        {
					int px = i % fr->width;
					int py = i / fr->width;
					int uv_x = px / 2; // x in u/v planes
					int uv_y = py / 2; // y in u/v planes
					unsigned char y, u, v;
					unsigned char r, g, b;
	                y = ((unsigned char*)(fr->data))[i];

    	            u = ((unsigned char*)(fr->data))[u_offset + uv_width * uv_y + uv_x];
    	            v = ((unsigned char*)(fr->data))[v_offset + uv_width * uv_y + uv_x];
					yuv2rgb(y, u, v, &r, &g, &b);

					fwrite( &r, 1, 1, fp );
					fwrite( &g, 1, 1, fp );
					fwrite( &b, 1, 1, fp );
				}

            }
            break;

        default:
            // Unsupported!
            fprintf( stderr, "frame_save(): Unsupported type!\n" );
            return -1;
    }

    fclose( fp );

    return 0;
}

//==========================================================================

#ifdef __cplusplus__
}
#endif /* __cplusplus__ */

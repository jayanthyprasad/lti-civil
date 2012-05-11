/*
 * yuv_convert.h --
 *
 *      Defines generic yuv_conversion routines
 *
 *	  destWidth and destHeight specify image dimensions for dest buffer
 *	  srcWidth and srcHeight specify image dimensions for src buffer
 *	  dimensions are specified in terms of size of video image
 *
 * Copyright (c) 2001 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the names of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef YUV_CONVERT_H
#define YUV_CONVERT_H

#include "converter.h"

#define PLANAR_YUYV422 0
#define PLANAR_YUYV420 1
#define PACKED_YUYV422 2
#define PACKED_UYVY422 3

class YUV_Converter : public Converter {
public:
    YUV_Converter(int inputType, int outputType);
    ~YUV_Converter();
    virtual void convert(u_int8_t* src, int w, int h, u_int8_t* dest, int outw = 0, int outh = 0, int invert = 0);
private:
    bool (*yuvFunc)(u_int8_t* dest, int destWidth, int destHeight,
    		u_int8_t* src, int srcWidth, int srcHeight);
};

#endif

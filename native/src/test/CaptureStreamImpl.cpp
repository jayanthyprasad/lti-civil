#include "VideoFormat.h"
#include "CaptureStreamImpl.h"
#include "CaptureObserver.h"
#include "CaptureException.h"
#include "Image.h"
#include <stdio.h>
#include <stdlib.h>

static unsigned char buf24[256 * 256 * 3];
static Image testImage24 = Image(RGB24, 256, 256, buf24, 256 * 256 * 3);

static unsigned char buf32[256 * 256 * 4];
static Image testImage32 = Image(RGB32, 256, 256, buf32, 256 * 256 * 4);


static void fillTestImage24()
{
		// fill the test image with a 2d gradient:
		// should start as black in upper left, yellow in lower right.
		// upper right should be red, lower left should be green.  blueish vertical stripe down middle.
		for (int y = 0; y < 256; ++y)
		{	for (int x = 0; x < 256; ++x)
			{	int off = 3 * (y * 256 + x);
				buf24[off + 0] = x;	// r
				buf24[off + 1] = y;	// g
				if (x > 128 - 16 && x < 128 + 16)
					buf24[off + 2] = 255;	// b
				else
					buf24[off + 2] = 0;	// b
			}
		}
}

static void fillTestImage32()
{
		// fill the test image with a 2d gradient:
		// should start as black in upper left, yellow in lower right.
		// upper right should be red, lower left should be green.  blueish vertical stripe down middle.
		for (int y = 0; y < 256; ++y)
		{	for (int x = 0; x < 256; ++x)
			{	int off = 4 * (y * 256 + x);
				buf32[off + 0] = x;	// r
				buf32[off + 1] = y;	// g
				if (x > 128 - 16 && x < 128 + 16)
					buf32[off + 2] = 255;	// b
				else
					buf32[off + 2] = 0;	// b
			}
		}
}



void CaptureStreamImpl::start()// throws CaptureException;
{
      	
	if (observer != 0)
	{	
		printf("CaptureStreamImpl::start(): observer != 0\n");
		
		fillTestImage24();
		fillTestImage32();

		printf("CaptureStreamImpl::start(): calling observer \n");
		observer->onNewImage(this, &testImage32);	// TEST
	}
	else
		printf("CaptureStreamImpl::start(): observer == 0\n");
       
}
void CaptureStreamImpl::stop()// throws CaptureException;
{
	if (observer != 0)
		observer->onError(this, new CaptureException("test exception", -1));
	//printf("stop\n");
}

void CaptureStreamImpl::dispose()// throws CaptureException;
{
}

void CaptureStreamImpl::threadMain()// throws CaptureException;
{

}


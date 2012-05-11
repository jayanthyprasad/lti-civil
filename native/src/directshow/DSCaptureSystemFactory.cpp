#include "stdafx.h"
#include "VideoFormat.h"
#include "DSCaptureSystemFactory.h"
#include "DSCaptureSystem.h"


CaptureSystemFactory *gCaptureSystemFactory = new DSCaptureSystemFactory();

CaptureSystem *DSCaptureSystemFactory::createCaptureSystem()// throws CaptureException
{	return new DSCaptureSystem();
}

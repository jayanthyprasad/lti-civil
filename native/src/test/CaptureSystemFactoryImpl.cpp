#include "CaptureSystemFactoryImpl.h"
#include "CaptureSystemImpl.h"


CaptureSystemFactory *gCaptureSystemFactory = new CaptureSystemFactoryImpl();

CaptureSystem *CaptureSystemFactoryImpl::createCaptureSystem()// throws CaptureException
{	return new CaptureSystemImpl();
}

#include "CaptureSystemImpl.h"
#include "CaptureException.h"
#include "CaptureStreamImpl.h"

void CaptureSystemImpl::init() /*throws CaptureException*/
{
	//throw new CaptureException("test exception", -1);
}

void CaptureSystemImpl::dispose() /*throws CaptureException*/
{
}

/** @return List of {@link CaptureDeviceInfo} */
void CaptureSystemImpl::getCaptureDeviceInfoList(list<CaptureDeviceInfo> &result) /*throws CaptureException*/
{
	result.push_back(CaptureDeviceInfo(L"abc", L"xyz"));	// TODO: deal with memory allocation issues.
}

CaptureStream *CaptureSystemImpl::openCaptureDeviceStream(const wchar_t *deviceId) /*throws CaptureException*/
{	
	return new CaptureStreamImpl();	// TODO: use deviceId
}

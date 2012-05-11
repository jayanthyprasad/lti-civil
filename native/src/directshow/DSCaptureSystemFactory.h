#include "CaptureSystemFactory.h"

class DSCaptureSystemFactory : public CaptureSystemFactory
{
public:
	virtual CaptureSystem *createCaptureSystem();// throws CaptureException
};

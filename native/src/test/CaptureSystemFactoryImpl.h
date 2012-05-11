#include "CaptureSystemFactory.h"

class CaptureSystemFactoryImpl : public CaptureSystemFactory
{
public:
	virtual CaptureSystem *createCaptureSystem();// throws CaptureException
};

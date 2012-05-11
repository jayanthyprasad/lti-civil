#include "CaptureStream.h"

class CaptureStreamImpl: public CaptureStream
{
private:
	CaptureObserver *observer;
public:
	CaptureStreamImpl()
	{	observer = 0;
	}
	virtual void start();// throws CaptureException;
	virtual void stop();// throws CaptureException;
	virtual void threadMain();// throws CaptureException;
	virtual void dispose();// throws CaptureException;
	virtual void setObserver(CaptureObserver *_observer) {observer = _observer;}
	virtual CaptureObserver *getObserver() {return observer;}
	
};

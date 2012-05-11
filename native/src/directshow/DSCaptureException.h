#include <winerror.h>
#include <stdio.h>
#include "CaptureException.h"

class DSCaptureException : public CaptureException
{
public:

	DSCaptureException(const char *msg, int code)
		: CaptureException(msg, code)
	{
		
	}
	DSCaptureException(const char *msg)
		: CaptureException(msg)
	{
		
	}
	const char *GetErrorString()
	{
		switch (errorCode)
		{
			case 0:
				return "";
			case 1:
				return "Misc";

		}
	}

	static void FailWithException(const char *msg, int hr)
	{
//		fprintf(stderr, "DSCaptureException::FailWithException: hr=0x%x.\n", hr);
//		fflush(stderr);
	
		throw new DSCaptureException(msg, hr);
	}

	static void CheckForFailure(const char *msg, int hr)
	{
		if (hr < 0)
		{	
			FailWithException(msg, hr);
		}
	}
};



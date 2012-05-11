#include "stdafx.h"
#include <dshow.h>
#include <Qedit.h>	// for ISampleGrabber

#include "VideoFormat.h"
#include "DSCaptureException.h"
#include "DSCaptureStream.h"
#include "DSCaptureInfo.h"
#include "Image.h"

const GUID MEDIASUBTYPE_I420 = { 0x30323449, 0x0000, 0x0010, {0x80, 0x00, 0x00, 0xaa, 0x00, 0x38, 0x9b, 0x71} };

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

extern int ConnectFilters(IGraphBuilder* pGraph, IBaseFilter* pFilterUpstream, IBaseFilter* pFilterDownstream);

class CMediaEventHandler : public ISampleGrabberCB
{
public:
	CMediaEventHandler(DSCaptureStream* pStream,CaptureObserver *pClient)
    {
		m_pStream = pStream;
		m_pClient = pClient;
		m_bytesPerPixel = -1;
		m_imagePtr = NULL;

		//m_bytesPerPixel = m_pStream->getBitsPerPixel() /8;
		//m_imageSize = m_pStream->getVideoFormat().getWidth() * m_pStream->getVideoFormat().getHeight() * m_bytesPerPixel;
		//m_imagePtr  = new unsigned char [m_imageSize];

		m_flipImage = true;
		m_bStop = false;
	    m_hStoppedEvent = CreateEvent(NULL,false,false,NULL);
		InitializeCriticalSection(&m_critSect);
		InitializeCriticalSection(&m_testCritSect);
		transferring = false;
   }
	~CMediaEventHandler()
	{
  	  delete [] m_imagePtr;
	  DeleteCriticalSection(&m_critSect);
	  DeleteCriticalSection(&m_testCritSect);
	  CloseHandle(m_hStoppedEvent);
	}

    STDMETHODIMP_(ULONG) AddRef() { return 2; }
    STDMETHODIMP_(ULONG) Release() { return 1; }

    STDMETHODIMP QueryInterface(REFIID riid, void ** ppv)
    {
        if (riid == IID_ISampleGrabberCB || riid == IID_IUnknown)
        {
            *ppv = (void *) static_cast<ISampleGrabberCB *>(this);
            return NOERROR;
        }
        return E_NOINTERFACE;
    }

    STDMETHODIMP SampleCB( double SampleTime, IMediaSample * pSample )
    {
         BYTE *pBuffer;
         EnterCriticalSection(&m_testCritSect);
         if (transferring) {
           	fprintf(stderr, "Missed sample as was transferring already\n");
         	fflush(stderr);
        	 LeaveCriticalSection(&m_testCritSect);
        	 return 0;
         }
         transferring = true;
         LeaveCriticalSection(&m_testCritSect);

		 EnterCriticalSection(&m_critSect);

		 if (m_bStop)
		 {
			 SetEvent(m_hStoppedEvent);
			 LeaveCriticalSection(&m_critSect);
			 return 0;
		 }

		 pSample->GetPointer(&pBuffer);

		Image image(m_pStream->getVideoFormat(), pBuffer , pSample->GetActualDataLength());

		//CString msg;
		//msg.Format("onNewImage: format= %d width = %d height = %d ptr=0x%x size = %d\n",format,m_pStream->getVideoFormat().getWidth(),m_pStream->getVideoFormat().getHeight(), m_imagePtr , pSample->GetActualDataLength());
		//OutputDebugString(msg);

		m_pClient->onNewImage(m_pStream,&image);

		LeaveCriticalSection(&m_critSect);

		EnterCriticalSection(&m_testCritSect);
		 transferring = false;
		 LeaveCriticalSection(&m_testCritSect);
        return 0;
    }

    STDMETHODIMP BufferCB( double SampleTime, BYTE * pBuffer, long BufferLen )
    {
        return 0;
    }

	void Stop()
	{
	    EnterCriticalSection(&m_critSect);
		m_bStop = true;
	    LeaveCriticalSection(&m_critSect);
		DWORD waitResult=WaitForSingleObject(m_hStoppedEvent,10000);
	}

	void Run()
	{
		m_bStop = false;
	}

private:
  CaptureObserver  *m_pClient;
  DSCaptureStream    *m_pStream;
  int m_imageSize;
  int m_bytesPerPixel;
  unsigned char * m_imagePtr;
  bool m_flipImage;
  bool m_bStop;
  CRITICAL_SECTION m_critSect;
  CRITICAL_SECTION m_testCritSect;
  bool transferring;
  HANDLE m_hStoppedEvent;

};

//  Free an existing media type (ie free resources it holds)

void WINAPI FreeMediaType(AM_MEDIA_TYPE& mt)
{
    if (mt.cbFormat != 0) {
        CoTaskMemFree((PVOID)mt.pbFormat);

        // Strictly unnecessary but tidier
        mt.cbFormat = 0;
        mt.pbFormat = NULL;
    }
    if (mt.pUnk != NULL) {
        mt.pUnk->Release();
        mt.pUnk = NULL;
    }
}

// general purpose function to delete a heap allocated AM_MEDIA_TYPE structure
// which is useful when calling IEnumMediaTypes::Next as the interface
// implementation allocates the structures which you must later delete
// the format block may also be a pointer to an interface to release

void WINAPI DeleteMediaType(AM_MEDIA_TYPE *pmt)
{
    // allow NULL pointers for coding simplicity

    if (pmt == NULL) {
        return;
    }

    FreeMediaType(*pmt);
    CoTaskMemFree((PVOID)pmt);
}

DSCaptureStream::DSCaptureStream(CaptureInfo *pCaptureInfo)
{

	m_pCaptureInfo = new struct CaptureInfo;
	*m_pCaptureInfo = *pCaptureInfo;
	m_pStreamEventHandler = NULL;
	m_format = VideoFormat();
	m_pCurMediaType = NULL;
	m_pCurOutputMediaType = NULL;
	m_bPaused = false;
	enumMyMediaTypes();
}

DSCaptureStream::~DSCaptureStream()
{
	for (unsigned i=0;i<m_pCaptureInfo->m_mediaTypes.size();i++)
	{
		struct mediaTypes *mediaTypes = m_pCaptureInfo->m_mediaTypes.front();
		DeleteMediaType(mediaTypes->captureMediaType);
		if (mediaTypes->outputMediaType != NULL) {
			DeleteMediaType(mediaTypes->outputMediaType);
		}
		m_pCaptureInfo->m_mediaTypes.pop_front();
		//delete mediaTypes;
	}
	delete m_pCaptureInfo;
}

#define USE_RESUMABLE_STOP 1

void DSCaptureStream::start()// throws CaptureException;
{
	HRESULT hr;

	hr = m_pCaptureInfo->m_pSampleGrabber->SetCallback(m_pStreamEventHandler,0);
	DSCaptureException::CheckForFailure("pSampleGrabber->SetBufferSamples(TRUE) failed", hr);

	hr = m_pCaptureInfo->m_pSampleGrabber->SetBufferSamples(true);
	DSCaptureException::CheckForFailure("pSampleGrabber->SetBufferSamples(TRUE) failed", hr);
 	hr = m_pCaptureInfo->m_pSampleGrabber->SetOneShot(false);
	DSCaptureException::CheckForFailure("pSampleGrabber->SetOneShot(TRUE) failed", hr);

	if (!USE_RESUMABLE_STOP || !m_bPaused)
	{
		if (m_pCurMediaType)
		{
	        IAMStreamConfig     *pSC;

	        // Retrieve a pointer to the IAMStreamConfig interface
	        m_pCaptureInfo->m_pBuild->FindInterface(&PIN_CATEGORY_CAPTURE,
	                                  &MEDIATYPE_Video,
	                                  m_pCaptureInfo->m_pBaseFilter,
	                                  IID_IAMStreamConfig, (void **)&pSC);

	        hr=pSC->SetFormat(m_pCurMediaType);
	        if (FAILED(hr))
	        {
	            pSC->Release();
	            return ;
	        }
	        pSC->Release();
		}

		if (m_pCurOutputMediaType) {
			hr = m_pCaptureInfo->m_pSampleGrabber->SetMediaType(m_pCurOutputMediaType);
			DSCaptureException::CheckForFailure("pSampleGrabber->SetMediaType failed", hr);
		}

		hr = ConnectFilters(m_pCaptureInfo->m_pGraph, m_pCaptureInfo->m_pBaseFilter, m_pCaptureInfo->m_pGrabberBaseFilter);
		DSCaptureException::CheckForFailure("ConnectFilters", hr);
		hr = ConnectFilters(m_pCaptureInfo->m_pGraph, m_pCaptureInfo->m_pGrabberBaseFilter,m_pCaptureInfo->m_pRenderer);
		DSCaptureException::CheckForFailure("ConnectFilters NULL renderer Failed", hr);

		//Step 4: Now we run the graph and collects the data from the sample grabber.
	}

	if (m_pStreamEventHandler)
	{
		m_pStreamEventHandler->Run();
	}


	hr = m_pCaptureInfo->m_pMediaControl->Run();
	DSCaptureException::CheckForFailure("pMediaControl->Run() failed", hr);
	m_bPaused = false;
}


void DSCaptureStream::stop()// throws CaptureException;
{
	HRESULT hr;

	if (m_pStreamEventHandler)
	{
		m_pStreamEventHandler->Stop();

		hr = m_pCaptureInfo->m_pSampleGrabber->SetCallback(NULL,0);
		DSCaptureException::CheckForFailure("pSampleGrabber->SetBufferSamples(TRUE) failed", hr);
	}

	if (m_pCaptureInfo)
	{
		hr = m_pCaptureInfo->m_pMediaControl->Stop();
		DSCaptureException::CheckForFailure("m_pCaptureInfo->m_pMediaControl->Stop failed", hr);
	}

#if USE_RESUMABLE_STOP
	m_bPaused = true;
#endif


}
void DSCaptureStream::setObserver(CaptureObserver *_observer)
{
	m_pObserver = _observer;
	if (m_pStreamEventHandler)
	{
		delete m_pStreamEventHandler;
	}
	m_pStreamEventHandler = new CMediaEventHandler(this,m_pObserver);
}

//-----------------------------------------------------------------------------------------
// Tear down everything downstream of a given filter
bool DSCaptureStream::nukeDownstream(IBaseFilter *pf,IGraphBuilder *pFilterGraph)
{
    IPin        *pP, *pTo;
    ULONG       u;
    IEnumPins   *pins = NULL;
    PIN_INFO    pininfo;
    HRESULT     hr;
    ULONG       uRefCount;

    hr = pf->EnumPins(&pins);

    pins->Reset();
    while (hr == NOERROR)
    {
        hr = pins->Next(1, &pP, &u);
        if (hr == S_OK && pP)
        {
            pP->ConnectedTo(&pTo);
            if (pTo)
            {
                hr = pTo->QueryPinInfo(&pininfo);
                if (hr == NOERROR)
                {
                    if (pininfo.dir == PINDIR_INPUT)
                    {
                        nukeDownstream(pininfo.pFilter,pFilterGraph);
                        pFilterGraph->Disconnect(pTo);
                        pFilterGraph->Disconnect(pP);
                        pFilterGraph->RemoveFilter(pininfo.pFilter);
                    }
                    uRefCount = pininfo.pFilter->Release();
                }
                pTo->Release();
            }
            pP->Release();
        }
    }
    if (pins)
        pins->Release();
    return true;
}

void DSCaptureStream::dispose()// throws CaptureException;
{
 	HRESULT     hr;

	if (m_pCaptureInfo)
	{
		if (m_pCaptureInfo->m_pGraph != NULL && m_pCaptureInfo->m_pBaseFilter != NULL)
		{	nukeDownstream(m_pCaptureInfo->m_pBaseFilter, m_pCaptureInfo->m_pGraph);
			m_pCaptureInfo->m_pGraph->Release();
		}
	}
}

void DSCaptureStream::threadMain()// throws CaptureException;
{	// called from another thread, this method is allowed to never return until dispose is called.
	// this saves us the trouble of creating our own thread.  Use of this method to do anything is optional.
}

struct captureFormat {
	LONG width;
	LONG height;
	std::list<AM_MEDIA_TYPE *> *types;
};

struct captureFormat *findFormat(std::list<struct captureFormat *> *formats, LONG width, LONG height) {
	std::list<struct captureFormat *>::iterator iter = formats->begin();
	for (int i = 0; i < formats->size(); i++) {
		struct captureFormat *format = *iter;
		if ((format->width == width) && (format->height == height)) {
			return format;
		}
		iter++;
	}
	struct captureFormat *newFormat = (struct captureFormat *) malloc(sizeof(struct captureFormat));
	newFormat->width = width;
	newFormat->height = height;
	newFormat->types = new std::list<AM_MEDIA_TYPE *>();
	formats->push_back(newFormat);
	return newFormat;
}

AM_MEDIA_TYPE *createType(GUID subtype, int samplesize, RECT source, RECT target, REFERENCE_TIME AvgTimePerFrame, int width, int height, int bitsPerPixel) {
	AM_MEDIA_TYPE *mt2 = (AM_MEDIA_TYPE *) malloc(sizeof(AM_MEDIA_TYPE));
	mt2->majortype = MEDIATYPE_Video;
	mt2->subtype = subtype;
	mt2->formattype = FORMAT_VideoInfo;
	mt2->lSampleSize = samplesize;
	mt2->pbFormat = (BYTE *) malloc(sizeof(VIDEOINFOHEADER));
	VIDEOINFOHEADER *vidheader = reinterpret_cast<VIDEOINFOHEADER*> (mt2->pbFormat);
	vidheader->rcSource = source;
	vidheader->rcTarget = target;
	vidheader->AvgTimePerFrame = AvgTimePerFrame;
	vidheader->bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
	vidheader->bmiHeader.biWidth = width;
	vidheader->bmiHeader.biHeight = height;
	vidheader->bmiHeader.biBitCount = bitsPerPixel;
	return mt2;
}

void DSCaptureStream::enumMyMediaTypes()
{
	std::list<struct captureFormat *> formatsSupported;
	IPin        *pPin;
	HRESULT hr = m_pCaptureInfo->m_pBuild->FindPin(m_pCaptureInfo->m_pBaseFilter,PINDIR_OUTPUT,&PIN_CATEGORY_CAPTURE, NULL, false, 0, &pPin);
	if (SUCCEEDED(hr))
	{
		IEnumMediaTypes *pEnum;
		pPin->EnumMediaTypes(&pEnum);
		if (pEnum)
		{	ULONG c=1;
			AM_MEDIA_TYPE *mt;
			while (c > 0)
			{
				pEnum->Next(1, &mt, &c);
				if (c)
				{
					VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (mt->pbFormat);
					LONG width = pInfo->bmiHeader.biWidth;
					LONG height = pInfo->bmiHeader.biHeight;
					struct captureFormat *format = findFormat(&formatsSupported, width, height);
					format->types->push_back(mt);
				}
			}
		}

		std::list<struct captureFormat *>::iterator iter = formatsSupported.begin();
		int largestWidth = 0;
		int largestHeight = 0;
		int largestArea = 0;
		struct mediaTypes *largestRGB32Format = NULL;
		for (int i = 0; i < formatsSupported.size(); i++) {
			struct captureFormat *format = *iter;

			int area = format->width * format->height;
			if (area > largestArea) {
				largestArea = area;
				largestWidth = format->width;
				largestHeight = format->height;
			}

			AM_MEDIA_TYPE *rgb24Format = NULL;
			AM_MEDIA_TYPE *rgb32Format = NULL;
			AM_MEDIA_TYPE *yuy2Format = NULL;
			AM_MEDIA_TYPE *uyvyFormat = NULL;
			bool containsI420 = false;
			std::list<AM_MEDIA_TYPE *>::iterator typeIter = format->types->begin();
			AM_MEDIA_TYPE *first = NULL;
			for (int k = 0; k < format->types->size(); k++) {
				AM_MEDIA_TYPE *origType = *typeIter;
				if (first == NULL) {
					first = origType;
				}

				struct mediaTypes *types = (struct mediaTypes *) malloc(sizeof(struct mediaTypes));
				types->captureMediaType = origType;
				types->outputMediaType = NULL;
				types->convertToI420 = false;
				m_pCaptureInfo->m_mediaTypes.push_back(types);

				VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (origType->pbFormat);

				if (origType->subtype == MEDIASUBTYPE_RGB32) {
					rgb32Format = origType;
					if (area == largestArea) {
						largestRGB32Format = types;
					}
					types->outputMediaType = createType(MEDIASUBTYPE_RGB32, 4,
						pInfo->rcSource, pInfo->rcTarget, pInfo->AvgTimePerFrame,
						pInfo->bmiHeader.biWidth, pInfo->bmiHeader.biHeight, 32);
					pInfo->bmiHeader.biHeight *= -1;
				} else if (origType->subtype == MEDIASUBTYPE_RGB24) {
					rgb24Format = origType;
					types->outputMediaType = createType(MEDIASUBTYPE_RGB24, 3,
						pInfo->rcSource, pInfo->rcTarget, pInfo->AvgTimePerFrame,
						pInfo->bmiHeader.biWidth, pInfo->bmiHeader.biHeight, 24);
					pInfo->bmiHeader.biHeight *= -1;
				} else if (origType->subtype == MEDIASUBTYPE_I420) {
					containsI420 = true;
				} else if (origType->subtype == MEDIASUBTYPE_YUY2) {
					yuy2Format = origType;
				} else if (origType->subtype == MEDIASUBTYPE_UYVY) {
					uyvyFormat = origType;
				}

			    typeIter++;
			}
			if (rgb24Format == NULL) {
				VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (first->pbFormat);
				struct mediaTypes *types = (struct mediaTypes *) malloc(sizeof(struct mediaTypes));
				types->captureMediaType = first;
				types->outputMediaType = createType(MEDIASUBTYPE_RGB24, 3,
						pInfo->rcSource, pInfo->rcTarget, pInfo->AvgTimePerFrame,
						pInfo->bmiHeader.biWidth, -1 * pInfo->bmiHeader.biHeight, 24);
				types->convertToI420 = false;
				m_pCaptureInfo->m_mediaTypes.push_back(types);
			}
			if (rgb32Format == NULL) {
				VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (first->pbFormat);
				struct mediaTypes *types = (struct mediaTypes *) malloc(sizeof(struct mediaTypes));
				types->captureMediaType = first;
				types->outputMediaType = createType(MEDIASUBTYPE_RGB32, 4,
						pInfo->rcSource, pInfo->rcTarget, pInfo->AvgTimePerFrame,
						pInfo->bmiHeader.biWidth, -1 * pInfo->bmiHeader.biHeight, 32);
				types->convertToI420 = false;
				m_pCaptureInfo->m_mediaTypes.push_back(types);
				if (area == largestArea) {
					largestRGB32Format = types;
				}
			}
			if (!containsI420) {
				struct mediaTypes *types = (struct mediaTypes *) malloc(sizeof(struct mediaTypes));
				if ((rgb24Format == NULL) && (rgb32Format == NULL) && (yuy2Format == NULL) && (uyvyFormat == NULL)) {
					VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (first->pbFormat);
					types->captureMediaType = first;
					types->outputMediaType = createType(MEDIASUBTYPE_RGB24, 3,
							pInfo->rcSource, pInfo->rcTarget, pInfo->AvgTimePerFrame,
							pInfo->bmiHeader.biWidth, -1 * pInfo->bmiHeader.biHeight, 24);
				} else if (yuy2Format != NULL) {
					types->captureMediaType = yuy2Format;
				} else if (uyvyFormat != NULL) {
					types->captureMediaType = uyvyFormat;
				} else if (rgb24Format != NULL) {
					VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (rgb24Format->pbFormat);
					types->captureMediaType = rgb24Format;
					types->outputMediaType = createType(MEDIASUBTYPE_RGB24, 3,
							pInfo->rcSource, pInfo->rcTarget, pInfo->AvgTimePerFrame,
							pInfo->bmiHeader.biWidth, -1 * pInfo->bmiHeader.biHeight, 24);
				} else if (rgb32Format != NULL) {
					VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (rgb32Format->pbFormat);
					types->captureMediaType = rgb32Format;
					types->outputMediaType = createType(MEDIASUBTYPE_RGB32, 4,
							pInfo->rcSource, pInfo->rcTarget, pInfo->AvgTimePerFrame,
							pInfo->bmiHeader.biWidth, -1 * pInfo->bmiHeader.biHeight, 32);
				}
				types->convertToI420 = true;
				m_pCaptureInfo->m_mediaTypes.push_back(types);
			}
			iter++;
		}

		if (largestRGB32Format != NULL) {
			if (largestRGB32Format->outputMediaType != NULL) {
			    setVideoFormat(buildVideoFormat(largestRGB32Format->outputMediaType, largestRGB32Format));
			} else {
				setVideoFormat(buildVideoFormat(largestRGB32Format->captureMediaType, largestRGB32Format));
			}
		}
	}
}

void DSCaptureStream::enumVideoFormats(std::list<VideoFormat> &result)
{
	std::list<struct mediaTypes *>::iterator iter = m_pCaptureInfo->m_mediaTypes.begin();

	for (unsigned i=0;i<m_pCaptureInfo->m_mediaTypes.size();i++)
	{
		struct mediaTypes *mt = *iter;
		VideoFormat format;

	    if (mt->outputMediaType != NULL) {
		    format = buildVideoFormat(mt->outputMediaType, mt);
		} else {
			format = buildVideoFormat(mt->captureMediaType, mt);
		}
	    if (mt->convertToI420) {
	    	format.setConvertToI420(true);
	    }
		result.push_back(format);
		iter++;
	}
}

VideoFormat DSCaptureStream::getVideoFormat() // throws CaptureException;
{
	return m_format;
}

int getVideoFormatId(GUID type) {
	if (type == MEDIASUBTYPE_RGB24) {
		return RGB24;
	} else if (type == MEDIASUBTYPE_RGB32) {
		return RGB32;
	} else if (type == MEDIASUBTYPE_RGB555) {
		return RGB555;
	} else if (type == MEDIASUBTYPE_RGB565) {
		return RGB565;
	} else if (type == MEDIASUBTYPE_ARGB32) {
		return ARGB32;
	} else if (type == MEDIASUBTYPE_ARGB1555) {
		return ARGB1555;
	} else if (type == MEDIASUBTYPE_UYVY) {
		return UYVY;
	} else if (type == MEDIASUBTYPE_YUYV) {
		return YUYV;
	} else if (type == MEDIASUBTYPE_YVYU) {
		return YVYU;
	} else if (type == MEDIASUBTYPE_YUY2) {
		return YUY2;
	} else if (type == MEDIASUBTYPE_NV12) {
		return NV12;
	} else if (type == MEDIASUBTYPE_I420) {
		return I420;
	} else if (type == MEDIASUBTYPE_IYUV) {
		return IYUV;
	} else if (type == MEDIASUBTYPE_Y411) {
		return Y411;
	} else if (type == MEDIASUBTYPE_YVU9) {
		return YVU9;
	} else if (type == MEDIASUBTYPE_MJPG) {
		return MJPG;
	} else if (type == MEDIASUBTYPE_dvsl) {
		return DVSL;
	} else if (type == MEDIASUBTYPE_dvsd) {
		return DVSD;
	} else if (type == MEDIASUBTYPE_dvhd) {
		return DVHD;
	}
}

VideoFormat DSCaptureStream::buildVideoFormat(AM_MEDIA_TYPE *mt, void *handle)
{
	VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (mt->pbFormat);
	float fps = 1.0f/(pInfo->AvgTimePerFrame * 100e-9f);

	int formatId = getVideoFormatId(mt->subtype);

	VideoFormat format = VideoFormat(handle,
							formatId,
							pInfo->bmiHeader.biWidth,
							pInfo->bmiHeader.biHeight,
							fps);
	if (formatId == RGB32) {
		format.setDataType(DATA_TYPE_INT_ARRAY);
	}
	format.setBytesPerPixel(pInfo->bmiHeader.biBitCount / 8);
	if (format.height < 0) {
		format.height *= -1;
		format.setFlipped(true);
	}
	return format;
}

void DSCaptureStream::setVideoFormat(VideoFormat &format)
{
	struct mediaTypes *mt = (struct mediaTypes *)format.handle;
	if (mt == NULL)
		throw new DSCaptureException("cannot set format: null handle");
	if (mt->captureMediaType->lSampleSize == 0)
		throw new DSCaptureException("cannot set format: lSampleSize == 0");

	m_pCurMediaType = mt->captureMediaType;
	m_pCurOutputMediaType = mt->outputMediaType;

    if (m_pCurOutputMediaType != NULL) {
		VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (m_pCurOutputMediaType->pbFormat);
		m_format = buildVideoFormat(m_pCurOutputMediaType, NULL);
	} else {
		VIDEOINFOHEADER *pInfo = reinterpret_cast<VIDEOINFOHEADER*> (m_pCurMediaType->pbFormat);
		m_format = buildVideoFormat(m_pCurMediaType, NULL);
	}
    if (mt->convertToI420) {
		m_format.setConvertToI420(true);
	}
}

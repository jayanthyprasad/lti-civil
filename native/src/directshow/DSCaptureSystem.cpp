#include "stdafx.h"
#include "DSCaptureSystem.h"

#include <dshow.h>
#include <Qedit.h>	// for ISampleGrabber

#include "VideoFormat.h"
#include "DSCaptureException.h"
#include "DSCaptureStream.h"
#include "DSCaptureInfo.h"

DWORD g_dwGraphRegister=0;
HRESULT AddGraphToRot(
        IUnknown *pUnkGraph,
        DWORD *pdwRegister
        ) ;

int ConnectFilters(IGraphBuilder* pGraph, IBaseFilter* pFilterUpstream, IBaseFilter* pFilterDownstream);

HRESULT InitCaptureGraphBuilder(
  IGraphBuilder **ppGraph,  // Receives the pointer.
  ICaptureGraphBuilder2 **ppBuild  // Receives the pointer.
);


void DSCaptureSystem::init() /*throws CaptureException*/
{
	HRESULT hrInitialize = CoInitialize(NULL);
	DSCaptureException::CheckForFailure("CoInitialize Failed", hrInitialize);
}

void DSCaptureSystem::dispose() /*throws CaptureException*/
{
	CoUninitialize( );
}

bool isVideo(long lType) {
	switch (lType)
	    {
	    case PhysConn_Video_Tuner:
	    case PhysConn_Video_Composite:
	    case PhysConn_Video_SVideo:
	    case PhysConn_Video_RGB:
	    case PhysConn_Video_YRYBY:
	    case PhysConn_Video_SerialDigital:
	    case PhysConn_Video_ParallelDigital:
	    case PhysConn_Video_SCSI:
	    case PhysConn_Video_AUX:
	    case PhysConn_Video_1394:
	    case PhysConn_Video_USB:
	    case PhysConn_Video_VideoDecoder:
	    case PhysConn_Video_VideoEncoder:
	    case PhysConn_Video_SCART:
	    case PhysConn_Video_Black:
	    	return true;
	    }
	return false;
}

const wchar_t * GetPhysicalPinName(long lType)
{
    switch (lType)
    {
    case PhysConn_Video_Tuner:            return L"Tuner";
    case PhysConn_Video_Composite:        return L"Composite";
    case PhysConn_Video_SVideo:           return L"S-Video";
    case PhysConn_Video_RGB:              return L"RGB";
    case PhysConn_Video_YRYBY:            return L"YRYBY";
    case PhysConn_Video_SerialDigital:    return L"Serial Digital";
    case PhysConn_Video_ParallelDigital:  return L"Parallel Digital";
    case PhysConn_Video_SCSI:             return L"SCSI";
    case PhysConn_Video_AUX:              return L"AUX";
    case PhysConn_Video_1394:             return L"FireWire";
    case PhysConn_Video_USB:              return L"USB";
    case PhysConn_Video_VideoDecoder:     return L"Decoder";
    case PhysConn_Video_VideoEncoder:     return L"Encoder";
    case PhysConn_Video_SCART:            return L"SCART";
    case PhysConn_Video_Black:            return L"Black";

    default:                              return L"Unknown Type";
    }
}

/** @return List of {@link CaptureDeviceInfo} */
void DSCaptureSystem::getCaptureDeviceInfoList(list<CaptureDeviceInfo> &result) /*throws CaptureException*/
{
// adapted from http://msdn.microsoft.com/library/default.asp?url=/library/en-us/directshow/htm/selectingacapturedevice.asp

	ICreateDevEnum *pDevEnum = NULL;
	IEnumMoniker *pEnum = NULL;

	// Create the System Device Enumerator.
	HRESULT hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL,
	    CLSCTX_INPROC_SERVER, IID_ICreateDevEnum,
	    reinterpret_cast<void**>(&pDevEnum));
	if (SUCCEEDED(hr))
	{
	    // Create an enumerator for the video capture category.
	    hr = pDevEnum->CreateClassEnumerator(
	        CLSID_VideoInputDeviceCategory,
	        &pEnum, 0);
	}
	else
	{
		DSCaptureException::FailWithException("CoCreateInstance Failed.", hr);
	}

	if (!pEnum)
	{
		return;
	}

	IMoniker *pMoniker = NULL;
	while (pEnum->Next(1, &pMoniker, NULL) == S_OK)
	{
	    IPropertyBag *pPropBag;
	    hr = pMoniker->BindToStorage(0, 0, IID_IPropertyBag,
	        (void**)(&pPropBag));
	    if (FAILED(hr))
	    {
	    	fprintf(stderr, "Failed to bind to device properties: %i\n", hr);
	    	fflush(stderr);
	        pMoniker->Release();
	        continue;  // Skip this one, maybe the next one will work.
	    }
		// Find the description or friendly name.
		// Since we've found cases where DS reports a VideoInputDevice
		// that isn't, we just roll with the punches when we find something that doesn't have all the
		// pieces parts of a device, like a device path or friendly name
		VARIANT friendlyName;	// TODO: free this when done
		VariantInit(&friendlyName);
		hr = pPropBag->Read(L"FriendlyName", &friendlyName, 0);
		if (hr == S_OK)
		{
			VARIANT devicePath;	// TODO: free this when done
			VariantInit(&devicePath);
			hr = pPropBag->Read(L"DevicePath", &devicePath, 0);
			if (hr == S_OK)
			{

				IGraphBuilder *pGraph = 0;
				ICaptureGraphBuilder2 *pBuild = 0;
				hr = InitCaptureGraphBuilder(&pGraph, &pBuild);
				if (SUCCEEDED(hr)) {

					IBaseFilter *pCap = NULL;
					hr = pMoniker->BindToObject(0, 0, IID_IBaseFilter, (void**)&pCap);

					if (SUCCEEDED(hr)) {
						hr = pGraph->AddFilter(pCap, L"Capture Filter");

						if (SUCCEEDED(hr)) {
							IAMCrossbar *xb;
							hr = pBuild->FindInterface(&LOOK_UPSTREAM_ONLY, NULL, pCap, IID_IAMCrossbar,
													 (void**)&xb);

							CaptureDeviceInfo info = CaptureDeviceInfo(devicePath.bstrVal, friendlyName.bstrVal);
							if (SUCCEEDED(hr)) {
							    IBaseFilter* pFilter;
							    hr = xb->QueryInterface(IID_IBaseFilter, (void**)&pFilter);
							    if ( SUCCEEDED(hr) ) {
							    	long noOutputs = -1;
							    	long noInputs = -1;
							    	hr = xb->get_PinCounts(&noOutputs, &noInputs);
							    	info.setNoOutputs(noOutputs);
							    	int realNoOutputs = 0;
							    	for (int i = 0; i < noOutputs; ++i) {
							            long outPinType;
							            long outRelated;
							    		xb->get_CrossbarPinInfo(FALSE, i, &outRelated, &outPinType);
							    		if (isVideo(outPinType)) {
											wchar_t *outputName = (wchar_t *) malloc(sizeof(wchar_t) * 11);
											swprintf(outputName, (size_t) 10, L"Output %i", i);
											info.setOutputName(realNoOutputs, outputName);
											int index[PhysConn_Video_Black + 1];
											memset(index, 0, (PhysConn_Video_Black + 1) * sizeof(int));

											int realNoInputs = 0;
											info.setNoInputs(realNoOutputs, noInputs);
											for (int j = 0; j < noInputs; j++) {
												long related;
												long pinType;
												xb->get_CrossbarPinInfo(TRUE, j, &related, &pinType);
												if (isVideo(pinType)) {
													if (xb->CanRoute(realNoOutputs, j) == S_OK) {
														const wchar_t *connector = GetPhysicalPinName(pinType);
														wchar_t *inputName = (wchar_t *) malloc(sizeof(wchar_t) * (wcslen(connector) + 4));
														index[pinType] += 1;
														if (index[pinType] <= 1) {
															swprintf(inputName, (size_t) (wcslen(connector) + 4), L"%s", connector);
														} else {
															swprintf(inputName, (size_t) (wcslen(connector) + 4), L"%s #%i", connector, index[pinType]);
														}
														info.setInputName(realNoOutputs, realNoInputs, inputName);
														realNoInputs++;
													}
												}
											}
											info.setNoInputs(realNoOutputs, realNoInputs);
											realNoOutputs++;
							    		}
								    }
							    	info.setNoOutputs(realNoOutputs);
							    }
					        }
					        result.push_back(info);	// TODO: deal with memory allocation issues
					    }
				    }
				}
			} else {
			    fprintf(stderr, "Failed to read path from device\n");
			    fflush(stderr);
			}
		} else {
			fprintf(stderr, "Failed to read name from device\n");
			fflush(stderr);
		}

	    pPropBag->Release();
	    pMoniker->Release();
	}

#if 0
// code to double check that our list is not corrupt from improper variant/string allocation above.
	list<CaptureDeviceInfo>::iterator i;

	for(i=result.begin(); i != result.end(); ++i)
	{
		// TODO: this does not handle unicode.
		const wchar_t *deviceID = (*i).getDeviceID();
		const wchar_t *description = (*i).getDescription();

		wprintf(L"getCaptureDeviceInfoList CHECK deviceID: [%s]\n", deviceID);
		wprintf(L"getCaptureDeviceInfoList CHECK description: [%s]\n", description);
		int noOutputs = (*i).getNoOutputs();
		wprintf(L"getCaptureDeviceInfoList CHECK noOutputs: [%i]\n", noOutputs);
		for (int j = 0; j < noOutputs; j++) {
			const wchar_t *outputName = (*i).getOutputName(j);
			wprintf(L"    CHECK Output: [%s]\n", outputName);
			int noInputs = (*i).getNoInputs(j);
			wprintf(L"    CHECK noInputs: [%i]\n", noInputs);
			for (int k = 0; k < noInputs; k++) {
				const wchar_t *inputName = (*i).getInputName(j, k);
				wprintf(L"        CHECK Input: [0x%.8x]\n", inputName);
				wprintf(L"        CHECK Input: [%s]\n", inputName);
			}
		}
		fflush(stdout);
	}
#endif
}

CaptureStream *DSCaptureSystem::openCaptureDeviceStream(const wchar_t *deviceId, int output, int input) /*throws CaptureException*/
{

//	wprintf(L"openCaptureDeviceStream: [%s]\n", deviceId);
// adapted from http://msdn.microsoft.com/library/default.asp?url=/library/en-us/directshow/htm/selectingacapturedevice.asp

	ICreateDevEnum *pDevEnum = NULL;
	IEnumMoniker *pEnum = NULL;

	// Create the System Device Enumerator.
	HRESULT hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL,
	    CLSCTX_INPROC_SERVER, IID_ICreateDevEnum,
	    reinterpret_cast<void**>(&pDevEnum));
	if (SUCCEEDED(hr))
	{
	    // Create an enumerator for the video capture category.
	    hr = pDevEnum->CreateClassEnumerator(
	        CLSID_VideoInputDeviceCategory,
	        &pEnum, 0);
	}
	else
	{
		DSCaptureException::FailWithException("CoCreateInstance Failed.", hr);
	}


	// if device not found, return NULL - should we throw exception?
	if (!pEnum)
	{
		return NULL;
	}

	IMoniker *pMoniker = NULL;
	while (pEnum->Next(1, &pMoniker, NULL) == S_OK)
	{
	    IPropertyBag *pPropBag;
	    hr = pMoniker->BindToStorage(0, 0, IID_IPropertyBag,
	        (void**)(&pPropBag));
	    if (FAILED(hr))
	    {
	        pMoniker->Release();
	        continue;  // Skip this one, maybe the next one will work.
	    }

		VARIANT devicePath;
		VariantInit(&devicePath);
		hr = pPropBag->Read(L"DevicePath", &devicePath, 0);
		DSCaptureException::CheckForFailure("Failed to retrieve device path for device.", hr);

//		wprintf(L"openCaptureDeviceStream: comparing    [%s]\n", deviceId);
//		wprintf(L"openCaptureDeviceStream: comparing to [%s]\n", devicePath.bstrVal);

		// see if the device path matches.
		if (wcscmp(devicePath.bstrVal, deviceId) == 0)
		{
			IGraphBuilder *pGraph = 0;
			ICaptureGraphBuilder2 *pBuild = 0;
			hr = InitCaptureGraphBuilder(&pGraph, &pBuild);
			DSCaptureException::CheckForFailure("InitCaptureGraphBuilder failed", hr);

			//When the user selects a device, create the capture filter for the device by calling IMoniker::BindToObject on the moniker. Then call AddFilter to add the filter to the filter graph:

			IBaseFilter *pCap = NULL;
			hr = pMoniker->BindToObject(0, 0, IID_IBaseFilter, (void**)&pCap);
			DSCaptureException::CheckForFailure("pMoniker->BindToObject failed", hr);
		    hr = pGraph->AddFilter(pCap, L"Capture Filter");
			DSCaptureException::CheckForFailure("pGraph->AddFilter failed", hr);

			// now add a sample grabber, see http://www.codeproject.com/audio/framegrabber.asp
			// Step 2: Create the Grabber filter and add it to the graph builder
			IBaseFilter* pGrabberBaseFilter = 0;
			ISampleGrabber *pSampleGrabber = 0;
			hr = ::CoCreateInstance(CLSID_SampleGrabber, NULL, CLSCTX_INPROC_SERVER,
			                        IID_IBaseFilter, (LPVOID *)&pGrabberBaseFilter);
			DSCaptureException::CheckForFailure("CoCreateInstance for CLSID_SampleGrabber failed", hr);
			pGrabberBaseFilter->QueryInterface(IID_ISampleGrabber, (void**)&pSampleGrabber);
			if (pSampleGrabber == NULL)
				DSCaptureException::FailWithException("pGrabberBaseFilter->QueryInterface(IID_ISampleGrabber Failed.", E_NOINTERFACE);

			hr = pGraph->AddFilter(pGrabberBaseFilter,L"Grabber");
			DSCaptureException::CheckForFailure("pGraphBuilder->AddFilter failed", hr);

			/*AM_MEDIA_TYPE mt;
			ZeroMemory(&mt,sizeof(AM_MEDIA_TYPE));
			mt.majortype = MEDIATYPE_Video;
			mt.subtype = MEDIASUBTYPE_RGB32;
			mt.formattype = FORMAT_VideoInfo;
			hr = pSampleGrabber->SetMediaType(&mt);
			DSCaptureException::CheckForFailure("pSampleGrabber->SetMediaType failed", hr); */

			// Don't Connect these now, because we have to set media type on
			// the camera before we connect these

			//hr = ConnectFilters(pGraph, pCap, pGrabberBaseFilter);
			//DSCaptureException::CheckForFailure("ConnectFilters", hr);

			IBaseFilter *pNullRenderer;
			hr = CoCreateInstance(CLSID_NullRenderer, NULL, CLSCTX_INPROC_SERVER,
								IID_IBaseFilter, (void **)&pNullRenderer);

   			DSCaptureException::CheckForFailure("Create NULL renderer", hr);

			pGraph->AddFilter(pNullRenderer, L"NullRenderer");
			DSCaptureException::CheckForFailure("pGraphBuilder->AddFilter failed", hr);

			// Don't Connect these now, because we have to set media type on
			// the camera before we connect these

			//	hr = ConnectFilters(pGraph, pGrabberBaseFilter,pNullRenderer);
			//	DSCaptureException::CheckForFailure("ConnectFilters NULL renderer Failed", hr);


			IAMCrossbar *xb;
		    hr = pBuild->FindInterface(&LOOK_UPSTREAM_ONLY, NULL, pCap, IID_IAMCrossbar,
									 (void**)&xb);
			if (SUCCEEDED(hr)) {
			  IBaseFilter* pFilter;
			  hr = xb->QueryInterface(IID_IBaseFilter, (void**)&pFilter);
			  if ( SUCCEEDED(hr) ) {

				  long noOutputs = -1;
				  long noInputs = -1;
				  long related;
				  long pinType;

				  hr = xb->get_PinCounts(&noOutputs, &noInputs);

				  int realInput = 0;
				  for (int i = 0; i < noInputs; i++) {
					  if (xb->CanRoute(output, i) != S_FALSE ) {
						  if (realInput == input) {
						      hr = xb->Route(output, i);
						  }
						  realInput++;
					  }
				  }

			  }
			} else {
				if (output != 0) {
					throw new DSCaptureException("Output index out of range", output);
				}
				if (input != 0) {
					throw new DSCaptureException("Input index out of range", input);
				}
			}

			IMediaControl *pMediaControl = NULL;    // Store pointer to interface
			hr = pGraph->QueryInterface(IID_IMediaControl, (void**)&pMediaControl);
			DSCaptureException::CheckForFailure("pGraph->QueryInterface failed for IID_IMediaControl", hr);

			IMediaEvent *pMediaEvent = NULL;    // Store pointer to interface
			hr = pGraph->QueryInterface(IID_IMediaEvent, (void**)&pMediaEvent);
			DSCaptureException::CheckForFailure("pGraph->QueryInterface failed for IID_IMediaEvent", hr);

		    pPropBag->Release();
		    pMoniker->Release();

			struct CaptureInfo info;
			info.m_pGraph = pGraph;
			info.m_pBuild =	pBuild;
			info.m_pSampleGrabber = pSampleGrabber;
			info.m_pMediaControl=pMediaControl;
			info.m_pMediaEvent=pMediaEvent;
			info.m_pBaseFilter=pCap;
			info.m_pRenderer = pNullRenderer;
			info.m_pGrabberBaseFilter = pGrabberBaseFilter;

			return new DSCaptureStream(&info);
		}

	    pPropBag->Release();
	    pMoniker->Release();

	}

	throw new DSCaptureException("device path not found");

}

// copied from http://msdn.microsoft.com/library/default.asp?url=/library/en-us/directshow/htm/aboutthecapturegraphbuilder.asp
HRESULT InitCaptureGraphBuilder(
  IGraphBuilder **ppGraph,  // Receives the pointer.
  ICaptureGraphBuilder2 **ppBuild  // Receives the pointer.
)
{
    if (!ppGraph || !ppBuild)
    {
        return E_POINTER;
    }
    IGraphBuilder *pGraph = NULL;
    ICaptureGraphBuilder2 *pBuild = NULL;

    // Create the Capture Graph Builder.
    HRESULT hr = CoCreateInstance(CLSID_CaptureGraphBuilder2, NULL,
        CLSCTX_INPROC_SERVER, IID_ICaptureGraphBuilder2, (void**)&pBuild );
    if (SUCCEEDED(hr))
    {
        // Create the Filter Graph Manager.
        hr = CoCreateInstance(CLSID_FilterGraph, 0, CLSCTX_INPROC_SERVER,
            IID_IGraphBuilder, (void**)&pGraph);
        if (SUCCEEDED(hr))
        {
            // Initialize the Capture Graph Builder.
            pBuild->SetFiltergraph(pGraph);

            // Return both interface pointers to the caller.
            *ppBuild = pBuild;
            *ppGraph = pGraph; // The caller must release both interfaces.

#if 1 // used for filter graph debugging only
    hr = AddGraphToRot(pGraph, &g_dwGraphRegister);
    if (FAILED(hr))
    {
            g_dwGraphRegister = 0;
    }
#endif
            return S_OK;
        }
        else
        {
            pBuild->Release();
        }
    }
    return hr; // Failed
}


// Adds a DirectShow filter graph to the Running Object Table,
// allowing GraphEdit to "spy" on a remote filter graph.
HRESULT AddGraphToRot(
        IUnknown *pUnkGraph,
        DWORD *pdwRegister
        )
{
    IMoniker *pMoniker;
    IRunningObjectTable *pROT;
    WCHAR wsz[128];
    HRESULT hr;

    if (FAILED(GetRunningObjectTable(0, &pROT)))
        return E_FAIL;

    wsprintfW(wsz, L"FilterGraph %08x pid %08x", (DWORD_PTR)pUnkGraph,
              GetCurrentProcessId());

    hr = CreateItemMoniker(L"!", wsz, &pMoniker);
    if (SUCCEEDED(hr))
        hr = pROT->Register(0, pUnkGraph, pMoniker, pdwRegister);
    return hr;
}



int ConnectFilters(IGraphBuilder* pGraph,IBaseFilter* pUpFilter,IBaseFilter* pDownFilter)
{
    if( !pUpFilter || !pDownFilter )
    {
        return E_INVALIDARG;
    }

    // All the need pin & pin enumerator pointers
    IEnumPins *pEnumUpFilterPins;
    IEnumPins *pEnumDownFilterPins;

    IPin *pUpFilterPin;
    IPin *pDownFilterPin;

    HRESULT hr = S_OK;

    // Get the pin enumerators for both the filter
    hr = pUpFilter->EnumPins(&pEnumUpFilterPins);
    if( FAILED( hr ) )
    {
    	fprintf(stderr, "Failed to get EnumPins up\n");
        fflush(stderr);
        return hr;
    }

    hr= pDownFilter->EnumPins(&pEnumDownFilterPins);
    if( FAILED( hr ) )
    {
    	fprintf(stderr, "Failed to get EnumPins down\n");
        fflush(stderr);
        return hr;
    }


    // Loop on every pin on the Upstream Filter
    BOOL bConnected = FALSE;
    PIN_DIRECTION pinDir;
    ULONG nFetched = 0;
    while(S_OK == pEnumUpFilterPins->Next(1, &pUpFilterPin, &nFetched) )
    {
        // Make sure that we have the output pin of the upstream filter
        hr = pUpFilterPin->QueryDirection( &pinDir );
        if( FAILED( hr ) || PINDIR_INPUT == pinDir )
        {
        	if (FAILED(hr)) {
        	    fprintf(stderr, "Failed to query up pin direction\n");
                fflush(stderr);
        	} else {
        		pUpFilterPin->Release();
        	}
            continue;
        }

        //
        // I have an output pin; loop on every pin on the Downstream Filter
        //

        while(S_OK == pEnumDownFilterPins->Next(1, &pDownFilterPin, &nFetched) )
        {
            hr = pDownFilterPin->QueryDirection( &pinDir );
            if( FAILED( hr ) || PINDIR_OUTPUT == pinDir )
            {
            	if (FAILED(hr)) {
                	fprintf(stderr, "Failed to query down pin direction\n");
                    fflush(stderr);
            	}
            	pDownFilterPin->Release();
                continue;
            }

            // Try to connect them and exit if u can else loop more until you can
             if(SUCCEEDED(pGraph->Connect(pUpFilterPin, pDownFilterPin))) {
                bConnected = TRUE;
                break;
             }
             pDownFilterPin->Release();
        }

        hr = pEnumDownFilterPins->Reset();
        if( FAILED( hr ) )
        {
        	fprintf(stderr, "Failed to reset\n");
            fflush(stderr);
            return hr;
        }

        pUpFilterPin->Release();
    }

    if( !bConnected )
    {
    	fprintf(stderr, "Failed to connect\n");
        fflush(stderr);
        return E_FAIL;
    }

    return S_OK;
}

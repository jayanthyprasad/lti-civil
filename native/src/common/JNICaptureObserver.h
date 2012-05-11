#include "CaptureObserver.h"
#include "converter.h"

class CaptureStream;


class JNICaptureObserver : public CaptureObserver
{
private:
    JavaVM *jvm; /* The virtual machine instance */
    jobject jCaptureStreamObj;	// must be a global reference
    jobject jCaptureObserverObj;	// must be a global reference
    jclass jCaptureObserverClass;
    jclass jByteImageClass;
	jclass jShortImageClass;
	jclass jIntImageClass;
    jclass jCaptureExceptionClass;
    jclass jNativeVideoFormatClass;
    jmethodID jCaptureObserverOnNewImageMethodID;
    jmethodID jByteImageConstructor;
    jmethodID jShortImageConstructor;
    jmethodID jIntImageConstructor;
    jmethodID jNativeVideoFormatConstructor;
    jmethodID jImageGetByteObjectMethodID;
    jmethodID jImageGetShortObjectMethodID;
    jmethodID jImageGetIntObjectMethodID;
    jmethodID jImageSetOffsetMethodID;

    jarray array;
    int arrayLength;
    Converter *converter;

    jobject jImage;
    jobject jVideoFormat;
    bool reentrant;
    int nextImageBuffer;
    int offsetDivider;
    int noImageBuffers;
public:
    JNICaptureObserver(JNIEnv *_pEnv, jobject _jCaptureStreamObj, jobject _jCaptureObserverObj, int numBuffersHint);
    virtual ~JNICaptureObserver();
    virtual void onNewImage(CaptureStream *sender, Image* image);
    virtual void onError(CaptureStream *sender, CaptureException *e);
};

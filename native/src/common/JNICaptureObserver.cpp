#include <jni.h>
#include "VideoFormat.h"
#include "JNICaptureObserver.h"
#include "CaptureStream.h"
#include "CaptureException.h"
#include "Image.h"
#include <stdio.h>
#include <string.h>
#include "rgb-converter.h"
#include "yuv_convert.h"


static jlong ptr2jlong(void *ptr)
{
    jlong jl = 0;
    if (sizeof(void *) > sizeof(jlong))
    {	fprintf(stderr, "sizeof(void *) > sizeof(jlong)\n");
        return 0;
        //(* (int *) 0) = 0;	// crash.
    }

    memcpy(&jl, &ptr, sizeof(void *));
    return jl;
}


JNICaptureObserver::JNICaptureObserver(JNIEnv *pEnv, jobject _jCaptureStreamObj, jobject _jCaptureObserverObj, int numBuffersHint)
{	jint res = pEnv->GetJavaVM(&jvm);
    reentrant = false;
    if (res < 0) {
        fprintf(stderr, "GetJavaVM failed\n");
        return;
     }
    jCaptureStreamObj = _jCaptureStreamObj;
    jCaptureObserverObj = _jCaptureObserverObj;

    /* Find classes used in the on... methods. The classes can't be looked up locally since these methods
     * are called from a system thread and FindClass therefore uses the system class loader to find
     * these classes. This fails if civil is used with Java Web Start, which uses its own class loader.
     * Looking the classes up in the constructor works because the constructor is called from a
     * Java -> Native call and the class loader of the calling Java class is used.
     * We need to create global refs since the class objects are used from a different thread.
     */
    jCaptureObserverClass = (jclass) pEnv->NewGlobalRef(pEnv->FindClass("com/lti/civil/CaptureObserver"));
    jByteImageClass = (jclass) pEnv->NewGlobalRef(pEnv->FindClass("com/lti/civil/impl/jni/NativeByteArrayImage"));
    jShortImageClass = (jclass) pEnv->NewGlobalRef(pEnv->FindClass("com/lti/civil/impl/jni/NativeShortArrayImage"));
    jIntImageClass = (jclass) pEnv->NewGlobalRef(pEnv->FindClass("com/lti/civil/impl/jni/NativeIntArrayImage"));
    jCaptureExceptionClass = (jclass) pEnv->NewGlobalRef(pEnv->FindClass("com/lti/civil/CaptureException"));
    jNativeVideoFormatClass = (jclass) pEnv->NewGlobalRef(pEnv->FindClass("com/lti/civil/impl/jni/NativeVideoFormat"));

    jCaptureObserverOnNewImageMethodID = pEnv->GetMethodID(jCaptureObserverClass, "onNewImage", "(Lcom/lti/civil/CaptureStream;Lcom/lti/civil/Image;)V");
    jByteImageConstructor = pEnv->GetMethodID(jByteImageClass, "<init>", "(ILcom/lti/civil/VideoFormat;)V");
    jShortImageConstructor = pEnv->GetMethodID(jShortImageClass, "<init>", "(ILcom/lti/civil/VideoFormat;)V");
    jIntImageConstructor = pEnv->GetMethodID(jIntImageClass, "<init>", "(ILcom/lti/civil/VideoFormat;)V");
    jNativeVideoFormatConstructor = pEnv->GetMethodID(jNativeVideoFormatClass, "<init>", "(JIIIFI)V");
    jImageGetByteObjectMethodID = pEnv->GetMethodID(jByteImageClass, "getObject", "()Ljava/lang/Object;");
    jImageGetShortObjectMethodID = pEnv->GetMethodID(jShortImageClass, "getObject", "()Ljava/lang/Object;");
    jImageGetIntObjectMethodID = pEnv->GetMethodID(jIntImageClass, "getObject", "()Ljava/lang/Object;");

    array = 0;
    arrayLength = 0;
    converter = NULL;
    nextImageBuffer = 0;
    noImageBuffers = numBuffersHint;
    jImage = 0;
    jVideoFormat = 0;
}

JNICaptureObserver::~JNICaptureObserver()
{
    // get a reference to the current JNIEnv
    JNIEnv *pEnv;
    jint res;
    bool attached = false;
    res = jvm->GetEnv((void**)&pEnv, JNI_VERSION_1_2); // TODO: is 1_2 ok, or need to support earlier versions?
    if (res == JNI_EDETACHED) {
        attached = true;
        res = jvm->AttachCurrentThread((void**)&pEnv, NULL);
        if (res < 0) {
            fprintf(stderr, "Attach failed\n");
            return;
        }
    } else if (res < 0) {
        fprintf(stderr, "GetEnv failed\n");
        return;
    }

    // release global class references
    pEnv->DeleteGlobalRef(jCaptureObserverClass);
    pEnv->DeleteGlobalRef(jByteImageClass);
    pEnv->DeleteGlobalRef(jShortImageClass);
    pEnv->DeleteGlobalRef(jIntImageClass);
    pEnv->DeleteGlobalRef(jCaptureExceptionClass);
    pEnv->DeleteGlobalRef(jNativeVideoFormatClass);

    if (jImage != 0) {
        pEnv->DeleteGlobalRef(array);
        pEnv->DeleteGlobalRef(jImage);
    }
    if (jVideoFormat != 0) {
        pEnv->DeleteGlobalRef(jVideoFormat);
    }

    if (attached) {
        jvm->DetachCurrentThread();
    }
}



void JNICaptureObserver::onNewImage(CaptureStream *sender, Image *image)
{
    if (reentrant)
    {
        fprintf(stderr, "JNICaptureObserver::onNewImage: reentrant==true\n");
        fflush(stderr);

    }
    reentrant = true;

    JNIEnv *pEnv;
    jint res;
    res = jvm->AttachCurrentThread((void**)&pEnv, NULL);
    if (res < 0) {
        fprintf(stderr, "Attach failed\n");
        reentrant = false;
        return;
    }

    if (image == 0)
    {
        fprintf(stderr, "JNICaptureObserver::onNewImage: image == 0, skipping.\n");
        fflush(stderr);
        jvm->DetachCurrentThread();
        reentrant = false;
        return;
    }
    else if (image->getLength() <= 0)
    {
        fprintf(stderr, "JNICaptureObserver::onNewImage: image->getLength() <= 0: %d, skipping.\n", image->getLength());
        fflush(stderr);
        jvm->DetachCurrentThread();
        reentrant = false;
        return;
    }
    else
    {
        unsigned char *imageBytes = image->getBytes();
        VideoFormat &format = image->getFormat();
        int dataLength = image->getLength();
        if (format.isConvertToI420()) {
        	int ySize = format.width * format.height;
			int uvSize = (format.width / 2) * (format.height / 2);
			dataLength = ySize + (uvSize * 2);
        }

        if ((arrayLength == 0) || (dataLength > arrayLength)) {
            if (jImage != 0) {
                pEnv->DeleteGlobalRef(array);
                pEnv->DeleteGlobalRef(jImage);
            }
            arrayLength = dataLength;

        	int formatType = format.formatType;
        	int dataType = format.dataType;

            if (jVideoFormat == 0) {

            	if (format.isConvertToI420()) {
            		formatType = I420;
            		dataType = DATA_TYPE_BYTE_ARRAY;
            		if (format.formatType == RGB24) {
            		    converter = new RGB_Converter_420(24, (u_int8_t *) NULL, 0);
            		} else if (format.formatType == RGB32) {
            			converter = new RGB_Converter_420(32, (u_int8_t *) NULL, 0);
            		} else if (format.formatType == YUYV || format.formatType == YUY2) {
            			converter = new YUV_Converter(PACKED_YUYV422, PLANAR_YUYV420);
            		} else if (format.formatType == UYVY) {
            			converter = new YUV_Converter(PACKED_UYVY422, PLANAR_YUYV420);
            		}
            	}

                jVideoFormat = pEnv->NewGlobalRef(pEnv->NewObject(
                                        jNativeVideoFormatClass,
                                        jNativeVideoFormatConstructor,
                                        ptr2jlong(format.handle),
                                        formatType,
                                        format.width,
                                        format.height,
                                        (jfloat) format.fps,
                                        dataType));

            }

            if (dataType == DATA_TYPE_BYTE_ARRAY) {
				jImage = pEnv->NewGlobalRef(pEnv->NewObject(jByteImageClass,
					jByteImageConstructor, arrayLength * noImageBuffers, jVideoFormat));
				array = (jarray) pEnv->NewGlobalRef(
					pEnv->CallObjectMethod(jImage, jImageGetByteObjectMethodID));
				offsetDivider = 1;
				jImageSetOffsetMethodID = pEnv->GetMethodID(jByteImageClass,
						"setOffset", "(I)V");
			} else if (dataType == DATA_TYPE_SHORT_ARRAY) {
				jImage = pEnv->NewGlobalRef(pEnv->NewObject(jShortImageClass,
					jShortImageConstructor, (arrayLength * noImageBuffers) / 2, jVideoFormat));
				array = (jarray) pEnv->NewGlobalRef(
					pEnv->CallObjectMethod(jImage, jImageGetShortObjectMethodID));
				offsetDivider = 2;
				jImageSetOffsetMethodID = pEnv->GetMethodID(jShortImageClass,
						"setOffset", "(I)V");
			} else if (dataType == DATA_TYPE_INT_ARRAY) {
				jImage = pEnv->NewGlobalRef(pEnv->NewObject(jIntImageClass,
					jIntImageConstructor, (arrayLength * noImageBuffers) / 4, jVideoFormat));
				array = (jarray) pEnv->NewGlobalRef(
					pEnv->CallObjectMethod(jImage, jImageGetIntObjectMethodID));
				offsetDivider = 4;
				jImageSetOffsetMethodID = pEnv->GetMethodID(jIntImageClass,
						"setOffset", "(I)V");
			}
        }

        int offset = arrayLength * nextImageBuffer;
        pEnv->CallVoidMethod(jImage, jImageSetOffsetMethodID, offset / offsetDivider);
        unsigned char *javaBytes = (unsigned char *)
            pEnv->GetPrimitiveArrayCritical(array, 0);
        unsigned char *bytes = javaBytes + offset;
        nextImageBuffer = (nextImageBuffer + 1) % noImageBuffers;
        if (format.isConvertToI420()) {
        	converter->convert(imageBytes, format.width, format.height, bytes,
        	                format.width, format.height, format.flipped);
        } else if (!format.flipped) {
			memcpy(bytes, imageBytes, arrayLength);
		} else {
			fflush(stderr);
			for (long row = 0; row < format.getHeight(); row++) {
				memcpy((bytes + (row * format.getWidth() * format.getBytesPerPixel())),
					imageBytes + (format.getHeight() - 1 - row) * format.getWidth() * format.getBytesPerPixel(),
					format.getWidth() * format.getBytesPerPixel());
			}
		}
        pEnv->ReleasePrimitiveArrayCritical(array, javaBytes, 0);
    }

    pEnv->CallVoidMethod(jCaptureObserverObj, jCaptureObserverOnNewImageMethodID, jCaptureStreamObj, jImage);

    // just in case the observer throws an exception, we will print it and ignore.  This keeps the JVM from crashing
    // TODO: we should stop the capture.
    jthrowable exc = pEnv->ExceptionOccurred();
    if (exc)
    {
        pEnv->ExceptionDescribe();
        pEnv->ExceptionClear();
    }

    jvm->DetachCurrentThread();
    reentrant = false;
}

void JNICaptureObserver::onError(CaptureStream *sender, CaptureException *e)
{
    JNIEnv *pEnv;
    jint res;
    res = jvm->AttachCurrentThread((void**)&pEnv, NULL);
    if (res < 0) {
        fprintf(stderr, "Attach failed\n");
        return;
    }

    jmethodID jCaptureObserverOnErrorMethodID = pEnv->GetMethodID(jCaptureObserverClass, "onError", "(Lcom/lti/civil/CaptureStream;Lcom/lti/civil/CaptureException;)V");

    jmethodID jCaptureExceptionConstructor = pEnv->GetMethodID(jCaptureExceptionClass, "<init>", "(Ljava/lang/String;I)V");
    jstring jMsgString = e->msg ? pEnv->NewStringUTF(e->msg) : 0;
    jobject jCaptureException = pEnv->NewObject(jCaptureExceptionClass, jCaptureExceptionConstructor, jMsgString, e->errorCode);

    pEnv->CallVoidMethod(jCaptureObserverObj, jCaptureObserverOnErrorMethodID, jCaptureStreamObj, jCaptureException);

    // just in case the observer throws an exception, we will print it and ignore.  This keeps the JVM from crashing
    // TODO: we should stop the capture.
    jthrowable exc = pEnv->ExceptionOccurred();
    if (exc)
    {
        pEnv->ExceptionDescribe();
        pEnv->ExceptionClear();
    }

    jvm->DetachCurrentThread();

}


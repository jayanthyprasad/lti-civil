// formats:
#define RGB24	1
#define RGB32	2
#define RGB565    3
#define RGB555    4
#define ARGB1555  5
#define ARGB32    6
#define UYVY      7
#define YUYV      8
#define YVYU      9
#define YUY2     10
#define YV12     11
#define I420     12
#define IYUV     13
#define NV12     14
#define Y411     15
#define YVU9     16
#define MJPG     17
#define DVSL     18
#define DVSD     19
#define DVHD     20

#define DATA_TYPE_BYTE_ARRAY 1
#define DATA_TYPE_SHORT_ARRAY 2
#define DATA_TYPE_INT_ARRAY 4

#define FPS_UNKNOWN -1.f


class VideoFormat
{
public:
	VideoFormat(void* _handle, int _formatType, int _width, int _height, float _fps)
	{
		handle = _handle;
		formatType = _formatType;
		width = _width;
		height= _height;
		fps = _fps;
		dataType = DATA_TYPE_BYTE_ARRAY;
		flipped = false;
		convertToI420 = false;

		// Set RGB bytes per pixel as they can be flipped
		switch (_formatType) {
		case RGB24:
			bytesPerPixel = 3;
			break;
		case RGB32:
		case ARGB32:
			bytesPerPixel = 4;
			break;
		case RGB565:
		case RGB555:
		case ARGB1555:
			bytesPerPixel = 2;
			break;
		}
	}
	VideoFormat()
	{
		handle = 0;
		formatType = -1;
		width = -1;
		height= -1;
		fps = FPS_UNKNOWN;
		dataType = DATA_TYPE_BYTE_ARRAY;
		flipped = false;
		bytesPerPixel = -1;
	}

	void *handle;	// arbitrary handle for storing implementation-specific data
	int formatType;
	int width;
	int height;
	float fps;
	int dataType;
	bool flipped;
	int bytesPerPixel;
	bool convertToI420;

	int getFormatType() {return formatType;}
	int getWidth() {return width;}
	int getHeight() {return height;}
	float getFPS() {return fps;}
	int getBytesPerPixel() {return bytesPerPixel;}
	void setBytesPerPixel(int bpp) {bytesPerPixel = bpp;}
	int getDataType() {return dataType;}
	void setDataType(int type) {dataType = type;}
	bool isFlipped() {return flipped;}
	void setFlipped(bool isFlipped) {flipped = isFlipped;}
	bool isConvertToI420() {return convertToI420;}
	void setConvertToI420(bool convert) {convertToI420 = convert;}
};

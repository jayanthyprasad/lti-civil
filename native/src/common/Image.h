#include <malloc.h>

class Image
{

private:
	VideoFormat format;
	unsigned char *bytes;
	int length;	// length of bytes
	bool copy;

public:
	
	Image(VideoFormat &_format, unsigned char *_bytes, int _length)
	{	format = _format;
		bytes = _bytes;
		length = _length;
		copy = false;
	}
	Image(VideoFormat &_format, unsigned char *_bytes, int _length, bool _copy)
	{	format = _format;
		bytes = _bytes;
		length = _length;
		copy = _copy;
	}
	~Image() {
		if (copy && bytes) {
			free(bytes);
		}
	}
	VideoFormat &getFormat() {return format;}
	unsigned char *getBytes() {return bytes;}
	int getLength() {return length;}
};

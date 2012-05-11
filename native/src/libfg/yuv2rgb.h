#ifdef __cplusplus
extern "C" {
#endif
inline void yuv2rgb(unsigned char y, unsigned char u, unsigned char v, unsigned char *pr, unsigned char *pg, unsigned char *pb);
void yuv2rgb_buf(unsigned char *src, int width, int height, unsigned char *dst);
/**
 * Converts an unpacked YUV buffer (3 bytes) into RGB24 buffer reusing the same buffer.
 * Input buffer contains YUV values (1 byte for Y, 1 for U, and 1 for V) in a raw
 * format. Each pixel has 3 bytes.
 */
void unpackedyuv2rgb_rwbuf(unsigned char *src, int width, int height);

/**
 * Converts a YUYV buffer into RGB24 buffer. Resulting buffer must be 
 * freed after used.
 */
unsigned char* yuyvToRGB(unsigned char * yuyv, int width, int height);

#ifdef __cplusplus
}
#endif


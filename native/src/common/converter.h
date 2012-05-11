#ifndef converter_h
#define converter_h

typedef unsigned char u_char;
typedef unsigned int u_int;
typedef unsigned short u_short;
typedef unsigned char u_int8_t;
typedef unsigned int u_int32_t;
typedef unsigned short u_int16_t;

class Converter {
public:
    virtual void convert(u_int8_t* in, int inw, int inh, u_int8_t* frm, int outw, int outh, int invert)= 0;
    virtual ~Converter(){}; //SV-XXX: This solves the "missing" virtual destructor warning from gcc4
};
#endif

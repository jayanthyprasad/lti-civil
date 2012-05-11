################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../libfg/camview.c \
../libfg/capture.c \
../libfg/fgmodule.c \
../libfg/frame.c \
../libfg/test_capture.c \
../libfg/yuv2rgb.c 

OBJS += \
./libfg/camview.o \
./libfg/capture.o \
./libfg/fgmodule.o \
./libfg/frame.o \
./libfg/test_capture.o \
./libfg/yuv2rgb.o 

C_DEPS += \
./libfg/camview.d \
./libfg/capture.d \
./libfg/fgmodule.d \
./libfg/frame.d \
./libfg/test_capture.d \
./libfg/yuv2rgb.d 


# Each subdirectory must supply rules for building sources it contributes
libfg/%.o: ../libfg/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



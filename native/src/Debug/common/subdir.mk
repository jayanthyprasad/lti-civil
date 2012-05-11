################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../common/JNICaptureObserver.cpp \
../common/civil.cpp \
../common/rgb-converter.cpp \
../common/yuv_convert.cpp 

OBJS += \
./common/JNICaptureObserver.o \
./common/civil.o \
./common/rgb-converter.o \
./common/yuv_convert.o 

CPP_DEPS += \
./common/JNICaptureObserver.d \
./common/civil.d \
./common/rgb-converter.d \
./common/yuv_convert.d 


# Each subdirectory must supply rules for building sources it contributes
common/%.o: ../common/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



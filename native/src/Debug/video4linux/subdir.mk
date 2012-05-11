################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../video4linux/PThreadStreamThrottle.cpp \
../video4linux/V4LCaptureStream.cpp \
../video4linux/V4LCaptureSystem.cpp \
../video4linux/V4LCaptureSystemFactory.cpp 

OBJS += \
./video4linux/PThreadStreamThrottle.o \
./video4linux/V4LCaptureStream.o \
./video4linux/V4LCaptureSystem.o \
./video4linux/V4LCaptureSystemFactory.o 

CPP_DEPS += \
./video4linux/PThreadStreamThrottle.d \
./video4linux/V4LCaptureStream.d \
./video4linux/V4LCaptureSystem.d \
./video4linux/V4LCaptureSystemFactory.d 


# Each subdirectory must supply rules for building sources it contributes
video4linux/%.o: ../video4linux/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../video4linux2/V4L2CaptureStream.cpp \
../video4linux2/V4L2CaptureSystem.cpp \
../video4linux2/V4L2CaptureSystemFactory.cpp 

OBJS += \
./video4linux2/V4L2CaptureStream.o \
./video4linux2/V4L2CaptureSystem.o \
./video4linux2/V4L2CaptureSystemFactory.o 

CPP_DEPS += \
./video4linux2/V4L2CaptureStream.d \
./video4linux2/V4L2CaptureSystem.d \
./video4linux2/V4L2CaptureSystemFactory.d 


# Each subdirectory must supply rules for building sources it contributes
video4linux2/%.o: ../video4linux2/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



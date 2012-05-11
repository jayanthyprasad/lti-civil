################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../directshow/DSCaptureStream.cpp \
../directshow/DSCaptureSystem.cpp \
../directshow/DSCaptureSystemFactory.cpp \
../directshow/stdafx.cpp 

OBJS += \
./directshow/DSCaptureStream.o \
./directshow/DSCaptureSystem.o \
./directshow/DSCaptureSystemFactory.o \
./directshow/stdafx.o 

CPP_DEPS += \
./directshow/DSCaptureStream.d \
./directshow/DSCaptureSystem.d \
./directshow/DSCaptureSystemFactory.d \
./directshow/stdafx.d 


# Each subdirectory must supply rules for building sources it contributes
directshow/%.o: ../directshow/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



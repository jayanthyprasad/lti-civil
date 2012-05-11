################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../test/CaptureStreamImpl.cpp \
../test/CaptureSystemFactoryImpl.cpp \
../test/CaptureSystemImpl.cpp 

OBJS += \
./test/CaptureStreamImpl.o \
./test/CaptureSystemFactoryImpl.o \
./test/CaptureSystemImpl.o 

CPP_DEPS += \
./test/CaptureStreamImpl.d \
./test/CaptureSystemFactoryImpl.d \
./test/CaptureSystemImpl.d 


# Each subdirectory must supply rules for building sources it contributes
test/%.o: ../test/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



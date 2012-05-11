################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CXX_SRCS += \
../camerav4l2/cam-player/cam-gui.cxx \
../camerav4l2/cam-player/capturecam.cxx 

OBJS += \
./camerav4l2/cam-player/cam-gui.o \
./camerav4l2/cam-player/capturecam.o 

CXX_DEPS += \
./camerav4l2/cam-player/cam-gui.d \
./camerav4l2/cam-player/capturecam.d 


# Each subdirectory must supply rules for building sources it contributes
camerav4l2/cam-player/%.o: ../camerav4l2/cam-player/%.cxx
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



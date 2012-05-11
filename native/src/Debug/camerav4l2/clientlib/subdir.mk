################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../camerav4l2/clientlib/playerv4l2.cpp 

C_SRCS += \
../camerav4l2/clientlib/playerv4l2c.c \
../camerav4l2/clientlib/plv4l2tools.c 

OBJS += \
./camerav4l2/clientlib/playerv4l2.o \
./camerav4l2/clientlib/playerv4l2c.o \
./camerav4l2/clientlib/plv4l2tools.o 

C_DEPS += \
./camerav4l2/clientlib/playerv4l2c.d \
./camerav4l2/clientlib/plv4l2tools.d 

CPP_DEPS += \
./camerav4l2/clientlib/playerv4l2.d 


# Each subdirectory must supply rules for building sources it contributes
camerav4l2/clientlib/%.o: ../camerav4l2/clientlib/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

camerav4l2/clientlib/%.o: ../camerav4l2/clientlib/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



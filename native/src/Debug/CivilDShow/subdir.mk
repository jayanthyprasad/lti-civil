################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../CivilDShow/CivilDShow.cpp \
../CivilDShow/directshow.cpp \
../CivilDShow/stdafx.cpp 

OBJS += \
./CivilDShow/CivilDShow.o \
./CivilDShow/directshow.o \
./CivilDShow/stdafx.o 

CPP_DEPS += \
./CivilDShow/CivilDShow.d \
./CivilDShow/directshow.d \
./CivilDShow/stdafx.d 


# Each subdirectory must supply rules for building sources it contributes
CivilDShow/%.o: ../CivilDShow/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



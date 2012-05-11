#include <stdlib.h>

class CaptureDeviceInfo
{
	// TODO: this does not handle unicode.
public:
	CaptureDeviceInfo(const wchar_t *_deviceID, const wchar_t *_description)
	{	deviceID = _deviceID;
		description = _description;
		inputName = 0;
		outputName = 0;
		noInputs = 0;
		noOutputs = 0;
	}
	~CaptureDeviceInfo() {

	}
	const wchar_t *getDeviceID() {return deviceID;}
	const wchar_t *getDescription() {return description;}
	int getNoOutputs() {return noOutputs;}
	int getNoInputs(int output) {if (output < noOutputs) return noInputs[output]; return 0;}
	const wchar_t *getOutputName(int output) {if (output < noOutputs) return outputName[output]; return NULL;}
	const wchar_t *getInputName(int output, int input) {if ((output < noOutputs) && (input < noInputs[output])) return inputName[output][input]; return NULL;}

	void setNoOutputs(int noOutputs) {
		int oldNoOutputs = this->noOutputs;
		this->noOutputs = noOutputs;
		noInputs = (int *) realloc(noInputs, sizeof(int) * noOutputs);
		outputName = (const wchar_t **) realloc(outputName, sizeof(const wchar_t *) * noOutputs);
		inputName = (const wchar_t ***) realloc(inputName, sizeof(const wchar_t **) * noOutputs);
		for (int i = oldNoOutputs; i < noOutputs; i++) {
			inputName[i] = 0;
		}
	}

	void setNoInputs(int output, int noInputs) {
		if (output < noOutputs) {
		    this->noInputs[output] = noInputs;
		    inputName[output] = (const wchar_t **) realloc(inputName[output], sizeof(const wchar_t *) * noInputs);
		}
	}

	void setOutputName(int output, const wchar_t *outputName) {
		if (output < noOutputs) {
			this->outputName[output] = outputName;
		}
	}

	void setInputName(int output, int input, const wchar_t *inputName) {
		if ((output < noOutputs) && (input < noInputs[output])) {
			this->inputName[output][input] = inputName;
		}
	}

private:
	const wchar_t *deviceID;
	const wchar_t *description;
	int noOutputs;
	int *noInputs;
	const wchar_t **outputName;
	const wchar_t ***inputName;

};

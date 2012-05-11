// CivilDShow.h : main header file for the CivilDShow DLL
//

#pragma once

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols


// CCivilDShowApp
// See CivilDShow.cpp for the implementation of this class
//

class CCivilDShowApp : public CWinApp
{
public:
	CCivilDShowApp();

// Overrides
public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};

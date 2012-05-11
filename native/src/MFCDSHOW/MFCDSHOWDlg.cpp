// MFCDSHOWDlg.cpp : implementation file
//

#include "stdafx.h"
#include "MFCDSHOW.h"
#include "MFCDSHOWDlg.h"


#include <list>
#include "VideoFormat.h"
#include "Image.h"
#include "captureexception.h"
#include "capturesystem.h"
//#include "CaptureSystemFactory.h"

#include "..\directshow\DSCaptureSystemFactory.h"
#include "capturestream.h"
#include "captureobserver.h"
#include ".\mfcdshowdlg.h"


#ifdef _DEBUG
#define new DEBUG_NEW
#endif

int numFramesAcq=0;
class FrameObserver : public CaptureObserver
{
public:
	virtual void onNewImage(CaptureStream *sender, Image *image)
	{
		printf("Resolution: %dx%d\n", image->getFormat().getWidth(), image->getFormat().getHeight());
		numFramesAcq++;
	}
	virtual void onError(CaptureStream *sender, CaptureException *e)
	{
		OutputDebugString("OnError");
	}
};

CaptureSystem *pCapture = NULL;
list<CaptureDeviceInfo> infoList;
FrameObserver myFrameObserver;
CaptureStream *pStream = NULL;    
// CAboutDlg dialog used for App About

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// Dialog Data
	enum { IDD = IDD_ABOUTBOX };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

// Implementation
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
END_MESSAGE_MAP()


// CMFCDSHOWDlg dialog



CMFCDSHOWDlg::CMFCDSHOWDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CMFCDSHOWDlg::IDD, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

CMFCDSHOWDlg::~CMFCDSHOWDlg()
{
	//if (pStream)
	//	delete pStream;
	if (pCapture)
		delete pCapture;

}
void CMFCDSHOWDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);

	DDX_Text(pDX,IDC_NUMFRAMES_ACQ,numFramesAcq);
}

BEGIN_MESSAGE_MAP(CMFCDSHOWDlg, CDialog)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	//}}AFX_MSG_MAP
	ON_BN_CLICKED(IDC_START, OnBnClickedStart)
	ON_BN_CLICKED(IDC_STOP, OnBnClickedStop)
	ON_WM_TIMER()
END_MESSAGE_MAP()


// CMFCDSHOWDlg message handlers

BOOL CMFCDSHOWDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Add "About..." menu item to system menu.

	// IDM_ABOUTBOX must be in the system command range.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		CString strAboutMenu;
		strAboutMenu.LoadString(IDS_ABOUTBOX);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon

	StartCapture();	

	UINT_PTR hTimer = SetTimer(1,1000,0);//,OnTimer);
	return TRUE;  // return TRUE  unless you set the focus to a control
}

void CMFCDSHOWDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialog::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CMFCDSHOWDlg::OnPaint() 
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this function to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CMFCDSHOWDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}


extern CaptureSystemFactory *gCaptureSystemFactory;

void CMFCDSHOWDlg::StartCapture()
{
	pCapture = gCaptureSystemFactory->createCaptureSystem();
	try 
	{
		pCapture->init();
		pCapture->getCaptureDeviceInfoList(infoList);
		if (!infoList.empty())
		{
			CaptureDeviceInfo &info = infoList.front();
			pStream = pCapture->openCaptureDeviceStream(info.getDeviceID());
			if (pStream)
			{
				pStream->setObserver(&myFrameObserver);
			}
		}
		//pCapture->dispose();
	}
	catch (CaptureException *except)
	{
		MessageBox(except->msg,"Capture Exception Occured",MB_OK);
	}
}

void CMFCDSHOWDlg::OnBnClickedStart()
{
	list<VideoFormat> formatList;
	if (!pStream)
	{
		StartCapture();
	}
	if (pStream)
	{
		pStream->enumVideoFormats(formatList);

		std::list<VideoFormat>::iterator iter = formatList.begin();
		iter++;
		VideoFormat myFormat = *iter;
		pStream->setVideoFormat(myFormat);
		pStream->start();
	}
}

void CMFCDSHOWDlg::OnBnClickedStop()
{
	if (pStream)
	{
		pStream->stop();
		pStream->dispose();
		delete pStream;
		pStream= NULL;
	}
}

void CMFCDSHOWDlg::OnTimer(UINT event){
 //  HWND ,      // handle of CWnd that called SetTimer
 //  UINT ,      // WM_TIMER
 //  UINT ,   // timer identification
 //  DWORD     // system time
//)
//{
	UpdateData(false);
}

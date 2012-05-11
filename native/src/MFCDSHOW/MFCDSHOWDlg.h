// MFCDSHOWDlg.h : header file
//

#pragma once


// CMFCDSHOWDlg dialog
class CMFCDSHOWDlg : public CDialog
{
// Construction
public:
	CMFCDSHOWDlg(CWnd* pParent = NULL);	// standard constructor
	~CMFCDSHOWDlg();

// Dialog Data
	enum { IDD = IDD_MFCDSHOW_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support


// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()

private:
	void StartCapture();
public:
	afx_msg void OnBnClickedStart();
	afx_msg void OnBnClickedStop();
	afx_msg void OnTimer(UINT );
#if 0		
		(   HWND ,      // handle of CWnd that called SetTimer
   UINT ,      // WM_TIMER
   UINT ,   // timer identification
   DWORD     // system time
);
#endif
};

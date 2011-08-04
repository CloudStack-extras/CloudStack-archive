//
// VMOpsServiceImpl.h
// Cloud.com instance manager implementation
//
// Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
//
// This software is licensed under the GNU General Public License v3 or later.
//
// It is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or any later version.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
#ifndef __VMOpsServiceImpl_H__
#define __VMOpsServiceImpl_H__

#include "VMOpsError.h"
#include "ThreadUtil.h"

#include <stdio.h>
#include <IPHlpApi.h>
#include <list>

namespace VMOps {

/////////////////////////////////////////////////////////////////////////////
// class diagram
//
class CVMOpsServiceProvider;
class CThread;
		class CVMOpsStartupWatcher;

class CLogger;

/////////////////////////////////////////////////////////////////////////////
// CVMOpsServiceProvider
//
class CVMOpsServiceProvider
{
public :
	CVMOpsServiceProvider();
	~CVMOpsServiceProvider();

public :
	HERROR SetPassword(LPCTSTR lpszUserName, LPCTSTR lpszPassword);
	HERROR GetNextPasswordProvider(LPSTR lpszBuf, LPDWORD pdwLength);
	HERROR GetDefaultGateway(LPSTR lpszBuf, LPDWORD pdwLength);
	HERROR SimpleHttpGet(LPCTSTR lpszUrl, LPCTSTR lpszHeaders, 
		LPVOID pOutputBuffer, DWORD dwBytesToRead, DWORD* pdwBytesRead);

	HERROR Start();
	HERROR Stop();

protected :
	CVMOpsStartupWatcher* m_pWatcher;

	std::list<IP_ADDRESS_STRING> m_lstProviders;
};

/////////////////////////////////////////////////////////////////////////////
// CVMOpsStartupWatcher
//
class CVMOpsStartupWatcher : public CThread
{
public :
	CVMOpsStartupWatcher(CVMOpsServiceProvider* pProvider);
	virtual ~CVMOpsStartupWatcher();

public :
	CVMOpsServiceProvider* GetProvider() { return m_pProvider; }

protected :
	virtual DWORD ThreadRun();

	BOOL DoStartupConfig();
	BOOL GetPasswordProviderUrl(LPTSTR lpszUrl);

protected :
	CVMOpsServiceProvider* m_pProvider;
};

/////////////////////////////////////////////////////////////////////////////
// CLogger
// A simple logger for internal use
//
class CLogger
{
public :
	CLogger();
	~CLogger();

public :
	static CLogger* GetInstance();
	BOOL Initialize();
	void RotateLog();
	void Cleanup();

	void Log(LPCSTR lpszCategory, LPCSTR lpszFormat, ...);

private :
	CCriticalSection m_lock;
	FILE* m_pFile;

private :
	static CLogger* s_pInstance;
};

}
#endif	// __VMOpsServiceProvider_H__

/////////////////////////////////////////////////////////////////////////////

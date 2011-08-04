//
// ThreadUtl.h
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
//
// 

#include "ThreadUtil.h"

#include <atlbase.h>

using namespace VMOps;

/////////////////////////////////////////////////////////////////////////////
// CCriticalSection
//
CCriticalSection::CCriticalSection()
{
    InitializeCriticalSection(&m_cs);
}

CCriticalSection::~CCriticalSection()
{
    DeleteCriticalSection(&m_cs);
}

void CCriticalSection::Lock()
{
    EnterCriticalSection(&m_cs);
}

void CCriticalSection::Unlock()
{
    LeaveCriticalSection(&m_cs);
}

/////////////////////////////////////////////////////////////////////////////
// CThread
//
CThread::CThread()
{
    m_hThread = NULL;
    m_idThread = 0;

    m_hStopEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
    _ASSERTE(m_hStopEvent);
}

CThread::~CThread()
{
    _ASSERTE(m_hStopEvent);
    if(m_hStopEvent)
        CloseHandle(m_hStopEvent);
    m_hStopEvent = NULL;

	_ASSERTE(m_hThread == NULL);
    if(m_hThread)
	{
		TerminateThread(m_hThread, 0);
        CloseHandle(m_hThread);
	}
    m_hThread = NULL;
}

BOOL CThread::Create(DWORD dwCreationFlag)
{
    _ASSERTE(m_hThread == NULL);

    m_hThread = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)ThreadProc,
        (LPVOID)this, dwCreationFlag, &m_idThread);
    _ASSERTE(m_hThread);
    _ASSERTE(m_idThread != 0);

    return m_hThread != NULL;
}

BOOL CThread::Stop(DWORD dwTimeOut)
{
    _ASSERTE(m_idThread != GetCurrentThreadId());
    if(m_idThread == GetCurrentThreadId())
    {
        // this may cause dead-lock if we wait here, 
        // so we just raise the signal
        // and return FALSE to indicate of that
        SetEvent(m_hStopEvent);
        return FALSE;
    }

    if(m_hThread)
    {
		BOOL bReturn = FALSE;

        _ASSERTE(m_hStopEvent);
        SetEvent(m_hStopEvent);
        if(WaitForSingleObject(m_hThread, dwTimeOut) == WAIT_OBJECT_0)
		{
            bReturn = TRUE;

			// restore to initial state
			if(m_hThread != NULL)
			{
				CloseHandle(m_hThread);
				m_hThread = NULL;
			}
			m_idThread = 0;
			ResetEvent(m_hStopEvent);
		}

        return bReturn;
    }
    return TRUE;
}

BOOL CThread::IsSelfThread()
{
    return m_idThread == GetCurrentThreadId();
}

DWORD CThread::ThreadRun()
{
	return 0;
}

DWORD WINAPI CThread::ThreadProc(LPVOID lpvParam)
{
    _ASSERTE(lpvParam);
    CThread* pThread = (CThread*)lpvParam;

    return pThread->ThreadRun();
}

/////////////////////////////////////////////////////////////////////////////


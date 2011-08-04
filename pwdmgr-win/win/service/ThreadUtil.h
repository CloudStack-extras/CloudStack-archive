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

#ifndef __ThreadUtil_H__
#define __ThreadUtil_H__

#include <windows.h>

namespace VMOps {

/////////////////////////////////////////////////////////////////////////////
// class diagram
//
class CLockable;
		class CCriticalSection;
class CLock;
class CThread;

/////////////////////////////////////////////////////////////////////////////
// CLockable
//
class CLockable
{
public :
    virtual void Lock() = 0;
    virtual void Unlock() = 0;
};

/////////////////////////////////////////////////////////////////////////////
// CLock
//
class CLock
{
public :
    CLock(CLockable& lockableObj) : m_lockableObj(lockableObj)
    {
        m_lockableObj.Lock();
    }

    ~CLock()
    {
        m_lockableObj.Unlock();
    }

protected :
    CLockable& m_lockableObj;
};

/////////////////////////////////////////////////////////////////////////////
// CCriticalSection
//
class CCriticalSection : public CLockable
{
public :
    CCriticalSection();
    virtual ~CCriticalSection();

public :
    virtual void Lock();
    virtual void Unlock();

protected :
    CRITICAL_SECTION m_cs;
};


// Simple thread implementation
class CThread
{
public :
    CThread();
    virtual ~CThread();

public :
    BOOL Create(DWORD dwCreationFlag);
    BOOL Stop(DWORD dwTimeOut = INFINITE);

    HANDLE GetThreadHandle() { return m_hThread; }
    DWORD GetThreadId() { return m_idThread; }
    BOOL IsSelfThread();

protected :
	HANDLE GetStopEventHandle() { return m_hStopEvent; }

public :
    virtual DWORD ThreadRun();

public :
    static DWORD WINAPI ThreadProc(LPVOID lpvParam);

protected :
    HANDLE m_hStopEvent;
    HANDLE m_hThread;
    DWORD m_idThread;
};

}

#endif	// !__ThreadUtil_H__

/////////////////////////////////////////////////////////////////////////////

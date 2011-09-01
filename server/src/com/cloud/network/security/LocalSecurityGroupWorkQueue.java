/**
 *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.network.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.cloud.network.security.SecurityGroupWork.Step;


/**
 * Security Group Work Queue that is not shared with other management servers
 *
 */
public class LocalSecurityGroupWorkQueue implements SecurityGroupWorkQueue {
    protected static Logger s_logger = Logger.getLogger(LocalSecurityGroupWorkQueue.class);

    protected TreeSet<SecurityGroupWork> _currentWork = new TreeSet<SecurityGroupWork>();    
    private final ReentrantLock _lock = new ReentrantLock();
    private final Condition _notEmpty = _lock.newCondition(); 
    private final AtomicInteger _count = new AtomicInteger(0);
    
    public static class LocalSecurityGroupWork implements SecurityGroupWork, Comparable<LocalSecurityGroupWork> {
        Long _logSequenceNumber;
        Long _instanceId;
        Step _step;
        
        public LocalSecurityGroupWork(Long instanceId, Long logSequence, Step step){
            this._instanceId = instanceId;
            this._logSequenceNumber = logSequence;
            this._step = step;
        }
        
        @Override
        public Long getInstanceId() {
            return _instanceId;
        }

        @Override
        public Long getLogsequenceNumber() {
            return _logSequenceNumber;
        }

        @Override
        public Step getStep() {
           return _step;
        }

        @Override
        public void setStep(Step step) {
            this._step = step;
        }

        @Override
        public void setLogsequenceNumber(Long logsequenceNumber) {
            this._logSequenceNumber = logsequenceNumber;
            
        }

        @Override
        public int compareTo(LocalSecurityGroupWork o) {
            return this._instanceId.compareTo(o.getInstanceId());
        }
        
    }
    
    
    @Override
    public void submitWorkForVm(long vmId, long sequenceNumber) {
        _lock.lock(); 
        try {
            SecurityGroupWork work = new LocalSecurityGroupWork(vmId, sequenceNumber, Step.Scheduled);
            boolean added = _currentWork.add(work);
            if (added)
                _count.incrementAndGet();
        } finally {
            _lock.unlock();
        }
        signalNotEmpty();

    }

   
    @Override
    public int submitWorkForVms(Set<Long> vmIds) {
        _lock.lock(); 
        int newWork = _count.get();
        try {
            for (Long vmId: vmIds) {
                SecurityGroupWork work = new LocalSecurityGroupWork(vmId, null, SecurityGroupWork.Step.Scheduled);
                boolean added = _currentWork.add(work);
                if (added)
                    _count.incrementAndGet();
            }
        } finally {
            newWork = _count.get() - newWork;
            _lock.unlock();
        }
        signalNotEmpty();
        return newWork;
    }

    
    @Override
    public List<SecurityGroupWork> getWork(int numberOfWorkItems) {
        List<SecurityGroupWork> work = new ArrayList<SecurityGroupWork>(numberOfWorkItems);
        _lock.lock();
        int i = 0;
        try {
            while (_count.get() == 0) {
                _notEmpty.await();
            }
            int n = Math.min(numberOfWorkItems, _count.get());
            while (i < n ) {
                SecurityGroupWork w = _currentWork.first();
                w.setStep(Step.Processing);
                work.add(w);
                _currentWork.remove(w);
                ++i;
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            int c = _count.addAndGet(-i);
            if (c > 0)
                _notEmpty.signal();
            _lock.unlock();
        }
        return work;

    }
    
    private void signalNotEmpty() {
        _lock.lock();
        try {
            _notEmpty.signal();
        } finally {
            _lock.unlock();
        }
    }


    @Override
    public int size() {
        return _count.get();
    }


    @Override
    public void clear() {
        _lock.lock();
        try {
            _currentWork.clear();
            _count.set(0);
        } finally {
            _lock.unlock();
        }
        
    }
        

}

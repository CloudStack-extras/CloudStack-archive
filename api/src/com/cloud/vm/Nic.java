/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
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
package com.cloud.vm;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.Mode;
import com.cloud.utils.fsm.FiniteState;
import com.cloud.utils.fsm.StateMachine;

/**
 * Nic represents one nic on the VM.
 */
public interface Nic {
    enum Event {
        ReservationRequested, ReleaseRequested, CancelRequested, OperationCompleted, OperationFailed,
    }

    public enum State implements FiniteState<State, Event> {
        Allocated("Resource is allocated but not reserved"), Reserving("Resource is being reserved right now"), Reserved("Resource has been reserved."), Releasing("Resource is being released"), Deallocating(
                "Resource is being deallocated");

        String _description;

        @Override
        public StateMachine<State, Event> getStateMachine() {
            return s_fsm;
        }

        @Override
        public State getNextState(Event event) {
            return s_fsm.getNextState(this, event);
        }

        @Override
        public List<State> getFromStates(Event event) {
            return s_fsm.getFromStates(this, event);
        }

        @Override
        public Set<Event> getPossibleEvents() {
            return s_fsm.getPossibleEvents(this);
        }

        private State(String description) {
            _description = description;
        }

        @Override
        public String getDescription() {
            return _description;
        }

        final static private StateMachine<State, Event> s_fsm = new StateMachine<State, Event>();
        static {
            s_fsm.addTransition(State.Allocated, Event.ReservationRequested, State.Reserving);
            s_fsm.addTransition(State.Reserving, Event.CancelRequested, State.Allocated);
            s_fsm.addTransition(State.Reserving, Event.OperationCompleted, State.Reserved);
            s_fsm.addTransition(State.Reserving, Event.OperationFailed, State.Allocated);
            s_fsm.addTransition(State.Reserved, Event.ReleaseRequested, State.Releasing);
            s_fsm.addTransition(State.Releasing, Event.OperationCompleted, State.Allocated);
            s_fsm.addTransition(State.Releasing, Event.OperationFailed, State.Reserved);
        }
    }

    public enum ReservationStrategy {
        PlaceHolder, Create, Start, Managed;
    }

    /**
     * @return id in the CloudStack database
     */
    long getId();

    /**
     * @return reservation id returned by the allocation source. This can be the String version of the database id if the
     *         allocation source does not need it's own implementation of the reservation id. This is passed back to the
     *         allocation source to release the resource.
     */
    String getReservationId();

    /**
     * @return unique name for the allocation source.
     */
    String getReserver();

    /**
     * @return the time a reservation request was made to the allocation source.
     */
    Date getUpdateTime();

    /**
     * @return the reservation state of the resource.
     */
    State getState();

    ReservationStrategy getReservationStrategy();

    boolean isDefaultNic();

    String getIp4Address();

    String getMacAddress();

    String getNetmask();

    String getGateway();

    /**
     * @return network profile id that this
     */
    long getNetworkId();

    /**
     * @return the vm instance id that this nic belongs to.
     */
    long getInstanceId();

    int getDeviceId();

    Mode getMode();

    URI getIsolationUri();

    URI getBroadcastUri();

    VirtualMachine.Type getVmType();

    AddressFormat getAddressFormat();
}

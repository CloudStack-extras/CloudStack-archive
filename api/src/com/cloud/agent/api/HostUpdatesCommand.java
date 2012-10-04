package com.cloud.agent.api;

public class HostUpdatesCommand extends Command {

    public HostUpdatesCommand(){
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}

/**
 * 
 */
package com.cloud.agent.api;

import com.cloud.agent.api.to.VirtualMachineTO;

public class StartAnswer extends Answer {
    VirtualMachineTO vm;
    
    protected StartAnswer() {
    }
    
    public StartAnswer(StartCommand cmd, String msg) {
        super(cmd, false, msg);
        this.vm  = cmd.getVirtualMachine();
    }
    
    public StartAnswer(StartCommand cmd, Exception e) {
        super(cmd, false, e.getMessage());
        this.vm  = cmd.getVirtualMachine();
    }
    
    public StartAnswer(StartCommand cmd) {
        super(cmd, true, null);
        this.vm  = cmd.getVirtualMachine();
    }
    
    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }
}

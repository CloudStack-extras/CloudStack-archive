package com.cloud.ucs.manager;

import com.cloud.api.commands.AddHostCmd;

public class AddUcsHostCmd extends AddHostCmd {
    private String bladeDn;

    public String getBladeDn() {
        return bladeDn;
    }

    public void setBladeDn(String bladeDn) {
        this.bladeDn = bladeDn;
    }
}

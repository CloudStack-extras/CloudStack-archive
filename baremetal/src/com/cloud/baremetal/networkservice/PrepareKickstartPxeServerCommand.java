package com.cloud.baremetal.networkservice;

import com.cloud.agent.api.Command;

public class PrepareKickstartPxeServerCommand extends Command {
    private String ksFile;
    private String repo;
    private String templateUuid;
    private String mac;
    
    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getKsFile() {
        return ksFile;
    }

    public void setKsFile(String ksFile) {
        this.ksFile = ksFile;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getTemplateUuid() {
        return templateUuid;
    }

    public void setTemplateUuid(String templateUuid) {
        this.templateUuid = templateUuid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
    
}

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


public class computeBlade {
    private String adminPower                  ;
    private String adminState                  ;
    private String assignedToDn                ;
    private String association                 ;
    private String availability                ;
    private String availableMemory             ;
    private String chassisId                   ;
    private String checkPoint                  ;
    private String connPath                    ;
    private String connStatus                  ;
    private String descr                       ;
    private String discovery                   ;
    private String dn                          ;
    private String fltAggr                     ;
    private String fsmDescr                    ;
    private String fsmFlags                    ;
    private String fsmPrev                     ;
    private String fsmProgr                    ;
    private String fsmRmtInvErrCode            ;
    private String fsmRmtInvErrDescr           ;
    private String fsmRmtInvRslt               ;
    private String fsmStageDescr               ;
    private String fsmStamp                    ;
    private String fsmStatus                   ;
    private String fsmTry                      ;
    private String intId                       ;
    private String lc                          ;
    private String lcTs                        ;
    private String lowVoltageMemory            ;
    private String managingInst                ;
    private String memorySpeed                 ;
    private String mfgTime                     ;
    private String model                       ;
    private String name                        ;
    private String numOfAdaptors               ;
    private String numOfCores                  ;
    private String numOfCoresEnabled           ;
    private String numOfCpus                   ;
    private String numOfEthHostIfs             ;
    private String numOfFcHostIfs              ;
    private String numOfThreads                ;
    private String operPower                   ;
    private String operQualifier               ;
    private String operState                   ;
    private String operability                 ;
    private String originalUuid                ;
    private String presence                    ;
    private String revision                    ;
    private String serial                      ;
    private String serverId                    ;
    private String slotId                      ;
    private String totalMemory                 ;
    private String usrLbl                      ;
    private String uuid                        ;
    private String vendor                      ;

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }
}

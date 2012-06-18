package com.cloud.api.commands;
import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserVmResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.uservm.UserVm;

@Implementation(responseObject = UserVmResponse.class, description="dynamically allocates VCPU's to a virtual machine without stopping it.")

public class ModifyVMCmd extends BaseAsyncCmd {
       public static final Logger s_logger = Logger.getLogger(ModifyVMCmd.class.getName());

       private static final String s_name = "Modifyvirtualmachineresponse";
       // API parameter
       @IdentityMapper(entityTableName = "vm_instance")
       @Parameter(name = ApiConstants.ID, type = CommandType.LONG, required = true, description = "The ID of the virtual machine")
       private Long id;
       private int vCpu;
       //accessor method
       public Long getId() {
          return id;
       }

      //API implementation
        @Override
        public String getCommandName() {
            return s_name;
         }

        public static string getResultObjectName() {
            return "virtualmachine";
         }

        @Override
        public long getEntityOwnerId() {
           UserVM vm = _responseGenerator.findUserVMById(getId());
           if( vm != null) {
               return vm.getAccountId(); 

         }
           return Account.ACCOUNT_ID_SYSTEM;

         }

        @Override
         public String getEventType() {
           return EventTypes.EVENT_VM_MODIFY;
 
         }
        
         @Override
         public String getEventDescription() {
            return "modifying vcpu's for user vm: "+ getId();
          }

         @Override 
         public AsyncJob.Type getInstanceType() {
           return AsyncJob.Type.VirtualMachine;
         } 


         @Override
         public Long getInstanceId() {
             return getId();
          }

         @Override
         public int getvCpu(){
          return vCpu;

         }
         @Override
         public void execute() throws ServerApiException,ConcurrentOperationException {

        UserContext.current().setEventDetails("Vm Id: " + getId());
        UserVm result;

        if (_userVmService.getHypervisorTypeOfUserVM(getId()) == HypervisorType.BareMetal) {
            result = _bareMetalVmService.modifyVirtualMachine(getId(),getvCpu());
        } 

         else {
            result = _userVmService.modifyVirtualMachine(getId(), getvCpu());
        }
        

        if (result != null) {
            UserVmResponse response = _responseGenerator.createUserVmResponse("virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to stop vm");
        }
    }
}

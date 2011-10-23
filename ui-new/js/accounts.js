(function(cloudStack, testData) {  
  cloudStack.sections.accounts = {
    title: 'Accounts',
    id: 'accounts',
    listView: {
      fields: {
        name: { label: 'Name' },
		accounttype: { 
		  label: 'Role', 
		  converter: function(args){
		    return cloudStack.converters.toRole(args);          
		  }
		},
        domain: { label: 'Domain' },
        state: { label: 'State' }
      },
	  
	  /*
      filters: {
        mine: { label: 'My Accounts' },
        all: { label: 'All Accounts' }
      },
      */
	  	  
	  dataProvider: function(args) {        
		$.ajax({
		  url: createURL("listAccounts&page=" + args.page + "&pagesize=" + pageSize),
		  dataType: "json",
		  async: true,
		  success: function(json) { 	
			var items = json.listaccountsresponse.account;			    
			args.response.success({
			  actionFilter: accountActionfilter,
			  data:items
			});			               			
		  }
		});  	
	  },
	  
	  //???
	  detailView: {
		name: 'Account details',   
		
		actions: {		
		
		},
		
		tabs: {
		  details: {
			title: 'details',
						
			fields: [
			  {
				name: { 
				  label: 'Name', 
				  isEditable: true 
				}
			  },
			  {			    
                id: { label: 'ID' },				
				accounttype: { 
				  label: 'Role', 
				  converter: function(args){
					return cloudStack.converters.toRole(args);          
				  }
				},
				domain: { label: 'Domain' },
				state: { label: 'State' },
				vmtotal: { label: 'Total of VM' },
				iptotal: { label: 'Total of IP Address' },
				receivedbytes: { 
				  label: 'Bytes received',
                  converter: function(args) {					    
					if (args == null || args == 0)
					  return "";
					else
					  return cloudStack.converters.convertBytes(args);                       
				  }						  
				},
				sentbytes: { 
				  label: 'Bytes sent',
				  converter: function(args) {					    
					if (args == null || args == 0)
					  return "";
					else
					  return cloudStack.converters.convertBytes(args);                       
				  }		
				}														
			  }
			],
			
			dataProvider: function(args) {	                  	
			  args.response.success(
				{
				  actionFilter: accountActionfilter,
				  data:args.context.accounts[0]
				}
			  );	
			}			
		  }		  
		}
	  }  
    }
  }; 

  var accountActionfilter = function(args) {	    		  
    var jsonObj = args.context.item;
	var allowedActions = [];	
	return allowedActions;
  }
  
})(cloudStack, testData);

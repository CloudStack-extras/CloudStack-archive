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
	  	 
	  actions: {		    
		add: {
		  label: 'Create account',			   		  

		  messages: {
			confirm: function(args) {
			  return 'Are you sure you want to create an account?';
			},
			success: function(args) {
			  return 'Your new account is being created.';
			},
			notification: function(args) {
			  return 'Creating new account';
			},
			complete: function(args) {
			  return 'Account has been created successfully!';
			}
		  },

		  createForm: {
			title: 'Create account',
			desc: 'Please fill in the following data to create a new account.',                
			preFilter: cloudStack.preFilter.createTemplate,	               
			fields: {			  
			  username: {
				label: 'Username',
				validation: { required: true }					
			  },
			  password: {
				label: 'Password',
				validation: { required: true },
                isPassword: true				
			  },
			  email: {
				label: 'Email',
				validation: { required: true }					
			  },
			  firstname: {
				label: 'First name',
				validation: { required: true }					
			  },
			  lastname: {
				label: 'Last name',
				validation: { required: true }					
			  },
			  domainid: {
				label: 'Domain',
				validation: { required: true },
                select: function(args) {				                 
				  $.ajax({
					url: createURL("listDomains"),				
					dataType: "json",
					async: false,
					success: function(json) {		
                      var items = [];					
					  var domainObjs = json.listdomainsresponse.domain;	                      
					  $(domainObjs).each(function() {
					    items.push({id: this.id, description: this.name});
					  });					  
                      args.response.success({data: items});					  
					}
				  });	
                } 
			  },
			  account: {
				label: 'Account'				
			  },
			  accounttype: {
				label: 'Role',
				validation: { required: true },
                select: function(args) {
                  var items = [];
				  items.push({id:0, description: "User"});
				  items.push({id:1, description: "Admin"});
				  args.response.success({data: items});
                }				
			  }	  
			}
		  },
		  
		  action: function(args) {					
			var array1 = [];				
			array1.push("&name=" + todb(args.data.name));				
			array1.push("&displayText=" + todb(args.data.description));				
			array1.push("&url=" + todb(args.data.url));				
			array1.push("&zoneid=" + args.data.zone);					
			array1.push("&format=" + args.data.format);		
			array1.push("&isextractable=" + (args.data.isExtractable=="on"));					
			array1.push("&passwordEnabled=" + (args.data.isPasswordEnabled=="on"));					
			array1.push("&osTypeId=" + args.data.osTypeId);			
			array1.push("&hypervisor=" + args.data.hypervisor);
									
			if(args.$form.find('.form-item[rel=isPublic]').css("display") != "none")
			  array1.push("&ispublic=" + (args.data.isPublic == "on"));	
			if(args.$form.find('.form-item[rel=isFeatured]').css("display") != "none")
			  array1.push("&isfeatured=" + (args.data.isFeatured == "on")); 			
			
			$.ajax({
			  url: createURL("registerTemplate" + array1.join("")),
			  dataType: "json",
			  success: function(json) {					    
				var items = json.registertemplateresponse.template;	 //items might have more than one array element if it's create templates for all zones.			       
				args.response.success({data:items[0]});	
				/*                        
				if(items.length > 1) {                               
				  for(var i=1; i<items.length; i++) {   
					var $midmenuItem2 = $("#midmenu_item").clone();
					templateToMidmenu(items[i], $midmenuItem2);
					bindClickToMidMenu($midmenuItem2, templateToRightPanel, templateGetMidmenuId); 
					$("#midmenu_container").append($midmenuItem2.show());
				  }                                    
				}  
				*/						
			  }, 
			  error: function(XMLHttpResponse) {	
				var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
				args.response.error(errorMsg);		
			  }						
			});						
		  },			

		  notification: {                
			poll: function(args) {			  
			  args.complete();
			}		
		  }
		} 
	  },
	  	  	  
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

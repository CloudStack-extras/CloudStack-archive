(function(cloudStack, testData) {
    login();

    var getAccounts = function(r) {        
        $.ajax({
	        url: createURL("listAccounts"),
		    dataType: "json",
		    async: true,
		    success: function(json) { 	
			    var items = json.listaccountsresponse.account;			    
				r.response.success({data:items});			               			
		    }
	    });  	
    }


  cloudStack.sections.accounts = {
    title: 'Accounts',
    id: 'accounts',
    listView: {
      fields: {
        name: { label: 'Name', editable: true },
        domain: { label: 'Domain' },
        state: { label: 'State' }
      },
      filters: {
        mine: { label: 'My Accounts' },
        all: { label: 'All Accounts' }
      },
      
	  //dataProvider: testData.dataProvider.listView('accounts')
	  dataProvider: getAccounts
	  
    }
  };  
})(cloudStack, testData);

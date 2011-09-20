(function(cloudStack, testData) {
  login();	

  cloudStack.sections.events = {
    title: 'Events',
    id: 'events',
    sections: {
      events: {
        title: 'Events',
        listView: {
          label: 'Events',
          fields: {
            type: { label: 'Type' },
            description: { label: 'Description' },
            username: { label: 'Initiated By' },
            created: { label: 'Date' }
          },          		  
		  dataProvider: function(args) {   
			$.ajax({
			  url: createURL("listEvents&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 	
				var items = json.listeventsresponse.event;			    
				args.response.success({data:items});			                			
			  }
			});  	
		  }		  
        }
      },
      alerts: {
        title: 'Alerts',
        listView: {
          label: 'Alerts',
          fields: {
            type: { label: 'Type' },
            description: { label: 'Description' },
            sent: { label: 'Date' }
          },          
		  dataProvider: function(args) {        
			$.ajax({
			  url: createURL("listAlerts&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 	
				var items = json.listalertsresponse.alert;			    
				args.response.success({data:items});		                			
			  }
		    });  	
		  },		  
          detailView: {
            name: 'Alert details',
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    type: { label: 'Type' },
                    description: { label: 'Description' },
                    created: { label: 'Sent' }
                  }
                ],
                dataProvider: testData.dataProvider.detailView('alerts')
              },
            }
          }
        }
      }
    }
  };
})(cloudStack, testData);

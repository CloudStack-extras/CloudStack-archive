(function(cloudStack, testData) {
    login();

    var getEvents = function(r) {        
        $.ajax({
	        url: createURL("listEvents"),
		    dataType: "json",
		    async: true,
		    success: function(json) { 	
			    var items = json.listeventsresponse.event;
			    if(items != null && items.length > 0) {
				    r.response.success({data:items});		
	            }    			
		    }
	    });  	
    }
	
	var getAlerts = function(r) {        
        $.ajax({
	        url: createURL("listAlerts"),
		    dataType: "json",
		    async: true,
		    success: function(json) { 	
			    var items = json.listalertsresponse.alert;
			    if(items != null && items.length > 0) {
				    r.response.success({data:items});		
	            }    			
		    }
	    });  	
    }

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
          
		  //dataProvider: testData.dataProvider.listView('events')
		  dataProvider: getEvents
		  
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
		  
          //dataProvider: testData.dataProvider.listView('alerts'),
		  dataProvider: getAlerts,
		  
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

(function(cloudStack, testData) {
    login();

    var getTemplates = function(r) {        
        $.ajax({
	        url: createURL("listTemplates&templatefilter=self"),
		    dataType: "json",
		    async: true,
		    success: function(json) { 	
			    var items = json.listtemplatesresponse.template;			    
				r.response.success({data:items});			                			
		    }
	    });  	
    }
	
	var getISOs = function(r) {        
        $.ajax({
	        url: createURL("listIsos&isofilter=self"),
		    dataType: "json",
		    async: true,
		    success: function(json) { 	
			    var items = json.listisosresponse.iso;			    
				r.response.success({data:items});		                			
		    }
	    });  	
    }

  cloudStack.sections.templates = {
    title: 'Templates',
    id: 'templates',
    sections: {
      templates: {
        title: 'Templates',
        listView: {
          label: 'Templates',
          fields: {
            name: { label: 'Name', editable: true },
            id: { label: 'ID' },
            zonename: { label: 'Zone' },
            hypervisor: { label: 'Hypervisor' }
          },
          actions: {
            edit: {
              label: 'Edit template name',
              action: function(args) {
                args.response.success(args.data[0]);
              }
            }
          },
		  
          //dataProvider: testData.dataProvider.listView('templates')
		  dataProvider: getTemplates
		  
        }
      },
      isos: {
        title: 'ISOs',
        listView: {
          label: 'ISOs',
          fields: {
            displaytext: { label: 'Name' },
            id: { label: 'ID' },
            size: { label: 'Size' },
            zonename: { label: 'Zone' }
          },
          
		  //dataProvider: testData.dataProvider.listView('isos')
		  dataProvider: getISOs
		  
        }
      }
    }
  };  
})(cloudStack, testData);

(function(cloudStack) {
  cloudStack.sections['global-settings'] = {
    title: 'label.menu.global.settings',
    id: 'global-settings',
    sectionSelect: {
      label: 'label.select-view'
    },
    sections: {
      globalSettings: {
        type: 'select',
        title: 'label.menu.global.settings',
        listView: {
          label: 'label.menu.global.settings',
          actions: {
            edit: {
              label: 'label.change.value',
              action: function(args) {           
                var name = args.data.jsonObj.name;
                var value = args.data.value;

                $.ajax({
                  url: createURL(
                    'updateConfiguration&name=' + name + '&value=' + value
                  ),
                  dataType: 'json',
                  async: true,
                  success: function(json) {                
                    var item = json.updateconfigurationresponse.configuration;
                    cloudStack.dialog.notice({ message: _l('message.restart.mgmt.server') });
                    args.response.success({data: item});
                  },
                  error: function(json) {                
                    args.response.error(parseXMLHttpResponse(json));
                  }
                });
              }
            }
          },
          fields: {
            name: { label: 'label.name', id: true },
            description: { label: 'label.description' },
            value: { label: 'label.value', editable: true }
          },
          dataProvider: function(args) {
            var data = {
              page: args.page,
              pagesize: pageSize
            };

            if (args.filterBy.search.value) {
              data.name = args.filterBy.search.value;
            }

            $.ajax({
              url: createURL('listConfigurations'),
              data: data,
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listconfigurationsresponse.configuration;
                args.response.success({ data: items });
              }
            });
          }
        }
      },
      hypervisorCapabilities: {
        type: 'select',
        title: 'label.hypervisor.capabilities',
        listView: {
          id: 'hypervisorCapabilities',
          label: 'label.hypervisor.capabilities',
          fields: {
            hypervisor: { label: 'label.hypervisor' },
            hypervisorversion: { label: 'label.hypervisor.version' },
            maxguestslimit: { label: 'label.max.guest.limit' }
          },
          dataProvider: function(args) {					  
						var array1 = [];  
						if(args.filterBy != null) {          
							if(args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
								switch(args.filterBy.search.by) {
								case "name":
									if(args.filterBy.search.value.length > 0)
										array1.push("&keyword=" + args.filterBy.search.value);
									break;
								}
							}
						}				
					  
            $.ajax({
              url: createURL("listHypervisorCapabilities&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listhypervisorcapabilitiesresponse.hypervisorCapabilities;
                args.response.success({data:items});
              },
              error: function(data) {
                args.response.error(parseXMLHttpResponse(data));
              }
            });
          },

          detailView: {
            name: 'label.details',
            actions: {
              edit: {
                label: 'label.edit',
                action: function(args) {
                  var array1 = [];
                  array1.push("&maxguestslimit=" + todb(args.data.maxguestslimit));
                  $.ajax({
                    url: createURL("updateHypervisorCapabilities&id=" + args.context.hypervisorCapabilities[0].id + array1.join("")),
                    dataType: "json",
                    success: function(json) {
                      var item = json.updatehypervisorcapabilitiesresponse['null'];
                      args.response.success({data: item});
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
                    }
                  });
                }
              }
            },

            tabs: {
              details: {
                title: 'label.details',
                fields: [
                  {
                    id: { label: 'label.id' },
                    hypervisor: { label: 'label.hypervisor' },
                    hypervisorversion: { label: 'label.hypervisor.version' },
                    maxguestslimit: {
                      label: 'label.max.guest.limit',
                      isEditable: true
                    }
                  }
                ],
                dataProvider: function(args) {
                  args.response.success(
                    {
                      data:args.context.hypervisorCapabilities[0]
                    }
                  );
                }
              }
            }
          }
        }
      }
    }
  };
})(cloudStack);

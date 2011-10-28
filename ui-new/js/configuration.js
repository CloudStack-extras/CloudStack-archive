(function(cloudStack, testData) {
  cloudStack.sections.configuration = {
    title: 'Configuration',
    id: 'configuration',
    sectionSelect: {
      label: 'Select Offering'
    },
    sections: {
      serviceOfferings: {
        type: 'select',
        title: 'Service',
        listView: {
          label: 'Service Offerings',
          fields: {
            name: { label: 'Name', editable: true },
            storagetype: { label: 'Storage Type' },
            cpuspeed: { label: 'CPU' },
            memory: { label: 'Memory' },
            domain: { label: 'Domain'}
          },
          actions: {
            add: {
              label: 'Add service offering',

              action: function(args) {
                args.response.success();
              },

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add ' + args.name + '?';
                },
                success: function(args) {
                  return 'Your new service offering is being created.';
                },
                notification: function(args) {
                  return 'Created service offering';
                },
                complete: function(args) {
                  return 'Service offering has been created';
                }
              },

              createForm: {
                title: 'New service offering',
                desc: 'Please fill in the following data to add a new service offering.',
                fields: {
                  name: { label: 'Name', editable: true },
                  displayText: { label: 'Display Text' },
                  storageType: {
                    label: 'Storage Type',
                    select: [
                      { id: 'shared', description: 'Shared' },
                      { id: 'local', description: 'Local' }
                    ]
                  },
                  cpuCores: { label: '# of CPU cores' },
                  cpuSpeed: { label: 'CPU Speed (in MHz)'},
                  memory: { label: 'Memory (in MB)' },
                  tags: { label: 'Tags' },
                  offerHA: { label: 'Offer HA', isBoolean: true },
                  isPublic: { label: 'Public', isBoolean: true }
                }
              },

              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          dataProvider: function(args) {
            $.ajax({
              url: createURL("listServiceOfferings&issystem=false&page="+args.page+"&pagesize="+pageSize),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listserviceofferingsresponse.serviceoffering;
                args.response.success({data:items});
              }
            });
          }
        }
      },

      systemServiceOfferings: {
        type: 'select',
        title: 'System Service',
        listView: {
          label: 'System Service Offerings',
          fields: {
            name: { label: 'Name', editable: true },
            storagetype: { label: 'Storage Type' },
            cpuspeed: { label: 'CPU' },
            memory: { label: 'Memory' },
            domain: { label: 'Domain'}
          },
          actions: {
            add: {
              label: 'Add system service offering',

              action: function(args) {
                args.response.success();
              },

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add ' + args.name + '?';
                },
                success: function(args) {
                  return 'Your new system service offering is being created.';
                },
                notification: function(args) {
                  return 'Created system service offering';
                },
                complete: function(args) {
                  return 'Sysmte service offering has been created';
                }
              },

              createForm: {
                title: 'New system service offering',
                desc: 'Please fill in the following data to add a new service offering.',
                fields: {
                  name: { label: 'Name', editable: true },
                  displayText: { label: 'Display Text' },
                  storageType: {
                    label: 'Storage Type',
                    select: [
                      { id: 'shared', description: 'Shared' },
                      { id: 'local', description: 'Local' }
                    ]
                  },
                  cpuCores: { label: '# of CPU cores' },
                  cpuSpeed: { label: 'CPU Speed (in MHz)'},
                  memory: { label: 'Memory (in MB)' },
                  tags: { label: 'Tags' },
                  offerHA: { label: 'Offer HA', isBoolean: true },
                  isPublic: { label: 'Public', isBoolean: true }
                }
              },

              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          dataProvider: function(args) {
            $.ajax({
              url: createURL("listServiceOfferings&issystem=true&page="+args.page+"&pagesize="+pageSize),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listserviceofferingsresponse.serviceoffering;
                args.response.success({data:items});
              }
            });
          }
        }
      },

      diskOfferings: {
        type: 'select',
        title: 'Disk',
        listView: {
          label: 'Disk Offerings',
          fields: {
            displaytext: { label: 'Name' },
            disksize: { label: 'Disk Size' },
            domain: { label: 'Domain'}
          },
          dataProvider: function(args) {
            $.ajax({
              url: createURL("listDiskOfferings&page="+args.page+"&pagesize="+pageSize),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listdiskofferingsresponse.diskoffering;
                args.response.success({data:items});
              }
            });
          },
          
          //???         
          actions: {            
            add: {
              label: 'Add disk offering',

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add a disk offering?';
                },
                success: function(args) {
                  return 'Your new disk offering is being created.';
                },
                notification: function(args) {
                  return 'Creating new disk offering';
                },
                complete: function(args) {
                  return 'Disk offering has been created successfully!';
                }
              },

              createForm: {
                title: 'Add disk offering',               
                fields: {
                  name: {
                    label: 'Name',
                    validation: { required: true }
                  },
                  description: {
                    label: 'Description',
                    validation: { required: true }
                  },
                  isCustomized: {
                    label: 'Custom disk size',
                    isBoolean: true,
                    defaultValue: false
                  },
                  disksize: {
                    label: 'Disk size (in GB)',
                    dependsOn: 'isCustomized',
                    validation: { required: true, number: true }//,
                    //isHidden: true  //uncomment this line when Brian fixes bug 157
                  },
                  tags: {
                    label: 'Storage tags'
                  },
                  isPublic: {
                    label: 'Public',
                    isBoolean: true,
                    defaultValue: true //will take effect when Brian fixes bug 157
                  },
                  domainId: {
                    label: 'Domain',
                    dependsOn: 'isPublic',
                    select: function(args) {                                         
                      $.ajax({
                        url: createURL("listDomains"),
                        dataType: "json",
                        async: false,
                        success: function(json) {
                          var items = [];
                          var domainObjs = json.listdomainsresponse.domain;                          
                          $(domainObjs).each(function(){                            
                            items.push({id: this.id, description: this.name});
                          });                         
                          args.response.success({data: items});
                        }
                      });                     
                    },
                    isHidden: true
                  }                  
                }
              },

              action: function(args) {
                var array1 = [];               
                array1.push("&name=" + args.data.name);
                array1.push("&displaytext=" + todb(args.data.description));
                              
                array1.push("&customized=" + (args.data.isCustomized=="on"));                
                if(args.$form.find('.form-item[rel=disksize]').css("display") != "none")     
                  array1.push("&disksize=" + args.data.disksize);                
                                
                if(args.data.tags != null && args.data.tags.length > 0)                  
                  array1.push("&tags=" + todb(args.data.tags));								
                
                //uncomment the following 2 lines when Brian fixes bug 157
                /*
                if(args.$form.find('.form-item[rel=domainId]').css("display") != "none") 
                  array1.push("&domainid=" + args.data.domainId);		
                */   
                
                $.ajax({
                  url: createURL("createDiskOffering&isMirrored=false" + array1.join("")),
                  dataType: "json",
                  async: true,
                  success: function(json) {                   
                    var item = json.creatediskofferingresponse.diskoffering;		
                    args.response.success({data: item});		
                  }
                });
              },

              notification: {
                poll: function(args) {
                  args.complete();
                }
              }
            }
          }
          //???          
        }
      },
      networkOfferings: {
        type: 'select',
        title: 'Network',
        listView: {
          label: 'Network Offerings',
          fields: {
            name: { label: 'Name', editable: true },
            networkrate: { label: 'Network Rate' },
            traffictype: { label: 'Traffic Type'}
          },
          dataProvider: function(args) {
            $.ajax({
              url: createURL("listNetworkOfferings&guestiptype=Virtual&page="+args.page+"&pagesize="+pageSize),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listnetworkofferingsresponse.networkoffering;
                args.response.success({data:items});
              }
            });
          }
        }
      }
    }
  };
})(cloudStack, testData);

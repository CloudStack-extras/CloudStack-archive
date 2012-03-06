(function(cloudStack, $) {

  var requiredNetworkOfferingExists = false;
  var networkServiceObjs = [], serviceCheckboxNames = [];
	
  cloudStack.sections.configuration = {
    title: 'label.menu.service.offerings',
    id: 'configuration',
    sectionSelect: {
      label: 'label.select.offering'
    },
    sections: {
      serviceOfferings: {
        type: 'select',
        title: 'label.compute.offerings',
        listView: {
          id: 'serviceOfferings',
          label: 'label.menu.service.offerings',
          fields: {
            name: { label: 'label.name', editable: true },
            displaytext: { label: 'label.description' }
          },

          reorder: cloudStack.api.actions.sort('updateServiceOffering', 'serviceOfferings'),

          actions: {
            add: {
              label: 'label.add.compute.offering',

              messages: {
                confirm: function(args) {
                  return 'message.add.service.offering';
                },
                notification: function(args) {
                  return 'label.add.compute.offering';
                }
              },

              createForm: {
                title: 'label.add.compute.offering',
                fields: {
                  name: {
                    label: 'label.name',
                    validation: { required: true }
                  },
                  description: {
                    label: 'label.description',
                    validation: { required: true }
                  },
                  storageType: {
                    label: 'label.storage.type',
                    select: function(args) {
                      var items = [];
                      items.push({id: 'shared', description: 'shared'});
                      items.push({id: 'local', description: 'local'});
                      args.response.success({data: items});
                    }
                  },
                  cpuNumber: {
                    label: 'label.num.cpu.cores',
                    validation: {
                      required: true,
                      number: true
                    }
                  },
                  cpuSpeed: {
                    label: 'label.cpu.mhz',
                    validation: {
                      required: true,
                      number: true
                    }
                  },
                  memory: {
                    label: 'label.memory.mb',
                    validation: {
                      required: true,
                      number: true
                    }
                  },
                  networkRate: {
                    label: 'label.network.rate',
                    validation: {
                      required: false, //optional
                      number: true
                    }
                  },
                  offerHA: {
                    label: 'label.offer.ha',
                    isBoolean: true,
                    isChecked: false
                  },
                  storageTags: {
                    label: 'label.storage.tags'
                  },
                  hostTags: {
                    label: 'label.host.tags'
                  },
                  cpuCap: {
                    label: 'label.CPU.cap',
                    isBoolean: true,
                    isChecked: false
                  },
                  isPublic: {
                    label: 'label.public',
                    isBoolean: true,
                    isReverse: true,
                    isChecked: true
                  },
                  domainId: {
                    label: 'label.domain',
                    dependsOn: 'isPublic',
                    select: function(args) {		
                      $.ajax({
                        url: createURL("listDomains&listAll=true"),
                        dataType: "json",
                        async: false,
                        success: function(json) {
                          var items = [];
                          var domainObjs = json.listdomainsresponse.domain;
                          $(domainObjs).each(function(){
                            items.push({id: this.id, description: this.path});
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
                array1.push("&storageType=" + todb(args.data.storageType));
                array1.push("&cpuNumber=" + args.data.cpuNumber);
                array1.push("&cpuSpeed="+ args.data.cpuSpeed);
                array1.push("&memory=" + args.data.memory);

                if(args.data.networkRate != null && args.data.networkRate.length > 0)
                  array1.push("&networkrate=" + args.data.networkRate);

                array1.push("&offerha=" + (args.data.offerHA == "on"));

                if(args.data.storageTags != null && args.data.storageTags.length > 0)
                  array1.push("&tags=" + todb(args.data.storageTags));

                if(args.data.hostTags != null && args.data.hostTags.length > 0)
                  array1.push("&hosttags=" + todb(args.data.hostTags));

                array1.push("&limitcpuuse=" + (args.data.cpuCap == "on"));

                if(args.$form.find('.form-item[rel=domainId]').css("display") != "none")
                  array1.push("&domainid=" + args.data.domainId);

                $.ajax({
                  url: createURL("createServiceOffering&issystem=false"+array1.join("")),
                  dataType: "json",
                  async: true,
                  success: function(json) {
                    var item = json.createserviceofferingresponse.serviceoffering;
                    args.response.success({data: item});
                  },
                  error: function(data) {
                    args.response.error(parseXMLHttpResponse(data));
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
              url: createURL("listServiceOfferings&issystem=false&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listserviceofferingsresponse.serviceoffering;
                args.response.success({
                  actionFitler: serviceOfferingActionfilter,
                  data:items
                });
              },
              error: function(data) {
                args.response.error(parseXMLHttpResponse(data));
              }
            });
          },

          detailView: {
            name: 'Service offering details',
            actions: {
              edit: {
                label: 'label.edit',
                action: function(args) {
                  var array1 = [];
                  array1.push("&name=" + todb(args.data.name));
                  array1.push("&displaytext=" + todb(args.data.displaytext));
                  $.ajax({
                    url: createURL("updateServiceOffering&id=" + args.context.serviceOfferings[0].id + array1.join("")),
                    dataType: "json",
                    success: function(json) {
                      var item = json.updateserviceofferingresponse.serviceoffering;
                      args.response.success({data: item});
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
                    }
                  });
                }
              },

              remove: {
                label: 'label.action.delete.service.offering',
                messages: {
                  confirm: function(args) {
                    return 'message.action.delete.service.offering';
                  },
                  notification: function(args) {
                    return 'label.action.delete.service.offering';
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("deleteServiceOffering&id=" + args.context.serviceOfferings[0].id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      args.response.success();
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
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

            tabs: {
              details: {
                title: 'label.details',

                fields: [
                  {
                    name: {
                      label: 'label.name',
                      isEditable: true
                    }
                  },
                  {
                    id: { label: 'label.id' },
                    displaytext: {
                      label: 'label.description',
                      isEditable: true
                    },
                    storagetype: { label: 'label.storage.type' },
                    cpunumber: { label: 'label.num.cpu.cores' },
                    cpuspeed: {
                      label: 'label.cpu.mhz',
                      converter: function(args) {
                        return cloudStack.converters.convertHz(args);
                      }
                    },
                    memory: {
                      label: 'label.memory.mb',
                      converter: function(args) {
                        return cloudStack.converters.convertBytes(args*1024*1024);
                      }
                    },
                    networkrate: { label: 'label.network.rate' },
                    offerha: {
                      label: 'label.offer.ha',
                      converter: cloudStack.converters.toBooleanText
                    },
                    limitcpuuse: {
                      label: 'label.CPU.cap',
                      converter: cloudStack.converters.toBooleanText
                    },
                    tags: { label: 'label.storage.tags' },
                    hosttags: { label: 'label.host.tags' },
                    domain: { label: 'label.domain' },
                    created: { label: 'label.created', converter: cloudStack.converters.toLocalDate }
                  }
                ],

                dataProvider: function(args) {
                  args.response.success(
                    {
                      actionFilter: serviceOfferingActionfilter,
                      data:args.context.serviceOfferings[0]
                    }
                  );
                }
              }
            }
          }
        }
      },

      systemServiceOfferings: {
        type: 'select',
        title: 'label.menu.system.service.offerings',
        listView: {
          id: 'systemServiceOfferings',
          label: 'label.menu.system.service.offerings',
          fields: {
            name: { 
						  label: 'label.name', 
							editable: true 
						},
            displaytext: { 
						  label: 'label.description' 
						}
          },

          reorder: cloudStack.api.actions.sort('updateServiceOffering', 'systemServiceOfferings'),

          actions: {
            add: {
              label: 'label.add.system.service.offering',

              messages: {
                confirm: function(args) {
                  return 'message.add.system.service.offering';
                },
                notification: function(args) {
                  return 'label.add.system.service.offering';
                }
              },

              createForm: {
                title: 'label.add.system.service.offering',
                fields: {
                  name: {
                    label: 'label.name',
                    validation: { required: true }
                  },
                  description: {
                    label: 'label.description',
                    validation: { required: true }
                  },
                  storageType: {
                    label: 'label.storage.type',
                    select: function(args) {
                      var items = [];
                      items.push({id: 'shared', description: 'shared'});
                      items.push({id: 'local', description: 'local'});
                      args.response.success({data: items});
                    }
                  },
                  cpuNumber: {
                    label: 'label.num.cpu.cores',
                    validation: {
                      required: true,
                      number: true
                    }
                  },
                  cpuSpeed: {
                    label: 'label.cpu.mhz',
                    validation: {
                      required: true,
                      number: true
                    }
                  },
                  memory: {
                    label: 'label.memory.mb',
                    validation: {
                      required: true,
                      number: true
                    }
                  },
                  networkRate: {
                    label: 'label.network.rate',
                    validation: {
                      required: false, //optional
                      number: true
                    }
                  },
                  offerHA: {
                    label: 'label.offer.ha',
                    isBoolean: true,
                    isChecked: false
                  },
                  storageTags: {
                    label: 'label.storage.tags'
                  },
                  hostTags: {
                    label: 'label.host.tags'
                  },
                  cpuCap: {
                    label: 'label.CPU.cap',
                    isBoolean: true,
                    isChecked: false
                  },
                  isPublic: {
                    label: 'label.public',
                    isBoolean: true,
                    isReverse: true,
                    isChecked: true
                  },
                  domainId: {
                    label: 'label.domain',
                    dependsOn: 'isPublic',
                    select: function(args) {										
                      $.ajax({
                        url: createURL("listDomains&listAll=true"),
                        dataType: "json",
                        async: false,
                        success: function(json) {
                          var items = [];
                          var domainObjs = json.listdomainsresponse.domain;
                          $(domainObjs).each(function(){
                            items.push({id: this.id, description: this.path});
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
                array1.push("&storageType=" + todb(args.data.storageType));
                array1.push("&cpuNumber=" + args.data.cpuNumber);
                array1.push("&cpuSpeed="+ args.data.cpuSpeed);
                array1.push("&memory=" + args.data.memory);

                if(args.data.networkRate != null && args.data.networkRate.length > 0)
                  array1.push("&networkrate=" + args.data.networkRate);

                array1.push("&offerha=" + (args.data.offerHA == "on"));

                if(args.data.storageTags != null && args.data.storageTags.length > 0)
                  array1.push("&tags=" + todb(args.data.storageTags));

                if(args.data.hostTags != null && args.data.hostTags.length > 0)
                  array1.push("&hosttags=" + todb(args.data.hostTags));

                array1.push("&limitcpuuse=" + (args.data.cpuCap == "on"));

                if(args.$form.find('.form-item[rel=domainId]').css("display") != "none")
                  array1.push("&domainid=" + args.data.domainId);

                $.ajax({
                  url: createURL("createServiceOffering&issystem=true"+array1.join("")),
                  dataType: "json",
                  async: true,
                  success: function(json) {
                    var item = json.createserviceofferingresponse.serviceoffering;
                    args.response.success({data: item});
                  },
                  error: function(data) {
                    args.response.error(parseXMLHttpResponse(data));
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
              url: createURL("listServiceOfferings&issystem=true&page=" + args.page + "&pagesize=" + pageSize  + array1.join("")),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listserviceofferingsresponse.serviceoffering;
                args.response.success({data:items});
              },
              error: function(data) {
                args.response.error(parseXMLHttpResponse(data));
              }
            });
          },

          detailView: {
            name: 'System service offering details',
            actions: {
              edit: {
                label: 'label.edit',
                action: function(args) {
                  var array1 = [];
                  array1.push("&name=" + todb(args.data.name));
                  array1.push("&displaytext=" + todb(args.data.displaytext));
                  $.ajax({
                    url: createURL("updateServiceOffering&id=" + args.context.systemServiceOfferings[0].id + array1.join("")),
                    dataType: "json",
                    success: function(json) {
                      var item = json.updateserviceofferingresponse.serviceoffering;
                      args.response.success({data: item});
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
                    }
                  });
                }
              },

              remove: {
                label: 'label.action.delete.system.service.offering',
                messages: {
                  confirm: function(args) {
                    return 'message.action.delete.system.service.offering';
                  },
                  notification: function(args) {
                    return 'label.action.delete.system.service.offering';
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("deleteServiceOffering&id=" + args.context.systemServiceOfferings[0].id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      args.response.success();
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
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

            tabs: {
              details: {
                title: 'label.details',

                fields: [
                  {
                    name: {
                      label: 'label.name',
                      isEditable: true
                    }
                  },
                  {
                    id: { label: 'label.id' },
                    displaytext: {
                      label: 'label.description',
                      isEditable: true
                    },
                    storagetype: { label: 'label.storage.type' },
                    cpunumber: { label: 'label.num.cpu.cores' },
                    cpuspeed: {
                      label: 'label.cpu.mhz',
                      converter: function(args) {
                        return cloudStack.converters.convertHz(args);
                      }
                    },
                    memory: {
                      label: 'label.memory.mb',
                      converter: function(args) {
                        return cloudStack.converters.convertBytes(args*1024*1024);
                      }
                    },
                    networkrate: { label: 'label.network.rate' },
                    offerha: {
                      label: 'label.offer.ha',
                      converter: cloudStack.converters.toBooleanText
                    },
                    limitcpuuse: {
                      label: 'label.CPU.cap',
                      converter: cloudStack.converters.toBooleanText
                    },
                    tags: { label: 'label.storage.tags' },
                    hosttags: { label: 'label.host.tags' },
                    domain: { label: 'label.domain' },
                    created: { label: 'label.created', converter: cloudStack.converters.toLocalDate }
                  }
                ],

                dataProvider: function(args) {
                  args.response.success(
                    {
                      actionFilter: systemServiceOfferingActionfilter,
                      data:args.context.systemServiceOfferings[0]
                    }
                  );
                }
              }
            }
          }
        }
      },

      diskOfferings: {
        type: 'select',
        title: 'label.menu.disk.offerings',
        listView: {
          id: 'diskOfferings',
          label: 'label.menu.disk.offerings',
          fields: {
            name: { label: 'label.name' },
            displaytext: { label: 'label.description' },
            iscustomized: {
              label: 'label.custom.disk.size',
              converter: cloudStack.converters.toBooleanText
            },
            disksize: {
              label: 'label.disk.size.gb',
              converter: function(args) {
                if(args != 0)
                  return args;
                else
                  return "N/A";
              }
            }
          },

          reorder: cloudStack.api.actions.sort('updateDiskOffering', 'diskOfferings'),

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
              url: createURL("listDiskOfferings&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listdiskofferingsresponse.diskoffering;
                args.response.success({data:items});
              },
              error: function(data) {
                args.response.error(parseXMLHttpResponse(data));
              }
            });
          },

          actions: {
            add: {
              label: 'label.add.disk.offering',

              messages: {
                confirm: function(args) {
                  return 'message.add.disk.offering';
                },
                notification: function(args) {
                  return 'label.add.disk.offering';
                }
              },

              createForm: {
                title: 'label.add.disk.offering',
                fields: {
                  name: {
                    label: 'label.name',
                    validation: { required: true }
                  },
                  description: {
                    label: 'label.description',
                    validation: { required: true }
                  },
                  isCustomized: {
                    label: 'label.custom.disk.size',
                    isBoolean: true,
                    isReverse: true,
                    isChecked: false
                  },
                  disksize: {
                    label: 'label.disk.size.gb',
                    dependsOn: 'isCustomized',
                    validation: { required: true, number: true }
                  },
                  tags: {
                    label: 'label.storage.tags'
                  },
                  isPublic: {
                    label: 'label.public',
                    isBoolean: true,
                    isReverse: true,
                    isChecked: true
                  },
                  domainId: {
                    label: 'label.domain',
                    dependsOn: 'isPublic',
                    select: function(args) {										 
                      $.ajax({
                        url: createURL("listDomains&listAll=true"),
                        dataType: "json",
                        async: false,
                        success: function(json) {
                          var items = [];
                          var domainObjs = json.listdomainsresponse.domain;
                          $(domainObjs).each(function(){
                            items.push({id: this.id, description: this.path});
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

                if(args.$form.find('.form-item[rel=domainId]').css("display") != "none")
                  array1.push("&domainid=" + args.data.domainId);

                $.ajax({
                  url: createURL("createDiskOffering&isMirrored=false" + array1.join("")),
                  dataType: "json",
                  async: true,
                  success: function(json) {
                    var item = json.creatediskofferingresponse.diskoffering;
                    args.response.success({data: item});
                  },
                  error: function(data) {
                    args.response.error(parseXMLHttpResponse(data));
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

          detailView: {
            name: 'Disk offering details',
            actions: {
              edit: {
                label: 'label.edit',
                action: function(args) {
                  var array1 = [];
                  array1.push("&name=" + todb(args.data.name));
                  array1.push("&displaytext=" + todb(args.data.displaytext));
                  $.ajax({
                    url: createURL("updateDiskOffering&id=" + args.context.diskOfferings[0].id + array1.join("")),
                    dataType: "json",
                    success: function(json) {
                      var item = json.updatediskofferingresponse.diskoffering;
                      args.response.success({data: item});
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
                    }
                  });
                }
              },

              remove: {
                label: 'label.action.delete.disk.offering',
                messages: {
                  confirm: function(args) {
                    return 'message.action.delete.disk.offering';
                  },
                  notification: function(args) {
                    return 'label.action.delete.disk.offering';
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("deleteDiskOffering&id=" + args.context.diskOfferings[0].id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      args.response.success();
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
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

            tabs: {
              details: {
                title: 'label.details',

                fields: [
                  {
                    name: {
                      label: 'label.name',
                      isEditable: true
                    }
                  },
                  {
                    id: { label: 'label.id' },
                    displaytext: {
                      label: 'label.description',
                      isEditable: true
                    },
                    iscustomized: {
                      label: 'label.custom.disk.size',
                      converter: cloudStack.converters.toBooleanText
                    },
                    disksize: {
                      label: 'label.disk.size.gb',
                      converter: function(args) {
                        if(args != 0)
                          return args;
                        else
                          return "N/A";
                      }
                    },
                    tags: { label: 'label.storage.tags' },
                    domain: { label: 'label.domain' }
                  }
                ],

                dataProvider: function(args) {
                  args.response.success(
                    {
                      actionFilter: diskOfferingActionfilter,
                      data:args.context.diskOfferings[0]
                    }
                  );
                }
              }
            }
          }
        }
      },      

      networkOfferings: {
        type: 'select',
        title: 'label.menu.network.offerings',
        listView: {
          id: 'networkOfferings',
          label: 'label.menu.network.offerings',
          fields: {
            name: { label: 'label.name' },
            state: {
              converter: function(str) {
                // For localization
                return str;
              },
              label: 'label.state', indicator: { 'Enabled': 'on', 'Disabled': 'off', 'Destroyed': 'off' }
            }
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
              url: createURL('listNetworkOfferings' + array1.join("")),
              data: {
                page: args.page,
                pagesize: pageSize
              },
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listnetworkofferingsresponse.networkoffering;
																
								$(items).each(function(){
								  if(this.availability == "Required") {
									  requiredNetworkOfferingExists = true;
										return false; //break each loop
									}
								});								
													
                args.response.success({
                  actionFilter: networkOfferingActionfilter,
                  data:items
                });
              },
              error: function(data) {
                args.response.error(parseXMLHttpResponse(data));
              }
            });
          },

          actions: {
            add: {
              label: 'label.add.network.offering',

							createForm: {
                title: 'label.add.network.offering',               														
								preFilter: function(args) {
                  var $availability = args.$form.find('.form-item[rel=availability]');
                  var $serviceOfferingId = args.$form.find('.form-item[rel=serviceOfferingId]');
                  var hasAdvancedZones = false;

                  // Check whether there are any advanced zones
                  $.ajax({
                    url: createURL('listZones'),
                    data: { listAll: true, networktype: 'advanced' },
                    async: false,
                    success: function(json) {
                      if (json.listzonesresponse.zone && json.listzonesresponse.zone.length) {
                        hasAdvancedZones = true;
                      }
                    }
                  });
									
                  args.$form.bind('change', function() { //when any field in the dialog is changed
									  //check whether to show or hide availability field
                    var $sourceNATField = args.$form.find('input[name=\"service.SourceNat.isEnabled\"]');
                    var $guestTypeField = args.$form.find('select[name=guestIpType]');
                    var $basicSharedFields = args.$form.find('.form-item').filter(function() {
                      var basicSharedFields = [
                        'service.SourceNat.isEnabled',
                        'service.StaticNat.isEnabled',
                        'service.PortForwarding.isEnabled',
                        'service.Lb.isEnabled'
                      ];

                      if ($.inArray($(this).attr('rel'), basicSharedFields) > -1) {
                        return true;
                      }

                      if ($.inArray($(this).attr('depends-on'), basicSharedFields) > -1) {
                        return true;
                      }

                      return false;
                    });

                    if (!requiredNetworkOfferingExists &&
                        $sourceNATField.is(':checked') &&
                        $guestTypeField.val() == 'Isolated') {
                      $availability.css('display', 'inline-block');
                    } else {
                      $availability.hide();
                    }

										//check whether to show or hide serviceOfferingId field										
                    var havingVirtualRouterForAtLeastOneService = false;									
										$(serviceCheckboxNames).each(function(){										  
											var checkboxName = this;                      								
											if($("input[name='" + checkboxName + "']").is(":checked") == true) {											  
											  var providerFieldName = checkboxName.replace(".isEnabled", ".provider"); //either dropdown or input hidden field
                        var providerName = $("[name='" + providerFieldName + "']").val(); 
												if(providerName == "VirtualRouter") {
												  havingVirtualRouterForAtLeastOneService = true;
													return false; //break each loop
												}
											}																					
										});
                    
                    if(havingVirtualRouterForAtLeastOneService == true)
                      $serviceOfferingId.css('display', 'inline-block');
                    else
                      $serviceOfferingId.hide();		

	                  $(':ui-dialog').dialog('option', 'position', 'center');

                    if (hasAdvancedZones && $guestTypeField.val() == 'Shared') {
                      $basicSharedFields.hide();
                      $basicSharedFields.find('input[type=checkbox]').attr('checked', false);
                    } else {
                      $basicSharedFields.each(function() {
                        var $field = $(this);
                        var $dependsOn = args.$form.find('.form-item').filter(function() {
                          return $(this).attr('rel') == $field.attr('depends-on');
                        });

                        if (!$field.attr('depends-on') ||
                            $dependsOn.find('input[type=checkbox]').is(':checked')) {
                          $field.css('display', 'inline-block');
                        }
                      });
                    }
                  });
									
									args.$form.change();
								},				
                fields: {
                  name: { label: 'label.name', validation: { required: true } },

                  displayText: { label: 'label.description', validation: { required: true } },

                  networkRate: { label: 'label.network.rate' },

                  trafficType: {
                    label: 'label.traffic.type', validation: { required: true },
                    select: function(args) {
                      args.response.success({
                        data: [
                          { id: 'GUEST', description: 'Guest' }
                        ]
                      });
                    }
                  },

                  guestIpType: {
                    label: 'label.guest.type',
                    select: function(args) {
                      args.response.success({
                        data: [
                          { id: 'Isolated', description: 'Isolated' },
                          { id: 'Shared', description: 'Shared' }
                        ]
                      });
																						
											args.$select.change(function() {											  
												var $form = $(this).closest("form");
                        
												if ($(this).val() == "Shared") {
                          $form.find('.form-item[rel=specifyVlan]').hide();
												} else {  //$(this).val() == "Isolated"   
												  $form.find('.form-item[rel=specifyVlan]').css('display', 'inline-block');
												}												
											});
                    }
                  },

                  specifyVlan: { label: 'label.specify.vlan', isBoolean: true },																
								
                  supportedServices: {
                    label: 'label.supported.services',

                    dynamic: function(args) {
                      $.ajax({
                        url: createURL('listSupportedNetworkServices'),
                        dataType: 'json',
                        async: true,
                        success: function(data) {
                          networkServiceObjs = data.listsupportednetworkservicesresponse.networkservice;
                          var fields = {}, providerCanenableindividualserviceMap = {}, providerServicesMap = {}, providerDropdownsForciblyChangedTogether = {};													
                          $(networkServiceObjs).each(function() {
                            var serviceName = this.name;
                            var providerObjs = this.provider;
                            var serviceDisplayName;

                            // Sanitize names
                            switch (serviceName) {
                            case 'Vpn': serviceDisplayName = 'VPN'; break;
                            case 'Dhcp': serviceDisplayName = dictionary['label.dhcp']; break;
                            case 'Dns': serviceDisplayName = 'DNS'; break;
                            case 'Lb': serviceDisplayName = 'Load Balancer'; break;
                            case 'SourceNat': serviceDisplayName = 'Source NAT'; break;
                            case 'StaticNat': serviceDisplayName = 'Static NAT'; break;
                            case 'PortForwarding': serviceDisplayName = 'Port Forwarding'; break;
                            case 'SecurityGroup': serviceDisplayName = 'Security Groups'; break;
                            case 'UserData': serviceDisplayName = 'User Data'; break;
                            default: serviceDisplayName = serviceName; break;
                            }

                            var id = {
                              isEnabled: 'service' + '.' + serviceName + '.' + 'isEnabled',
                              capabilities: 'service' + '.' + serviceName + '.' + 'capabilities',
                              provider: 'service' + '.' + serviceName + '.' + 'provider'
                            };
                            
														serviceCheckboxNames.push(id.isEnabled);														
														
                            fields[id.isEnabled] = { label: serviceDisplayName, isBoolean: true };
																												
														if(providerObjs != null && providerObjs.length > 1) {	//present provider dropdown when there are multiple providers for a service												
															fields[id.provider] = {
																label: serviceDisplayName + ' Provider',
																isHidden: true,
																dependsOn: id.isEnabled,
																select: function(args) {																
																	//Virtual Router needs to be the first choice in provider dropdown (Bug 12509)																	
																	var items = [];
																	$(providerObjs).each(function(){
																	  if(this.name == "VirtualRouter")
																		  items.unshift({id: this.name, description: this.name});
																		else
																		  items.push({id: this.name, description: this.name});
																																				
																		if(!(this.name in providerCanenableindividualserviceMap))
																		  providerCanenableindividualserviceMap[this.name] = this.canenableindividualservice;
																																				
                                    if(!(this.name in providerServicesMap))													
                                      providerServicesMap[this.name] = [serviceName];
                                    else																			
																		  providerServicesMap[this.name].push(serviceName);																		
																	});
																															
																	args.response.success({
																		data: items
																	});
																																																	
																	args.$select.change(function() {		
                                    var $thisProviderDropdown = $(this);																	
                                    var providerName = $(this).val();																	
																		var canenableindividualservice = providerCanenableindividualserviceMap[providerName];																	  
																		if(canenableindividualservice == false) { //This provider can NOT enable individual service, therefore, force all services supported by this provider have this provider selected in provider dropdown
																		  var serviceNames = providerServicesMap[providerName];			
																			if(serviceNames != null && serviceNames.length > 1) {			
                                        providerDropdownsForciblyChangedTogether = {};  //reset																			
																				$(serviceNames).each(function(){																			 
																					var providerDropdownId = 'service' + '.' + this + '.' + 'provider';		
                                          providerDropdownsForciblyChangedTogether[providerDropdownId] = 1;																					
																					$("select[name='" + providerDropdownId + "']").val(providerName);																				
																				});	
                                      }
																		}		
                                    else { //canenableindividualservice == true
																		  if($thisProviderDropdown.context.name in providerDropdownsForciblyChangedTogether) { //if this provider dropdown is one of provider dropdowns forcibly changed together earlier, make other forcibly changed provider dropdowns restore default option (i.e. 1st option in dropdown)
																			  for(var key in providerDropdownsForciblyChangedTogether) {																				  
																					if(key == $thisProviderDropdown.context.name)
																					  continue; //skip to next item in for loop
																					else 															
																						$("select[name='" + key + "'] option:first").attr("selected", "selected");																					
																				}																			 																																	
																				providerDropdownsForciblyChangedTogether = {};  //reset			
                                      }																				
																		}																		
																	});		
																}
															};
														}
														else if(providerObjs != null && providerObjs.length == 1){ //present hidden field when there is only one provider for a service		
														  fields[id.provider] = {
															  label: serviceDisplayName + ' Provider',
																isHidden: true,
																defaultValue: providerObjs[0].name
															};
														}														
                          });

                          args.response.success({
                            fields: fields
                          });
                        },
                        error: function(data) {
                          args.response.error(parseXMLHttpResponse(data));
                        }
                      });
                    }
                  },

									//show or hide upon checked services and selected providers above (begin)
                  serviceOfferingId: {
                    label: 'label.compute.offering',
                    select: function(args) {
                      $.ajax({
                        url: createURL('listServiceOfferings&issystem=true'),
                        dataType: 'json',
                        async: true,
                        success: function(data) {
                          var serviceOfferings = data.listserviceofferingsresponse.serviceoffering;

                          args.response.success({
                            data: $.merge(
                              [{
                                id: null,
                                description: 'None'
                              }],
                              $.map(serviceOfferings, function(elem) {
                                return {
                                  id: elem.id,
                                  description: elem.name
                                };
                              })
                            )
                          });
                        },
                        error: function(data) {
                          args.response.error(parseXMLHttpResponse(data));
                        }
                      });
                    }
                  },
									
                  "service.SourceNat.redundantRouterCapabilityCheckbox" : {
                    label: "label.redundant.router.capability",
                    isHidden: true,
                    dependsOn: 'service.SourceNat.isEnabled',
                    isBoolean: true
                  },

                  "service.SourceNat.sourceNatTypeDropdown": {
                    label: 'label.supported.source.NAT.type',
                    isHidden: true,
                    dependsOn: 'service.SourceNat.isEnabled',
                    select: function(args) {
                      args.response.success({
                        data: [
                          { id: 'peraccount', description: 'Per account'},
                          { id: 'perzone', description: 'Per zone'},
                        ]
                      });
                    }
                  },
									"service.Lb.elasticLbCheckbox" : {
                    label: "label.elastic.LB",
                    isHidden: true,
                    dependsOn: 'service.Lb.isEnabled',
                    isBoolean: true
                  },
                  "service.Lb.lbIsolationDropdown": {
                    label: 'label.LB.isolation',
                    isHidden: true,
                    dependsOn: 'service.Lb.isEnabled',
                    select: function(args) {
                      args.response.success({
                        data: [
                          { id: 'dedicated', description: 'Dedicated' },
                          { id: 'shared', description: 'Shared' }
                        ]
                      })
                    }
                  },									
									"service.StaticNat.elasticIpCheckbox" : {
										label: "label.elastic.IP",
										isHidden: true,
										dependsOn: 'service.StaticNat.isEnabled',
										isBoolean: true
									},	
                  //show or hide upon checked services and selected providers above (end)
									
									
									conservemode: { label: 'label.conserve.mode', isBoolean: true },
									
                  tags: { label: 'label.tags' },
									
									availability: {
                    label: 'label.availability',
                    isHidden: true,  
                    select: function(args) {
                      args.response.success({
                        data: [
                          { id: 'Optional', description: 'Optional' },
                          { id: 'Required', description: 'Required' }                          
                        ]
                      });
                    }
                  }
                }
              },
							
              action: function(args) {
                var formData = args.data;
                var inputData = {};
                var serviceProviderMap = {};
                var serviceCapabilityIndex = 0;
								
                $.each(formData, function(key, value) {
                  var serviceData = key.split('.');

                  if (serviceData.length > 1) {
                    if (serviceData[0] == 'service' &&
                        serviceData[2] == 'isEnabled' &&
                        value == 'on') { // Services field

                      serviceProviderMap[serviceData[1]] = formData[
                        'service.' + serviceData[1] + '.provider'
                      ];
                    } 	                   							
										else if((key == 'service.SourceNat.redundantRouterCapabilityCheckbox') && ("SourceNat" in serviceProviderMap)) { //if checkbox is unchecked, it won't be included in formData in the first place. i.e. it won't fall into this section
										  inputData['serviceCapabilityList[' + serviceCapabilityIndex + '].service'] = 'SourceNat';
											inputData['serviceCapabilityList[' + serviceCapabilityIndex + '].capabilitytype'] = "RedundantRouter";
											inputData['serviceCapabilityList[' + serviceCapabilityIndex + '].capabilityvalue'] = true; //because this checkbox's value == "on"
										  serviceCapabilityIndex++;
										}		
										else if ((key == 'service.SourceNat.sourceNatTypeDropdown') && ("SourceNat" in serviceProviderMap)) {											
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].service'] = 'SourceNat';
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].capabilitytype'] = 'SupportedSourceNatTypes';
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].capabilityvalue'] = value;
										  serviceCapabilityIndex++;
										} 
                    else if ((key == 'service.Lb.elasticLbCheckbox') && ("Lb" in serviceProviderMap)) {	//if checkbox is unchecked, it won't be included in formData in the first place. i.e. it won't fall into this section								
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].service'] = 'lb';
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].capabilitytype'] = 'ElasticLb'; 
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].capabilityvalue'] = true; //because this checkbox's value == "on"
											serviceCapabilityIndex++;
										} 										
										else if ((key == 'service.Lb.lbIsolationDropdown') && ("Lb" in serviceProviderMap)) {											
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].service'] = 'lb';
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].capabilitytype'] = 'SupportedLbIsolation';
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].capabilityvalue'] = value;
											serviceCapabilityIndex++;
										} 
                    else if ((key == 'service.StaticNat.elasticIpCheckbox') && ("StaticNat" in serviceProviderMap)) {	//if checkbox is unchecked, it won't be included in formData in the first place. i.e. it won't fall into this section								
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].service'] = 'StaticNat';
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].capabilitytype'] = 'ElasticIp'; 
											inputData['servicecapabilitylist[' + serviceCapabilityIndex + '].capabilityvalue'] = true; //because this checkbox's value == "on"
											serviceCapabilityIndex++;
										} 										
                  } 									
									else if (value != '') { // Normal data
                    inputData[key] = value;
                  }
                });

                // Make supported services list
                inputData['supportedServices'] = $.map(serviceProviderMap, function(value, key) {
                  return key;
                }).join(',');

								
								if(inputData['guestIpType'] == "Shared"){ //specifyVlan checkbox is hidden
								  inputData['specifyVlan'] = true;
									inputData['specifyIpRanges'] = true;
								}
								else if (inputData['guestIpType'] == "Isolated") { //specifyVlan checkbox is shown
									if (inputData['specifyVlan'] == 'on') { //specifyVlan checkbox is checked
										inputData['specifyVlan'] = true;	
                    inputData['specifyIpRanges'] = true;										
									}
									else { //specifyVlan checkbox is unchecked
										inputData['specifyVlan'] = false;
										inputData['specifyIpRanges'] = false;
									}					
								}			
								
																
								if (inputData['conservemode'] == 'on') {
                  inputData['conservemode'] = true;
                } else {
                  inputData['conservemode'] = false;
                }
								
                // Make service provider map
                var serviceProviderIndex = 0;
                $.each(serviceProviderMap, function(key, value) {
                  inputData['serviceProviderList[' + serviceProviderIndex + '].service'] = key;
                  inputData['serviceProviderList[' + serviceProviderIndex + '].provider'] = value;
                  serviceProviderIndex++;
                });      
												
								if(args.$form.find('.form-item[rel=availability]').css("display") == "none")
                  inputData['availability'] = 'Optional';		
								
                if(args.$form.find('.form-item[rel=serviceOfferingId]').css("display") == "none")									
									delete inputData.serviceOfferingId;
																
                $.ajax({
                  url: createURL('createNetworkOffering'),
                  data: inputData,
                  dataType: 'json',
                  async: true,
                  success: function(data) {
									  var item = data.createnetworkofferingresponse.networkoffering;
								
										if(inputData['availability'] == "Required")
										  requiredNetworkOfferingExists = true;
																			
                    args.response.success({
                      data: item,
                      actionFilter: networkOfferingActionfilter
                    });
                  },

                  error: function(data) {
                    args.response.error(parseXMLHttpResponse(data));
                  }
                });
              },							             
							
              notification: {
                poll: function(args) {
                  args.complete({
                    actionFilter: networkOfferingActionfilter
                  });
                }
              },
							
              messages: {
                notification: function(args) {
                  return 'Added network offering';
                }
              }
            }
          },

          reorder: cloudStack.api.actions.sort('updateNetworkOffering', 'networkOfferings'),

          detailView: {
            name: 'Network offering details',
            actions: {						
							edit: {
                label: 'label.edit',
                action: function(args) {
                  var array1 = [];
                  array1.push("&name=" + todb(args.data.name));
                  array1.push("&displaytext=" + todb(args.data.displaytext));
									array1.push("&availability=" + args.data.availability);								
                  $.ajax({
                    url: createURL("updateNetworkOffering&id=" + args.context.networkOfferings[0].id + array1.join("")),
                    dataType: "json",
                    success: function(json) {										 									
											//if availability is being updated from Required to Optional
										  if(args.context.networkOfferings[0].availability == "Required" && args.data.availability == "Optional") 
										    requiredNetworkOfferingExists = false;											
											//if availability is being updated from Optional to Required
										  if(args.context.networkOfferings[0].availability == "Optional" && args.data.availability == "Required") 
										    requiredNetworkOfferingExists = true;
																							
                      var item = json.updatenetworkofferingresponse.networkoffering;											
                      args.response.success({data: item});
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
                    }
                  });
                }
              },
												
              enable: {
                label: 'Enable network offering',
                messages: {
                  confirm: function(args) {
                    return 'Are you sure you want to enable this network offering?';
                  },
                  notification: function(args) {
                    return 'Enabling network offering';
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("updateNetworkOffering&id=" + args.context.networkOfferings[0].id + "&state=Enabled"),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var item = json.updatenetworkofferingresponse.networkoffering;
                      args.response.success();
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
                    }
                  });
                },
                notification: {
                  poll: function(args) {
                    args.complete({
                      data: { state: 'Enabled' }
                    });
                  }
                }
              },

              disable: {
                label: 'Disable network offering',
                messages: {
                  confirm: function(args) {
                    return 'Are you sure you want to disable this network offering?';
                  },
                  notification: function(args) {
                    return 'Disabling network offering';
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("updateNetworkOffering&id=" + args.context.networkOfferings[0].id + "&state=Disabled"),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var item = json.updatenetworkofferingresponse.networkoffering;
                      args.response.success();
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
                    }
                  });
                },
                notification: {
                  poll: function(args) {
                    args.complete({ data: { state: 'Disabled' }});
                  }
                }
              },

              remove: {
                label: 'Remove network offering',
                action: function(args) {
                  $.ajax({
                    url: createURL('deleteNetworkOffering'),
                    data: {
                      id: args.context.networkOfferings[0].id
                    },
                    success: function(json) {			
											if(args.context.networkOfferings[0].availability == "Required") 
												requiredNetworkOfferingExists = false; //since only one or zero Required network offering can exist
																				
                      args.response.success();
                    },
                    error: function(data) {
                      args.response.error(parseXMLHttpResponse(data));
                    }
                  });
                },
                messages: {
                  confirm: function() { return 'Are you sure you want to remove this network offering?'; },
                  notification: function() { return 'Remove network offering'; }
                },
                notification: {
                  poll: function(args) {
                    args.complete({
                      data: {
                        state: 'Destroyed'
                      },
                      actionFilter: networkOfferingActionfilter
                    });
                  }
                }
              }
            },
            tabs: {
              details: {
                title: 'label.details',

                fields: [
                  {
                    name: {
                      label: 'label.name',
                      isEditable: true
                    }
                  },
                  {
                    id: { label: 'label.id' },
                    displaytext: {
                      label: 'label.description',
                      isEditable: true
                    },
                    state: { label: 'label.state' },
                    guestiptype: {
                      label: 'label.guest.type'
                    },
                    availability: {
                      label: 'label.availability',
                      isEditable: true,
                      select: function(args) {
                        var items = [];
                        items.push({id: 'Required', description: 'Required'});
                        items.push({id: 'Optional', description: 'Optional'});
                        //items.push({id: 'Unavailable', description: 'Unavailable'});
                        args.response.success({data: items});
                      }
                    },
                    isdefault: { //created by system by default
                      label: 'label.created.by.system',
                      converter: cloudStack.converters.toBooleanText
                    },
                    specifyvlan: {
                      label: 'label.specify.vlan',
                      converter: cloudStack.converters.toBooleanText
                    },
										specifyipranges: { 
										  label: 'label.specify.IP.ranges', 
											converter: cloudStack.converters.toBooleanText
										},
										conservemode: {
                      label: 'label.conserve.mode',
                      converter: cloudStack.converters.toBooleanText
                    },
                    networkrate: {
                      label: 'label.network.rate',
                      converter: function(args) {
                        var networkRate = args;
                        if (args == null || args == -1) {
                          return "Unlimited";
                        }
                        else {
                          return fromdb(args) + " Mb/s";

                        }
                      }
                    },
                    traffictype: {
                      label: 'label.traffic.type'
                    },
                    supportedServices: {
                      label: 'label.supported.services'
                    },
                    serviceCapabilities: {
                      label: 'label.service.capabilities'
                    }
                  }
                ],

                dataProvider: function(args) {
                  var networkOffering = args.context.networkOfferings[0];

                  args.response.success({
                    actionFilter: networkOfferingActionfilter,
                    data: $.extend(args.context.networkOfferings[0], {
                      supportedServices: $.map(networkOffering.service, function(service) {
                        return service.name;
                      }).join(', '),

                      serviceCapabilities: $.map(networkOffering.service, function(service) {
                        return service.capability ? $.map(service.capability, function(capability) {
                          return capability.name + ': ' + capability.value;
                        }).join(', ') : null;
                      }).join(', ')
                    })
                  });
                }
              }
            }
          }
        }
      }
    }
  };

  var serviceOfferingActionfilter = function(args) {
    var jsonObj = args.context.item;
    var allowedActions = [];
    allowedActions.push("edit");
    allowedActions.push("remove");
    return allowedActions;
  };

  var systemServiceOfferingActionfilter = function(args) {
    var jsonObj = args.context.item;
    var allowedActions = [];
    allowedActions.push("edit");
    allowedActions.push("remove");
    return allowedActions;
  };

  var diskOfferingActionfilter = function(args) {
    var jsonObj = args.context.item;
    var allowedActions = [];
    allowedActions.push("edit");
    allowedActions.push("remove");
    return allowedActions;
  };

  var networkOfferingActionfilter = function(args) {
    var jsonObj = args.context.item;

    if (jsonObj.state == 'Destroyed') 
      return [];
    
    var allowedActions = [];
    allowedActions.push("edit");	

    if(jsonObj.state == "Enabled")
			allowedActions.push("disable");
		else if(jsonObj.state == "Disabled")
			allowedActions.push("enable");
		
		if(jsonObj.isdefault == false) 
			allowedActions.push("remove");		
			
    return allowedActions;		
  };

})(cloudStack, jQuery);

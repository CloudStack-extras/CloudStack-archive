(function($, cloudStack) {
  // Admin dashboard
  cloudStack.sections.dashboard = {
    title: 'label.menu.dashboard',
    show: cloudStack.uiCustom.dashboard,

    adminCheck: function(args) {
      return isAdmin() ? true : false;
    },

    // User dashboard
    user: {
      dataProvider: function(args) {
        var dataFns = {
          instances: function(data) {
            $.ajax({
              url: createURL('listVirtualMachines'),
              success: function(json) {
                var instances = json.listvirtualmachinesresponse.virtualmachine ?
                  json.listvirtualmachinesresponse.virtualmachine : [];

                dataFns.account($.extend(data, {
                  runningInstances: $.grep(instances, function(instance) {
                    return instance.state == 'Running';
                  }).length,
                  stoppedInstances: $.grep(instances, function(instance) {
                    return instance.state == 'Stopped';
                  }).length,
                  totalInstances: instances.length
                }));
              }
            });
          },

          account: function(data) {
            var user = cloudStack.context.users[0];
            dataFns.events($.extend(data, {
              accountID: user.userid,
              accountName: user.account,
              userName: user.username,
              accountType: cloudStack.converters.toRole(user.type),
              accountDomainID: user.domainid
            }));
          },

          events: function(data) {
            $.ajax({
              url: createURL('listEvents'),
              data: {
                listAll: true,
                page: 1,
                pageSize: 4
              },
              success: function(json) {
                dataFns.ipAddresses($.extend(data, {
                  events: json.listeventsresponse.event ?
                    json.listeventsresponse.event : []
                }));
              }
            });
          },

          ipAddresses: function(data) {
            $.ajax({
              url: createURL('listNetworks'),
              data: {
                listAll: true,
                type: 'isolated',
                supportedServices: 'SourceNat'
              },
              success: function(json) {
                var netTotal = json.listnetworksresponse.count ?
                  json.listnetworksresponse.count : 0;

                 $.ajax({
                  url: createURL('listPublicIpAddresses'),
                  success: function(json) {
                    var ipTotal = json.listpublicipaddressesresponse.count ?
                      json.listpublicipaddressesresponse.count : 0;

                    complete($.extend(data, {
                      netTotal: netTotal,
                      ipTotal: ipTotal
                    }));
                  }
                });                
              }
            });
          }
        };

        var complete = function(data) {
          args.response.success({
            data: data
          });
        };

        dataFns.instances({});
      }
    },

    // Admin dashboard
    admin: {
      zoneDetailView: {
        tabs: {
          resources: {
            title: 'label.resources',
            custom: cloudStack.uiCustom.systemChart('resources')
          }
        }
      },
      
      dataProvider: function(args) {
        var dataFns = {
          zones: function(data) {
            $.ajax({
              url: createURL('listZones'),
              success: function(json) {
                dataFns.capacity({
                  zones: json.listzonesresponse.zone
                });
              }
            });
          },
          capacity: function(data) {
            if (data.zones) {
              $.ajax({
                url: createURL('listCapacity'),
                success: function(json) {
                  var capacities = json.listcapacityresponse.capacity;

                  var capacity = function(id, converter) {
                    var result = $.grep(capacities, function(capacity) {
                      return capacity.type == id;
                    });
                    return result[0] ? result[0] : {
                      capacityused: 0,
                      capacitytotal: 0,
                      percentused: 0
                    };
                  };

                  dataFns.alerts($.extend(data, {
                    publicIPAllocated: capacity(8).capacityused,
                    publicIPTotal: capacity(8).capacitytotal,
                    publicIPPercentage: parseInt(capacity(8).percentused),
                    privateIPAllocated: capacity(5).capacityused,
                    privateIPTotal: capacity(5).capacitytotal,
                    privateIPPercentage: parseInt(capacity(8).percentused),
                    memoryAllocated: cloudStack.converters.convertBytes(capacity(0).capacityused),
                    memoryTotal: cloudStack.converters.convertBytes(capacity(0).capacitytotal),
                    memoryPercentage: parseInt(capacity(0).percentused),
                    cpuAllocated: cloudStack.converters.convertHz(capacity(1).capacityused),
                    cpuTotal: cloudStack.converters.convertHz(capacity(1).capacitytotal),
                    cpuPercentage: parseInt(capacity(1).percentused)
                  }));
                }
              });
            } else {
              dataFns.alerts($.extend(data, {
                publicIPAllocated: 0,
                publicIPTotal: 0,
                publicIPPercentage: 0,
                privateIPAllocated: 0,
                privateIPTotal: 0,
                privateIPPercentage: 0,
                memoryAllocated: 0,
                memoryTotal: 0,
                memoryPercentage: 0,
                cpuAllocated: 0,
                cpuTotal: 0,
                cpuPercentage: 0
              }));
            }
          },

          alerts: function(data) {
            $.ajax({
              url: createURL('listAlerts'),
              data: {
                page: 1,
                pageSize: 4
              },
              success: function(json) {
                var alerts = json.listalertsresponse.alert ?
                  json.listalertsresponse.alert : [];

                dataFns.hostAlerts($.extend(data, {
                  alerts: $.map(alerts, function(alert) {
                    return {
                      name: cloudStack.converters.toAlertType(alert.type),
                      description: alert.description
                    };
                  })
                }));
              }
            });
          },

          hostAlerts: function(data) {
            $.ajax({
              url: createURL('listHosts'),
              data: {
                state: 'Alert',
                page: 1,
                pageSize: 4
              },
              success: function(json) {
                var hosts = json.listhostsresponse.host ?
                  json.listhostsresponse.host : [];

                dataFns.zoneCapacity($.extend(data, {
                  hostAlerts: $.map(hosts, function(host) {
                    return {
                      name: host.name,
                      description: 'message.alert.state.detected'
                    };
                  })
                }));
              }
            });
          },

          zoneCapacity: function(data) {
            $.ajax({
              url: createURL('listCapacity'),
              data: {
                sortBy: 'usage',
                page: 0,
                pagesize: 8
              },
              success: function(json) {
                var capacities = json.listcapacityresponse.capacity ?
                  json.listcapacityresponse.capacity : [];

                complete($.extend(data, {
                  zoneCapacities: $.map(capacities, function(capacity) {
                    if (capacity.podname) {
                      capacity.zonename = capacity.zonename.concat('<br/>' + _l('label.pod') + ': ' + capacity.podname);
                    }

                    if (capacity.clustername) {
                      capacity.zonename = capacity.zonename.concat('<br/>' + _l('label.cluster') + ': ' + capacity.clustername);
                    }

                    capacity.zonename.replace('Zone:', _l('label.zone') + ':');

                    return {
                      zoneID: capacity.zoneid, // Temporary fix for dashboard
                      zoneName: capacity.zonename,
                      type: cloudStack.converters.toAlertType(capacity.type),
                      percent: parseInt(capacity.percentused),
                      used: cloudStack.converters.convertByType(capacity.type, capacity.capacityused),
                      total: cloudStack.converters.convertByType(capacity.type, capacity.capacitytotal)
                    };
                  })
                }));
              }
            });
          }
        };

        var complete = function(data) {
          args.response.success({
            data: data
          });
        };

        dataFns.zones({});
      }
    }
  };
})(jQuery, cloudStack);

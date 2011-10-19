(function(cloudStack, testData) {
  cloudStack.sections.network = {
    title: 'Network',
    id: 'network',
    sectionSelect: {
      label: 'Select view'
    },
    sections: {
      ipAddresses: {
        type: 'select',
        title: 'IP Addresses',
        listView: {
          id: 'ipAddresses',
          label: 'IPs',
          filters: {
            allocated: { label: 'Allocated ' },
            mine: { label: 'My network' }
          },
          fields: {
            ipaddress: { label: 'IP' },
            zonename: { label: 'Zone' },
            vlanname: { label: 'VLAN' },
            networkid: { label: 'Network Type' },
            state: { label: 'State', indicator: { 'Allocated': 'on' } }
          },

          actions: {
            add: {
              label: 'Acquire new IP',

              action: function(args) {
                args.response.success();
              },

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add this new IP?';
                },
                notification: function(args) {
                  return 'Allocated IP';
                }
              },

              createForm: {
                title: 'Acquire new IP',
                desc: 'Please select a zone from which you want to acquire your new IP from.',
                fields: {
                  zonename: {
                    label: 'Zone',
                    select: function(args) {
                      setTimeout(function() {
                        args.response.success({
                          data: [
                            { id: 'San Jose', description: 'San Jose' },
                            { id: 'Chicago', description: 'Chicago' }
                          ]
                        });
                      }, 10);
                    }
                  }
                }
              },

              notification: {
                poll: testData.notifications.customPoll(testData.data.network[0])
              }
            },
            stop: {
              label: 'Disable static NAT',
              action: function(args) {
                setTimeout(function() {
                  args.response.success({
                    data: {
                      state: 'Disabling'                      
                    }
                  });
                }, 500);
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to disable ' + args.name + '?';
                },
                notification: function(args) {
                  return 'Disabled NAT: ' + args.name;
                }
              },
              notification: {
                poll: testData.notifications.customPoll({
                  state: 'Disabled'
                }, function() { return []; })
              }
            }
          },
          dataProvider: testData.dataProvider.listView('network'),

          // Detail view
          detailView: {
            name: 'IP address detail',
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    ipaddress: { label: 'IP' }
                  },
                  {
                    state: { label: 'State' },
                    zonename: { label: 'Zone' },
                    vlanname: { label: 'VLAN' },
                    issourcenat: { label: 'Source NAT' }
                  }
                ],
                dataProvider: testData.dataProvider.detailView('network')
              },

              portForwarding: {
                title: 'Port forwarding',
                custom: cloudStack.portForwarding({
                  dataProvider: function(args) {
                    setTimeout(function() {
                      args.response.success({
                        data: [
                          {
                            'start-port': 1,
                            'end-port': 80,
                            'protocol': 'TCP',
                            'add-vm': 'vm1232'
                          },
                          {
                            'start-port': 90,
                            'end-port': 120,
                            'protocol': 'TCP',
                            'add-vm': 'brianvm121'
                          }
                        ]
                      });                      
                    }, 100);
                  }
                })
              },

              loadBalancing: {
                title: 'Load Balancing',
                custom: cloudStack.portForwarding({
                  type: 'multiple',
                  dataProvider: function(args) {
                    setTimeout(function() {
                      args.response.success({
                        data: [
                          {
                            'start-port': 1,
                            'end-port': 80,
                            'protocol': 'TCP',
                            'add-vm': '4 VMs'
                          },
                          {
                            'start-port': 90,
                            'end-port': 120,
                            'protocol': 'TCP',
                            'add-vm': '2 VMs'
                          }
                        ]
                      });                      
                    }, 100);
                  }
                })
              }
            }
          }
        }
      },
      securityGroups: {
        type: 'select',
        title: 'Security Groups',
        listView: {
          id: 'securityGroups',
          label: 'Security Groups',
          fields: {
            name: { label: 'Name', editable: true },
            description: { label: 'Description' },
            domain: { label: 'Domain' },
            account: { label: 'Account' }
          },
          actions: {
            add: {
              label: 'Add security group',

              action: function(args) {
                args.response.success();
              },

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add ' + args.name + '?';
                },
                notification: function(args) {
                  return 'Created security group';
                }
              },

              createForm: {
                title: 'New security group',
                desc: 'Please name your security group.',
                fields: {
                  name: { label: 'Name' },
                  description: { label: 'Description' }
                }
              },

              notification: {
                poll: testData.notifications.testPoll
              }
            },
            destroy: {
              label: 'Delete security group',
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to delete ' + args.name + '?';
                },
                notification: function(args) {
                  return 'Deleted security group: ' + args.name;
                }
              },
              action: function(args) {
                setTimeout(function() {
                  args.response.success();
                }, 200);
              },
              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          dataProvider: testData.dataProvider.listView('securityGroups'),
          detailView: {
            name: 'Security group details',
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    name: { label: 'Name' }
                  },
                  {
                    domain: { label: 'Domain' },
                    account: { label: 'Account' }
                  }
                ],
                dataProvider: testData.dataProvider.detailView('securityGroups')
              },
              ingressRules: {
                title: 'Ingress Rules',
                multiEdit: true,
                fields: {
                  protocol: {
                    label: 'Protocol',
                    editable: true,
                    select: function(args) {
                      setTimeout(function() {
                        args.response.success({
                          data: [
                            { id: 'tcp', label: 'TCP' },
                            { id: 'udp', label: 'UDP' }
                          ]
                        });
                      }, 100);
                    }
                  },
                  startport: { label: 'Start Port', editable: true },
                  endport: { label: 'End Port', editable: true },
                  cidr: { label: 'CIDR', editable: true }
                },
                actions: {
                  create: {
                    label: 'Add ingress rule',
                    messages: {
                      confirm: function(args) {
                        return 'Are you sure you want to add this port range?';
                      },
                      notification: function(args) {
                        return 'Added port range';
                      }
                    },
                    notification: {
                      poll: testData.notifications.testPoll
                    },
                    action: function(args) {
                      setTimeout(function() {
                        args.response.success();
                      }, 500);
                    }
                  },
                  destroy: {
                    label: 'Remove rule',
                    messages: {
                      confirm: function(args) {
                        return 'Are you sure you want to remove this ingress rule?';
                      },
                      notification: function(args) {
                        return 'Removed ingress rule: ' + args.name;
                      }
                    },
                    notification: {
                      poll: testData.notifications.testPoll
                    },
                    action: function(args) {
                      setTimeout(function() {
                        args.response.success();
                      }, 500);
                    }
                  }
                },
                dataProvider: function(args) {
                  setTimeout(function() {
                    args.response.success({
                      data: [
                        {
                          "ruleid": 2,
                          "protocol": "tcp",
                          "startport": 22,
                          "endport": 22,
                          "cidr": "0.0.0.0/0"
                        },
                        {
                          "ruleid": 3,
                          "protocol": "icmp",
                          "startport": 80,
                          "endport": 90,
                          "cidr": "0.0.0.0/0"
                        }
                      ]
                    });
                  });
                }
              }
            }
          }
        }
      }
    }
  };
})(cloudStack, testData);

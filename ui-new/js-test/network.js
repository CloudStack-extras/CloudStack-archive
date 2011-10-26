(function(cloudStack, testData, $) {
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
            ipaddress: {
              label: 'IP',
              converter: function(text, item) {
                if (item.issourcenat) {
                  return text + ' [Source NAT]';
                }

                return text;
              }
            },
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
            enableStaticNAT: {
              label: 'Enable static NAT',
              action: {
                noAdd: true,
                custom: cloudStack.uiCustom.enableStaticNAT({
                  listView: cloudStack.sections.instances,
                  action: function(args) {
                    args.response.success();
                  }
                })
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to enable static NAT?';
                },
                notification: function(args) {
                  return 'Enabled Static NAT';
                }
              },
              notification: {
                poll: testData.notifications.customPoll({ isstaticnat: true })
              }
            },
            disableStaticNAT: {
              label: 'Disable static NAT',
              action: function(args) {
                args.response.success();
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to disable static NAT?';
                },
                notification: function(args) {
                  return 'Disabled Static NAT';
                }
              },
              notification: {
                poll: testData.notifications.customPoll({ isstaticnat: false })
              }
            },
            enableVPN: {
              label: 'Enable VPN',
              action: function(args) {
                args.response.success();
              },
              messages: {
                confirm: function(args) {
                  return 'Please confirm that you want VPN enabled for this IP address.';
                },
                notification: function(args) {
                  return 'Enabled VPN';
                },
                complete: function(args) {
                  return 'VPN is now enabled for IP ' + args.publicip + '.'
                    + '<br/>Your IPsec pre-shared key is:<br/>' + args.presharedkey;
                }
              },
              notification: {
                poll: testData.notifications.customPoll({
                  publicip: '10.2.2.1',
                  presharedkey: '23fudh881ssx88199488PP!#Dwdw',
                  vpnenabled: true
                })
              }
            },
            disableVPN: {
              label: 'Disable VPN',
              action: function(args) {
                args.response.success();
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to disable VPN?';
                },
                notification: function(args) {
                  return 'Disabled VPN';
                }
              },
              notification: {
                poll: testData.notifications.customPoll({ vpnenabled: false })
              }
            }
          },
          dataProvider: testData.dataProvider.listView('network'),

          // Detail view
          detailView: {
            name: 'IP address detail',
            // Example tab filter
            tabFilter: function(args) {
              var disabledTabs = [];
              var ipAddress = args.context.ipAddresses[0];

              if (!ipAddress.issourcenat ||
                  (ipAddress.issourcenat && !ipAddress.vpnenabled)) {
                disabledTabs.push('vpn');
              }

              return disabledTabs;
            },
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

              ipRules: {
                title: 'Configuration',
                custom: cloudStack.ipRules({
                  preFilter: function(args) {
                    if (args.context.ipAddresses[0].isstaticnat) {
                      return args.items; // All items filtered means static NAT
                    }

                    return [];
                  },

                  // Firewall rules
                  firewall: {
                    noSelect: true,
                    fields: {
                      'cidrlist': { edit: true, label: 'Source CIDR' },
                      'protocol': {
                        label: 'Protocol',
                        select: function(args) {
                          args.$select.change(function() {
                            var $inputs = args.$form.find('input');
                            var $icmpFields = $inputs.filter(function() {
                              var name = $(this).attr('name');

                              return $.inArray(name, [
                                'icmptype',
                                'icmpcode'
                              ]) > -1;
                            });
                            var $otherFields = $inputs.filter(function() {
                              var name = $(this).attr('name');

                              return name != 'icmptype' && name != 'icmpcode' && name != 'cidrlist';
                            });

                            if ($(this).val() == 'icmp') {
                              $icmpFields.attr('disabled', false);
                              $otherFields.attr('disabled', 'disabled');
                            } else {
                              $otherFields.attr('disabled', false);
                              $icmpFields.attr('disabled', 'disabled');
                            }
                          });

                          args.response.success({
                            data: [
                              { name: 'tcp', description: 'TCP' },
                              { name: 'udp', description: 'UDP' },
                              { name: 'icmp', description: 'ICMP' }
                            ]
                          });
                        }
                      },
                      'startport': { edit: true, label: 'Start Port' },
                      'endport': { edit: true, label: 'End Port' },
                      'icmptype': { edit: true, label: 'ICMP Type', isDisabled: true },
                      'icmpcode': { edit: true, label: 'ICMP Code', isDisabled: true },
                      'add-rule': {
                        label: 'Add Rule',
                        addButton: true
                      }
                    },
                    add: {
                      label: 'Add',
                      action: function(args) {
                        setTimeout(function() {
                          args.response.success({
                            notification: {
                              label: 'Add firewall rule',
                              poll: testData.notifications.testPoll
                            }
                          });
                        }, 500);
                      }
                    },
                    actions: {
                      destroy: {
                        label: 'Remove Rule',
                        action: function(args) {
                          setTimeout(function() {
                            args.response.success({
                              notification: {
                                label: 'Remove firewall rule',
                                poll: testData.notifications.testPoll
                              }
                            });
                          }, 500);
                        }
                      }
                    },
                    dataProvider: function(args) {
                      setTimeout(function() {
                        args.response.success({
                          data: [
                            {
                              "id": 11,
                              "protocol": "icmp",
                              "ipaddressid": 4,
                              "ipaddress": "10.223.71.23",
                              "state": "Active",
                              "cidrlist": "0.0.0.0/0",
                              "icmptype": 2,
                              "icmpcode": 22
                            },
                            {
                              "id": 10,
                              "protocol": "udp",
                              "startport": "500",
                              "endport": "10000",
                              "ipaddressid": 4,
                              "ipaddress": "10.223.71.23",
                              "state": "Active",
                              "cidrlist": "0.0.0.0/24"
                            },
                            {
                              "id": 9,
                              "protocol": "tcp",
                              "startport": "20",
                              "endport": "200",
                              "ipaddressid": 4,
                              "ipaddress": "10.223.71.23",
                              "state": "Active",
                              "cidrlist": "0.0.0.0/24"
                            }
                          ]
                        });
                      }, 100);
                    }
                  },

                  staticNAT: {
                    noSelect: true,
                    fields: {
                      'protocol': {
                        label: 'Protocol',
                        select: function(args) {
                          args.response.success({
                            data: [
                              { name: 'tcp', description: 'TCP' },
                              { name: 'udp', description: 'UDP' }
                            ]
                          });
                        }
                      },
                      'startport': { edit: true, label: 'Start Port' },
                      'endport': { edit: true, label: 'End Port' },
                      'add-rule': {
                        label: 'Add Rule',
                        addButton: true
                      }
                    },
                    add: {
                      label: 'Add',
                      action: function(args) {
                        setTimeout(function() {
                          args.response.success({
                            notification: {
                              label: 'Add firewall rule',
                              poll: testData.notifications.testPoll
                            }
                          });
                        }, 500);
                      }
                    },
                    actions: {
                      destroy: {
                        label: 'Remove Rule',
                        action: function(args) {
                          setTimeout(function() {
                            args.response.success({
                              notification: {
                                label: 'Remove firewall rule',
                                poll: testData.notifications.testPoll
                              }
                            });
                          }, 500);
                        }
                      }
                    },
                    dataProvider: function(args) {
                      setTimeout(function() {
                        args.response.success({
                          data: [
                            {
                              "id": 10,
                              "protocol": "udp",
                              "startport": "500",
                              "endport": "10000",
                              "ipaddressid": 4,
                              "ipaddress": "10.223.71.23",
                              "state": "Active",
                              "cidrlist": "0.0.0.0/24"
                            },
                            {
                              "id": 9,
                              "protocol": "tcp",
                              "startport": "20",
                              "endport": "200",
                              "ipaddressid": 4,
                              "ipaddress": "10.223.71.23",
                              "state": "Active",
                              "cidrlist": "0.0.0.0/24"
                            }
                          ]
                        });
                      }, 100);
                    }
                  },

                  // Load balancing rules
                  loadBalancing: {
                    listView: cloudStack.sections.instances,
                    multipleAdd: true,
                    fields: {
                      'name': { edit: true, label: 'Name' },
                      'publicport': { edit: true, label: 'Public Port' },
                      'privateport': { edit: true, label: 'Private Port' },
                      'algorithm': {
                        label: 'Algorithm',
                        select: function(args) {
                          args.response.success({
                            data: [
                              { name: 'roundrobin', description: 'Round-robin' },
                              { name: 'leastconn', description: 'Least connections' },
                              { name: 'source', description: 'Source' }
                            ]
                          });
                        }
                      },
                      'add-vm': {
                        label: 'Add VMs',
                        addButton: true
                      }
                    },
                    add: {
                      label: 'Add VMs',
                      action: function(args) {
                        setTimeout(function() {
                          args.response.success({
                            notification: {
                              label: 'Add load balancing rule',
                              poll: testData.notifications.testPoll
                            }
                          });
                        }, 500);
                      }
                    },
                    actions: {
                      destroy:  {
                        label: 'Remove load balancing rule',
                        action: function(args) {
                          setTimeout(function() {
                            args.response.success({
                              notification: {
                                label: 'Remove load balancing rule',
                                poll: testData.notifications.testPoll
                              }
                            });
                          }, 500);
                        }
                      }
                    },
                    dataProvider: function(args) {
                      setTimeout(function() {
                        args.response.success({
                          data: [
                            {
                              "id": 13,
                              "name": "HTTP",
                              "publicipid": 4,
                              "publicip": "10.223.71.23",
                              "publicport": "80",
                              "privateport": "80",
                              "algorithm": "roundrobin",
                              "cidrlist": "",
                              "account": "admin",
                              "domainid": 1,
                              "domain": "ROOT",
                              "state": "Active",
                              "zoneid": 1,
                              _itemData: [
                                testData.data.instances[0],
                                testData.data.instances[1],
                                testData.data.instances[2],
                                testData.data.instances[3]
                              ]
                            }
                          ]
                        });
                      }, 100);
                    }
                  },

                  // Port forwarding rules
                  portForwarding: {
                    listView: cloudStack.sections.instances,
                    fields: {
                      'private-ports': {
                        edit: true,
                        label: 'Private Ports',
                        range: ['privateport', 'privateendport']
                      },
                      'public-ports': {
                        edit: true,
                        label: 'Public Ports',
                        range: ['publicport', 'publicendport']
                      },
                      'protocol': {
                        label: 'Protocol',
                        select: function(args) {
                          args.response.success({
                            data: [
                              { name: 'tcp', description: 'TCP' },
                              { name: 'udp', description: 'UDP' }
                            ]
                          });
                        }
                      },
                      'add-vm': {
                        label: 'Add VM',
                        addButton: true
                      }
                    },
                    add: {
                      label: 'Add VM',
                      action: function(args) {
                        setTimeout(function() {
                          args.response.success({
                            notification: {
                              label: 'Add port forwarding rule',
                              poll: testData.notifications.testPoll
                            }
                          });
                        }, 500);
                      }
                    },
                    actions: {
                      destroy: {
                        label: 'Remove port forwarding rule',
                        action: function(args) {
                          setTimeout(function() {
                            args.response.success({
                              notification: {
                                label: 'Remove port forwarding rule',
                                poll: testData.notifications.testPoll
                              }
                            });
                          }, 500);
                        }
                      }
                    },
                    dataProvider: function(args) {
                      setTimeout(function() {
                        args.response.success({
                          data: [
                            {
                              "id": 12,
                              "privateport": "22",
                              "privateendport": "22",
                              "protocol": "tcp",
                              "publicport": "22",
                              "publicendport": "22",
                              "virtualmachineid": 10,
                              "virtualmachinename": "i-2-10-TEST",
                              "virtualmachinedisplayname": "i-2-10-TEST",
                              "ipaddressid": 4,
                              "ipaddress": "10.223.71.23",
                              "state": "Active",
                              "cidrlist": "",
                              _itemData: [
                                testData.data.instances[5]
                              ]
                            }
                          ]
                        });
                      }, 100);
                    }
                  }
                })
              },
              vpn: {
                title: 'VPN',
                custom: function(args) {
                  return $('<div>').multiEdit({
                    noSelect: true,
                    fields: {
                      'username': { edit: true, label: 'Username' },
                      'password': { edit: true, label: 'Password' },
                      'add-user': { addButton: true, label: 'Add user' }
                    },
                    add: {
                      label: 'Add user',
                      action: function(args) {
                        setTimeout(function() {
                          args.response.success({
                            notification: {
                              label: 'Add user to VPN',
                              poll: testData.notifications.testPoll
                            }
                          });
                        }, 500);
                      }
                    },
                    actions: {
                      destroy: {
                        label: 'Remove user',
                        action: function(args) {
                          setTimeout(function() {
                            args.response.success({
                              notification: {
                                label: 'Remove user from VPN',
                                poll: testData.notifications.testPoll
                              }
                            });
                          }, 500);
                        }
                      }
                    },
                    dataProvider: function(args) {
                      setTimeout(function() {
                        args.response.success({
                          data: [
                          ]
                        });
                      }, 100);
                    }
                  })
                }
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
              }
            }
          }
        }
      }
    }
  };
})(cloudStack, testData, jQuery);

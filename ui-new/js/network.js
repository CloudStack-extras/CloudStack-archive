(function(cloudStack, $, testData) {
  var actionFilters = {
    ipAddress: function(args) {
      var allowedActions = args.context.actions;
      var disallowedActions = [];
      var item = args.context.item;
      var status = item.state;

      if (status == 'Destroyed' ||
          status == 'Releasing' ||
          status == 'Released' ||
          status == 'Creating' ||
          status == 'Allocating' ||
          item.account == 'system') {
        disallowedActions = allowedActions;
      }

      if (item.isstaticnat) {
        disallowedActions.push('enableStaticNAT');
      } else {
        disallowedActions.push('disableStaticNAT');
      }

      if (item.vpnenabled) {
        disallowedActions.push('enableVPN');
      } else {
        disallowedActions.push('disableVPN');
      }

      if (item.issourcenat){
        disallowedActions.push('enableStaticNAT');
        disallowedActions.push('disableStaticNAT');
        disallowedActions.push('destroy');
      } else {
        disallowedActions.push('enableVPN');
        disallowedActions.push('disableVPN');
      }

      allowedActions = $.grep(allowedActions, function(item) {
        return $.inArray(item, disallowedActions) == -1;
      });

      return allowedActions;
    }
  };

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
            account: { label: 'Account' },
            state: { label: 'State', indicator: { 'Allocated': 'on' } }
          },
          actions: {
            add: {
              label: 'Acquire new IP',

              action: function(args) {
                $.ajax({
                  url: createURL('associateIpAddress'),
                  data: {
                    zoneid: args.data.availabilityZone
                  },
                  dataType: 'json',
                  async: true,
                  success: function(data) {
                    args.response.success({
                      _custom: {
                        jobId: data.associateipaddressresponse.jobid,
                        getUpdatedItem: function(data) {
                          return data.queryasyncjobresultresponse.jobresult.ipaddress;
                        },
                        getActionFilter: function(args) {
                          return ['enableStaticNAT', 'destroy'];
                        }
                      }
                    });
                  }
                });
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
                  availabilityZone: {
                    label: 'Zone',
                    select: function(args) {
                      $.ajax({
                        url: createURL('listZones'),
                        dataType: 'json',
                        async: true,
                        success: function(data) {
                          args.response.success({
                            data: $.map(data.listzonesresponse.zone, function(zone) {
                              return {
                                id: zone.id,
                                description: zone.name
                              };
                            })
                          });
                        }
                      });
                    }
                  }
                }
              },

              notification: {
                poll: pollAsyncJobResult
              }
            },
            enableVPN: {
              label: 'Enable VPN',
              action: function(args) {
                $.ajax({
                  url: createURL('createRemoteAccessVpn'),
                  data: {
                    publicipid: args.context.ipAddresses[0].id,
                    domainid: args.context.ipAddresses[0].domainid
                  },
                  dataType: 'json',
                  async: true,
                  success: function(data) {
                    args.response.success({
                      _custom: { jobId: data.createremoteaccessvpnresponse.jobid }
                    });
                  }
                });
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
                poll: pollAsyncJobResult
              }
            },
            disableVPN: {
              label: 'Disable VPN',
              action: function(args) {
                $.ajax({
                  url: createURL('deleteRemoteAccessVpn'),
                  data: {
                    publicipid: args.context.ipAddresses[0].id,
                    domainid: args.context.ipAddresses[0].domainid
                  },
                  dataType: 'json',
                  async: true,
                  success: function(data) {
                    args.response.success({
                      _custom: {
                        getUpdatedItem: function(data) {

                        },
                        jobId: data.deleteremoteaccessvpnresponse.jobid
                      }
                    });
                  }
                });
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
                poll: pollAsyncJobResult
              }
            },
            enableStaticNAT: {
              label: 'Enable static NAT',
              action: {
                noAdd: true,
                custom: cloudStack.uiCustom.enableStaticNAT({
                  listView: cloudStack.sections.instances,
                  action: function(args) {
                    $.ajax({
                      url: createURL('enableStaticNat'),
                      data: {
                        ipaddressid: args.context.ipAddresses[0].id,
                        virtualmachineid: args.context.instances[0].id
                      },
                      dataType: 'json',
                      async: true,
                      success: function(data) {
                        args.response.success();
                      }
                    });
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
                poll: function(args) {
                  args.complete();
                }
              }
            },
            disableStaticNAT: {
              label: 'Disable static NAT',
              action: function(args) {
                $.ajax({
                  url: createURL('disableStaticNat'),
                  data: {
                    ipaddressid: args.context.ipAddresses[0].id
                  },
                  dataType: 'json',
                  async: true,
                  success: function(data) {
                    args.response.success({
                      _custom: {
                        jobId: data.disablestaticnatresponse.jobid,
                        getActionFilter: function() {
                          return function(args) {
                            return ['enableStaticNAT'];
                          };
                        }
                      }
                    });
                  }
                });
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to disable static NAT?';
                },
                notification: function(args) {
                  return 'Disable Static NAT';
                }
              },
              notification: {
                poll: pollAsyncJobResult
              }
            },
            destroy: {
              label: 'Release IP',
              action: function(args) {
                $.ajax({
                  url: createURL('disassociateIpAddress'),
                  data: {
                    id: args.context.ipAddresses[0].id
                  },
                  dataType: 'json',
                  async: true,
                  success: function(data) {
                    args.response.success({
                      _custom: {
                        jobId: data.disassociateipaddressresponse.jobid,
                        getActionFilter: function() {
                          return function(args) {
                            var allowedActions = ['enableStaticNAT'];

                            return allowedActions;
                          };
                        },
                        getUpdatedItem: function(args) {
                          return {
                            state: 'Released'
                          };
                        }
                      }
                    });
                  }
                });
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to release this IP?';
                },
                notification: function(args) {
                  return 'Release IP';
                }
              },
              notification: { poll: pollAsyncJobResult }
            }
          },

          //dataProvider: testData.dataProvider.listView('network'),
          dataProvider: function(args) {
            $.ajax({
              url: createURL("listPublicIpAddresses&page="+args.page+"&pagesize="+pageSize),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listpublicipaddressesresponse.publicipaddress;
                var processedItems = 0;

                if (!items) {
                  args.response.success({
                    data: []
                  });
                  return;
                }

                // Get network data
                $(items).each(function() {
                  var item = this;
                  $.ajax({
                    url: createURL('listNetworks'),
                    data: {
                      networkid: this.networkid
                    },
                    dataType: 'json',
                    async: true,
                    success: function(data) {
                      // Get VPN data
                      $.ajax({
                        url: createURL('listRemoteAccessVpns'),
                        data: {
                          publicipid: item.id
                        },
                        dataType: 'json',
                        async: true,
                        success: function(vpnResponse) {
                          var isVPNEnabled = vpnResponse.listremoteaccessvpnsresponse.count;
                          if (isVPNEnabled) {
                            item.vpnenabled = true;
                            item.remoteaccessvpn = vpnResponse.listremoteaccessvpnsresponse.remoteaccessvpn[0];
                          };

                          // Check if data retrieval complete
                          item.network = data.listnetworksresponse.network[0];
                          processedItems++;

                          if (processedItems == items.length) {
                            args.response.success({
                              actionFilter: actionFilters.ipAddress,
                              data: items
                            });
                          }
                        }
                      });
                    }
                  });
                });
              }
            });
          },

          // Detail view
          detailView: {
            name: 'IP address detail',
            tabFilter: function(args) {
              var disabledTabs = [];
              var ipAddress = args.context.ipAddresses[0];

              if (!ipAddress.issourcenat ||
                  (ipAddress.issourcenat && !ipAddress.vpnenabled)) {
                disabledTabs.push('vpn');
              }

              return disabledTabs;
            },
            actions: {
              enableVPN: {
                label: 'Enable VPN',
                action: function(args) {
                  $.ajax({
                    url: createURL('createRemoteAccessVpn'),
                    data: {
                      publicipid: args.context.ipAddresses[0].id,
                      domainid: args.context.ipAddresses[0].domainid
                    },
                    dataType: 'json',
                    async: true,
                    success: function(data) {
                      args.response.success({
                        _custom: { jobId: data.createremoteaccessvpnresponse.jobid }
                      });
                    }
                  });
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
                  poll: pollAsyncJobResult
                }
              },
              disableVPN: {
                label: 'Disable VPN',
                action: function(args) {
                  $.ajax({
                    url: createURL('deleteRemoteAccessVpn'),
                    data: {
                      publicipid: args.context.ipAddresses[0].id,
                      domainid: args.context.ipAddresses[0].domainid
                    },
                    dataType: 'json',
                    async: true,
                    success: function(data) {
                      args.response.success({
                        _custom: {
                          getUpdatedItem: function(data) {

                          },
                          jobId: data.deleteremoteaccessvpnresponse.jobid
                        }
                      });
                    }
                  });
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
                  poll: pollAsyncJobResult
                }
              },
              enableStaticNAT: {
                label: 'Enable static NAT',
                action: {
                  noAdd: true,
                  custom: cloudStack.uiCustom.enableStaticNAT({
                    listView: cloudStack.sections.instances,
                    action: function(args) {
                      $.ajax({
                        url: createURL('enableStaticNat'),
                        data: {
                          ipaddressid: args.context.ipAddresses[0].id,
                          virtualmachineid: args.context.instances[0].id
                        },
                        dataType: 'json',
                        async: true,
                        success: function(data) {
                          args.response.success();
                        }
                      });
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
                  poll: function(args) {
                    args.complete();
                  }
                }
              },
              disableStaticNAT: {
                label: 'Disable static NAT',
                action: function(args) {
                  $.ajax({
                    url: createURL('disableStaticNat'),
                    data: {
                      ipaddressid: args.context.ipAddresses[0].id
                    },
                    dataType: 'json',
                    async: true,
                    success: function(data) {
                      args.response.success({
                        _custom: {
                          jobId: data.disablestaticnatresponse.jobid,
                          getActionFilter: function() {
                            return function(args) {
                              return ['enableStaticNAT'];
                            };
                          }
                        }
                      });
                    }
                  });
                },
                messages: {
                  confirm: function(args) {
                    return 'Are you sure you want to disable static NAT?';
                  },
                  notification: function(args) {
                    return 'Disable Static NAT';
                  }
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },
              destroy: {
                label: 'Release IP',
                action: function(args) {
                  $.ajax({
                    url: createURL('disassociateIpAddress'),
                    data: {
                      id: args.context.ipAddresses[0].id
                    },
                    dataType: 'json',
                    async: true,
                    success: function(data) {
                      args.response.success({
                        _custom: {
                          jobId: data.disassociateipaddressresponse.jobid,
                          getActionFilter: function() {
                            return function(args) {
                              var allowedActions = ['enableStaticNAT'];

                              return allowedActions;
                            };
                          },
                          getUpdatedItem: function(args) {
                            return {
                              state: 'Released'
                            };
                          }
                        }
                      });
                    }
                  });
                },
                messages: {
                  confirm: function(args) {
                    return 'Are you sure you want to release this IP?';
                  },
                  notification: function(args) {
                    return 'Release IP';
                  }
                },
                notification: { poll: pollAsyncJobResult }
              }
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

                //dataProvider: testData.dataProvider.detailView('network')
                dataProvider: function(args) {
                  $.ajax({
                    url: createURL("listPublicIpAddresses&id="+args.id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var items = json.listpublicipaddressesresponse.publicipaddress;
                      if(items != null && items.length > 0) {
                        args.response.success({
                          actionFilter: actionFilters.ipAddress,
                          data: items[0]
                        });
                      }
                    }
                  });
                }
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
                        $.ajax({
                          url: createURL('createFirewallRule'),
                          data: $.extend(args.data, {
                            ipaddressid: args.context.ipAddresses[0].id
                          }),
                          dataType: 'json',
                          success: function(data) {
                            args.response.success({
                              _custom: {
                                jobId: data.createfirewallruleresponse.jobid
                              },
                              notification: {
                                label: 'Add firewall rule',
                                poll: pollAsyncJobResult
                              }
                            });
                          }
                        });
                      }
                    },
                    actions: {
                      destroy: {
                        label: 'Remove Rule',
                        action: function(args) {
                          $.ajax({
                            url: createURL('deleteFirewallRule'),
                            data: {
                              id: args.context.multiRule[0].id
                            },
                            dataType: 'json',
                            async: true,
                            success: function(data) {
                              var jobID = data.deletefirewallruleresponse.jobid;

                              args.response.success({
                                _custom: {
                                  jobId: jobID
                                },
                                notification: {
                                  label: 'Remove firewall rule ' + args.context.multiRule[0].id,
                                  poll: pollAsyncJobResult
                                }
                              });
                            }
                          });
                        }
                      }
                    },
                    dataProvider: function(args) {
                      $.ajax({
                        url: createURL('listFirewallRules'),
                        data: {
                          ipaddressid: args.context.ipAddresses[0].id
                        },
                        dataType: 'json',
                        async: true,
                        success: function(data) {
                          args.response.success({
                            data: data.listfirewallrulesresponse.firewallrule
                          });
                        }
                      });
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
                        $.ajax({
                          url: createURL('createIpForwardingRule'),
                          data: $.extend(args.data, {
                            ipaddressid: args.context.ipAddresses[0].id
                          }),
                          dataType: 'json',
                          success: function(data) {
                            args.response.success({
                              _custom: {
                                jobId: data.createipforwardingruleresponse.jobid
                              },
                              notification: {
                                label: 'Added static NAT rule',
                                poll: pollAsyncJobResult
                              }
                            });
                          }
                        });
                      }
                    },
                    actions: {
                      destroy: {
                        label: 'Remove Rule',
                        action: function(args) {
                          $.ajax({
                            url: createURL('deleteIpForwardingRule'),
                            data: {
                              id: args.context.multiRule[0].id
                            },
                            dataType: 'json',
                            async: true,
                            success: function(data) {
                              var jobID = data.deleteipforwardingruleresponse.jobid;
                              args.response.success({
                                _custom: {
                                  jobId: jobID
                                },
                                notification: {
                                  label: 'Removed static NAT rule',
                                  poll: pollAsyncJobResult
                                }
                              });
                            }
                          });
                        }
                      }
                    },
                    dataProvider: function(args) {
                      setTimeout(function() {
                        $.ajax({
                          url: createURL('listIpForwardingRules'),
                          data: {
                            ipaddressid: args.context.ipAddresses[0].id
                          },
                          dataType: 'json',
                          async: true,
                          success: function(data) {
                            args.response.success({
                              data: data.listipforwardingrulesresponse.ipforwardingrule
                            });
                          }
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
                        $.ajax({
                          url: createURL(),
                          data: $.extend(args.data, {
                            command: 'createLoadBalancerRule',
                            publicipid: args.context.ipAddresses[0].id
                          }),
                          dataType: 'json',
                          async: true,
                          success: function(data) {
                            var itemData = args.itemData;

                            $.ajax({
                              url: createURL(),
                              data: {
                                command: 'assignToLoadBalancerRule',
                                id: data.createloadbalancerruleresponse.id,
                                virtualmachineids: $.map(itemData, function(elem) {
                                  return elem.id;
                                }).join(',')
                              },
                              dataType: 'json',
                              async: true,
                              success: function(data) {

                              }
                            });

                            args.response.success({
                              _custom: {
                                jobId: data.createloadbalancerruleresponse.jobid
                              },
                              notification: {
                                label: 'Add load balancer rule',
                                poll: pollAsyncJobResult
                              }
                            });
                          }
                        });
                      }
                    },
                    actions: {
                      destroy:  {
                        label: 'Remove load balancer rule',
                        action: function(args) {
                          $.ajax({
                            url: createURL(),
                            data: {
                              command: 'deleteLoadBalancerRule',
                              id: args.context.multiRule[0].id
                            },
                            dataType: 'json',
                            async: true,
                            success: function(data) {
                              var jobID = data.deleteloadbalancerruleresponse.jobid;

                              args.response.success({
                                _custom: {
                                  jobId: jobID
                                },
                                notification: {
                                  label: 'Remove load balancer rule ' + args.context.multiRule[0].id,
                                  poll: pollAsyncJobResult
                                }
                              });
                            }
                          });
                        }
                      }
                    },
                    dataProvider: function(args) {
                      $.ajax({
                        url: createURL(),
                        data: {
                          command: 'listLoadBalancerRules',
                          publicipid: args.context.ipAddresses[0].id
                        },
                        dataType: 'json',
                        async: true,
                        success: function(data) {
                          var loadBalancerData = data.listloadbalancerrulesresponse.loadbalancerrule;
                          var loadVMTotal = loadBalancerData.length;
                          var loadVMCurrent = 0;

                          $(loadBalancerData).each(function() {
                            var item = this;

                            // Get instances
                            $.ajax({
                              url: createURL(),
                              dataType: 'json',
                              async: true,
                              data: {
                                command: 'listLoadBalancerRuleInstances',
                                id: item.id
                              },
                              success: function(data) {
                                loadVMCurrent++;
                                $.extend(item, {
                                  _itemData: data
                                    .listloadbalancerruleinstancesresponse.loadbalancerruleinstance
                                });

                                if (loadVMCurrent == loadVMTotal) {
                                  args.response.success({
                                    data: loadBalancerData
                                  });
                                }
                              }
                            });
                          });
                        }
                      });
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
                        $.ajax({
                          url: createURL(),
                          data: $.extend(args.data, {
                            command: 'createPortForwardingRule',
                            ipaddressid: args.context.ipAddresses[0].id,
                            virtualmachineid: args.itemData[0].id
                          }),
                          dataType: 'json',
                          async: true,
                          success: function(data) {
                            args.response.success({
                              _custom: {
                                jobId: data.createportforwardingruleresponse.jobid
                              },
                              notification: {
                                label: 'Add port forwarding rule',
                                poll: pollAsyncJobResult
                              }
                            });
                          }
                        });
                      }
                    },
                    actions: {
                      destroy: {
                        label: 'Remove port forwarding rule',
                        action: function(args) {
                          $.ajax({
                            url: createURL(),
                            data: {
                              command: 'deletePortForwardingRule',
                              id: args.context.multiRule[0].id
                            },
                            dataType: 'json',
                            async: true,
                            success: function(data) {
                              var jobID = data.deleteportforwardingruleresponse.jobid;

                              args.response.success({
                                _custom: {
                                  jobId: jobID
                                },
                                notification: {
                                  label: 'Remove port forwarding rule ' + args.context.multiRule[0].id,
                                  poll: pollAsyncJobResult
                                }
                              });
                            }
                          });
                        }
                      }
                    },
                    dataProvider: function(args) {
                      $.ajax({
                        url: createURL(),
                        data: {
                          command: 'listPortForwardingRules',
                          ipaddressid: args.context.ipAddresses[0].id
                        },
                        dataType: 'json',
                        async: true,
                        success: function(data) {
                          // Get instance
                          var portForwardingData = data
                                .listportforwardingrulesresponse.portforwardingrule;
                          var loadTotal = portForwardingData.length;
                          var loadCurrent = 0;

                          $(portForwardingData).each(function() {
                            var item = this;

                            $.ajax({
                              url: createURL(),
                              dataType: 'json',
                              async: true,
                              data: {
                                command: 'listVirtualMachines',
                                id: item.virtualmachineid
                              },
                              success: function(data) {
                                loadCurrent++;
                                $.extend(item, {
                                  _itemData: data.listvirtualmachinesresponse.virtualmachine,
                                  _context: {
                                    instances: data.listvirtualmachinesresponse.virtualmachine
                                  }
                                });

                                if (loadCurrent == loadTotal) {
                                  args.response.success({
                                    data: portForwardingData
                                  });
                                }
                              }
                            });
                          });
                        }
                      });
                    }
                  }
                })
              },
              vpn: {
                title: 'VPN',
                custom: function(args) {
                  var ipAddress = args.context.ipAddresses[0].ipaddress;
                  var psk = args.context.ipAddresses[0].remoteaccessvpn.presharedkey;

                  return $('<div>')
                    .append(
                      $('<ul>').addClass('info')
                        .append(
                          // VPN IP
                          $('<li>').addClass('ip').html('Your VPN access is currently enabled and can be accessed via the IP: ')
                            .append($('<strong>').html(ipAddress))
                        )
                        .append(
                          // PSK
                          $('<li>').addClass('psk').html('Your IPSec pre-shared key is: ')
                            .append($('<strong>').html(psk))
                        )
                    ).multiEdit({
                      context: args.context,
                      noSelect: true,
                      fields: {
                        'username': { edit: true, label: 'Username' },
                        'password': { edit: true, isPassword: true, label: 'Password' },
                        'add-user': { addButton: true, label: 'Add user' }
                      },
                      add: {
                        label: 'Add user',
                        action: function(args) {
                          $.ajax({
                            url: createURL('addVpnUser'),
                            data: args.data,
                            dataType: 'json',
                            success: function(data) {
                              args.response.success({
                                _custom: {
                                  jobId: data.addvpnuserresponse.jobid
                                },
                                notification: {
                                  label: 'Added VPN user',
                                  poll: pollAsyncJobResult
                                }
                              });
                            }
                          });
                        }
                      },
                      actions: {
                        destroy: {
                          label: 'Remove user',
                          action: function(args) {
                            $.ajax({
                              url: createURL('removeVpnUser'),
                              data: {
                                username: args.context.multiRule[0].username,
                                id: args.context.multiRule[0].domainid
                              },
                              dataType: 'json',
                              async: true,
                              success: function(data) {
                                var jobID = data.removevpnuserresponse.jobid;

                                args.response.success({
                                  _custom: {
                                    jobId: jobID
                                  },
                                  notification: {
                                    label: 'Removed VPN user',
                                    poll: pollAsyncJobResult
                                  }
                                });
                              }
                            });
                          }
                        }
                      },
                      dataProvider: function(args) {
                        $.ajax({
                          url: createURL('listVpnUsers'),
                          data: {
                            domainid: args.context.ipAddresses[0].domainid
                          },
                          dataType: 'json',
                          async: true,
                          success: function(data) {
                            args.response.success({
                              data: data.listvpnusersresponse.vpnuser
                            });
                          }
                        });
                      }
                    });
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
                $.ajax({
                  url: createURL('createSecurityGroup'),
                  data: {
                    name: args.data.name,
                    description: args.data.description
                  },
                  dataType: 'json',
                  async: true,
                  success: function(data) {
                    args.response.success({
                      data: data.createsecuritygroupresponse.securitygroup
                    });
                  }
                });
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
                $.ajax({
                  url: createURL('deleteSecurityGroup'),
                  data: {
                    id: args.context.securityGroups[0].id
                  },
                  dataType: 'json',
                  async: true,
                  success: function(data) {
                    args.response.success({
                      actionFilter: function() { return []; }
                    });
                  }
                });
              }
            }
          },

          //dataProvider: testData.dataProvider.listView('securityGroups'),
          dataProvider: function(args) {
            $.ajax({
              url: createURL("listSecurityGroups&page="+args.page+"&pagesize="+pageSize),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listsecuritygroupsresponse.securitygroup;
                args.response.success({data:items});
              }
            });
          },

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

                //dataProvider: testData.dataProvider.detailView('securityGroups')
                dataProvider: function(args) {
                  $.ajax({
                    url: createURL("listSecurityGroups&id="+args.id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var items = json.listsecuritygroupsresponse.securitygroup;
                      if(items != null && items.length > 0) {
                        args.response.success({data:items[0]});
                      }
                    }
                  });
                }
              },
              ingressRules: {
                title: 'Ingress Rules',
                custom: cloudStack.uiCustom.securityRules({
                  noSelect: true,
                  noHeaderActionsColumn: true,
                  fields: {
                    'protocol': {
                      label: 'Protocol',
                      select: function(args) {
                        args.$select.change(function() {
                          var $inputs = args.$form.find('th, td');
                          var $icmpFields = $inputs.filter(function() {
                            var name = $(this).attr('rel');

                            return $.inArray(name, [
                              'icmptype',
                              'icmpcode'
                            ]) > -1;
                          });
                          var $otherFields = $inputs.filter(function() {
                            var name = $(this).attr('rel');

                            return name != 'icmptype' &&
                              name != 'icmpcode' &&
                              name != 'protocol' &&
                              name != 'add-rule' &&
                              name != 'cidr' &&
                              name != 'accountname' &&
                              name != 'securitygroup';
                          });

                          if ($(this).val() == 'icmp') {
                            $icmpFields.show();
                            $otherFields.hide();
                          } else {
                            $icmpFields.hide();
                            $otherFields.show();
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
                    'icmptype': { edit: true, label: 'ICMP Type', isHidden: true },
                    'icmpcode': { edit: true, label: 'ICMP Code', isHidden: true },
                    'cidr': { edit: true, label: 'CIDR', isHidden: true },
                    'accountname': {
                      edit: true,
                      label: 'Account, Security Group',
                      isHidden: true,
                      range: ['accountname', 'securitygroup']
                    },
                    'add-rule': {
                      label: 'Add',
                      addButton: true
                    }
                  },
                  add: {
                    label: 'Add',
                    action: function(args) {
                      var data = args.data;

                      $.ajax({
                        url: createURL('authorizeSecurityGroupIngress'),
                        data: $.extend(
                          true, {},
                          data,
                          {
                            securitygroupid: args.context.securityGroups[0].id  
                          },
                          data.cidr ? {
                            cidrlist: data.cidr
                          } : {
                            'usersecuritygrouplist[0].account': data.accountname,
                            'usersecuritygrouplist[0].group': data.securitygroup
                          }
                        ),
                        dataType: 'json',
                        async: true,
                        success: function(data) {
                          var jobId = data.authorizesecuritygroupingressresponse.jobid;

                          args.response.success({
                            _custom: {
                              jobId: jobId
                            },
                            notification: {
                              label: 'Add new ingress rule',
                              poll: pollAsyncJobResult
                            }
                          });
                        }
                      });
                    }
                  },
                  actions: {
                    destroy: {
                      label: 'Remove Rule',
                      action: function(args) {
                        $.ajax({
                          url: createURL('revokeSecurityGroupIngress'),
                          data: {
                            domainid: args.context.securityGroups[0].domainid,
                            id: args.context.multiRule[0].id
                          },
                          dataType: 'json',
                          async: true,
                          success: function(data) {
                            var jobID = data.revokesecuritygroupingress.jobid;

                            args.response.success({
                              _custom: {
                                jobId: jobID
                              },
                              notification: {
                                label: 'Revoke ingress rule',
                                poll: pollAsyncJobResult
                              }
                            });
                          }
                        });
                      }
                    }
                  },
                  ignoreEmptyFields: true,
                  dataProvider: function(args) {
                    $.ajax({
                      url: createURL('listSecurityGroups'),
                      data: {
                        id: args.context.securityGroups[0].id
                      },
                      dataType: 'json',
                      async: true,
                      success: function(data) {
                        args.response.success({
                          data: $.map(
                            data.listsecuritygroupsresponse.securitygroup[0].ingressrule,
                            function(elem) {
                              return {
                                id: elem.ruleid,
                                protocol: elem.protocol,
                                startport: elem.startport ? elem.startport : elem.icmptype,
                                endport: elem.endport ? elem.endport : elem.icmpcode,
                                cidr: elem.cidr ? elem.cidr : ''.concat(elem.account, ' - ', elem.securitygroupname)
                              };
                            }
                          )
                        });
                      }
                    });
                  }
                })
              }
            }
          }
        }
      }
    }
  };
})(cloudStack, jQuery, testData);

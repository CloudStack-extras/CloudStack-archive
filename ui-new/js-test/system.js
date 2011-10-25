(function($, cloudStack) {
  cloudStack.sections.system = {
    title: 'System',
    id: 'system',
    show: cloudStack.uiCustom.physicalResources({
      sections: {
        physicalResources: {
          type: 'select',
          title: 'Physical Resources',
          listView: {
            id: 'physicalResources',
            label: 'Physical Resources',
            fields: {
              name: { label: 'Zone' },
              dns1: { label: 'DNS' },
              internaldns1: { label: 'Internal DNS' },
              networktype: { label: 'Network Type' },
              allocationstate: { label: 'State' }
            },
            actions: {
              destroy: testData.actions.destroy('zone'),
              add: {
                label: 'Add zone',
                action: {
                  custom: cloudStack.zoneWizard({
                    steps: [
                      // Step 1: Setup
                      null,

                      // Step 2: Setup Zone
                      function(args) {
                        args.response.success({
                          domains: [
                            {
                              id: '1',
                              name: 'Domain A'
                            },
                            {
                              id: '2',
                              name: 'Domain B'
                            },
                            {
                              id: '3',
                              name: 'Domain C'
                            }
                          ]
                        });
                      },

                      // Step 3: Setup Pod
                      null,

                      // Step 4: Setup IP Range
                      function(args) {
                        args.response.success({
                          domains: [
                            {
                              id: '1',
                              name: 'Domain A'
                            },
                            {
                              id: '2',
                              name: 'Domain B'
                            },
                            {
                              id: '3',
                              name: 'Domain C'
                            }
                          ]
                        });
                      }
                    ],
                    action: function(args) {
                      args.response.success({
                        _custom: { jobID: new Date() }
                      });
                    }
                  })
                },
                messages: {
                  confirm: function(args) {
                    return 'Are you sure you want to add ' + args.name + '?';
                  },
                  notification: function(args) {
                    return 'Created new zone';
                  }
                },
                notification: {
                  poll: testData.notifications.customPoll(testData.data.zones[0])
                }
              }
            },
            dataProvider: testData.dataProvider.listView('zones'),
            detailView: {
              isMaximized: true,
              pageGenerator: cloudStack.zoneChart({
                dataProvider: testData.dataProvider.detailView('zones'),
                detailView: {
                  name: 'Zone details',
                  viewAll: { path: '_zone.pods', label: 'Pods' },
                  tabs: {
                    details: {
                      title: 'Details',
                      fields: [
                        {
                          name: { label: 'Zone' }
                        },
                        {
                          dns1: { label: 'DNS 1' },
                          dns2: { label: 'DNS 2' },
                          internaldns1: { label: 'Internal DNS 1' },
                          internaldns2: { label: 'Internal DNS 2' }
                        },
                        {
                          networktype: { label: 'Network Type' },
                          allocationstate: { label: 'State' },
                          vlan: { label: 'VLAN' },
                          networktype: { label: 'Network Type' },
                          securitygroupenabled: { label: 'Security Group?' }
                        }
                      ],
                      dataProvider: testData.dataProvider.detailView('zones')
                    },
                    resources: {
                      title: 'Resources',
                      fields: [
                        {
                          iptotal: { label: 'Total IPs' },
                          cputotal: { label: 'Total CPU' },
                          bandwidthout: { label: 'Bandwidth (Out)'},
                          bandwidthin: { label: 'Bandwidth (In)'}
                        }
                      ],
                      dataProvider: function(args) {
                        setTimeout(function() {
                          args.response.success({
                            data: {
                              iptotal: 1000,
                              cputotal: '500 Ghz',
                              bandwidthout: '14081 mb',
                              bandwidthin: '31000 mb'
                            }
                          });
                        }, 500);
                      }
                    }
                  }
                }
              })
            }
          }
        },
        virtualAppliances: {
          type: 'select',
          title: 'Virtual Appliances',
          id: 'virtualAppliances',
          listView: {
            label: 'Virtual Appliances',
            fields: {
              name: { label: 'Name' },
              hostname: { label: 'Hostname' },
              publicip: { label: 'Public IP' },
              publicmacaddress: { label: 'Public MAC' },
              state: { label: 'Status' }
            },
            dataProvider: testData.dataProvider.listView('virtualAppliances')
          }
        },
        systemVMs: {
          type: 'select',
          title: 'System VMs',
          listView: {
            label: 'System VMs',
            fields: {
              name: { label: 'Name' },
              zonename: { label: 'Zone' },
              hostname: { label: 'Hostname' },
              privateip: { label: 'Private IP' },
              publicip: { label: 'Public IP' },
              state: { label: 'Status' }
            },
            dataProvider: testData.dataProvider.listView('systemVMs')
          }
        }
      }
    }),
    subsections: {
      networks: {
        sectionSelect: { label: 'Network type' },
        sections: {
          publicNetwork: {
            type: 'select',
            title: 'Public network',
            listView: {
              section: 'networks',
              id: 'networks',
              fields: {
                name: { label: 'Name' },
                startip: { label: 'Start IP' },
                endip: { label: 'End IP' },
                type: { label: 'Type' }
              },
              dataProvider: testData.dataProvider.listView('networks'),
              detailView: {
                viewAll: { label: 'Hosts', path: 'instances' },
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'name' },
                        displaytext: { label: 'displaytext' }
                      },
                      {
                        broadcastdomaintype: { label: 'broadcastdomaintype' },
                        traffictype: { label: 'traffictype' },
                        gateway: { label: 'gateway' },
                        netmask: { label: 'netmask' },
                        startip: { label: 'startip' },
                        endip: { label: 'endip' },
                        zoneid: { label: 'zoneid' },
                        networkofferingid: { label: 'networkofferingid' },
                        networkofferingname: { label: 'networkofferingname' },
                        networkofferingdisplaytext: { label: 'networkofferingdisplaytext' },
                        networkofferingavailability: { label: 'networkofferingavailability' },
                        isshared: { label: 'isshared' },
                        issystem: { label: 'issystem' },
                        state: { label: 'state' },
                        related: { label: 'related' },
                        broadcasturi: { label: 'broadcasturi' },
                        dns1: { label: 'dns1' },
                        type: { label: 'type' }
                      }
                    ],
                    dataProvider: testData.dataProvider.detailView('networks')
                  }
                }
              }
            }
          },
          directNetwork: {
            title: 'Direct network',
            type: 'select',
            listView: {
              section: 'networks',
              id: 'networks',
              fields: {
                name: { label: 'Name' },
                startip: { label: 'Start IP' },
                endip: { label: 'End IP' },
                type: { label: 'Type' }
              },
              dataProvider: testData.dataProvider.listView('networks'),
              detailView: {
                viewAll: { label: 'Hosts', path: 'instances' },
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'name' },
                        displaytext: { label: 'displaytext' }
                      },
                      {
                        broadcastdomaintype: { label: 'broadcastdomaintype' },
                        traffictype: { label: 'traffictype' },
                        gateway: { label: 'gateway' },
                        netmask: { label: 'netmask' },
                        startip: { label: 'startip' },
                        endip: { label: 'endip' },
                        zoneid: { label: 'zoneid' },
                        networkofferingid: { label: 'networkofferingid' },
                        networkofferingname: { label: 'networkofferingname' },
                        networkofferingdisplaytext: { label: 'networkofferingdisplaytext' },
                        networkofferingavailability: { label: 'networkofferingavailability' },
                        isshared: { label: 'isshared' },
                        issystem: { label: 'issystem' },
                        state: { label: 'state' },
                        related: { label: 'related' },
                        broadcasturi: { label: 'broadcasturi' },
                        dns1: { label: 'dns1' },
                        type: { label: 'type' }
                      }
                    ],
                    dataProvider: testData.dataProvider.detailView('networks')
                  }
                }
              }
            }
          }
        }
      },
      pods: {
        title: 'Pod',
        listView: {
          id: 'pods',
          section: 'pods',
          fields: {
            name: { label: 'Name' },
            startip: { label: 'Start IP' },
            endip: { label: 'End IP' },
            allocationstate: { label: 'Status' }
          },
          dataProvider: testData.dataProvider.listView('pods'),
          actions: {
            destroy: testData.actions.destroy('pod')
          },
          detailView: {
            viewAll: { path: '_zone.clusters', label: 'Clusters' },
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    name: { label: 'Name' }
                  },
                  {
                    allocationstate: { label: 'State' },
                    startip: { label: 'Start IP' },
                    endip: { label: 'End IP' }
                  }
                ],
                dataProvider: testData.dataProvider.detailView('pods')
              }
            }
          },
          actions: {
            add: {
              label: 'Add pod',

              createForm: {
                title: 'Add new pod',
                desc: 'Please fill in the following information to add a new pod',

                preFilter: function(args) {
                  var $guestFields = args.$form.find('.form-item[rel=guestGateway], .form-item[rel=guestNetmask], .form-item[rel=startGuestIp], .form-item[rel=endGuestIp]');
                  if (args.context.zones[0].networktype == "Basic") {
                    $guestFields.css('display', 'inline-block');
                  }
                  else if(args.context.zones[0].networktype == "Advanced") { //advanced-mode network (zone-wide VLAN)
                    $guestFields.hide();
                  }
                },

                fields: {
                  name: {
                    label: 'Name',
                    validation: { required: true }
                  },
                  gateway: {
                    label: 'Gateway',
                    validation: { required: true }
                  },
                  netmask: {
                    label: 'Netmask',
                    validation: { required: true }
                  },
                  startip: {
                    label: 'Start IP',
                    validation: { required: true }
                  },
                  endip: {
                    label: 'End IP',
                    validation: { required: false }
                  },

                  //only basic zones show guest fields (begin)
                  guestGateway: {
                    label: 'Guest Gateway',
                    validation: { required: true },
                    isHidden: true
                  },
                  guestNetmask: {
                    label: 'Guest Netmask',
                    validation: { required: true },
                    isHidden: true
                  },
                  startGuestIp: {
                    label: 'Start Guest IP',
                    validation: { required: true },
                    isHidden: true
                  },
                  endGuestIp: {
                    label: 'End Guest IP',
                    validation: { required: false },
                    isHidden: true
                  }
                  //only basic zones show guest fields (end)
                }
              },

              action: function(args) {
                args.response.success();
              },

              notification: {
                poll: testData.notifications.testPoll
              },

              messages: {
                notification: function(args) {
                  return 'Added new pod';
                }
              }
            }
          }
        }
      },
      clusters: {
        title: 'Cluster',
        listView: {
          id: 'clusters',
          section: 'clusters',
          fields: {
            name: { label: 'Name' },
            zonename: { label: 'Zone' },
            podname: { label: 'Pod' }
          },
          dataProvider: testData.dataProvider.listView('clusters'),
          actions: {
            add: {
              label: 'Add cluster',

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add a cluster?';
                },
                success: function(args) {
                  return 'Your new cluster is being created.';
                },
                notification: function(args) {
                  return 'Creating new cluster';
                },
                complete: function(args) {
                  return 'Cluster has been created successfully!';
                }
              },

              createForm: {
                title: 'Add cluster',
                desc: 'Please fill in the following data to add a new cluster.',
                fields: {
                  hypervisor: {
                    label: 'Hypervisor',
                    select: function(args) {
                      args.response.success({ data: testData.data.hypervisors });
                      args.$select.bind("change", function(event) {
                        var $form = $(this).closest('form');
                        if($(this).val() == "VMware") {
                          //$('li[input_sub_group="external"]', $dialogAddCluster).show();
                          $form.find('.form-item[rel=vCenterHost]').css('display', 'inline-block');
                          $form.find('.form-item[rel=vCenterUsername]').css('display', 'inline-block');
                          $form.find('.form-item[rel=vCenterPassword]').css('display', 'inline-block');
                          $form.find('.form-item[rel=vCenterDatacenter]').css('display', 'inline-block');

                          //$("#cluster_name_label", $dialogAddCluster).text("vCenter Cluster:");
                        }
                        else {
                          //$('li[input_group="vmware"]', $dialogAddCluster).hide();
                          $form.find('.form-item[rel=vCenterHost]').css('display', 'none');
                          $form.find('.form-item[rel=vCenterUsername]').css('display', 'none');
                          $form.find('.form-item[rel=vCenterPassword]').css('display', 'none');
                          $form.find('.form-item[rel=vCenterDatacenter]').css('display', 'none');

                          //$("#cluster_name_label", $dialogAddCluster).text("Cluster:");
                        }
                      });
                    }
                  },
                  podId: {
                    label: 'Pod',
                    select: function(args) {
                      args.response.success({ descriptionField: 'name', data: testData.data.pods });
                    }
                  },
                  name: {
                    label: 'Cluster Name',
                    validation: { required: true }
                  },

                  //hypervisor==VMWare begins here
                  vCenterHost: {
                    label: 'vCenter Host',
                    validation: { required: true }
                  },
                  vCenterUsername: {
                    label: 'vCenter Username',
                    validation: { required: true }
                  },
                  vCenterPassword: {
                    label: 'vCenter Password',
                    validation: { required: true },
                    isPassword: true
                  },
                  vCenterDatacenter: {
                    label: 'vCenter Datacenter',
                    validation: { required: true }
                  }
                  //hypervisor==VMWare ends here
                }
              },

              action: function(args) {
                args.response.success();
              },

              notification: {
                poll: testData.notifications.testPoll
              }
            },
            destroy: testData.actions.destroy('cluster')
          },
          detailView: {
            viewAll: { path: '_zone.hosts', label: 'Hosts' },
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    name: { label: 'Name' }
                  },
                  {
                    allocationstate: { label: 'State' },
                    podname: { label: 'Pod' },
                    hypervisortype: { label: 'Hypervisor' },
                    clustertype: { label: 'Cluster' }
                  }
                ],

                dataProvider: testData.dataProvider.detailView('clusters')
              }
            }
          }
        }
      },
      hosts: {
        title: 'Host',
        id: 'hosts',
        listView: {
          section: 'hosts',
          fields: {
            name: { label: 'Name' },
            zonename: { label: 'Zone' },
            podname: { label: 'Pod' }
          },
          dataProvider: testData.dataProvider.listView('hosts'),
          actions: {
            add: {
              label: 'Add host',

              createForm: {
                title: 'Add new host',
                desc: 'Please fill in the following information to add a new host fro the specified zone configuration.',
                preFilter: function(args) {
                  var $form = args.$form;
                  var $guestFields = $form.find('.form-item[rel=guestGateway], '
                                                + '.form-item[rel=guestNetmask], '
                                                + '.form-item[rel=guestIPRange]');

                  if (args.context.zones[0].name == 'Chicago') {
                    $guestFields.css('display', 'inline-block');                        
                  }
                },
                fields: {
                  pod: {
                    label: 'Pod',

                    select: function(args) {
                      /**
                       * Example to show/hide fields
                       * 
                       * -Select Pod2 to show conditional fields
                       * -Select any other field to hide conditional fields
                       */
                      args.$select.change(function() {
                        var $input = $(this);
                        var $form = $input.closest('form');

                        // Note: need to actually select the .form-item div containing the input
                        var $condTestA = $form.find('.form-item[rel=condTestA]');
                        var $condTestB = $form.find('.form-item[rel=condTestB]');

                        $condTestA.hide();
                        $condTestB.hide();

                        if ($input.val() == 2) {
                          // Note: need to show by setting display=inline-block, not .show()
                          $condTestA.css('display', 'inline-block');
                          $condTestB.css('display', 'inline-block');
                        }
                      });

                      setTimeout(function() {
                        args.response.success({
                          descriptionField: 'name',
                          data: testData.data.pods
                        });
                      }, 100);
                    }
                  },
                  cluster: {
                    label: 'Cluster',

                    dependsOn: 'pod',

                    select: function(args) {
                      setTimeout(function() {
                        args.response.success({
                          descriptionField: 'name',
                          data: testData.data.clusters
                        });                            
                      }, 20);
                    }
                  },

                  hostname: {
                    label: 'Host name',
                    validation: { required: true }
                  },

                  username: {
                    label: 'User name',
                    validation: { required: true }
                  },

                  password: {
                    label: 'Password',
                    validation: { required: true }
                  },
                  
                  /**
                   * Test for conditional fields
                   * note that these are hidden by default
                   */
                  condTestA: {
                    // Hidden by default
                    hidden: true,

                    label: 'Conditional A',
                    validation: { required: true }
                  },

                  condTestB: {
                    // Hidden by default
                    hidden: true,

                    label: 'Conditional B',
                    validation: { required: true }
                  },

                  guestGateway: {
                    hidden: true,
                    label: 'Guest Gateway',
                    validation: { required: true }
                  },

                  guestNetmask: {
                    hidden: true,
                    label: 'Guest Netmask',
                    validation: { required: true }
                  },

                  guestIPRange: {
                    hidden: true,
                    label: 'Guest IP Range',
                    validation: { required: true }
                  }
                }
              },

              action: function(args) {
                args.response.success();
              },

              notification: {
                poll: testData.notifications.customPoll(testData.data.hosts[0])
              },

              messages: {
                notification: function(args) {
                  return 'Added new host';
                }
              }
            },
            destroy: testData.actions.destroy('host')
          },
          detailView: {
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    name: { label: 'Name' },
                  },
                  {
                    type: { label: 'Type' },
                    zonename: { label: 'Zone' },
                  }
                ],

                dataProvider: testData.dataProvider.detailView('hosts')
              },
            }
          }
        }
      },
      'primary-storage': {
        title: 'Primary Storage',
        id: 'primaryStorage',
        listView: {
          section: 'primary-storage',
          fields: {
            name: { label: 'Name' },
            zonename: { label: 'Zone' },
            podname: { label: 'Pod' }
          },
          dataProvider: testData.dataProvider.listView('clusters'),
          actions: {
            add: {
              label: 'Add primary storage',

              createForm: {
                title: 'Add new primary storage',
                desc: 'Please fill in the following information to add a new primary storage',
                fields: {
                  //always appear (begin)
                  podId: {
                    label: 'Pod',
                    validation: { required: true },
                    select: function(args) {
                      args.response.success({
                        descriptionField: 'name',
                        data: testData.data.pods
                      });
                    }
                  },

                  clusterId: {
                    label: 'Cluster',
                    validation: { required: true },
                    dependsOn: 'podId',
                    select: function(args) {
                      args.response.success({
                        descriptionField: 'name',
                        data: testData.data.clusters
                      });
                    }
                  },

                  name: {
                    label: 'Name',
                    validation: { required: true }
                  },

                  protocol: {
                    label: 'Protocol',
                    validation: { required: true },
                    dependsOn: 'clusterId',
                    select: function(args) {
                      args.response.success({
                        data: [
                          {
                            id: 'nfs',
                            description: 'NFS'
                          },
                          {
                            id: 'iscsi',
                            description: 'iSCSI'
                          }
                        ]
                      });
                    }
                  },
                  //always appear (end)

                  server: {
                    label: 'Server',
                    validation: { required: true },
                    isHidden: true
                  },

                  //nfs
                  path: {
                    label: 'Path',
                    validation: { required: true },
                    isHidden: true
                  },

                  //iscsi
                  iqn: {
                    label: 'Target IQN',
                    validation: { required: true },
                    isHidden: true
                  },
                  lun: {
                    label: 'LUN #',
                    validation: { required: true },
                    isHidden: true
                  },

                  //vmfs
                  vCenterDataCenter: {
                    label: 'vCenter Datacenter',
                    validation: { required: true },
                    isHidden: true
                  },
                  vCenterDataStore: {
                    label: 'vCenter Datastore',
                    validation: { required: true },
                    isHidden: true
                  },

                  //always appear (begin)
                  storageTags: {
                    label: 'Storage Tags',
                    validation: { required: false }
                  }
                  //always appear (end)
                }
              },

              action: function(args) {
                args.response.success();
              },

              notification: {
                poll: function(args){
                  args.complete();
                }
              },

              messages: {
                notification: function(args) {
                  return 'Added new primary storage';
                }
              }
            },
            destroy: testData.actions.destroy('cluster')
          },
          detailView: {
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    name: { label: 'Name' }
                  },
                  {
                    zonename: { label: 'Zone' },
                    hostname: { label: 'Host' }
                  }
                ],

                dataProvider: testData.dataProvider.detailView('clusters')
              }
            }
          }
        }
      },
      'secondary-storage': {
        title: 'Secondary Storage',
        id: 'secondary-storage',
        listView: {
          section: 'seconary-storage',
          fields: {
            name: { label: 'Name' },
            zonename: { label: 'Zone' },
            podname: { label: 'Pod' }
          },
          dataProvider: testData.dataProvider.listView('clusters'),
          actions: {
            add: {
              label: 'Add secondary storage',

              createForm: {
                title: 'Add new secondary storage',
                desc: 'Please fill in the following information to add a new secondary storage',
                fields: {
                  nfsServer: {
                    label: 'NFS Server',
                    validation: { required: true }
                  },
                  path: {
                    label: 'Path',
                    validation: { required: true }
                  }
                }
              },

              action: function(args) {
                args.response.success();
              },

              notification: {
                poll: function(args) {
                  args.complete();
                }
              },

              messages: {
                notification: function(args) {
                  return 'Added new secondary storage';
                }
              }
            },
            destroy: testData.actions.destroy('cluster')
          },
          detailView: {
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    name: { label: 'Name' }
                  },
                  {
                    zonename: { label: 'Zone' },
                    hostname: { label: 'Host' }
                  }
                ],

                dataProvider: testData.dataProvider.detailView('clusters')
              }
            }
          }
        }
      }
    }
  };
})($, cloudStack);

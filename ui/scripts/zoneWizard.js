(function(cloudStack, $) {
  cloudStack.zoneWizard = {
    customUI: {
      publicTrafficIPRange: function(args) {
        var multiEditData = [];
        var totalIndex = 0;

        return $('<div>').multiEdit({
          context: args.context,
          noSelect: true,
          fields: {
            'gateway': { edit: true, label: 'Gateway' },
            'netmask': { edit: true, label: 'Netmask' },
            'vlanid': { edit: true, label: 'VLAN', isOptional: true },
            'startip': { edit: true, label: 'Start IP' },
            'endip': { edit: true, label: 'End IP' },
            'add-rule': { label: 'Add', addButton: true }
          },
          add: {
            label: 'Add',
            action: function(args) {
              multiEditData.push($.extend(args.data, {
                index: totalIndex
              }));

              totalIndex++;
              args.response.success();
            }
          },
          actions: {
            destroy: {
              label: 'Remove Rule',
              action: function(args) {
                multiEditData = $.grep(multiEditData, function(item) {
                  return item.index != args.context.multiRule[0].index
                });
                args.response.success();
              }
            }
          },
          dataProvider: function(args) {
            args.response.success({
              data: multiEditData
            });
          }
        });
      }
    },
    
    preFilters: {
      addPublicNetwork: function(args) {
        return args.data['network-model'] == 'Advanced' ||
               args.data['isPublicTrafficTypeEnabled'];
      },

      addBasicPhysicalNetwork: function(args) {
        return args.data['network-model'] == 'Basic';
      },

      setupPhysicalNetwork: function(args) {
        return args.data['network-model'] == 'Advanced';
      }
    },
    
    forms: {
      zone: {
        preFilter: function(args) {
          var $form = args.$form;

          $form.find('input[name=security-groups-enabled]').change(function() {
            if ($(this).is(':checked')) {
              $form.find('[rel=networkOfferingIdWithoutSG]').hide();
              $form.find('[rel=networkOfferingIdWithSG]').show();
            } else {
              $form.find('[rel=networkOfferingIdWithoutSG]').show();
              $form.find('[rel=networkOfferingIdWithSG]').hide();
            }
          });

          if (args.data['network-model'] == 'Advanced') {
            args.$form.find('[rel=security-groups-enabled]').hide();
            args.$form.find('[rel=networkOfferingIdWithoutSG]').hide();
            args.$form.find('[rel=networkOfferingIdWithSG]').hide();
          } else {
            args.$form.find('[rel=security-groups-enabled]').show();
            args.$form.find('[rel=networkOfferingIdWithoutSG]').show();
            args.$form.find('[rel=networkOfferingIdWithSG]').show();

            $form.find('input[name=security-groups-enabled]:visible').trigger('change');
          }

          setTimeout(function() {
            if ($form.find('input[name=ispublic]').is(':checked')) {
              $form.find('[rel=domain]').hide();
            }
          });
        },
        fields: {
          name: { label: 'Name', validation: { required: true } },
          dns1: { label: 'DNS 1', validation: { required: true } },
          dns2: { label: 'DNS 2' },
          internaldns1: { label: 'Internal DNS 1', validation: { required: true } },
          internaldns2: { label: 'Internal DNS 2' },
          networkdomain: { label: 'Network Domain' },
          ispublic: {
            isReverse: true,
            isBoolean: true,
            label: 'Public'
          },
          domain: {
            label: 'Domain',
            dependsOn: 'ispublic',
            isHidden: true,
            select: function(args) {
              $.ajax({
                url: createURL("listDomains"),
                data: { viewAll: true },
                dataType: "json",
                async: false,
                success: function(json) {
                  domainObjs = json.listdomainsresponse.domain;
                  args.response.success({
                    data: $.map(domainObjs, function(domain) {
                      return {
                        id: domain.id,
                        description: domain.name
                      };
                    })
                  });
                }
              });
            }
          },
          'security-groups-enabled': {
            label: 'Security Groups Enabled',
            isBoolean: true,
            isReverse: true,
          },

          networkOfferingIdWithoutSG: {
            label: 'Network Offering',
            dependsOn: 'security-groups-enabled',
            select: function(args) {
              var networkOfferingObjsWithSG = [];
              var networkOfferingObjsWithoutSG = [];
              $.ajax({
                url: createURL("listNetworkOfferings&state=Enabled&guestiptype=Shared"),
                dataType: "json",
                async: false,
                success: function(json) {
                  networkOfferingObjs = json.listnetworkofferingsresponse.networkoffering;

                  $(networkOfferingObjs).each(function() {
                    var includingSGP = false;
                    var serviceObjArray = this.service;
                    for(var k = 0; k < serviceObjArray.length; k++) {
                      if(serviceObjArray[k].name == "SecurityGroup") {
                        includingSGP = true;
                        break;
                      }
                    }
                    if(includingSGP == false) //without SG
                      networkOfferingObjsWithoutSG.push(this);
                    else //with SG
                      networkOfferingObjsWithSG.push(this);
                  });

                  var targetNetworkOfferings = networkOfferingObjsWithoutSG;

                  args.response.success({
                    data: $.map(targetNetworkOfferings, function(offering) {
                      return {
                        id: offering.id,
                        description: offering.name
                      };
                    })
                  });
                }
              });
            }
          },

          networkOfferingIdWithSG: {
            label: 'Network Offering',
            isHidden: true,
            select: function(args) {
              var networkOfferingObjsWithSG = [];
              var networkOfferingObjsWithoutSG = [];
              $.ajax({
                url: createURL("listNetworkOfferings&state=Enabled&guestiptype=Shared"),
                dataType: "json",
                async: false,
                success: function(json) {
                  networkOfferingObjs = json.listnetworkofferingsresponse.networkoffering;

                  $(networkOfferingObjs).each(function() {
                    var includingSGP = false;
                    var serviceObjArray = this.service;
                    for(var k = 0; k < serviceObjArray.length; k++) {
                      if(serviceObjArray[k].name == "SecurityGroup") {
                        includingSGP = true;
                        break;
                      }
                    }
                    if(includingSGP == false) //without SG
                      networkOfferingObjsWithoutSG.push(this);
                    else //with SG
                      networkOfferingObjsWithSG.push(this);
                  });

                  var targetNetworkOfferings = networkOfferingObjsWithSG;

                  args.response.success({
                    data: $.map(targetNetworkOfferings, function(offering) {
                      return {
                        id: offering.id,
                        description: offering.name
                      };
                    })
                  });
                }
              });
            }
          }
        }
      },

      pod: {
        fields: {
          name: {
            label: 'Pod name',
            validation: { required: true }
          },
          reservedSystemGateway: {
            label: 'Reserved system gateway',
            validation: { required: true }
          },
          reservedSystemNetmask: {
            label: 'Reserved system netmask',
            validation: { required: true }
          },
          reservedSystemStartIp: {
            label: 'Start Reserved system IP',
            validation: { required: true }
          },
          reservedSystemEndIp: {
            label: 'End Reserved system IP',
            validation: { required: false }
          }
        }
      },

      basicPhysicalNetwork: {
        fields: {
          name: { 
					  label: 'Physical network name', 
						validation: { required: true } 
					},
          isStorageTrafficTypeEnabled: { 
					  isBoolean: true, 
						label: 'Storage traffic type enabled' 
					},
					isPublicTrafficTypeEnabled: { 
					  isBoolean: true, 
						label: 'Public traffic type enabled' 
					}
        }
      },

      guestTraffic: {
        preFilter: function(args) {
          var selectedZoneObj = {
            networktype: args.data['network-model']
          };

          if (selectedZoneObj.networktype == "Basic") {
            args.$form.find('[rel=vlanRange]').hide();
            args.$form.find('[rel=vlanId]').hide();
            args.$form.find('[rel=scope]').hide();
            args.$form.find('[rel=domainId]').hide();
            args.$form.find('[rel=account]').hide();
            args.$form.find('[rel=networkdomain]').hide();

            args.$form.find('[rel=podId]').show();
          } else {  // Advanced
            args.$form.find('[rel=vlanRange]').show();
            args.$form.find('[rel=vlanId]').show();
            args.$form.find('[rel=scope]').show();
            args.$form.find('[rel=domainId]').show();
            args.$form.find('[rel=account]').show();
            args.$form.find('[rel=networkdomain]').show();
          }
        },

        fields: {
          vlanRange: {
            label: 'VLAN Range',
            range: ['vlanRangeStart', 'vlanRangeEnd'],
            validation: { required: true }
          },
          name: {
            label: 'Name',
            validation: { required: true }
          },
          description: {
            label: 'Description',
            validation: { required: true }
          },
          vlanId: {
            label: "VLAN ID"
          },
          scope: {
            label: 'Scope',
            select: function(args) {
              var array1 = [];
              var selectedZoneObj = {
                securitygroupsenabled: args.context.zones[0]['security-groups-enabled']
              }
              if(selectedZoneObj.securitygroupsenabled) {
                array1.push({id: 'account-specific', description: 'Account'});
              }
              else {
                array1.push({id: 'zone-wide', description: 'All'});
                array1.push({id: 'domain-specific', description: 'Domain'});
                array1.push({id: 'account-specific', description: 'Account'});
              }
              args.response.success({ data: array1 });

              args.$select.change(function() {
                var $form = args.$select.closest('form');
                
                if($(this).val() == "zone-wide") {
                  $form.find('[rel=domainId]').hide();
                  $form.find('[rel=account]').hide();
                }
                else if ($(this).val() == "domain-specific") {
                  $form.find('[rel=domainId]').show();
                  $form.find('[rel=account]').hide();
                }
                else if($(this).val() == "account-specific") {
                  $form.find('[rel=domainId]').show();
                  $form.find('[rel=account]').show();
                }
              });

              setTimeout(function() {
                args.$select.trigger('change');
              });
            }
          },
          domainId: {
            label: 'Domain',
            select: function(args) {
              var selectedZoneObj = {
                domainid: args.context.zones[0].domainid
              };
              
              var items = [];
              if(selectedZoneObj.domainid != null) { //list only domains under selectedZoneObj.domainid
                $.ajax({
                  url: createURL("listDomainChildren&id=" + selectedZoneObj.domainid + "&isrecursive=true"),
                  dataType: "json",
                  async: false,
                  success: function(json) {
                    var domainObjs = json.listdomainchildrenresponse.domain;
                    $(domainObjs).each(function() {
                      items.push({id: this.id, description: this.name});
                    });
                  }
                });
                $.ajax({
                  url: createURL("listDomains&id=" + selectedZoneObj.domainid),
                  dataType: "json",
                  async: false,
                  success: function(json) {
                    var domainObjs = json.listdomainsresponse.domain;
                    $(domainObjs).each(function() {
                      items.push({id: this.id, description: this.name});
                    });
                  }
                });
              }
              else { //list all domains
                $.ajax({
                  url: createURL("listDomains&listAll=true"),
                  dataType: "json",
                  async: false,
                  success: function(json) {
                    var domainObjs = json.listdomainsresponse.domain;
                    $(domainObjs).each(function() {
                      items.push({id: this.id, description: this.name});
                    });
                  }
                });
              }
              args.response.success({data: items});
            }
          },
          account: { label: 'Account' },
          guestGateway: { label: 'Guest gateway' },
          guestNetmask: { label: 'Guest netmask' },
          guestStartIp: { label: 'Guest start IP' },
          guestEndIp: { label: 'Guest end IP' },
          networkdomain: { label: 'Network domain' }
        }
      },
      cluster: {
        fields: {
          hypervisor: {
            label: 'Hypervisor',
            select: function(args) {
              $.ajax({
                url: createURL("listHypervisors"),
                dataType: "json",
                async: false,
                success: function(json) {
                  var hypervisors = json.listhypervisorsresponse.hypervisor;
                  var items = [];
                  $(hypervisors).each(function() {
                    items.push({id: this.name, description: this.name})
                  });
                  args.response.success({data: items});
                }
              });

              args.$select.bind("change", function(event) {
                var $form = $(this).closest('form');
                if($(this).val() == "VMware") {
                  //$('li[input_sub_group="external"]', $dialogAddCluster).show();
                  $form.find('[rel=vCenterHost]').css('display', 'block');
                  $form.find('[rel=vCenterUsername]').css('display', 'block');
                  $form.find('[rel=vCenterPassword]').css('display', 'block');
                  $form.find('[rel=vCenterDatacenter]').css('display', 'block');

                  //$("#cluster_name_label", $dialogAddCluster).text("vCenter Cluster:");
                }
                else {
                  //$('li[input_group="vmware"]', $dialogAddCluster).hide();
                  $form.find('[rel=vCenterHost]').css('display', 'none');
                  $form.find('[rel=vCenterUsername]').css('display', 'none');
                  $form.find('[rel=vCenterPassword]').css('display', 'none');
                  $form.find('[rel=vCenterDatacenter]').css('display', 'none');

                  //$("#cluster_name_label", $dialogAddCluster).text("Cluster:");
                }
              });
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
      host: {
        preFilter: function(args) {
          var selectedClusterObj = {
            hypervisortype: args.data.hypervisor
          };

          var $form = args.$form;

          if(selectedClusterObj.hypervisortype == "VMware") {
            //$('li[input_group="general"]', $dialogAddHost).hide();
            $form.find('[rel=hostname]').hide();
            $form.find('[rel=username]').hide();
            $form.find('[rel=password]').hide();

            //$('li[input_group="vmware"]', $dialogAddHost).show();
            $form.find('[rel=vcenterHost]').css('display', 'block');

            //$('li[input_group="baremetal"]', $dialogAddHost).hide();
            $form.find('[rel=baremetalCpuCores]').hide();
            $form.find('[rel=baremetalCpu]').hide();
            $form.find('[rel=baremetalMemory]').hide();
            $form.find('[rel=baremetalMAC]').hide();

            //$('li[input_group="Ovm"]', $dialogAddHost).hide();
            $form.find('[rel=agentUsername]').hide();
            $form.find('[rel=agentPassword]').hide();
          }
          else if (selectedClusterObj.hypervisortype == "BareMetal") {
            //$('li[input_group="general"]', $dialogAddHost).show();
            $form.find('[rel=hostname]').css('display', 'block');
            $form.find('[rel=username]').css('display', 'block');
            $form.find('[rel=password]').css('display', 'block');

            //$('li[input_group="baremetal"]', $dialogAddHost).show();
            $form.find('[rel=baremetalCpuCores]').css('display', 'block');
            $form.find('[rel=baremetalCpu]').css('display', 'block');
            $form.find('[rel=baremetalMemory]').css('display', 'block');
            $form.find('[rel=baremetalMAC]').css('display', 'block');

            //$('li[input_group="vmware"]', $dialogAddHost).hide();
            $form.find('[rel=vcenterHost]').hide();

            //$('li[input_group="Ovm"]', $dialogAddHost).hide();
            $form.find('[rel=agentUsername]').hide();
            $form.find('[rel=agentPassword]').hide();
          }
          else if (selectedClusterObj.hypervisortype == "Ovm") {
            //$('li[input_group="general"]', $dialogAddHost).show();
            $form.find('[rel=hostname]').css('display', 'block');
            $form.find('[rel=username]').css('display', 'block');
            $form.find('[rel=password]').css('display', 'block');

            //$('li[input_group="vmware"]', $dialogAddHost).hide();
            $form.find('[rel=vcenterHost]').hide();

            //$('li[input_group="baremetal"]', $dialogAddHost).hide();
            $form.find('[rel=baremetalCpuCores]').hide();
            $form.find('[rel=baremetalCpu]').hide();
            $form.find('[rel=baremetalMemory]').hide();
            $form.find('[rel=baremetalMAC]').hide();

            //$('li[input_group="Ovm"]', $dialogAddHost).show();
            $form.find('[rel=agentUsername]').css('display', 'block');
            $form.find('[rel=agentUsername]').find('input').val("oracle");
            $form.find('[rel=agentPassword]').css('display', 'block');
          }
          else {
            //$('li[input_group="general"]', $dialogAddHost).show();
            $form.find('[rel=hostname]').css('display', 'block');
            $form.find('[rel=username]').css('display', 'block');
            $form.find('[rel=password]').css('display', 'block');

            //$('li[input_group="vmware"]', $dialogAddHost).hide();
            $form.find('[rel=vcenterHost]').hide();

            //$('li[input_group="baremetal"]', $dialogAddHost).hide();
            $form.find('[rel=baremetalCpuCores]').hide();
            $form.find('[rel=baremetalCpu]').hide();
            $form.find('[rel=baremetalMemory]').hide();
            $form.find('[rel=baremetalMAC]').hide();

            //$('li[input_group="Ovm"]', $dialogAddHost).hide();
            $form.find('[rel=agentUsername]').hide();
            $form.find('[rel=agentPassword]').hide();
          }
        },
        fields: {
          hostname: {
            label: 'Host name',
            validation: { required: true },
            isHidden: true
          },

          username: {
            label: 'User name',
            validation: { required: true },
            isHidden: true
          },

          password: {
            label: 'Password',
            validation: { required: true },
            isHidden: true,
            isPassword: true
          },
          //input_group="general" ends here

          //input_group="VMWare" starts here
          vcenterHost: {
            label: 'ESX/ESXi Host',
            validation: { required: true },
            isHidden: true
          },
          //input_group="VMWare" ends here

          //input_group="BareMetal" starts here
          baremetalCpuCores: {
            label: '# of CPU Cores',
            validation: { required: true },
            isHidden: true
          },
          baremetalCpu: {
            label: 'CPU (in MHz)',
            validation: { required: true },
            isHidden: true
          },
          baremetalMemory: {
            label: 'Memory (in MB)',
            validation: { required: true },
            isHidden: true
          },
          baremetalMAC: {
            label: 'Host MAC',
            validation: { required: true },
            isHidden: true
          },
          //input_group="BareMetal" ends here

          //input_group="OVM" starts here
          agentUsername: {
            label: 'Agent Username',
            validation: { required: false },
            isHidden: true
          },
          agentPassword: {
            label: 'Agent Password',
            validation: { required: true },
            isHidden: true,
            isPassword: true
          },
          //input_group="OVM" ends here

          //always appear (begin)
          hosttags: {
            label: 'Host tags',
            validation: { required: false }
          }
          //always appear (end)
        }
      },
      primaryStorage: {
        preFilter: function(args) {},
        
        fields: {
          name: {
            label: 'Name',
            validation: { required: true }
          },

          protocol: {
            label: 'Protocol',
            validation: { required: true },
            select: function(args) {
              var selectedClusterObj = {
                hypervisortype: args.context.zones[0].hypervisor
              };

              if(selectedClusterObj == null)
                return;

              if(selectedClusterObj.hypervisortype == "KVM") {
                var items = [];
                items.push({id: "nfs", description: "nfs"});
                items.push({id: "SharedMountPoint", description: "SharedMountPoint"});
                args.response.success({data: items});
              }
              else if(selectedClusterObj.hypervisortype == "XenServer") {
                var items = [];
                items.push({id: "nfs", description: "nfs"});
                items.push({id: "PreSetup", description: "PreSetup"});
                items.push({id: "iscsi", description: "iscsi"});
                args.response.success({data: items});
              }
              else if(selectedClusterObj.hypervisortype == "VMware") {
                var items = [];
                items.push({id: "nfs", description: "nfs"});
                items.push({id: "vmfs", description: "vmfs"});
                args.response.success({data: items});
              }
              else if(selectedClusterObj.hypervisortype == "Ovm") {
                var items = [];
                items.push({id: "nfs", description: "nfs"});
                items.push({id: "ocfs2", description: "ocfs2"});
                args.response.success({data: items});
              }
              else {
                args.response.success({data:[]});
              }

              args.$select.change(function() {
                var $form = $(this).closest('form');

                var protocol = $(this).val();
                if(protocol == null)
                  return;

                if(protocol == "nfs") {
                  //$("#add_pool_server_container", $dialogAddPool).show();
                  $form.find('[rel=server]').css('display', 'block');
                  //$dialogAddPool.find("#add_pool_nfs_server").val("");
                  $form.find('[rel=server]').find(".value").find("input").val("");

                  //$('li[input_group="nfs"]', $dialogAddPool).show();
                  $form.find('[rel=path]').css('display', 'block');
                  //$dialogAddPool.find("#add_pool_path_container").find("label").text(g_dictionary["label.path"]+":");
                  $form.find('[rel=path]').find(".name").find("label").text("Path:");

                  //$('li[input_group="iscsi"]', $dialogAddPool).hide();
                  $form.find('[rel=iqn]').hide();
                  $form.find('[rel=lun]').hide();

                  //$('li[input_group="vmfs"]', $dialogAddPool).hide();
                  $form.find('[rel=vCenterDataCenter]').hide();
                  $form.find('[rel=vCenterDataStore]').hide();
                }
                else if(protocol == "ocfs2") {//ocfs2 is the same as nfs, except no server field.
                  //$dialogAddPool.find("#add_pool_server_container").hide();
                  $form.find('[rel=server]').hide();
                  //$dialogAddPool.find("#add_pool_nfs_server").val("");
                  $form.find('[rel=server]').find(".value").find("input").val("");

                  //$('li[input_group="nfs"]', $dialogAddPool).show();
                  $form.find('[rel=path]').css('display', 'block');
                  //$dialogAddPool.find("#add_pool_path_container").find("label").text(g_dictionary["label.path"]+":");
                  $form.find('[rel=path]').find(".name").find("label").text("Path:");

                  //$('li[input_group="iscsi"]', $dialogAddPool).hide();
                  $form.find('[rel=iqn]').hide();
                  $form.find('[rel=lun]').hide();

                  //$('li[input_group="vmfs"]', $dialogAddPool).hide();
                  $form.find('[rel=vCenterDataCenter]').hide();
                  $form.find('[rel=vCenterDataStore]').hide();
                }
                else if(protocol == "PreSetup") {
                  //$dialogAddPool.find("#add_pool_server_container").hide();
                  $form.find('[rel=server]').hide();
                  //$dialogAddPool.find("#add_pool_nfs_server").val("localhost");
                  $form.find('[rel=server]').find(".value").find("input").val("localhost");

                  //$('li[input_group="nfs"]', $dialogAddPool).show();
                  $form.find('[rel=path]').css('display', 'block');
                  //$dialogAddPool.find("#add_pool_path_container").find("label").text(g_dictionary["label.SR.name"]+":");
                  $form.find('[rel=path]').find(".name").find("label").text("SR Name-Label:");

                  //$('li[input_group="iscsi"]', $dialogAddPool).hide();
                  $form.find('[rel=iqn]').hide();
                  $form.find('[rel=lun]').hide();

                  //$('li[input_group="vmfs"]', $dialogAddPool).hide();
                  $form.find('[rel=vCenterDataCenter]').hide();
                  $form.find('[rel=vCenterDataStore]').hide();
                }
                else if(protocol == "iscsi") {
                  //$dialogAddPool.find("#add_pool_server_container").show();
                  $form.find('[rel=server]').css('display', 'block');
                  //$dialogAddPool.find("#add_pool_nfs_server").val("");
                  $form.find('[rel=server]').find(".value").find("input").val("");

                  //$('li[input_group="nfs"]', $dialogAddPool).hide();
                  $form.find('[rel=path]').hide();

                  //$('li[input_group="iscsi"]', $dialogAddPool).show();
                  $form.find('[rel=iqn]').css('display', 'block');
                  $form.find('[rel=lun]').css('display', 'block');

                  //$('li[input_group="vmfs"]', $dialogAddPool).hide();
                  $form.find('[rel=vCenterDataCenter]').hide();
                  $form.find('[rel=vCenterDataStore]').hide();
                }
                else if(protocol == "vmfs") {
                  //$dialogAddPool.find("#add_pool_server_container").show();
                  $form.find('[rel=server]').css('display', 'block');
                  //$dialogAddPool.find("#add_pool_nfs_server").val("");
                  $form.find('[rel=server]').find(".value").find("input").val("");

                  //$('li[input_group="nfs"]', $dialogAddPool).hide();
                  $form.find('[rel=path]').hide();

                  //$('li[input_group="iscsi"]', $dialogAddPool).hide();
                  $form.find('[rel=iqn]').hide();
                  $form.find('[rel=lun]').hide();

                  //$('li[input_group="vmfs"]', $dialogAddPool).show();
                  $form.find('[rel=vCenterDataCenter]').css('display', 'block');
                  $form.find('[rel=vCenterDataStore]').css('display', 'block');
                }
                else if(protocol == "SharedMountPoint") {  //"SharedMountPoint" show the same fields as "nfs" does.
                  //$dialogAddPool.find("#add_pool_server_container").hide();
                  $form.find('[rel=server]').hide();
                  //$dialogAddPool.find("#add_pool_nfs_server").val("localhost");
                  $form.find('[rel=server]').find(".value").find("input").val("localhost");

                  //$('li[input_group="nfs"]', $dialogAddPool).show();
                  $form.find('[rel=path]').css('display', 'block');
                  $form.find('[rel=path]').find(".name").find("label").text("Path:");

                  //$('li[input_group="iscsi"]', $dialogAddPool).hide();
                  $form.find('[rel=iqn]').hide();
                  $form.find('[rel=lun]').hide();

                  //$('li[input_group="vmfs"]', $dialogAddPool).hide();
                  $form.find('[rel=vCenterDataCenter]').hide();
                  $form.find('[rel=vCenterDataStore]').hide();
                }
                else {
                  //$dialogAddPool.find("#add_pool_server_container").show();
                  $form.find('[rel=server]').css('display', 'block');
                  //$dialogAddPool.find("#add_pool_nfs_server").val("");
                  $form.find('[rel=server]').find(".value").find("input").val("");

                  //$('li[input_group="iscsi"]', $dialogAddPool).hide();
                  $form.find('[rel=iqn]').hide();
                  $form.find('[rel=lun]').hide();

                  //$('li[input_group="vmfs"]', $dialogAddPool).hide();
                  $form.find('[rel=vCenterDataCenter]').hide();
                  $form.find('[rel=vCenterDataStore]').hide();
                }
              });

              args.$select.trigger("change");
            }
          },
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
      secondaryStorage: {
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
      }
    },

    action: function(args) {    
      var success = args.response.success;
      var error = args.response.error;
      var message = args.response.message;
      //var data = args.data; 
      var startFn = args.startFn;

			var addPodFnBeingCalled = false;
			
      var stepFns = {
        addZone: function() {
          message('Creating zone');
          
					var array1 = [];					
					var networkType = args.data.zone.networkType;  //"Basic", "Advanced"			
					array1.push("&networktype=" + todb(networkType));
					if(networkType == "Advanced") 
						array1.push("&securitygroupenabled=false");  					

					array1.push("&name=" + todb(args.data.zone.name));

					array1.push("&dns1=" + todb(args.data.zone.dns1));

					var dns2 = args.data.zone.dns2;
					if (dns2 != null && dns2.length > 0)
						array1.push("&dns2=" + todb(dns2));

					array1.push("&internaldns1="+todb(args.data.zone.internaldns1));

					var internaldns2 = args.data.zone.internaldns2;
					if (internaldns2 != null && internaldns2.length > 0)
						array1.push("&internaldns2=" + todb(internaldns2));
						
					if(args.data.zone.ispublic == null) //public checkbox is unchecked
						array1.push("&domainid=" + args.data.zone.domain);
				 
					if(args.data.zone.networkdomain != null && args.data.zone.networkdomain.length > 0)  
						array1.push("&domain=" + todb(args.data.zone.networkdomain));
										
					$.ajax({
						url: createURL("createZone" + array1.join("")),
						dataType: "json",
						async: false,
						success: function(json) {	
							stepFns.addPhysicalNetworks({
								data: $.extend(args.data, {
									returnedZone: json.createzoneresponse.zone
								})
							});							
						}
					});
        },
        
        addPhysicalNetworks: function(args) {
          message('Creating physical network(s)');
          	
          var returnedPhysicalNetworks = [];
					
					if(args.data.zone.networkType == "Basic") {					  
						var requestedTrafficTypeCount = 2;
						if(args.data.basicPhysicalNetwork.isPublicTrafficTypeEnabled == "on")
						  requestedTrafficTypeCount++;
						if(args.data.basicPhysicalNetwork.isStorageTrafficTypeEnabled == "on")
						  requestedTrafficTypeCount++;
						
						$.ajax({
							url: createURL("createPhysicalNetwork&zoneid=" + args.data.returnedZone.id + "&name=" + todb(args.data.basicPhysicalNetwork.name)),
							dataType: "json",														
							success: function(json) {														 
								var jobId = json.createphysicalnetworkresponse.jobid;
								var timerKey = "createPhysicalNetworkJob_" + jobId;
								$("body").everyTime(2000, timerKey, function(){															  
									$.ajax({
										url: createURL("queryAsyncJobResult&jobid=" + jobId),
										dataType: "json",
										success: function(json) {
											var result = json.queryasyncjobresultresponse;																		
											if (result.jobstatus == 0) {
												return; //Job has not completed
											}
											else {
												$("body").stopTime(timerKey);
												if (result.jobstatus == 1) {
													var returnedBasicPhysicalNetwork = result.jobresult.physicalnetwork;																										
																										
													var returnedTrafficTypes = [];		
													
													$.ajax({
														url: createURL("addTrafficType&trafficType=Guest&physicalnetworkid=" + returnedBasicPhysicalNetwork.id),
														dataType: "json",
														success: function(json) {																					  
															var jobId = json.addtraffictyperesponse.jobid;
															var timerKey = "addTrafficTypeJob_" + jobId;
															$("body").everyTime(2000, timerKey, function() {																						  
																$.ajax({
																	url: createURL("queryAsyncJobResult&jobid=" + jobId),
																	dataType: "json", 
																	success: function(json) {
																		var result = json.queryasyncjobresultresponse;
																		if (result.jobstatus == 0) {
																			return; //Job has not completed
																		}
																		else {
																			$("body").stopTime(timerKey);
																			if (result.jobstatus == 1) {	                                          
																				returnedTrafficTypes.push(result.jobresult.traffictype);	
																				
																				if(returnedTrafficTypes.length == requestedTrafficTypeCount) { //all requested traffic types have been added
																				  returnedBasicPhysicalNetwork.returnedTrafficTypes = returnedTrafficTypes;
																																																													
																					stepFns.afterCreateZonePhysicalNetworkTrafficTypes({
																						data: $.extend(args.data, {
																							returnedBasicPhysicalNetwork: returnedBasicPhysicalNetwork
																						})
																					});			
																				}																				
																			}
																			else if (result.jobstatus == 2) {
																				alert("Failed to add Guest traffic type to basic zone. Error: " + fromdb(result.jobresult.errortext));
																			}
																		}	
																	},																								
																	error: function(XMLHttpResponse) {
																		var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																		alert("Failed to add Guest traffic type to basic zone. Error: " + errorMsg);
																	}
																});																							
															});	
														}
													});		
													
                          $.ajax({
														url: createURL("addTrafficType&trafficType=Management&physicalnetworkid=" + returnedBasicPhysicalNetwork.id),
														dataType: "json",
														success: function(json) {																					  
															var jobId = json.addtraffictyperesponse.jobid;
															var timerKey = "addTrafficTypeJob_" + jobId;
															$("body").everyTime(2000, timerKey, function() {																						  
																$.ajax({
																	url: createURL("queryAsyncJobResult&jobid=" + jobId),
																	dataType: "json", 
																	success: function(json) {
																		var result = json.queryasyncjobresultresponse;
																		if (result.jobstatus == 0) {
																			return; //Job has not completed
																		}
																		else {
																			$("body").stopTime(timerKey);
																			if (result.jobstatus == 1) {	                                          
																				returnedTrafficTypes.push(result.jobresult.traffictype);	
																				
																				if(returnedTrafficTypes.length == requestedTrafficTypeCount) { //all requested traffic types have been added
																				  returnedBasicPhysicalNetwork.returnedTrafficTypes = returnedTrafficTypes;
																																																													
																					stepFns.afterCreateZonePhysicalNetworkTrafficTypes({
																						data: $.extend(args.data, {
																							returnedBasicPhysicalNetwork: returnedBasicPhysicalNetwork
																						})
																					});			
																				}																									
																			}
																			else if (result.jobstatus == 2) {
																				alert("Failed to add Management traffic type to basic zone. Error: " + fromdb(result.jobresult.errortext));
																			}
																		}	
																	},																								
																	error: function(XMLHttpResponse) {
																		var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																		alert("Failed to add Management traffic type to basic zone. Error: " + errorMsg);
																	}
																});																							
															});	
														}
													});			

                          if(args.data.basicPhysicalNetwork.isPublicTrafficTypeEnabled == "on") {													
														$.ajax({
															url: createURL("addTrafficType&trafficType=Public&physicalnetworkid=" + returnedBasicPhysicalNetwork.id),
															dataType: "json",
															success: function(json) {																					  
																var jobId = json.addtraffictyperesponse.jobid;
																var timerKey = "addTrafficTypeJob_" + jobId;
																$("body").everyTime(2000, timerKey, function() {																						  
																	$.ajax({
																		url: createURL("queryAsyncJobResult&jobid=" + jobId),
																		dataType: "json", 
																		success: function(json) {
																			var result = json.queryasyncjobresultresponse;
																			if (result.jobstatus == 0) {
																				return; //Job has not completed
																			}
																			else {
																				$("body").stopTime(timerKey);
																				if (result.jobstatus == 1) {	                                          
																					returnedTrafficTypes.push(result.jobresult.traffictype);	
																					
																					if(returnedTrafficTypes.length == requestedTrafficTypeCount) { //all requested traffic types have been added
																						returnedBasicPhysicalNetwork.returnedTrafficTypes = returnedTrafficTypes;
																																																														
																						stepFns.afterCreateZonePhysicalNetworkTrafficTypes({
																							data: $.extend(args.data, {
																								returnedBasicPhysicalNetwork: returnedBasicPhysicalNetwork
																							})
																						});			
																					}		
																				}
																				else if (result.jobstatus == 2) {
																					alert("Failed to add Public traffic type to basic zone. Error: " + fromdb(result.jobresult.errortext));
																				}
																			}	
																		},																								
																		error: function(XMLHttpResponse) {
																			var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																			alert("Failed to add Public traffic type to basic zone. Error: " + errorMsg);
																		}
																	});																							
																});	
															}
														});		
													}
													
													if(args.data.basicPhysicalNetwork.isStorageTrafficTypeEnabled == "on") {													
														$.ajax({
															url: createURL("addTrafficType&trafficType=Storage&physicalnetworkid=" + returnedBasicPhysicalNetwork.id),
															dataType: "json",
															success: function(json) {																					  
																var jobId = json.addtraffictyperesponse.jobid;
																var timerKey = "addTrafficTypeJob_" + jobId;
																$("body").everyTime(2000, timerKey, function() {																						  
																	$.ajax({
																		url: createURL("queryAsyncJobResult&jobid=" + jobId),
																		dataType: "json", 
																		success: function(json) {
																			var result = json.queryasyncjobresultresponse;
																			if (result.jobstatus == 0) {
																				return; //Job has not completed
																			}
																			else {
																				$("body").stopTime(timerKey);
																				if (result.jobstatus == 1) {	                                          
																					returnedTrafficTypes.push(result.jobresult.traffictype);	
																					
																					if(returnedTrafficTypes.length == requestedTrafficTypeCount) { //all requested traffic types have been added
																						returnedBasicPhysicalNetwork.returnedTrafficTypes = returnedTrafficTypes;
																																																														
																						stepFns.afterCreateZonePhysicalNetworkTrafficTypes({
																							data: $.extend(args.data, {
																								returnedBasicPhysicalNetwork: returnedBasicPhysicalNetwork
																							})
																						});			
																					}			
																				}
																				else if (result.jobstatus == 2) {
																					alert("Failed to add Storage traffic type to basic zone. Error: " + fromdb(result.jobresult.errortext));
																				}
																			}	
																		},																								
																		error: function(XMLHttpResponse) {
																			var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																			alert("Failed to add Storage traffic type to basic zone. Error: " + errorMsg);
																		}
																	});																							
																});	
															}
														});		
													}	                      
												}
												else if (result.jobstatus == 2) {																			  
													alert("createPhysicalNetwork failed. Error: " + fromdb(result.jobresult.errortext));
												}
											}		
										},																																		
										error: function(XMLHttpResponse) {
											var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
											alert("createPhysicalNetwork failed. Error: " + errorMsg);
										}																			
									});															
								});															
							}
						});		
					}					
					else if(args.data.zone.networkType == "Advanced") {					  
						$(args.data.physicalNetworks).each(function(){					  	            
							var thisPhysicalNetwork = this;						
							$.ajax({
								url: createURL("createPhysicalNetwork&zoneid=" + args.data.returnedZone.id + "&name=" + todb(thisPhysicalNetwork.name)),
								dataType: "json",														
								success: function(json) {														 
									var jobId = json.createphysicalnetworkresponse.jobid;
									var timerKey = "createPhysicalNetworkJob_" + jobId;
									$("body").everyTime(2000, timerKey, function(){															  
										$.ajax({
											url: createURL("queryAsyncJobResult&jobid=" + jobId),
											dataType: "json",
											success: function(json) {
												var result = json.queryasyncjobresultresponse;																		
												if (result.jobstatus == 0) {
													return; //Job has not completed
												}
												else {
													$("body").stopTime(timerKey);
													if (result.jobstatus == 1) {
														var returnedPhysicalNetwork = result.jobresult.physicalnetwork;																										
																											
														var returnedTrafficTypes = [];													
														$(thisPhysicalNetwork.trafficTypes).each(function(){													  
															var thisTrafficType = this;
															var apiCmd = "addTrafficType&physicalnetworkid=" + returnedPhysicalNetwork.id;
															if(thisTrafficType == "public")
																apiCmd += "&trafficType=Public";
															else if(thisTrafficType == "management")
																apiCmd += "&trafficType=Management";
															else if(thisTrafficType == "guest")
																apiCmd += "&trafficType=Guest";
															else if(thisTrafficType == "storage")
																apiCmd += "&trafficType=Storage";
																													
															$.ajax({
																url: createURL(apiCmd),
																dataType: "json",
																success: function(json) {																					  
																	var jobId = json.addtraffictyperesponse.jobid;
																	var timerKey = "addTrafficTypeJob_" + jobId;
																	$("body").everyTime(2000, timerKey, function() {																						  
																		$.ajax({
																			url: createURL("queryAsyncJobResult&jobid=" + jobId),
																			dataType: "json", 
																			success: function(json) {
																				var result = json.queryasyncjobresultresponse;
																				if (result.jobstatus == 0) {
																					return; //Job has not completed
																				}
																				else {
																					$("body").stopTime(timerKey);
																					if (result.jobstatus == 1) {	                                          
																						returnedTrafficTypes.push(result.jobresult.traffictype);			

																						if(returnedTrafficTypes.length == thisPhysicalNetwork.trafficTypes.length) { //this physical network is complete (specified traffic types are added)
																							returnedPhysicalNetwork.returnedTrafficTypes = returnedTrafficTypes;
																							returnedPhysicalNetworks.push(returnedPhysicalNetwork);
																							
																							if(returnedPhysicalNetworks.length == args.data.physicalNetworks.length) { //all physical networks are complete
																								stepFns.afterCreateZonePhysicalNetworkTrafficTypes({
																									data: $.extend(args.data, {
																										returnedPhysicalNetworks: returnedPhysicalNetworks
																									})
																								});
																							}
																						}																						
																					}
																					else if (result.jobstatus == 2) {
																						alert(apiCmd + " failed. Error: " + fromdb(result.jobresult.errortext));
																					}
																				}	
																			},																								
																			error: function(XMLHttpResponse) {
																				var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																				alert(apiCmd + " failed. Error: " + errorMsg);
																			}
																		});																							
																	});	
																}
															});								
														});													
													}
													else if (result.jobstatus == 2) {																			  
														alert("createPhysicalNetwork failed. Error: " + fromdb(result.jobresult.errortext));
													}
												}		
											},																																		
											error: function(XMLHttpResponse) {
												var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
												alert("createPhysicalNetwork failed. Error: " + errorMsg);
											}																			
										});															
									});															
								}
							});										
						});	
          }        				
        },
        
				//enable physical network, enable virtual router element, enable network service provider
				afterCreateZonePhysicalNetworkTrafficTypes: function(args) {				  
					message('Enable physical network');
					
					if(args.data.zone.networkType == "Basic") {							  			
						$.ajax({
							url: createURL("updatePhysicalNetwork&state=Enabled&id=" + args.data.returnedBasicPhysicalNetwork.id),
							dataType: "json",
							success: function(json) {
								var jobId = json.updatephysicalnetworkresponse.jobid;
								var timerKey = "updatePhysicalNetworkJob_"+jobId;
								$("body").everyTime(2000, timerKey, function() {
									$.ajax({
										url: createURL("queryAsyncJobResult&jobId="+jobId),
										dataType: "json",
										success: function(json) {
											var result = json.queryasyncjobresultresponse;
											if (result.jobstatus == 0) {
												return; //Job has not completed
											}
											else {
												$("body").stopTime(timerKey);
												if (result.jobstatus == 1) {
													//alert("updatePhysicalNetwork succeeded.");

													// get network service provider ID of Virtual Router
													var virtualRouterProviderId;
													$.ajax({
														url: createURL("listNetworkServiceProviders&name=VirtualRouter&physicalNetworkId=" + args.data.returnedBasicPhysicalNetwork.id),
														dataType: "json",
														async: false,
														success: function(json) {
															var items = json.listnetworkserviceprovidersresponse.networkserviceprovider;
															if(items != null && items.length > 0) {
																virtualRouterProviderId = items[0].id;
															}
														}
													});
													if(virtualRouterProviderId == null) {
														alert("error: listNetworkServiceProviders API doesn't return VirtualRouter provider ID");
														return;
													}

													var virtualRouterElementId;
													$.ajax({
														url: createURL("listVirtualRouterElements&nspid=" + virtualRouterProviderId),
														dataType: "json",
														async: false,
														success: function(json) {
															var items = json.listvirtualrouterelementsresponse.virtualrouterelement;
															if(items != null && items.length > 0) {
																virtualRouterElementId = items[0].id;
															}
														}
													});
													if(virtualRouterElementId == null) {
														alert("error: listVirtualRouterElements API doesn't return Virtual Router Element Id");
														return;
													}

													$.ajax({
														url: createURL("configureVirtualRouterElement&enabled=true&id=" + virtualRouterElementId),
														dataType: "json",
														async: false,
														success: function(json) {
															var jobId = json.configurevirtualrouterelementresponse.jobid;
															var timerKey = "configureVirtualRouterElementJob_"+jobId;
															$("body").everyTime(2000, timerKey, function() {
																$.ajax({
																	url: createURL("queryAsyncJobResult&jobId="+jobId),
																	dataType: "json",
																	success: function(json) {
																		var result = json.queryasyncjobresultresponse;
																		if (result.jobstatus == 0) {
																			return; //Job has not completed
																		}
																		else {
																			$("body").stopTime(timerKey);
																			if (result.jobstatus == 1) {
																				//alert("configureVirtualRouterElement succeeded.");

																				$.ajax({
																					url: createURL("updateNetworkServiceProvider&state=Enabled&id=" + virtualRouterProviderId),
																					dataType: "json",
																					async: false,
																					success: function(json) {
																						var jobId = json.updatenetworkserviceproviderresponse.jobid;
																						var timerKey = "updateNetworkServiceProviderJob_"+jobId;
																						$("body").everyTime(2000, timerKey, function() {
																							$.ajax({
																								url: createURL("queryAsyncJobResult&jobId="+jobId),
																								dataType: "json",
																								success: function(json) {
																									var result = json.queryasyncjobresultresponse;
																									if (result.jobstatus == 0) {
																										return; //Job has not completed
																									}
																									else {
																										$("body").stopTime(timerKey);
																										if (result.jobstatus == 1) {
																											//alert("Virtual Router Provider is enabled");
																																																						
																											if(args.data.zone["security-groups-enabled"] == "on") { //need to Enable security group provider first
																												// get network service provider ID of Security Group
																												var securityGroupProviderId;
																												$.ajax({
																													url: createURL("listNetworkServiceProviders&name=SecurityGroupProvider&physicalNetworkId=" + args.data.returnedBasicPhysicalNetwork.id),
																													dataType: "json",
																													async: false,
																													success: function(json) {																																				
																														var items = json.listnetworkserviceprovidersresponse.networkserviceprovider;
																														if(items != null && items.length > 0) {
																															securityGroupProviderId = items[0].id;
																														}
																													}
																												});
																												if(securityGroupProviderId == null) {
																													alert("error: listNetworkServiceProviders API doesn't return security group provider ID");
																													return;
																												}                                      
																													
																												$.ajax({
																													url: createURL("updateNetworkServiceProvider&state=Enabled&id=" + securityGroupProviderId),
																													dataType: "json",
																													async: false,
																													success: function(json) {
																														var jobId = json.updatenetworkserviceproviderresponse.jobid;
																														var timerKey = "updateNetworkServiceProviderJob_"+jobId;
																														$("body").everyTime(2000, timerKey, function() {
																															$.ajax({
																																url: createURL("queryAsyncJobResult&jobId="+jobId),
																																dataType: "json",
																																success: function(json) {
																																	var result = json.queryasyncjobresultresponse;
																																	if (result.jobstatus == 0) {
																																		return; //Job has not completed
																																	}
																																	else {																																										
																																		$("body").stopTime(timerKey);
																																		if (result.jobstatus == 1) {
																																			//alert("Security group provider is enabled");

																																			//create network (for basic zone only)
																																			var array2 = [];
																																			array2.push("&zoneid=" + args.data.returnedZone.id);
																																			array2.push("&name=guestNetworkForBasicZone");
																																			array2.push("&displaytext=guestNetworkForBasicZone");
																																			array2.push("&networkofferingid=" + args.data.zone.networkOfferingIdWithSG); 
																																			$.ajax({
																																				url: createURL("createNetwork" + array2.join("")),
																																				dataType: "json",
																																				async: false,
																																				success: function(json) {		
																																					if(addPodFnBeingCalled == false) {																																					
																																						stepFns.addPod({
																																							data: args.data
																																						});
																																					}
																																				}
																																			});
																																		}
																																		else if (result.jobstatus == 2) {
																																			alert("failed to enable security group provider. Error: " + fromdb(result.jobresult.errortext));
																																		}
																																	}
																																},
																																error: function(XMLHttpResponse) {
																																	var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																																	alert("updateNetworkServiceProvider failed. Error: " + errorMsg);
																																}
																															});
																														});
																													}
																												});
																											}					
																											else { //args.data.zone["security-groups-enabled"] == null
																												//create network (for basic zone only)
																												var array2 = [];
																												array2.push("&zoneid=" + args.data.returnedZone.id);
																												array2.push("&name=guestNetworkForBasicZone");
																												array2.push("&displaytext=guestNetworkForBasicZone");
																												array2.push("&networkofferingid=" + args.data.zone.networkOfferingIdWithoutSG); 
																												$.ajax({
																													url: createURL("createNetwork" + array2.join("")),
																													dataType: "json",
																													async: false,
																													success: function(json) {
																														if(addPodFnBeingCalled == false) {		
																															stepFns.addPod({
																																data: args.data
																															});
																														}																																
																													}
																												});		
																											}																															  
																										}
																										else if (result.jobstatus == 2) {
																											alert("failed to enable Virtual Router Provider. Error: " + fromdb(result.jobresult.errortext));
																										}
																									}
																								},
																								error: function(XMLHttpResponse) {
																									var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																									alert("updateNetworkServiceProvider failed. Error: " + errorMsg);
																								}
																							});
																						});
																					}
																				});
																			}
																			else if (result.jobstatus == 2) {
																				alert("configureVirtualRouterElement failed. Error: " + fromdb(result.jobresult.errortext));
																			}
																		}
																	},
																	error: function(XMLHttpResponse) {
																		var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																		alert("configureVirtualRouterElement failed. Error: " + errorMsg);
																	}
																});
															});
														}
													});
												}
												else if (result.jobstatus == 2) {
													alert("updatePhysicalNetwork failed. Error: " + fromdb(result.jobresult.errortext));
												}
											}
										},
										error: function(XMLHttpResponse) {
											var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
											alert("updatePhysicalNetwork failed. Error: " + errorMsg);
										}
									});
								});
							}
						});		
					}
          else if(args.data.zone.networkType == "Advanced") {							
						$(args.data.returnedPhysicalNetworks).each(function(){					 
							var thisPhysicalNetwork = this;
							$.ajax({
								url: createURL("updatePhysicalNetwork&state=Enabled&id=" + thisPhysicalNetwork.id),
								dataType: "json",
								success: function(json) {
									var jobId = json.updatephysicalnetworkresponse.jobid;
									var timerKey = "updatePhysicalNetworkJob_"+jobId;
									$("body").everyTime(2000, timerKey, function() {
										$.ajax({
											url: createURL("queryAsyncJobResult&jobId="+jobId),
											dataType: "json",
											success: function(json) {
												var result = json.queryasyncjobresultresponse;
												if (result.jobstatus == 0) {
													return; //Job has not completed
												}
												else {
													$("body").stopTime(timerKey);
													if (result.jobstatus == 1) {
														//alert("updatePhysicalNetwork succeeded.");

														// get network service provider ID of Virtual Router
														var virtualRouterProviderId;
														$.ajax({
															url: createURL("listNetworkServiceProviders&name=VirtualRouter&physicalNetworkId=" + thisPhysicalNetwork.id),
															dataType: "json",
															async: false,
															success: function(json) {
																var items = json.listnetworkserviceprovidersresponse.networkserviceprovider;
																if(items != null && items.length > 0) {
																	virtualRouterProviderId = items[0].id;
																}
															}
														});
														if(virtualRouterProviderId == null) {
															alert("error: listNetworkServiceProviders API doesn't return VirtualRouter provider ID");
															return;
														}

														var virtualRouterElementId;
														$.ajax({
															url: createURL("listVirtualRouterElements&nspid=" + virtualRouterProviderId),
															dataType: "json",
															async: false,
															success: function(json) {
																var items = json.listvirtualrouterelementsresponse.virtualrouterelement;
																if(items != null && items.length > 0) {
																	virtualRouterElementId = items[0].id;
																}
															}
														});
														if(virtualRouterElementId == null) {
															alert("error: listVirtualRouterElements API doesn't return Virtual Router Element Id");
															return;
														}

														$.ajax({
															url: createURL("configureVirtualRouterElement&enabled=true&id=" + virtualRouterElementId),
															dataType: "json",
															async: false,
															success: function(json) {
																var jobId = json.configurevirtualrouterelementresponse.jobid;
																var timerKey = "configureVirtualRouterElementJob_"+jobId;
																$("body").everyTime(2000, timerKey, function() {
																	$.ajax({
																		url: createURL("queryAsyncJobResult&jobId="+jobId),
																		dataType: "json",
																		success: function(json) {
																			var result = json.queryasyncjobresultresponse;
																			if (result.jobstatus == 0) {
																				return; //Job has not completed
																			}
																			else {
																				$("body").stopTime(timerKey);
																				if (result.jobstatus == 1) {
																					//alert("configureVirtualRouterElement succeeded.");

																					$.ajax({
																						url: createURL("updateNetworkServiceProvider&state=Enabled&id=" + virtualRouterProviderId),
																						dataType: "json",
																						async: false,
																						success: function(json) {
																							var jobId = json.updatenetworkserviceproviderresponse.jobid;
																							var timerKey = "updateNetworkServiceProviderJob_"+jobId;
																							$("body").everyTime(2000, timerKey, function() {
																								$.ajax({
																									url: createURL("queryAsyncJobResult&jobId="+jobId),
																									dataType: "json",
																									success: function(json) {
																										var result = json.queryasyncjobresultresponse;
																										if (result.jobstatus == 0) {
																											return; //Job has not completed
																										}
																										else {
																											$("body").stopTime(timerKey);
																											if (result.jobstatus == 1) {
																												//alert("Virtual Router Provider is enabled");																												
																												if(addPodFnBeingCalled == false) {																													
																													stepFns.addPod({
																														data: args.data
																													});
																												}																													
																											}
																											else if (result.jobstatus == 2) {
																												alert("failed to enable Virtual Router Provider. Error: " + fromdb(result.jobresult.errortext));
																											}
																										}
																									},
																									error: function(XMLHttpResponse) {
																										var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																										alert("updateNetworkServiceProvider failed. Error: " + errorMsg);
																									}
																								});
																							});
																						}
																					});
																				}
																				else if (result.jobstatus == 2) {
																					alert("configureVirtualRouterElement failed. Error: " + fromdb(result.jobresult.errortext));
																				}
																			}
																		},
																		error: function(XMLHttpResponse) {
																			var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
																			alert("configureVirtualRouterElement failed. Error: " + errorMsg);
																		}
																	});
																});
															}
														});
													}
													else if (result.jobstatus == 2) {
														alert("updatePhysicalNetwork failed. Error: " + fromdb(result.jobresult.errortext));
													}
												}
											},
											error: function(XMLHttpResponse) {
												var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
												alert("updatePhysicalNetwork failed. Error: " + errorMsg);
											}
										});
									});
								}
							});							
						});		
          }						
				},
				
				
        addPod: function(args) {
				  addPodFnBeingCalled = true;
				
          message('Creating pod');
                   
					var array3 = [];
					array3.push("&zoneId=" + args.data.returnedZone.id);
					array3.push("&name=" + todb(args.data.pod.name));
					array3.push("&gateway=" + todb(args.data.pod.reservedSystemGateway));
					array3.push("&netmask=" + todb(args.data.pod.reservedSystemNetmask));
					array3.push("&startIp=" + todb(args.data.pod.reservedSystemStartIp));

					var endip = args.data.pod.reservedSystemEndIp;      //optional
					if (endip != null && endip.length > 0)
						array3.push("&endIp=" + todb(endip));

					$.ajax({
						url: createURL("createPod" + array3.join("")),
						dataType: "json",
						async: false,
						success: function(json) {						  							
							stepFns.configurePublicTraffic({
								data: $.extend(args.data, {
									returnedpod: json.createpodresponse.pod
								})
							});		
						},
						error: function(XMLHttpResponse) {
							var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
							alert("createPod failed. Error: " + errorMsg);
						}
					}); 						
        },  
       			
        configurePublicTraffic: function(args) {					
					if((args.data.zone.networkType == "Basic" && args.data.basicPhysicalNetwork.isPublicTrafficTypeEnabled == "on")
					 ||(args.data.zone.networkType == "Advanced")) {	
					 
						message('Configuring public traffic');

						var returnedPublicTraffic = [];
            $(args.data.publicTraffic).each(function(){
						  //debugger;							
							var array1 = [];
							array1.push("&zoneId=" + args.data.returnedZone.id);

							if (this.vlanid != null && this.vlanid.length > 0)
								array1.push("&vlan=" + todb(this.vlanid));
							else
								array1.push("&vlan=untagged");

							array1.push("&gateway=" + this.gateway);
							array1.push("&netmask=" + this.netmask);
							array1.push("&startip=" + this.startip);
							if(this.endip != null && this.endip.length > 0)
								array1.push("&endip=" + this.endip);

							if(args.data.returnedZone.securitygroupsenabled == false)
								array1.push("&forVirtualNetwork=true");
							else
								array1.push("&forVirtualNetwork=false");

							$.ajax({
								url: createURL("createVlanIpRange" + array1.join("")),
								dataType: "json",
								success: function(json) {								 
									var item = json.createvlaniprangeresponse.vlan;
									returnedPublicTraffic.push(item);
								},
								error: function(XMLHttpResponse) {
									var errorMsg = parseXMLHttpResponse(XMLHttpResponse);
									args.response.error(errorMsg);
								}
							});							
						});				

            //debugger;
						stepFns.configureGuestTraffic({
							data: $.extend(args.data, {
								returnedPublicTraffic: returnedPublicTraffic
							})
						});		
						
					}
					else { //basic zone without public traffic type , skip to next step
					  stepFns.configureGuestTraffic({
							data: args.data
						});		
					}
        },    
				
        configureGuestTraffic: function(args) {
          message('Configuring guest traffic');

					//debugger;
          setTimeout(function() {
            stepFns.addCluster({
              data: args.data
            });
          }, 200);
        },
        				
        addCluster: function(args) {
          message('Creating cluster');
          
          var cluster = {};

          setTimeout(function() {
            stepFns.addHost({
              data: $.extend(args.data, {
                cluster: cluster
              })
            });
          }, 200);
        },
        
        addHost: function(args) {
          message('Adding host');
          
          var host = {};

          setTimeout(function() {
            stepFns.addPrimaryStorage({
              data: $.extend(args.data, {
                host: host
              })
            });
          }, 400);
        },
        
        addPrimaryStorage: function(args) {
          message('Creating primary storage');

          setTimeout(function() {
            stepFns.addSecondaryStorage({
              data: args.data
            });
          }, 300);
        },
        
        addSecondaryStorage: function(args) {
          message('Creating secondary storage');

          setTimeout(function() {
            complete({
              data: args.data
            });
          }, 300);
        }
      };

      var complete = function(args) {
        message('Zone creation complete!');
        success({});
      };
      
      if (startFn) {
        stepFns[startFn.fn](startFn.args);
      } else {
        stepFns.addZone({});
      }
    },

    enableZoneAction: function(args) {
      args.response.success();
    }
  };
}(cloudStack, jQuery));

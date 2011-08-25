(function($, cloudStack) {
  /**
   * Make system zone 'org' chart
   */
  var zoneChart = function(args) {
    return function(listViewArgs) {
      var $browser = listViewArgs.$browser;
      var $chartView = $('<div>').addClass('system-chart-view')
        .append(
          $('<div>').addClass('toolbar')
        )
        .append(
          $('#template').find('div.zone-chart').clone()
        );
      
      args.dataProvider({
        id: listViewArgs.id,
        response: {
          success: function(dataProviderArgs) {
            var data = dataProviderArgs.data;
            var name = data.name;
            
            // Replace cell contents
            $chartView.find('li.zone div.name span').html(name);

            // Events
            $chartView.click(function(event) {
              var $target = $(event.target);

              if ($target.is('ul li div.view-all')) {
                var $panel = $browser.cloudBrowser('addPanel', {
                  title: $target.closest('li').find('div.name span').html(),
                  data: '',
                  noSelectPanel: true,
                  maximizeIfSelected: true
                });
                $browser.cloudBrowser('toggleMaximizePanel', {
                  panel: $panel
                });

                $panel.listView(
                  $.extend(cloudStack.sections.system.sections.physicalResources.subsections[
                    $target.attr('zone-target')
                  ], {
                    $browser: $browser,
                    $chartView: $chartView
                  })
                );

                return false;
              };

              return true;
            }); 
          }          
        }
      });

      return $chartView;
    };
  };

  cloudStack.sections.system = {
    title: 'System',
    sections: {
      physicalResources: {
        title: 'Physical Resources',
        listView: {
          label: 'Physical Resources',
          fields: {
            name: { label: 'Zone' },
            dns1: { label: 'DNS' },
            internaldns1: { label: 'Internal DNS' },
            networktype: { label: 'Network Type' },
            allocationstate: { label: 'State' }
          },
          dataProvider: testData.dataProvider.listView('zones'),
          detailView: {
            pageGenerator: zoneChart({
              dataProvider: testData.dataProvider.detailView('zones')
            })
          }
        },
        subsections: {
          networks: {
            listView: {
              section: 'networks',
              fields: {
                name: { label: 'Name' },
                startip: { label: 'Start IP' },
                endip: { label: 'End IP' },
                type: { label: 'Type' }
              },
              dataProvider: testData.dataProvider.listView('networks'),
              detailView: {
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
          pods: {
            listView: {
              section: 'pods',
              fields: {
                name: { label: 'Name' },
                startip: { label: 'Start IP' },
                endip: { label: 'End IP' },
                allocationstate: { label: 'Status' }
              },
              dataProvider: testData.dataProvider.listView('pods')
            }
          },
          clusters: {
            
          },
          hosts: {
            
          },
          primaryStorage: {
            
          },
          secondaryStorage: {
            
          }
        }        
      },
      virtualAppliances: {
        title: 'Virtual Appliances',
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
  };
})($, cloudStack);
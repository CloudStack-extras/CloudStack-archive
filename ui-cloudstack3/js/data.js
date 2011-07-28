(function($, _cloudStack) {
  var mockAJAX = function(data, callback) {
    setTimeout(function() {
      callback(data);
    }, 500);
  };

  var _data = {
    instances: {
      details: function(instanceID, callback) {
        return mockAJAX({
          name: instanceID,
          
          details: {
              id: '134',
              zone: 'San Jose',
              template: 'Windows XP',
              osType: 'Windows Server 2003 Standard',
              serviceOffering: 'Small instance',
              haEnabled: false,
              created: new Date().toString(),
              account: 'bfederle',
              domain: 'cloud.com',
              host: 'Demo12',
              attachedISO: false,
              group: null
          },
          nics: [
            {
              name: 'NIC 1',
              ip: '192.168.1.1',
              netmask: '255.255.255.0',
              type: 'Virtual'
            },
            {
              name: 'NIC 2',
              ip: '192.168.1.24',
              netmask: '255.255.255.0',
              type: 'Physical'
            }
          ],
          volumes: [
            {
              name: 'ROOT-574',
              id: '3409',
              type: 'Root (shared storage)',
              size: '84.00 GB',
              created: new Date().toString()
            },
            {
              name: 'ROOT-54274',
              id: '3409',
              type: 'Root (shared storage)',
              size: '12.00 GB',
              created: new Date().toString()
            },
            {
              name: 'LS-23',
              id: '3409',
              type: 'LVM',
              size: '4.00 GB',
              created: new Date().toString()
            },
            {
              name: 'ROOT-Other',
              id: '3409',
              type: 'Root (shared storage - Huge)',
              size: '8432.00 GB',
              created: new Date().toString()
            }
          ],
          stats: {
            cpuTotal: '1 x 1024mhz',
            cpuUtilized: '32 %',
            networkRead: '203.4 mb',
            networkWrite: '1002 mb'
          }
        }, callback);
      },
      save: function(instances, callback) {
        return mockAJAX(instances, callback);
      },
      all: function(callback) {
        return mockAJAX([
          {
            name: 'brianvm',
            ip: '192.168.12.4',
            owner: 'bfederle',
            zone: 'Cupertino',
            status: 'running'
          },
          {
            name: 'testvm',
            ip: '192.168.13.8',
            owner: 'bfederle',
            zone: 'Cupertino',
            status: 'stopped'
          },
          {
            name: 'mastersrv',
            ip: '192.168.1.1',
            owner: 'wchan',
            zone: 'San Jose',
            status: 'stopped'
          },
          {
            name: 'sf12',
            ip: '10.23.55.1',
            owner: 'bfederle',
            zone: 'San Francisco',
            status: 'running'
          },
          {
            name: 'brianvm',
            ip: '192.168.12.4',
            owner: 'bfederle',
            zone: 'Cupertino',
            status: 'running'
          },
          {
            name: 'testvm',
            ip: '192.168.13.8',
            owner: 'bfederle',
            zone: 'Cupertino',
            status: 'stopped'
          },
          {
            name: 'mastersrv',
            ip: '192.168.1.1',
            owner: 'wchan',
            zone: 'San Jose',
            status: 'stopped'
          },
          {
            name: 'sf12',
            ip: '10.23.55.1',
            owner: 'bfederle',
            zone: 'San Francisco',
            status: 'running'
          },
          {
            name: 'brianvm',
            ip: '192.168.12.4',
            owner: 'bfederle',
            zone: 'Cupertino',
            status: 'running'
          },
          {
            name: 'testvm',
            ip: '192.168.13.8',
            owner: 'bfederle',
            zone: 'Cupertino',
            status: 'stopped'
          },
          {
            name: 'mastersrv',
            ip: '192.168.1.1',
            owner: 'wchan',
            zone: 'San Jose',
            status: 'stopped'
          },
          {
            name: 'sf12',
            ip: '10.23.55.1',
            owner: 'bfederle',
            zone: 'San Francisco',
            status: 'running'
          },
          {
            name: 'brianvm',
            ip: '192.168.12.4',
            owner: 'bfederle',
            zone: 'Cupertino',
            status: 'running'
          },
          {
            name: 'testvm',
            ip: '192.168.13.8',
            owner: 'bfederle',
            zone: 'Cupertino',
            status: 'stopped'
          },
          {
            name: 'mastersrv',
            ip: '192.168.1.1',
            owner: 'wchan',
            zone: 'San Jose',
            status: 'stopped'
          },
          {
            name: 'sf12',
            ip: '10.23.55.1',
            owner: 'bfederle',
            zone: 'San Francisco',
            status: 'running'
          },
          {
            name: 'mastersrv',
            ip: '192.168.1.1',
            owner: 'wchan',
            zone: 'San Jose',
            status: 'stopped'
          }
        ], callback);
      }
    },
    groups: {
      all: function(callback) {
        return mockAJAX([
          {
            id: 'db',
            name: 'DB',
            vmCount: 4
          },
          {
            id: 'vpn-sf',
            name: 'VPN - SF',
            vmCount: 42
          },
          {
            id: 'vpn-nyc',
            name: 'VPN - NYC',
            vmCount: 12
          },
          {
            id: 'apache',
            name: 'Apache',
            vmCount: 5
          },
          {
            id: 'proxy',
            name: 'Proxy',
            vmCount: 2
          },
          {
            id: 'qa',
            name: 'QA Staging',
            vmCount: 8
          }
        ], callback);
      },
      instances: function(groupID, callback) {
        return mockAJAX([
          {
            name: groupID + '-brianvm',
            ip: '192.168.23.4',
            owner: 'bfederle',
            zone: 'SF',
            status: 'stopped'
          },
          {
            name: groupID + '-sonnycomp',
            ip: '192.168.23.4',
            owner: 'bfederle',
            zone: 'LA',
            status: 'stopped'
          },
          {
            name: groupID + '-mastersrv',
            ip: '192.168.23.4',
            owner: 'bfederle',
            zone: 'Cupertino',
            status: 'stopped'
          },
          {
            name: groupID + '-otherstuff',
            ip: '192.168.23.4',
            owner: 'bfederle',
            zone: 'SF',
            status: 'stopped'
          }
        ], callback);
      },
      remove: function(groupID, callback) {
        return mockAJAX({ result: true }, callback);
      }
    }
  };

  _cloudStack.data.api = _data;
  })(jQuery, _cloudStack);

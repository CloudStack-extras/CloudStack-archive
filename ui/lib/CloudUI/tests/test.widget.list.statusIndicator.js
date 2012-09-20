(function($, cloudUI) {
  module('List: Status indicator');

  test('Basic', function() {
    var $list = $('<div>');
    var list;

    list = cloudUI.widgets.list({
      $list: $list,
      id: 'testList',
      fields: {
        name: { label: 'name' },
        status: {
          label: 'status',
          indicator: {
            'running': 'on',
            'stopped': 'off'
          }
        }
      },
      dataProvider: function(args) {
        var runningItem = {
          name: 'runningItem',
          status: 'running'
        };
        var stoppedItem = {
          name: 'stoppedItem',
          status: 'stopped'
        };
        
        args.response.success({
          data: [runningItem, stoppedItem]
        });
      }
    });

    ok($list.find('tbody tr:first td.status').hasClass('state on'), 'runningItem has correct state');
    ok($list.find('tbody tr:last td.status').hasClass('state off'), 'stoppedItem has correct state');
  });  
}(jQuery, cloudUI));

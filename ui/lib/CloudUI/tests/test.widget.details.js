(function($, cloudUI) {
  module('Details');

  test('Basic', function() {
    var $details = $('<div>').appendTo('#qunit-fixture');
    var details;

    details = cloudUI.widgets.details({
      $container: $details
    });

    ok($details.hasClass('detail-view details'), 'Detail view has CSS classes');
    equal($details.find('.toolbar').size(), 1, 'Detail view has toolbar');
    equal($details.find('ul').size(), 1, 'Detail view has tab list');
    equal($details.find('li').size(), 0, 'Detail view has no tabs');
  });

  test('Tabs', function() {
    var $details = $('<div>').appendTo('#qunit-fixture');
    var details;

    details = cloudUI.widgets.details({
      $container: $details,
      tabs: {
        tabA: {
          title: 'Tab A'
        },
        tabB: {
          title: 'Tab B'
        }
      }
    });

    equal($details.find('ul li').size(), 2, 'Detail view has tabs');
    equal($details.find('ul li[details-tab=tabA].first').size(), 1, 'Detail view has tabA');
    equal($details.find('ul li[details-tab=tabB].last').size(), 1, 'Detail view has tabB');

    // Add tab post-creation
    details.addTab({
      id: 'tabC',
      tab: { title: 'Tab C' }
    });

    equal($details.find('ul li').size(), 3, 'Detail view has correct tab count');
    equal($details.find('ul li[details-tab=tabA].first').size(), 1, 'Detail view has tabA');
    equal($details.find('ul li[details-tab=tabB]:not(.last)').size(), 1, 'Detail view has tabB');
    equal($details.find('ul li[details-tab=tabC].last').size(), 1, 'Detail view has tabC');
  });

  test('Tab display', function() {
    var $details = $('<div>').appendTo('#qunit-fixture');
    var details;
    
    details = cloudUI.widgets.details({
      $container: $details,
      tabDisplay: function() {
        return ['tabB', 'tabA'];
      },
      tabs: {
        tabA: {
          title: 'Tab A'
        },
        tabB: {
          title: 'Tab B'
        }
      }
    });

    equal($details.find('ul li').size(), 2, 'Detail view has tabs');
    equal($details.find('ul li[details-tab=tabA].last').size(), 1, 'Detail view has tabA');
    equal($details.find('ul li[details-tab=tabB].first').size(), 1, 'Detail view has tabB');

    // Add tab post-creation
    details.addTab({
      id: 'tabC',
      tab: { title: 'Tab C' }
    });

    equal($details.find('ul li').size(), 3, 'Detail view has correct tab count');
    equal($details.find('ul li[details-tab=tabA]:not(.last)').size(), 1, 'Detail view has tabA');
    equal($details.find('ul li[details-tab=tabB].first').size(), 1, 'Detail view has tabB');
    equal($details.find('ul li[details-tab=tabC].last').size(), 1, 'Detail view has tabC');
  });

  test('Tab fields', function() {
    var $details = $('<div>').appendTo('#qunit-fixture');
    var details;

    details = cloudUI.widgets.details({
      $container: $details,
      tabs: {
        tabA: {
          title: 'Tab A',
          fields: [
            {
              name: { label: 'Name' }
            },
            {
              fieldA: { label: 'Field A' },
              fieldB: { label: 'Field A' }
            }
          ]
        }
      }
    });

    equal($details.find('#details-tab-tabA .main-groups .detail-group').size(), 2, 'Detail groups correct');
    equal($details.find('.main-groups .detail-group:first tr').size(), 1, 'First detail group has correct number of fields');
    equal($details.find('.main-groups .detail-group:last tr').size(), 2, 'Last detail group has correct number of fields');
    equal($details.find('.main-groups .detail-group:first tr.name').size(), 1, 'Name field present');
    equal($details.find('.main-groups .detail-group:last tr.fieldA').size(), 1, 'fieldA field present');
    equal($details.find('.main-groups .detail-group:last tr.fieldB').size(), 1, 'fieldB field present');
  });

  test('Data provider', function() {
    var $details = $('<div>').appendTo('#qunit-fixture');
    var details;

    stop();
    details = cloudUI.widgets.details({
      $container: $details,
      tabs: {
        tabA: {
          title: 'Tab A',
          fields: [
            {
              name: { label: 'Name' }
            }
          ],
          dataProvider: function(args) {
            start();
            ok(true, 'Data provider called');
            args.response.success({
              data: { name: 'testData' }
            });
          }
        }
      }
    });

    equal($details.find('tr.name td.value span').html(), 'testData', 'Data for field correct');
  });
}(jQuery, cloudUI));

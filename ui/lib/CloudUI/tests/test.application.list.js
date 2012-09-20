(function($, cloudUI) {
  module('Application: List');

  test('Basic', function() {
    var $app = $('<div>').appendTo('#qunit-fixture');
    var app;
    
    // Test with list: {}
    app = cloudUI.application({
      $container: $app,
      home: 'sectionA',
      sections: {
        sectionA: {
          title: 'sectionATitle',
          list: {
            id: 'testList'
          }
        }
      }
    });
    
    equal($app.find('#browser .container .list-view[cs-event-id=application-list]').size(), 1, 'List present in browser');

    // [legacy] Test with listView: {}
    $app.remove();
    $app = $('<div>').appendTo('#qunit-fixture');
    app = cloudUI.application({
      $container: $app,
      home: 'sectionA',
      sections: {
        sectionA: {
          title: 'sectionATitle',
          listView: {
            id: 'testList'
          }
        }
      }
    });

    equal($app.find('#browser .container .list-view[cs-event-id=application-list]').size(), 1, 'List present in browser');
  });
}(jQuery, cloudUI));
(function($, cloudUI) {
  // Make sure no animations play -- messes up the timing for tests!
  $.fx.off = true;

  module('Core');

  test('Basic', function() {
    ok($.isPlainObject(window.cloudUI), 'cloudUI namespace exists');
    ok($.isPlainObject(window.cloudUI.widgets), 'cloudUI widget namespace exists');
  });

  test('Data', function() {
    var $elem = $('<div>');
    var data = cloudUI.data($elem);

    ok($.isPlainObject(data), 'Data object present');
    ok(cloudUI.data($elem).test = 'test', 'Store data');
    equal(cloudUI.data($elem).test, 'test', 'Data retrieved correctly');
    ok(cloudUI.data($elem).test2 = 'test2', 'Store more data');
    equal(cloudUI.data($elem).test, 'test', 'First data item retrieved correctly');
    equal(cloudUI.data($elem).test2, 'test2', 'Second data item retrieved correctly');
  });

  test('Register event', function() {
    var $elem = $('<div>');

    // Test init event
    var testEvent = true;
    stop();
    cloudUI.event.handler({
      'test-event': {
        init: function(args) {
          if (!testEvent) return;
          
          start();
          ok(true, 'init called');
          ok(args.testData, 'testData passed');

          testEvent = false;
        }
      }
    });
    
    ok(cloudUI.event.register({
      $elem: $elem,
      id: 'test-event',
      data: {
        testData: true
      }
    }), 'Register event');
    equal($elem.attr('cs-event-id'), 'test-event', 'Element has event ID');
    ok($elem.data('cloudUI').eventData.testData, 'Element has embedded data');
  });

  test('Handle event', function() {
    var $elem = $('<div>').appendTo('#qunit-fixture');

    ok(cloudUI.event.register({
      $elem: $elem,
      id: 'test-event',
      data: {
        testData: true
      }
    }), 'Event registered');

    ok(cloudUI.event.handler({
      'test-event': {
        click: function(args) {
          start();
          ok(true, 'Click event handled');
          ok(args.testData, 'Test data present');
          stop();
        },
        customEventA: function(args) {
          start();
          ok(true, 'Custom event A called');
          ok(args.testData, 'Test data present');
          stop();
        },
        customEventB: function(args) {
          start();
          ok(true, 'Custom event B called');
          ok(args.testData, 'Test data present');
          ok(args.customData, 'Custom data present');
        }
      }
    }), 'Event handler setup');

    stop();
    $elem.click();
    cloudUI.event.call('customEventA', $elem);
    cloudUI.event.call('customEventB', $elem, { customData: true });
  });

  test('dataProvider handler', function() {
    var dataProvider = function(args) {
      start();

      ok(true, 'Data provider called');
      ok(_.isObject(args.context), 'Context passed');
      ok(args.response.success, 'Success callback passed');
      ok(args.response.error, 'Error callback passed');

      stop();

      args.response.success({ data: [ { testItem: 'testItemData' } ] });
    };

    stop();

    cloudUI.dataProvider({
      dataProvider: dataProvider,
      success: function(args) {
        start();

        ok(true, 'Success called');
        ok(args.data.length, 1, 'Data passed');
        equal(args.data[0].testItem, 'testItemData', 'Data values correct');
      },
      error: function(args) {}
    });
  });

  test('Context', function() {
    // Test new context
    var context;

    context = cloudUI.context(context);

    ok(_.isObject(context), 'Context created new object');

    // Test append existing context, with object
    context = {
      instances: [
        { id: 'instance1' }
      ]
    };
    context = cloudUI.context(context, {
      id: 'storage',
      data: {
        id: 'storage1'
      }
    });

    equal(context.storage[0].id, 'storage1', 'New context item value present');

    // Test append existing item, with object
    context = {
      instances: [
        { id: 'instance1' }
      ]
    };
    context = cloudUI.context(context, {
      id: 'instances',
      data: {
        id: 'instance2'
      }
    });
    equal(context.instances.length, 2, 'Instances have correct length');
    equal(context.instances[0].id, 'instance1', 'instance1 present');
    equal(context.instances[1].id, 'instance2', 'instance2 present');

    // Test append existing item, with array
    context = {
      instances: [
        { id: 'instance1' }
      ]
    };
    context = cloudUI.context(context, {
      id: 'instances',
      data: [
        { id: 'instance2' },
        { id: 'instance3' }
      ]
    });
    equal(context.instances.length, 3, 'Instances have correct length');
    equal(context.instances[0].id, 'instance1', 'instance1 present');
    equal(context.instances[1].id, 'instance2', 'instance2 present');
    equal(context.instances[2].id, 'instance3', 'instance3 present');
  });

  test('Widget factory', function() {
    var $testEventElem = $('<div>').addClass('test-event-item').appendTo('#qunit-fixture');
    var widget = cloudUI.widget({
      methods: {
        _init: function(widget, widgetArgs) {
          start();
          ok(true, '_init called');
          equal(widgetArgs.$container.size(), 1, 'widgetArgs has pased $container');
          equal(widgetArgs.testWidgetArg, 'test123', 'widgetArgs has passed test option');
          stop();

          cloudUI.event.register({
            $elem: $testEventElem,
            id: 'test-event',
            data: {
              widgetArgs: widgetArgs,
              testData: 'testData123'
            }
          });
        },
        testMethod: function(widget, widgetArgs, args) {
          start();
          ok(true, '_init called');
          equal(widgetArgs.$container.size(), 1, 'widgetArgs has pased $container');
          equal(widgetArgs.testWidgetArg, 'test123', 'widgetArgs has passed test option');
          equal(args.testArg, 'testArg123', 'args has passed option');
          stop();
        },
        testMethodWithReturn: function() {
          return 'return123';
        }
      },
      events: {
        'test-event': {
          click: function(args) {
            start();
            ok(true, 'Click event triggered');
            equal(args.testData, 'testData123', 'Test data passed');
            equal(args.widgetArgs.$container.size(), 1, 'Widget passed');
          }
        }
      }
    });

    stop();
    var testWidget = widget({
      $container: $('<div>'),
      testWidgetArg: 'test123'
    });

    testWidget.testMethod({ testArg: 'testArg123' });
    
    // Trigger test-event
    $testEventElem.click();

    // Test method with return
    equal(testWidget.testMethodWithReturn(), 'return123', 'testMethodWithReturn value returned');
  });
}(jQuery, cloudUI));

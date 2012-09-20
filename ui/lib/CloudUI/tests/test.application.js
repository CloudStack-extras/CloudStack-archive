(function($, cloudUI) {
  module('Application', {
    setup: function() {
      cloudUI.widgets._container = cloudUI.widgets.container;
      cloudUI.widgets._browser = cloudUI.widgets.browser;
    },
    teardown: function() {
      cloudUI.widgets.container = cloudUI.widgets._container;
      cloudUI.widgets.browser = cloudUI.widgets._browser;
    }
  });

  test('Basic', function() {
    var $app = $('<div>').appendTo('#qunit-fixture');
    var app;

    app = cloudUI.application({
      $container: $app,
      home: 'sectionA',
      sections: {
        sectionA: {
          title: 'sectionATitle'
        }
      }
    });

    equal($app.find('#browser .container').size(), 1, 'Browser container present');
    equal($app.find('#breadcrumbs').size(), 1, 'Breadcrumbs present');
    equal($app.find('#breadcrumbs > .home').size(), 1, 'Home button present');
    ok($app.find('#breadcrumbs > .home').next().hasClass('end'), 'Home button end piece present');
  });

  test('Container widget', function() {
    var $app = $('<div>').appendTo('#qunit-fixture');
    var app;

    cloudUI.widgets.container = function(args) {
      start();
      ok(true, 'Container widget called');
      stop();

      return {
        addNavItem: function(args) {
          start();
          ok(true, 'addNavItem called');
          equal(args.id, 'sectionA', 'Section ID correct');
          equal(args.navItem.title, 'sectionATitle', 'Section title correct');
          stop();
        },
        selectNavItem: function(sectionID) {
          start();
          ok(true, 'selectNavItem called to show default section');
          equal(sectionID, 'sectionA', 'Home section ID is correct');
        }
      };
    };
    stop();
    app = cloudUI.application({
      $container: $app,
      home: 'sectionA',
      sections: {
        sectionA: {
          title: 'sectionATitle'
        }
      }
    });
    ok($app.is('[cs-event-id=application-container]'), 'Application container is registered');
  });

  test('Browser widget', function() {
    var $app = $('<div>').appendTo('#qunit-fixture');
    var app;

    stop();
    cloudUI.widgets.browser = function(args) {
      start();
      ok(true, 'Browser widget called');
      stop();

      return {
        reset: function() {
          start();
          ok(true, 'Reset called');
          stop();
        },
        addPanel: function(args) {
          start();
          ok(true, 'addPanel called');
        }
      };
    };
    app = cloudUI.application({
      $container: $app,
      home: 'sectionA',
      sections: {
        sectionA: {
          title: 'sectionATitle'
        }
      }
    });
  });

  test('Show section', function() {
    var $app = $('<div>').appendTo('#qunit-fixture');
    var app;

    stop();
    app = cloudUI.application({
      $container: $app,
      home: 'sectionA',
      sections: {
        sectionA: {
          title: 'sectionATitle'
        },
        sectionB: {
          title: 'sectionBTitle'
        },
        sectionC: {
          title: 'sectionCTitle'
        }
      }
    });
    app.widgets.container.selectNavItem = function(sectionID) {
      start();
      ok(true, 'Nav item selected');
      equal(sectionID, 'sectionB', 'Section ID correct');
    };
    app.showSection('sectionB');

    // Test via nav item click
    app.widgets.container.selectNavItem = function(sectionID) {
      start();
      ok(true, 'Nav item selected');
      equal(sectionID, 'sectionC', 'Section ID correct');
    };
    stop();
    $app.find('li.sectionC').click();

    // Test home button
    app.widgets.container.selectNavItem = function(sectionID) {
      start();
      ok(true, 'Nav item selected');
      equal(sectionID, 'sectionA', 'Section ID correct');
      equal($app.find('#breadcrumbs li > span').html(), 'sectionATitle', 'Breadcrumb correct');
    };
    $app.find('#breadcrumbs > .home').click();

    // application-container->showSection event
    stop();
    var testEvent = true;
    cloudUI.event.handler({
      'application-container': {
        showSection: function(args) {
          if (!testEvent) return;
          
          start();
          ok(true, 'showSection called');
          equal(args.sectionID, 'sectionA', 'sectionID passed');
          ok($.isPlainObject(args.section), 'section passed');

          testEvent = false;
        }
      }
    });
    app.showSection('sectionA');
  });

  test('Show content', function() {
    var $app = $('<div>').appendTo('#qunit-fixture');
    var app;

    stop();

    // Test with content: function() {}
    app = cloudUI.application({
      $container: $app,
      home: 'testSection',
      sections: {
        testSection: {
          title: 'testSectionTitle',
          content: function() {
            start();
            ok(true, 'content called');

            return $('<div>').addClass('testSectionContent');
          }
        }
      }
    });
    equal($app.find('#browser .container .panel > div.testSectionContent').size(), 1, 'Content rendered');

    // [legacy] Test with custom: function() {}
    $app.remove();
    $app = $('<div>').appendTo('#qunit-fixture');
    app = cloudUI.application({
      $container: $app,
      home: 'testSection',
      sections: {
        testSection: {
          title: 'testSectionTitle',
          custom: function() {
            start();
            ok(true, 'content called');

            return $('<div>').addClass('testSectionContent');
          }
        }
      }
    });
    equal($app.find('#browser .container .panel > div.testSectionContent').size(), 1, 'Content rendered');    
  });
}(jQuery, cloudUI));
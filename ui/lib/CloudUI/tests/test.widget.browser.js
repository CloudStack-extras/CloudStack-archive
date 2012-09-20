(function($, cloudUI) {
  module('Browser');

  test('Basic', function() {
    var $container = $('<div>');
    var $navigation = $('<div>');
    var browser = cloudUI.widgets.browser({
      $container: $container,
      $navigation: $navigation
    });

    ok(browser, 'Browser object initialized');
    equal($navigation.find('ul').size(), 1, 'Navigation list present');
  });


  test('Add panel', function() {
    var $container = $('<div>').appendTo('#qunit-fixture');
    var $navigation = $('<div>');
    var browser = cloudUI.widgets.browser({
      $container: $container,
      $navigation: $navigation
    });
    var zIndexPanel1, zIndexPanel2;

    // Explictly set container width for testing panels
    $container.width(1000);

    stop();
    browser.addPanel({
      title: 'test',
      content: function($panel1) {
        zIndexPanel1 = parseInt($panel1.css('z-index'));

        $panel1.append(
          $('<div>').addClass('testContents').html('test contents')
        );

        start();
        ok(true, 'addPanel complete called');
        equal($navigation.find('ul li').size(), 1, 'Navigation item added');
        equal($navigation.find('ul li').attr('title'), 'test', 'Navigation has title tooltip');
        equal($navigation.find('ul li span').html(), 'test', 'Navigation item has label');
        equal($navigation.find('ul > div.end').size(), 1, 'Navigation item has end piece');
        equal($container.find('.panel').size(), 1, 'Panel added');
        equal($container.find('.panel > div.shadow').size(), 1, 'Panel has shadow');
        equal($container.find('.panel > div.testContents').html(), 'test contents', 'Panel has contents');
        equal($panel1.width(), $container.width(), 'Panel 1 width correct');
        stop();
      }
    });
    browser.addPanel({
      title: 'test2',
      content: function($panel2) {
        zIndexPanel2 = parseInt($panel2.css('z-index'));

        $panel2.append(
          $('<div>').addClass('testContents2').html('test contents 2')
        );

        start();
        equal($navigation.find('ul li').size(), 2, 'Second navigation item added');
        equal($container.find('.panel').size(), 2, 'Second panel added');
        equal($container.find('.panel:last > div.testContents2').html(), 'test contents 2', 'Second panel has contents');
        equal(zIndexPanel2, zIndexPanel1 + 1, 'Z-index correct');
        equal($panel2.width(), $container.width() - $container.width() / 4, 'Panel 2 width correct');
      }
    });

  });

  test('Reset', function() {
    var $container = $('<div>').appendTo('#qunit-fixture');
    var $navigation = $('<div>').appendTo('#qunit-fixture');
    var browser = cloudUI.widgets.browser({
      $container: $container,
      $navigation: $navigation
    });

    stop();
    browser.addPanel({
      title: 'test',
      content: function($panel) {
        browser.addPanel({
          title: 'test2',
          content: function($panel) {
            start();
            browser = browser.reset();
            equal($container.find('.panel').size(), 0, 'All panels cleared');
            equal($navigation.find('li, .end').size(), 0, 'All nav items cleared');
          }
        });
      }
    });
  });

  test('Select panel', function() {
    var $container = $('<div>').appendTo('#qunit-fixture');
    var $navigation = $('<div>').appendTo('#qunit-fixture');
    var browser = cloudUI.widgets.browser({
      $container: $container,
      $navigation: $navigation
    });
    var $panel1, $panel2, $panel3, $panel4, $lastPanel;

    stop();
    browser = browser.addPanel({
      title: 'test',
      content: function($panel) { $panel1 = $panel; }
    });
    browser.addPanel({
      title: 'test2',
      content: function($panel) { $panel2 = $panel; }
    });
    browser.addPanel({
      title: 'test3',
      content: function($panel) { $panel4 = $panel; }
    });
    browser = browser.selectPanel({
      $panel: $panel1,
      complete: function($panel) {
        $lastPanel = $panel;

        start();
        equal($container.find('.panel').size(), 1, 'Correct # of panels');
        equal($navigation.find('li').size(), 1, 'Correct # of nav items');
        equal($navigation.find('ul .end').size(), 1, 'Correct # of nav item ends');
        equal($panel1[0], $lastPanel[0], 'Correct last panel');
        ok($panel1.is(':visible'), '$panel1 visible');
        ok(!$panel2.is(':visible'), '$panel2 not visible');

        // Test via breadcrumb click
        stop();
        browser.addPanel({
          title: 'test4',
          content: function($panel3) {
            start();
            ok($panel1.is(':visible'), '$panel1 visible');
            ok($panel3.is(':visible'), '$panel3 visible');
            equal($container.find('.panel').size(), 2, 'Correct # of panels');
            equal($navigation.find('li').size(), 2, 'Correct # of nav items');

            $navigation.find('li:first').click();

            stop();

            // Panel selection is animated
            setTimeout(function() {
              start();
              equal($container.find('.panel').size(), 1, 'Correct # of panels');
              equal($navigation.find('li').size(), 1, 'Correct # of nav items');
              equal($navigation.find('ul .end').size(), 1, 'Correct # of nav item ends');
              ok($panel1.is(':visible'), '$panel1 visible');
              ok(!$panel2.is(':visible'), '$panel2 not visible');
              ok(!$panel3.is(':visible'), '$panel3 not visible');
            });
          }
        });
      }
    });
  });

  test('Select panel by index', function() {
    var $container = $('<div>').appendTo('#qunit-fixture');
    var $navigation = $('<div>').appendTo('#qunit-fixture');
    var browser = cloudUI.widgets.browser({
      $container: $container,
      $navigation: $navigation
    });
    var $panel1, $panel2, $lastPanel;

    stop();
    browser = browser.addPanel({
      title: 'test',
      content: function($panel) { $panel1 = $panel; }
    });
    browser.addPanel({
      title: 'test2',
      content: function($panel) { $panel2 = $panel; }
    });
    browser = browser.selectPanel({
      index: 1,
      complete: function($panel) {
        $lastPanel = $panel;
        start();
        equal($container.find('.panel').size(), 1, 'Correct # of panels');
        equal($navigation.find('li').size(), 1, 'Correct # of nav items');
        equal($navigation.find('ul .end').size(), 1, 'Correct # of nav item ends');
        equal($panel1[0], $lastPanel[0], 'Correct last panel');
        ok($panel1.is(':visible'), '$panel1 visible');
        ok(!$panel2.is(':visible'), '$panel2 not visible');
      }
    });
  });

  test('Add panel as maximized', function() {
    var $container = $('<div>').appendTo('#qunit-fixture');
    var $navigation = $('<div>');
    var browser = cloudUI.widgets.browser({
      $container: $container,
      $navigation: $navigation
    });

    // Explictly set container width for testing panels
    $container.width(1000);

    stop();
    browser.addPanel({
      title: 'test',
      content: function($panel1) {}
    });
    browser.addPanel({
      title: 'test2',
      isMaximized: true,
      content: function($panel2) {
        start();
        equal($panel2.width(), $container.width(), 'Panel width correct');
        equal($panel2.position().left, 0, 'Panel position correct');
        stop();
      }
    });

    // Test legacy 'maximizeIfSelected' option
    browser.addPanel({
      title: 'test3',
      maximizeIfSelected: true,
      content: function($panel3) {
        start();
        equal($panel3.width(), $container.width(), 'Panel width correct');
        equal($panel3.position().left, 0, 'Panel position correct');
      }
    });
  });

  test('Focus panel', function() {
    var $container = $('<div>').appendTo('#qunit-fixture');
    var $navigation = $('<div>');
    var browser = cloudUI.widgets.browser({
      $container: $container,
      $navigation: $navigation
    });
    var $panel1, $panel2, $panel3;

    browser.addPanel({
      title: 'test1',
      content: function($panel) {
        $panel1 = $panel;
      }
    });
    browser.addPanel({
      title: 'test2',
      content: function($panel) {
        $panel2 = $panel;
      }
    });
    browser.addPanel({
      title: 'test3',
      content: function($panel) {
        $panel3 = $panel;
      }
    });

    browser.focusPanel({ $panel: $panel1 });

    ok($panel1.is(':visible'), 'Panel 1 visible');
    ok($panel2.is(':hidden'), 'Panel 2 hidden');
    ok($panel3.is(':hidden'), 'Panel 3 hidden');

    browser.focusPanel({ $panel: $panel2 });

    ok($panel1.is(':visible'), 'Panel 1 still visible');
    ok($panel2.is(':visible'), 'Panel 2 visible');
    ok($panel3.is(':hidden'), 'Panel 3 hidden');

    // Test panel positioning
    equal($panel1.position().left, 0, 'Panel 1 position correct');
    notEqual($panel2.position().left, $container.width(), 'Panel 2 position correct');

    browser.defocusPanel();

    ok($panel1.is(':visible'), 'Panel 1 visible');
    ok($panel2.is(':visible'), 'Panel 2 visible');
    ok($panel3.is(':visible'), 'Panel 3 visible');

    // Test focus by index
    browser.focusPanel({ index: 1 });

    ok($panel1.is(':visible'), 'Panel 1 visible');
    ok($panel2.is(':hidden'), 'Panel 2 hidden');
    ok($panel3.is(':hidden'), 'Panel 3 hidden');

    // Test select while focused
    browser.selectPanel({ index: 1 });
    browser.defocusPanel();
    equal($container.find('.panel').size(), 1, 'Panel count correct');
    ok(!$panel3.parent().size(), 'Panel 3 removed'); // Panel is not part of container
  });
}(jQuery, cloudUI));

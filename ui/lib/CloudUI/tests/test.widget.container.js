(function($, cloudUI) {
  module('Container');

  test('Basic', function() {
    var $container = $('<div>');
    var container = cloudUI.widgets.container({
      $container: $container,
      navigation: {
        navItemA: {
          title: 'navItemATitle',
          action: function() {}
        },
        navItemB: {
          title: 'navItemBTitle',
          action: function() {}
        }
      }
    });
    var $header = $container.find('#header[cs-event-id=container-header]');
    var $logo = $header.find('.logo[cs-event-id=container-logo]');
    var $navigation = $container.find('#navigation[cs-event-id=container-navigation] > ul');
    var $mainArea = $container.find('#main-area[cs-event-id=container-main-area]');
    equal($header.size(), 1, 'Header present');
    equal($logo.size(), 1, 'Logo present');
    equal($navigation.size(), 1, 'Navigation present');
    equal($mainArea.size(), 1, 'Main area present');
    ok($navigation.find('li.navItemA[cs-event-id=container-navigation-item]').size(), 'Nav item A has correct ID');
    equal($navigation.find('li.navItemA > span.title').html(), 'navItemATitle', 'Nav item A has correct title');
    ok($navigation.find('li.navItemB').size(), 'Nav item B has correct ID');
    equal($navigation.find('li.navItemB > span.title').html(), 'navItemBTitle', 'Nav item B has correct title');
  });

  test('Control navigation item display', function() {
    var $ui = $('<div>');
    var container = cloudUI.widgets.container({
      $container: $ui,
      navigationDisplay: function() {
        return ['navItemB', 'navItemA'];
      },
      navigation: {
        navItemA: {
          title: 'navItemATitle'
        },
        navItemB: {
          title: 'navItemBTitle'
        },
        doNotUse: {
          title: 'Hide this item'
        }
      }
    });
    var $navItems = $ui.find('#navigation ul li');

    ok($navItems.filter(':first').hasClass('navItemB'), 'Item B is first nav item');
    ok($navItems.filter(':last').hasClass('navItemA'), 'Item A is last nav item');
    ok(!$navItems.filter('.doNotUse').size(), 'doNotUse section is hidden');
  });

  test('Handle nav item action', function() {
    var $container = $('<div>').addClass('ui-container').appendTo('#qunit-fixture');
    var container = cloudUI.widgets.container({
      $container: $container,
      navigation: {
        navItemA: {
          title: 'navItemATitle'
        },
        navItemB: {
          title: 'navItemBTitle',
          action: function(args) {
            var $content = args.$content;

            start();
            ok(true, 'Nav item action called');
            ok($content.size(), 'Content area passed');
            equal($navItems.filter('.active').size(), 1, 'One section is active');
          }
        }
      }
    });
    var $navItems = $container.find('#navigation ul li');

    stop();
    $navItems.filter('.navItemB').click();
  });

  test('Append new nav item', function() {
    var $container = $('<div>').addClass('ui-container').appendTo('#qunit-fixture');
    var container = cloudUI.widgets.container({
      $container: $container,
      navigation: {
        navItemA: {
          title: 'navItemATitle'
        },
        navItemB: {
          title: 'navItemBTitle'
        }
      }
    });
    var $navItems, $navItemC;

    $navItems = $container.find('#navigation ul li');
    container.addNavItem({
      id: 'navItemC',
      navItem: {
        title: 'navItemCTitle',
        action: function() {
          start();
          ok(true, 'New item action called');
        }
      }
    });
    $navItems = $container.find('#navigation ul li');
    $navItemC = $container.find('#navigation ul li:last');
    equal($navItems.size(), 3, 'Correct # of nav items');
    ok($navItemC.hasClass('navItemC'), 'New section has correct CSS class');
    equal($navItemC.find('span.title').html(), 'navItemCTitle', 'New section has correct title');
    equal($navItemC.attr('title'), 'navItemCTitle', 'New section has tooltip');
    stop();
    $navItemC.click();
  });

  test('selectNavItem event', function() {
    var $container = $('<div>').addClass('ui-container').appendTo('#qunit-fixture');
    var container = cloudUI.widgets.container({
      $container: $container,
      navigation: {
        navItemA: {
          title: 'navItemATitle'
        },
        navItemB: {
          title: 'navItemBTitle'
        }
      },
      events: {
        selectNavItem: function(args) {
          start();
          ok(true, 'selectNavItem called');
          equal(args.navID, 'navItemB', 'Correct section ID passed');
        }
      }
    });
    stop();
    ok($container.find('#navigation li.navItemB').click(), 'Click nav item B');
  });
}(jQuery, cloudUI));

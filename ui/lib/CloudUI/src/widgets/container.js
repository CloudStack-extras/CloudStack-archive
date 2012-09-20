(function($, _, cloudUI) {
  // UI elements
  var elems = {
    header: function() {
      var $header = $('<div>').attr('id', 'header');

      cloudUI.event.register({
        $elem: $header,
        id: 'container-header'
      });
      
      return $header;
    },
    logo: function() {
      var $logo = $('<div>').addClass('logo');
      
      cloudUI.event.register({
        $elem: $logo,
        id: 'container-logo'
      });
      
      return $logo;
    },
    navigation: function() {
      var $navigation = $('<div>').attr('id', 'navigation');

      cloudUI.event.register({
        $elem: $navigation,
        id: 'container-navigation'
      });
      $navigation.append($('<ul>'));

      return $navigation;
    },
    navItem: function(args) {
      var $navItem = $('<li>');
      var $icon = $('<span>').addClass('icon').html('&nbsp;');
      var $title = $('<span>').addClass('title');
      var $container = args.$container;
      var container = args.container;
      var navID = args.navID;
      var title = args.title;

      $title.html(title);
      $navItem.attr('title', title);
      $navItem.addClass('navigation-item');
      $navItem.addClass(navID);
      $navItem.append($icon, $title);
      cloudUI.event.register({
        $elem: $navItem,
        id: 'container-navigation-item',
        data: {
          container: container,
          $container: $container,
          navID: navID
        }
      });

      return $navItem;
    },

    // Where the content is contained
    mainArea: function() {
      var $main = $('<div>').attr('id', 'main-area');

      cloudUI.event.register({
        $elem: $main,
        id: 'container-main-area'
      });
      
      return $main;
    }
  };

  // Navigation bar-related functions
  var navigation = {
    selectItem: function(args) {
      var $container = args.$container;
      var container = args.container;
      var navigation = cloudUI.data($container).container.navigation;
      var navID = args.navID;
      var browser = container.browser;
      var navItem = navigation[navID];
      var $navigation = args.$navigation;
      var $navItems = $navigation.find('li');
      var $targetNavItem = $navItems.filter(function() {
        return $(this).hasClass(navID);
      });
      var $content = $container.find('#main-area');
      var events = args.events;

      $targetNavItem.addClass('active');
      $targetNavItem.siblings().removeClass('active');

      // Trigger nav item's action
      if (navItem.action) navItem.action({
        $content: $content
      });

      if (events.selectNavItem) {
        events.selectNavItem({
          navID: navID
        });
      }
    },
    addItem: function(args) {
      var container = args.container;
      var $container = args.$container;
      var $navigation = args.$navigation;
      var $navItem, $navItems;
      var navItem = args.navItem;
      var navID = args.navID;
      var title = args.navItem.title;
      var navigation = cloudUI.data($container).container.navigation;

      // Presist navigation item by adding to navigation data
      navigation[navID] = navItem;

      $navItem = elems.navItem({
        $container: $container,
        container: container,
        navID: navID,
        title: title
      });
      $navItem.appendTo($navigation.find('ul'));

      // Setup first/last item CSS styling
      $navItems = $navigation.find('li');
      $navItems.removeClass('first last');
      $navItems.filter(':first').addClass('first');
      $navItems.filter(':last').addClass('last');

      return $navItem;
    },
    makeActive: function(args) {
      var sectionID = args.sectionID;
      var $navigation = args.$navigation;
      var $navItems = $navigation.find('li');
      var $targetNavItem = $navItems.filter(function() {
        return $(this).hasClass(sectionID);
      });

      $targetNavItem.addClass('active');
      $targetNavItem.siblings().removeClass('active');
    }
  };

  var buildUI = function(args) {
    var container = args.container;
    var $container = args.$container;
    var $header = elems.header();
    var $logo = elems.logo();
    var $navigation = elems.navigation();
    var $mainArea = elems.mainArea();
    var navItems = args.navigation;
    var navDisplay, firstNavItem;

    $header.append($logo);
    $container.append($header,
                      $navigation,
                      $mainArea);

    if (navItems) {
      navDisplay = args.navigationDisplay ?
        args.navigationDisplay() : _.keys(navItems);
      
      _.map(navDisplay, function(item) {
        var navID = item.toString();
        var navItem = navItems[navID];

        navigation.addItem({
          container: container,
          $container: $container,
          $navigation: $navigation,
          navID: navID,
          navItem: navItem
        });
      });
    }
  };

  cloudUI.widgets.container = function(args) {
    var $container = args.$container;
    var events = args.events;
    var container;

    // Define return object
    container = {
      selectNavItem: function(navID) {
        navigation.selectItem({
          container: container,
          $container: $container,
          $navigation: $container.find('#navigation'),
          navID: navID,
          events: events ? events : {}
        });
      },
      addNavItem: function(args) {
        navigation.addItem({
          container: container,
          $container: $container,
          $navigation: $container.find('#navigation'),
          navID: args.id,
          navItem: args.navItem
        });

        return container;
      }
    };

    // Create persistent data store for nav item data
    $.extend(cloudUI.data($container), {
      container: {
        navigation: {}
      }
    });

    buildUI(_.extend(args, {
      container: container,
      $container: $container
    }));

    return container;
  };

  cloudUI.event.handler({
    'container-navigation-item': {
      click: function(args) {
        var container = args.container;
        var navID = args.navID;

        container.selectNavItem(navID);

        return false;
      }
    }
  });
}(jQuery, _, cloudUI));

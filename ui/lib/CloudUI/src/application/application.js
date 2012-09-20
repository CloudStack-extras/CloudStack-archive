(function($, _, cloudUI) {
  // Build widget elements
  var elems = {
    container: function(args) {
      var $container = args.$container;
      var sections = args.sections;
      var events = args.events;
      var application = args.application;
      var container;

      // Make widget
      container = cloudUI.widgets.container({
        $container: $container,
        navItems: {}, // These will be built dynamically
        events: events
      });

      // Make nav item map
      _.each(sections, function(section, sectionID) {
        container.addNavItem({
          id: sectionID,
          navItem: {
            title: section.title,
            action: function() {}
          }
        });
      });

      cloudUI.event.register({
        $elem: $container,
        id: 'application-container',
        data: {
          application: application,
          container: container,
          $container: $container
        }
      });

      return container;
    },
    browser: function(args) {
      var $container = args.$container;
      var $browserContainer = $('<div>').attr('id', 'browser');
      var $browserSubContainer = $('<div>').addClass('container');
      var $navigation = $('<div>').addClass('navigation').attr('id', 'breadcrumbs');
      var $homeButton = $.merge( // Home button placed in breadcrumbs
        $('<div>').addClass('home'),
        $('<div>').addClass('end')
      );
      var application = args.application;
      var browser;

      // Setup home button behavior
      cloudUI.event.register({
        $elem: $homeButton,
        id: 'application-home',
        data: {
          $application: $container,
          application: application
        }
      });

      $navigation.append($homeButton);
      $browserContainer.append($navigation, $browserSubContainer);
      $container.find('#main-area').append($browserContainer);

      // Initialize browser widget
      browser = cloudUI.widgets.browser({
        $container: $browserSubContainer,
        $navigation: $navigation
      });

      return browser;
    }
  };

  // Make section active, by selecting nav item and making new browser pane
  var showSection = function(args) {
    var application = args.application;
    var browser = args.application.widgets.browser;
    var container = args.application.widgets.container;
    var sectionID = args.sectionID;
    var sectionTitle = args.section.title;
    var sectionContent = cloudUI.which(args.section, ['content', 'custom']);
    var selectNavItem = args.selectNavItem;

    browser.reset();
    browser.addPanel({
      title: sectionTitle,
      content: function($panel) {
        $panel.append($('<div>').addClass('toolbar'));

        cloudUI.event.call('showSection', 'application-container', {
          container: container,
          $panel: $panel,
          sectionID: sectionID,
          section: args.section
        });
        
        return sectionContent ?
          sectionContent().appendTo($panel) : '';
      }
    });

    if (selectNavItem) {
      container.selectNavItem(sectionID);
    }
  };

  cloudUI.application = function(args) {
    var $container = args.$container;
    var sections = args.sections;
    var home = args.home;
    var application, container;

    // Define return object
    application = {
      widgets: {}, // Stores widget instances used by app

      showSection: function(sectionID) {
        var section = sections[sectionID];

        showSection({
          application: application,
          sectionID: sectionID,
          section: section,
          selectNavItem: true
        });

        return application;
      }
    };

    // Create widgets
    _.extend(application.widgets, {
      container: elems.container({
        $container: $container,
        application: application,
        sections: sections,
        home: home,
        events: {
          selectNavItem: function(args) {
            var sectionID = args.navID;
            var section = sections[sectionID];

            showSection({
              application: application,
              sectionID: sectionID,
              section: section,
              selectNavItem: false
            });
          }
        }
      }),
      browser: elems.browser({
        $container: $container,
        application: application
      })
    });

    // Create persistent data store
    _.extend(cloudUI.data($container), {
      application: {
        _application: application,
        home: home
      }
    });

    // Make home section active by default
    application.showSection(home);

    return application;
  };

  cloudUI.event.handler({
    'application-home': {
      click: function(args) {
        var $application = args.$application;
        var application = args.application;
        var homeSection = cloudUI.data($application).application.home;

        application.showSection(homeSection);
      }
    }
  });
}(jQuery, _, cloudUI));

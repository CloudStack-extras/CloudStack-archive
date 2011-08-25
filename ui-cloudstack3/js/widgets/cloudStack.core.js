(function($, window) {
  window.cloudStack = {};

  /**
   * Generate navigation <li>s
   * 
   * @param args cloudStack data args
   */
  var makeNavigation = function(args) {
    var $navList = $('<ul>');

    $.each(args.sections, function(sectionID, args) {
      var $li = $('<li>')
            .addClass('navigation-item')
            .addClass(sectionID)
            .append($('<span>').addClass('icon').html('&nbsp;'))
            .append($('<span>').html(args.title))
            .data('cloudStack-section-id', sectionID);
      
      $li.appendTo($navList);
    });

    // Special classes for first and last items
    $navList.find('li:first').addClass('first');
    $navList.find('li:last').addClass('last');

    return $navList;
  };

  /**
   * Create section contents
   * 
   * @param sectionID Section's ID to show
   * @param args CloudStack3 configuration 
   */
  var showSection = function(sectionID, args) {
    var $panel;
    var $browser = $('#browser div.container');
    var $navItem = $('#navigation').find('li').filter(function() { 
      return $(this).hasClass(sectionID);
    });
    var data = args.sections[sectionID];
    data.$browser = $browser;

    $navItem.siblings().removeClass('active');
    $navItem.addClass('active');

    // Reset browser panels
    $browser.cloudBrowser('removeAllPanels');
    $panel = $browser.cloudBrowser('addPanel', {
      title: data.title,
      data: ''
    });

    // Hide breadcrumb if this is the home section
    if (args.home === sectionID) {
      $('#breadcrumbs').find('li:first, div.end:last').hide();
    }

    $browser.cloudBrowser('toggleMaximizePanel', { panel: $panel, noAnimate: true });

    if (data.show)
      $panel.append(data.show());
    else
      $panel.listView(data);

    return $navItem;
  };

  // Define page element generation fns
  var pageElems = {
    header: function(args) {
      // Make notification area
      var $notificationArea = $('<div>').addClass('button notifications')
            .append(
              $('<div>').addClass('total')
                // Total notifications
                .append($('<span>').html(0))
            )
            .append($('<span>').html('Notifications'))
            .notifications();

      // Project switcher
      var $viewSwitcher = $('<div>').addClass('button view-switcher')
            .append(
              // Default View
              $('<div>').addClass('default-view')
                .html('Default View')
                .prepend(
                  $('<span>').addClass('icon').html('&nbsp;')
                )
            )
            .append(
              // Project View
              $('<div>').addClass('select')
                .html('Select View')
                .prepend(
                  $('<span>').addClass('icon').html('&nbsp;')
                )
            );

      // User status area
      var $userInfo = $('<div>').attr({ id: 'user' }).addClass('button')
            .append(
              $('<div>').addClass('name').html('Will Chan')
            )
            .append(
              $('<div>').addClass('icon options')
            );
      
      return [
        $('<div>').addClass('logo'),
        $('<div>').addClass('controls')
          .append($notificationArea)
          .append($viewSwitcher)
          .append($userInfo)
      ];
    },

    'main-area': function(args) {
      var $navigation = $('<div>').attr({ id: 'navigation' });
      var $browser = $('<div>').attr({ id: 'browser' })
            .append(
              // Home breadcrumb
              $('<div>').attr({ id: 'breadcrumbs' })
                .append($('<div>').addClass('home'))
                .append($('<div>').addClass('end'))
            )

            .append(
              // Panel container
              $('<div>').addClass('container')
            );

      makeNavigation(args).appendTo($navigation);

      return [
        $navigation, $browser
      ];
    }
  };

  $.fn.cloudStack = function(args) {
    var $container = $('<div>')
          .attr({
            id: 'container',
            'cloudStack-container': true
          })
          .data('cloudStack-args', args)
          .appendTo(this);

    // Create pageElems
    $.each(pageElems, function(id, fn) {
      var $elem = $('<div>').attr({ id: id });
      
      $(fn(args)).each(function() {
        $elem.append($(this));
      });

      $elem.appendTo($container);
    });

    // User options
    var $options = $('<div>').attr({ id: 'user-options' })
          .appendTo($('#header'));
    $(['Logout']).each(function() {
      $('<a>')
        .attr({ href: '#' })
        .html(this.toString())
        .appendTo($options);
    });

    // Initialize browser
    $('#browser div.container').cloudBrowser();
    $('#navigation li')
      .filter(function() {
        return $(this).hasClass(args.home);
      })
      .click();

    return this;
  };

  // Events
  $(function() {
    $(document).click(function(event) {
      var $target = $(event.target);
      var $container = $target.closest('[cloudStack-container]');
      var args = $container.data('cloudStack-args');

      if (!$container.size()) return true;

      // Navigation items
      if ($target.closest('li.navigation-item').size() && $target.closest('#navigation').size()) {
        var $navItem = $target.closest('li.navigation-item');
        showSection($navItem.data('cloudStack-section-id'), args);

        return false;
      }

      // Browser expand
      if ($target.hasClass('control expand') && $target.closest('div.panel div.toolbar').size()) {
        $('#browser div.container').cloudBrowser('toggleMaximizePanel', { 
          panel: $target.closest('div.panel') 
        });

        return false;
      }

      // Home breadcrumb
      if ($target.is('#breadcrumbs div.home')) {
        showSection(args.home, args);
        return false;
      }

      // Project buttons
      var $projectSwitcher = $target.closest('div.controls div.button.view-switcher');
      if ($projectSwitcher.size()) {
        $projectSwitcher.toggleClass('alt');
        return false;
      }

      // User options
      if ($target.is('#user div.icon.options')) {
        $('#user-options').toggle();

        return false;
      }

      // Hide 'modal' elements
      $('#user-options').hide();
      
      return true;
    });
  });
})(jQuery, window);
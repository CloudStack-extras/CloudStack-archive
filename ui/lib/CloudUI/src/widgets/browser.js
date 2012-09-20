(function($, _, cloudUI) {
  var elems = {
    navigationItem: function(args) {
      var $li = $('<li>');
      var $end = $('<div>').addClass('end');
      var $label = $('<span>').html(args.title);

      $li.append($label);
      $li.attr('title', args.title);

      return $.merge($li, $end);
    },
    panel: function() {
      var $panel = $('<div>').addClass('panel');
      var $shadow = $('<div>').addClass('shadow');

      // For consistency, width and positioning computation is handled directly in JS
      $panel.css({
        position: 'absolute'
      });

      return $panel.append($shadow);
    }
  };

  var navigation = {
    // Gets the nav item corresponding to the specified panel;
    getItem: function(args) {
      var $panel = args.$panel;
      var $navigation = args.$navigation;
      var $navigationItem, $navigationItemEnd;
      var panelIndex = cloudUI.data($panel).panelIndex;

      $navigationItem = $navigation.find('li').filter(function() {
        return cloudUI.data($(this)).panelIndex == panelIndex;
      });
      $navigationItemEnd = $navigationItem.next('.end');

      return $.merge($navigationItem, $navigationItemEnd);
    },

    removeItem: function($navItem) {
      $navItem.remove();
    }
  };

  var panel = {
    // Get new z-index for adding panel, for correct stacking
    zIndex: function(args) {
      var $container = args.$container;
      var $panels = panel.getAll($container);

      return $panels.size() ?
        parseInt($panels.filter(':last').css('z-index')) + 1 : 0;
    },

    // Compute initial panel width, based on container's dimensions
    width: function(args) {
      var $container = args.$container;
      var isMaximized = args.isMaximized;
      var width, containerWidth, panelCount;

      containerWidth = $container.width();
      panelCount = $container.find('.panel').size();

      if (!panelCount || isMaximized) {
        // First panel is always full-sized
        width = containerWidth;
      } else {
        // Partial size
        width = containerWidth - containerWidth / 4;
      }

      return width;
    },

    // Compute initial position of hidden panel
    //
    // Returns CSS 'left' attr in pixels
    hiddenPosition: function(args) {
      var $container = args.$container;

      return $container.width();
    },

    // Compute position of panel in visible position
    //
    // Returns CSS 'left' attr in pixels
    visiblePosition: function(args) {
      var $container = args.$container;
      var isMaximized = args.isMaximized;
      var containerWidth, panelWidth;

      containerWidth = $container.width();
      panelWidth = panel.width({
       $container: $container,
       isMaximized: isMaximized
      });

      return containerWidth - panelWidth;
    },

    // Append new panel to browser
    add: function(args) {
      var browser = args.browser;
      var duration = args.duration;
      var $panel = elems.panel();
      var $container = args.$container;
      var $navigationList = args.$navigation.find('ul');
      var $navigationItem = elems.navigationItem({
        title: args.title
      });
      var isMaximized = args.isMaximized;
      var zIndex, panelWidth, panelInitialPos, panelVisiblePos, panelIndex;

      // Setup nav item event behavior
      cloudUI.event.register({
        $elem: $navigationItem,
        id: 'browser-navigation-item',
        data: {
          browser: browser,
          $panel: $panel
        }
      });

      // Get initial positioning
      zIndex = panel.zIndex({ $container: $container });
      panelWidth = panel.width({
        $container: $container,
        isMaximized: isMaximized
      });
      panelInitialPos = panel.hiddenPosition({
        $container: $container,
        $panel: $panel
      });

      $panel.css({
        zIndex: zIndex,
        left: panelInitialPos
      });
      $panel.width(panelWidth);

      // Append elements
      $container.append($panel);
      $navigationList.append($navigationItem);

      // Setup panel index
      panelIndex = $panel.index();
      cloudUI.data($panel).panelIndex = panelIndex;
      cloudUI.data($navigationItem).panelIndex = panelIndex;

      // Slide-in panel
      if (panelIndex) {
        panel.slideIn({
          $panel: $panel,
          $container: $container,
          useOverlay: true,
          duration: duration,
          complete: function() {
            args.content($panel);
          }
        });
      } else {
        $panel.css({ left: 0 });
        args.content($panel);
      }
    },

    // Remove panel from browser
    remove: function(args) {
      var $panel = args.$panel;
      var $navigation = args.$navigation;
      var $navigationItem = navigation.getItem({
        $panel: $panel,
        $navigation: $navigation
      });
      var $container = args.$container;
      var animate = args.animate;
      var complete = args.complete;
      var slideOutDuration = args.slideOutDuration;

      if (animate) {
        $navigationItem.remove();

        panel.slideOut({
          $panel: $panel,
          $container: $container,
          duration: slideOutDuration,
          complete: function() {
            $panel.remove();
            navigation.removeItem($navigationItem);

            if (complete) complete();
          }
        });
      } else {
        $panel.remove();
        navigation.removeItem($navigationItem);

        if (complete) complete();
      }
    },

    // Clears out all panels from browser
    removeAll: function(args) {
      var $container = args.$container;
      var $navigation = args.$navigation;
      var $panels = panel.getAll($container);

      _.map($panels, function(thisPanel) {
        var $thisPanel = $(thisPanel);

        panel.remove({
          $panel: $thisPanel,
          $navigation: $navigation,
          $container: $container
        });
      });
    },

    // Get every panel from container
    getAll: function($container) {
      return $container.find('.panel');
    },

    // Get panel by index (starting at 1)
    getByIndex: function(index, $container) {
      var $panels = panel.getAll($container);

      return $panels.filter(function() {
        return $(this).index() == index;
      });
    },

    // Make target panel the last
    // -- remove all panels/navigation after it
    makeLast: function(args) {
      var $container = args.$container;
      var $navigation = args.$navigation;
      var $targetPanel = args.$targetPanel;
      var complete = args.complete;
      var $removePanels, removePanelTotal, removePanelCurrent;
      var duration = args.duration;

      // Get panels to remove
      $removePanels = panel.getAll($container).filter(function() {
        var $panel = $(this);
        var thisPanelIndex = cloudUI.data($panel).panelIndex;
        var targetPanelIndex = cloudUI.data($targetPanel).panelIndex;

        return thisPanelIndex > targetPanelIndex;
      });

      // Remove specified panels + navigation
      removePanelTotal = $removePanels.size();
      removePanelCurrent = 0;
      _.map($removePanels, function(thisPanel) {
        var $thisPanel = $(thisPanel);

        panel.remove({
          $panel: $thisPanel,
          $navigation: $navigation,
          $container: $container,
          animate: true,
          slideOutDuration: duration,
          complete: function() {
            removePanelCurrent++;

            if (removePanelTotal == removePanelCurrent && complete) {
              complete($targetPanel);
            }
          }
        });
      });
    },

    // Animated slide-in
    slideIn: function(args) {
      var $panel = args.$panel;
      var $container = args.$container;
      var $overlay = $('<div>').addClass('loading-overlay').css('opacity', 0);
      var duration = args.duration;
      var complete = args.complete;
      var panelVisiblePos;
      var useOverlay = args.useOverlay;

      panelVisiblePos = panel.visiblePosition({
        $container: $container,
        $panel: $panel,
        isMaximized: $panel.width() == $container.width()
      });

      if (useOverlay) $overlay.appendTo($container); // Prevent clicks while animating

      if ($panel.is(':visible')) {
        $panel.animate(
          {
            left: panelVisiblePos
          },
          {
            duration: duration,
            easing: 'easeOutCirc',
            complete: function() {
              $overlay.remove();

              if (!$panel.is(':visible')) {
                return false;
              }

              if (complete) args.complete();

              return true;
            }
          }
        );
      } else {
        $overlay.remove();
        $panel.hide();

        if (complete) complete();
      }
    },

    // Animated slide-out
    slideOut: function(args) {
      var $panel = args.$panel;
      var $container = args.$container;
      var complete = args.complete;
      var duration = args.duration;

      if ($panel.is(':visible')) {
        $panel.animate(
          {
            left: panel.hiddenPosition({
              $container: $container,
              $panel: $panel
            })
          },
          {
            duration: duration,
            easing: 'easeOutCirc',
            complete: function() {
              $panel.hide();

              if (complete) complete();
            }
          }
        );
      } else {
        $panel.hide();
        if (complete) complete();
      }
    },
    hideOthers: function(args) {
      var $panel = args.$panel;
      var $container = args.$container;
      var $otherPanels = $panel.siblings();
      var duration = args.duration;
      var $hidePanels = $otherPanels.filter(function() {
        return $(this).index() > $panel.index();
      });
      var panelVisiblePos = panel.visiblePosition({
        $container: $container,
        isMaximized: $panel.width() == $container.width()
      });

      $panel.show();
      $hidePanels.fadeOut();
    },
    showAll: function(args) {
      var $container = args.$container;
      var $panels = $container.find('.panel');
      var duration = args.duration;

      $panels.show();
    }
  };

  var makeNavigation = function(args) {
    var $navigation = args.$navigation;

    $navigation.append($('<ul>'));
  };

  cloudUI.widgets.browser = cloudUI.widget({
    methods: {
      _init: function(browser, widgetArgs) {
        var $container = widgetArgs.$container;
        var $navigation = widgetArgs.$navigation;
        
        makeNavigation({
          $container: $container,
          $navigation: $navigation,
          browser: browser
        });
      },
      addPanel: function(browser, widgetArgs, args) {
        panel.add({
          $container: widgetArgs.$container,
          $navigation: widgetArgs.$navigation,
          browser: browser,
          content: args.content,
          title: args.title,
          duration: widgetArgs.panelSpeed,
          isMaximized: args.maximizeIfSelected ?
            args.maximizeIfSelected : args.isMaximized
        });
      },
      selectPanel: function(browser, widgetArgs, args) {
        var $panel = args.$panel;
        var index = args.index ? args.index - 1 : 0; // Index in options starts at 1
        var complete = args.complete;

        if (!$panel) {
          // Get panel from index
          $panel = panel.getByIndex(index, widgetArgs.$container);
        }

        panel.makeLast({
          $container: widgetArgs.$container,
          $navigation: widgetArgs.$navigation,
          $targetPanel: $panel,
          browser: browser,
          complete: complete,
          duration: widgetArgs.panelSpeed
        });
      },
      reset: function(browser, widgetArgs, args) {
        panel.removeAll({
          $container: widgetArgs.$container,
          $navigation: widgetArgs.$navigation
        });
      },
      focusPanel: function(browser, widgetArgs, args) {
        var $panel = args.$panel;
        var index = args.index ? args.index - 1 : 0; // Index in options starts at 1
        var slideInDuration = args.slideInDuration;

        if (!$panel) {
          // Get panel from index
          $panel = panel.getByIndex(index, widgetArgs.$container);
        }

        panel.hideOthers({
          $panel: $panel,
          $container: widgetArgs.$container,
          duration: widgetArgs.panelSpeed
        });
      },

      // Defocuses any currently focused panel
      defocusPanel: function(browser, widgetArgs, args) {
        panel.showAll({
          $container: widgetArgs.$container,
          duration: widgetArgs.panelSpeed
        });
      }
    },
    events: {
      'browser-navigation-item': {
        click: function(args) {
          var browser = args.browser;
          var $panel = args.$panel;

          browser.selectPanel({ $panel: $panel });
        },
        mouseover: function(args) {
          var browser = args.browser;
          var $panel = args.$panel;

          if ($panel.hasClass('focused')) return;

          $panel.addClass('focused');

          setTimeout(function() {
            if ($panel.hasClass('focused')) {
              browser.focusPanel({ $panel: $panel });
            }
          }, 700); // Delay until panel is focused
        },
        mouseout: function(args) {
          var browser = args.browser;
          var $panel = args.$panel;

          $panel.parent().find('.panel').removeClass('focused');
          browser.defocusPanel();
        }
      }
    }
  });
}(jQuery, _, cloudUI));

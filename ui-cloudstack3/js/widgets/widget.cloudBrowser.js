(function($) {
  /**
   * Creates a side-stacked browser, with breadcrumb navigation
   *
   * @author Brian Federle
   */
  $.fn.cloudBrowser = function(method) {
    var $browserContainer = this;
    var $browserWindow = $browserContainer.parent();
    var animateOptions = {
      easing: $.easing.easeInOut,
      queue: false
    };

    var panelWidth = function(args) {
      if (!args) args = {};

      var defaultWidth = ($browserWindow.width() / 2) + ($browserWindow.width() / 4);

      if (args.useReducedSize)
        return defaultWidth / 3;
      else
        return defaultWidth;
    };

    var getNavItem = function($panel) {
      return $($('#breadcrumbs').find('ul li')[$panel.index()]);
    };

    var updateBrowserWidth = function() {
      return $browserContainer.width(
        (function() {
          var totalWidth = 0;
          $browserContainer.find('div.panel').each(function() {
            totalWidth += $(this).width() + 50;
          });

          return totalWidth;
        }())
      );
    };

    var repositionToPanel = function($panel) {
      var maximizeIfFirstPanel = function() {
        if ((!$panel.prev().size() && $panel.siblings().size() >= 1)) {
          $browserContainer.cloudBrowser('toggleMaximizePanel', {
            panel: $panel,
            noAnimate: true
          });
        }

        $browserContainer.cloudBrowser('removePanelChildren', {
          panel: $panel
        });

        if ($panel.index() == 0 && $panel.siblings().size() == 0) {
          $panel.addClass('single');
        }
      };

      var doReposition = function() {
        $browserContainer.clearQueue();

        var $navigationItem = getNavItem($panel);
        $('#breadcrumbs').find('ul li').removeClass('active reduced');

        $navigationItem.addClass('active');
        $navigationItem.prev().prev().addClass('reduced');

        var maximizeIfSelected = $panel.hasClass('maximize-if-selected');

        if ($browserContainer.children().size() > 1) {
          var targetPos = 0;

          if (maximizeIfSelected)
            $panel.animate({ width: $browserContainer.parent().width() }, animateOptions);
          else
            $panel.animate({ width: panelWidth() }, animateOptions);

          $panel.siblings().animate({ width: panelWidth({ useReducedSize: true }) }, animateOptions);

          if ($panel.index() >= 2 && !maximizeIfSelected)
            targetPos = -panelWidth({ useReducedSize: true }) * ($panel.index() - 1);
          
          if (maximizeIfSelected) {
            targetPos = -panelWidth({ useReducedSize: true }) * ($panel.index());            
          }

          // Small size for following panel
          $('div.panel').removeClass('reduced');
          var $prevPanel = $panel.prev();
          if ($prevPanel.size()) {
            $prevPanel.animate({ width: panelWidth({ useReducedSize: true })}, animateOptions);
            $prevPanel.addClass('reduced');
          }

          return $browserContainer.animate(
            {
              left: targetPos
            },
            {
              queue: false,
              complete: function() {
                maximizeIfFirstPanel();
              }
            }
          );
        }

        maximizeIfFirstPanel();

        return $browserContainer;
      };

      var $maximizedPanel = $browserContainer.find('div.panel').filter(function() {
        return $(this).hasClass('maximized');
      });

      if ($maximizedPanel.size()) {
        var args = { panel: $maximizedPanel };

        if ($maximizedPanel[0] != $panel[0]) {
          args.callback = doReposition;
        }

        $browserContainer.cloudBrowser('toggleMaximizePanel', args);
      } else {
        doReposition();
      }

      return false;
    };

    var setPanelStyle = function($panel) {
      return $panel.css({
        position: 'relative',
        'float': 'left',
        width: panelWidth()
      });
    };

    var methods = {
      init: function(args) {
        // Setup navigation
        $('#breadcrumbs').append('<ul></ul>');

        $browserContainer.css({
          position: 'relative',
          'float': 'left'
        });

        // Setup events
        $('#breadcrumbs').live('click', function(event) {
          var $target = $(event.target).closest('li');

          if ($target.size()) {
            var index = $target.index('#breadcrumbs ul li');
            var $panel = $($browserContainer.children()[index]);

            $browserContainer.cloudBrowser('selectPanel', { panel: $panel });
          }
        });
      },

      selectPanel: function(args) {
        var $panel = args.panel;

        if (!$panel.size()) return false;

        var $panelNavItem = $($('#breadcrumbs').find('ul li')[$panel.index()]);

        $panel.siblings().removeClass('selected');
        $panel.addClass('selected');

        if (!$panelNavItem.is('.active'))
          repositionToPanel($panel);

        return $panel;
      },

      addPanel: function(args) {
        var $panels = $browserContainer.find('div.panel');
        var $newPanel = $('<div class="panel"></div>').html(args.data);
        var $navigation = $('#breadcrumbs').find('ul');
        var $newBreadcrumb = $('<li></li>');
        var $parentPanel = args.parent;

        if (args.parent && args.parent.is(':visible')) {
          $browserContainer.cloudBrowser('removePanelChildren', {
            panel: $parentPanel
          });
        }

        $panels.removeClass('single');
        $newPanel.appendTo($browserContainer);
        $newBreadcrumb.html('<div class="title">' + args.title + '</div>');
        $navigation.append($newBreadcrumb);
        $navigation.append('<div class="end"></div>');

        setPanelStyle($newPanel);
        updateBrowserWidth();

        if (args.breadcrumbEvent) {
          $newBreadcrumb.bind('click', args.breadcrumbEvent);
        }

        if (!args.noSelectPanel) {
          repositionToPanel($newPanel);
          $browserContainer.cloudBrowser('selectPanel', { panel: $newPanel });
        } else {
          $('#breadcrumbs').find('ul li').removeClass('active reduced');
          $('#breadcrumbs').find('ul li:last').addClass('active');
        }

        if (args.maximizeIfSelected) {
          $newPanel.addClass('selected maximize-if-selected');
        }

        // Add panel
        if (args.withToolbar) {
          var $toolbar = $('<div class="toolbar">');
          $toolbar.append(args.withToolbar);
          $newPanel.prepend($toolbar);
        }

        return $newPanel;
      },

      toggleMaximizePanel: function(args) {
        var $panel = args.panel;

        var callback = args.callback;
        var newPanelWidth = 0;
        var useReducedSize = $panel.hasClass('reduced') || $panel.hasClass('maximized-reduced');

        if (!callback) callback = function() {
          return false;
        };

        var doMinimize = function() {
          if ($('div.panel').size() == 1) return false;

          var targetPos = 0;

          newPanelWidth = panelWidth({ useReducedSize: useReducedSize });
          $panel.removeClass('maximized');
          getNavItem($panel)
            .removeClass('maximized')
            .siblings().find('div.title')
            .animate({ opacity: 1 }, animateOptions);

          if (!args.noAnimate) {
            $panel.animate({ width: newPanelWidth }, animateOptions);
          } else {
            $panel.width(newPanelWidth);
          }

          updateBrowserWidth();

          $panel.removeClass('maximized-reduced');

          if (useReducedSize) {
            $panel.addClass('reduced');
            targetPos = -$panel.position().left;
          }
          else {
            targetPos = -$panel.position().left + panelWidth({ useReducedSize: true });
          }

          if (!args.noAnimate) {
            $browserContainer.animate({ left: targetPos }, animateOptions);
          } else {
            $browserContainer.css({ left: targetPos });
          }

          return $panel;
        };

        var doMaximize = function() {
          newPanelWidth = $browserContainer.parent().width();

          $panel.removeClass('reduced');

          if (useReducedSize) {
            $panel.addClass('maximized-reduced');
          }

          $panel.addClass('maximized');
          var $navItem = getNavItem($panel);
          $navItem.addClass('maximized');
          
          if (!$panel.hasClass('maximize-if-selected')) {
            $navItem.siblings().find('div.title')
              .animate({ opacity: 0.3 });
          }

          if (!args.noAnimate) {
            $panel.animate({ width: newPanelWidth });
            $browserContainer.animate({ left: -$panel.position().left}, { queue: false });
          } else {
            $panel.width(newPanelWidth);
            $browserContainer.css({ left: -$panel.position().left });
          }

          $browserContainer.width($browserContainer.width() + newPanelWidth);

        };

        if ($panel.hasClass('maximized')) {
          doMinimize();
        } else {
          doMaximize();
        }

        callback();

        return $panel;
      },

      removePanelChildren: function(args) {
        var $panel = args.panel;

        if (!$panel.siblings().size()) return false;

        var $panelChildren = $panel.siblings().filter(function() {
          return $(this).index() > $panel.index();
        });

        $panelChildren.each(function() {
          var $panelNavItem = $($('#breadcrumbs').find('ul li')[$(this).index()]);
          $panelNavItem.remove();
          $('#breadcrumbs div.end:last').remove();
          $(this).remove();
        });

        updateBrowserWidth();

        return $panel;
      },

      removePanel: function(args) {
        var $panel = args.panel;

        $browserContainer.cloudBrowser('removePanelChildren', { panel: $panel });
        $panel.remove();
        $('#breadcrumbs').find('ul li:last').remove();
        $('#breadcrumbs').find('div.end:last').remove();

        return $panel;
      },

      removeAllPanels: function() {
        var $panels = $browserContainer.find('div.panel');
        var $navigationItems = $('#breadcrumbs').find('ul li');
        var $navigationItemEnds = $navigationItems.siblings('div.end');

        $panels.remove();
        $navigationItems.remove();
        $navigationItemEnds.remove();
      }
    };

    if (methods[method]) {
      return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || ! method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.cloudBrowser');
    }

    return this;
  };
}(jQuery));

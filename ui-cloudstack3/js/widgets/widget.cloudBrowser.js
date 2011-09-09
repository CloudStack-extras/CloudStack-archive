(function($, cloudStack) {
  cloudStack.ui.api.browser = {};

  // Breadcrumb-related functions
  var breadcrumb = cloudStack.ui.api.browser.breadcrumb = {
    // Generate new breadcrumb
    create: function($panel, title) {
      return $('<div>')
        .append($('<li>').html(title))
        .append($('<div>').addClass('end'))
        .children();
    }
  };

  // Panel-related functions
  var panel = cloudStack.ui.api.browser.panel = {
    calc: {
      // Compute width of panel, relative to container
      width: function($container, options) {
        options = options ? options : {};
        var width = $container.find('div.panel').size() < 1 || options.maximized == true ? 
              $container.width() : $container.width() - $container.width() / 4;

        return width;
      },

      // Get left position
      position: function($container, options) {
        return $container.find('div.panel').size() <= 1 || options.maximized == true ?
          0 : panel.calc.width($container, options) - panel.calc.width($container, options) / 1.5;
      },

      // Get the top panel z-index, for proper stacking
      topIndex: function($container) {
        var base = 1000; // Minimum z-index

        return Math.max.apply(
          null,
          $.map(
            $container.find('div.panel'),
            function(elem) {
              return parseInt($(elem).css('z-index')) || base;
            }
          )
        ) + 1;
      }
    },

    initialState: function($container, $panel) {
      return {
        left: $container.width()
      };
    },

    // Generate new panel
    create: function($container, options, complete) {
      var $panel = $('<div>').addClass('panel').css(
        {
          position: 'absolute',
          width: panel.calc.width($container, { maximized: options.maximized }),
          zIndex: panel.calc.topIndex($container)
        }
      ).append(
        // Shadow
        $('<div>').addClass('shadow')
      ).append(options.data);


      if (complete) complete($container, $panel, options.maximized);

      return $panel;
    },

    stack: function($container, $topPanel, duration, actions, options) {
      // Position panel
      actions.initial($container, $topPanel, panel.initialState($container, $topPanel));

      // Reduced appearance for previous panels
      actions.reduce(
        $topPanel.siblings().filter(function() {
          return $(this).index() < $topPanel.index();
        })
      );

      // Slide-in panel
      var position = panel.calc.position($container, { maximized: options.maximized });
      actions.slideIn(
        $container,
        
        // Panel to slide-in
        $topPanel,

        // Positioning
        { left: position },

        // Complete
        function(complete) {
          return function() {
            complete ? complete() : function() { return false; };

            actions.hide(
              $topPanel.siblings().filter(function() {
                return $(this).width() == $topPanel.width();
              })
            );
          };
        },

        // Duration
        $container.find('div.panel').size() > 1 ? duration : 0
      );
    }
  };

  $.widget('cloudStack.cloudBrowser', {
    _init: function() {
      $('#breadcrumbs').append(
        $('<ul>')
      );
    },

    selectPanel: function(args) {
    },

    // Append new panel
    addPanel: function(args) {
      return panel.create(
        this.element, // Container

        // Data
        {
          maximized: args.maximizeIfSelected,
          data: args.data
        },

        // Post-creation
        function($container, $panel, maximized) {
          $panel.appendTo($container);
          breadcrumb.create($panel, args.title).appendTo('#breadcrumbs ul');
          
          panel.stack(
            $container, // Container
            $panel, // Top panel
            500, // Duration
            {
              initial: function($container, $target, position, options) {
                $target.css(position);
              },
              slideIn: function($container, $target, position, complete, duration) {
                var completeFn = complete(function() {
                  args.complete ? args.complete($panel) : function() { return false; };
                });
                
                if (args.parent && args.parent.index() < $target.index() - 1) {
                  $target.css(position);
                  completeFn();
                } else {
                  $target.animate(position, {
                    duration: duration,
                    easing: 'easeOutCirc',
                    complete: completeFn
                  });                  
                }
              },
              reduce: function($reduce) {
                $reduce.addClass('reduced');
              },
              hide: function($hide) {
                $hide.hide();
              }
            },
            {
              maximized: maximized ? true : false,
              parent: args.parent
            }
          );
        }
      );
    },

    // Clear all panels
    removeAllPanels: function(args) {
      this.element.find('div.panel').remove();
      $('#breadcrumbs').find('ul li').remove();
      $('#breadcrumbs').find('ul div.end').remove();
    }
  });
})(jQuery, cloudStack);
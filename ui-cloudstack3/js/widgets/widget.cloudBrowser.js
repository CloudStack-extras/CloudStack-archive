(function($, cloudStack) {
  cloudStack.ui.api.browser = {};
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

    stack: function($container, $topPanel, duration, data, actions, options) {
      // Position panel
      actions.initial($container, $topPanel, panel.initialState($container, $topPanel));

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

            actions.reduce(
              $topPanel.siblings().filter(function() {
                return $(this).index() < $topPanel.index();
              }),
              $topPanel.siblings().filter(function() {
                return $(this).width() == $topPanel.width();
              }),
              duration
            );
          };
        },

        // Duration
        $container.find('div.panel').size() > 1 ? duration : 0
      );
    },

    initialState: function($container, $panel) {
      return {
        left: $container.width()
      };
    }
  };

  $.widget('cloudStack.cloudBrowser', {
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
          
          panel.stack(
            $container, // Container
            $panel, // Top panel
            500, // Duration
            args.data, // Initial panel data
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
              reduce: function($reduced, $hide, duration) {
                $reduced.addClass('reduced');
                $hide.hide();
              }
            },
            {
              maximized: maximized ? true : false
            }
          );
        }
      );
    },

    // Clear all panels
    removeAllPanels: function(args) {
      this.element.find('div.panel').remove();
    }
  });
})(jQuery, cloudStack);
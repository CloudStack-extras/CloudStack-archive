(function($, cloudStack) {
  cloudStack.ui.api.browser = {};
  var panel = cloudStack.ui.api.browser.panel = {
    calc: {
      // Compute width of panel, relative to container
      width: function($container, options) {
        var width = $container.width();

        if ($container.find('div.panel').size() || (options && options.reduced))
          width = width - width / 4;
        
        return width;
      },
      
      // Get left position
      position: function($container) {
        return $container.find('div.panel').size() ? 
          panel.calc.width($container) - panel.calc.width($container) / 1.5 : 0;
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
    create: function($container, options) {
      return $('<div>').addClass('panel').css(
        {
          position: 'absolute',
          width: panel.calc.width($container),
          zIndex: panel.calc.topIndex($container)
        }
      ).prepend(
        // Shadow
        $('<div>').addClass('shadow')
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
      var $container = this.element;
      var $panel = panel.create(this.element);
      var $items;
      var duration = 400;

      // Parent panel behavior
      if (args.parent && args.parent.size()) {
        $items = $container.find('div.panel').filter(function() {
          return $(this).index() > args.parent.index();
        }).remove();
      }

      // Don't animate panel if it is the first or there is a parent
      if ($items && $items.size() || 
          !$container.find('div.panel').size()) {
        duration = 0;
      }

      // Animation and positioning
      $panel
        .append(args.data)
        .css(panel.initialState($container, $panel))
        .animate({
          left: panel.calc.position($container)
        }, {
          duration: duration,
          complete: function() {
            if (args.complete)
              args.complete($panel).hide().fadeIn(duration / 1.5);
          }
        })
        .appendTo($container);

      return $panel;
    },

    // Clear all panels
    removeAllPanels: function(args) {
      this.element.find('div.panel').remove();
    }
  });
})(jQuery, cloudStack);
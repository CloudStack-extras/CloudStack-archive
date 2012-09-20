(function($, _) {
  var cloudUI = window.cloudUI = {
    // DOM data storage and retrieval
    // -- based on jQuery data handling
    data: function($elem) {
      var cloudUI = $elem.data('cloudUI');

      if (!_.isObject(cloudUI)) {
        cloudUI = {};
        $elem.data('cloudUI', cloudUI);
      }

      return cloudUI;
    },

    // Event handling
    event: {
      register: function(args) {
        var $elem = args.$elem;
        var id = args.id;
        var data = args.data;

        $elem.attr('cs-event-id', id);
        cloudUI.data($elem).eventData = data;
        cloudUI.event.call('init', $elem);

        return $elem;
      },
      handler: function(args) {
        var handlers = args;

        _.each(handlers, function(handler, id) {
          var events = _.keys(handler).join(' ');

          $(document).bind(events, function(event, eventData) {
            var $target = $(event.target);
            var $eventTarget = eventData && eventData.$elem ?
                  eventData.$elem : $target.closest('[cs-event-id=' + id + ']');
            var type = event.type;
            var data;

            if (!$eventTarget.size() ||
                $eventTarget.attr('cs-event-id') != id) return true;

            data = $eventTarget.data('cloudUI').eventData;

            return handler[type](
              eventData && eventData.options ?
                _.extend(_.clone(data), eventData.options) : data
            );
          });
        });

        return true;
      },
      call: function(eventID, item, options) {
        var eventArgs = {
          $elem: _.isString(item) ?
            $(document).find('[cs-event-id=' + item + ']') : // Is an event ID
            item // Is a jQuery object
        };

        if (options) {
          _.extend(eventArgs, { options: options });
        }

        return $(document).trigger(eventID, eventArgs);
      }
    }
  };

  // Handler for dataProvider, to maintain consistency for data retrieval
  cloudUI.dataProvider = function(args) {
    var dataProvider = args.dataProvider;
    var success = args.success;
    var error = args.error;
    var context = cloudUI.context(args.context);

    dataProvider({
      context: context,
      response: {
        success: function(args) {
          success(args);
        },
        error: function(args) {
          error(args);
        }
      }
    });
  };

  // Handles context objects, so they are passed in a standard format
  cloudUI.context = function(context, args) {
    var newContext = _.clone(context ? context : {});
    var id, data, contextItem;

    if (args) {
      id = args.id;
      data = args.data;
      contextItem = newContext[id] ?
        newContext[id] : [];
      
      if (_.isArray(data)) {
        // Merge given data set with existing list
        newContext[id] = _.flatten([contextItem, data]);
      } else {
        // Is an object, just push into existing list
        contextItem.push(data);
        newContext[id] = contextItem;
      }
    }

    return newContext;
  };

  // Widget factory
  cloudUI.widget = function(args) {
    var methods = args.methods;
    var events = args.events;

    return function(args) {
      var widgetArgs = args;
      var widget = {};

      // Build method map
      _.map(methods, function(method, methodID) {
        widget[methodID] = function(args) {
          var method = methods[methodID](widget, widgetArgs, args);

          return method ? method : widget;
        };
      });

      widget._init(widget, widgetArgs, args);


      // Register event handling
      if (events) {
        cloudUI.event.handler(events);
      }

      return widget;
    };
  };

  // Holds standard widgets
  cloudUI.widgets = {};
}(jQuery, _));

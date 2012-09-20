(function($, _, cloudUI) {
  var elems = {
    action: function(args) {
      var id = args.id;
      var action = args.action;
      var list = args.list;
      var listArgs = args.listArgs;
      var $tr = args.$tr;
      var $action = $('<div>').addClass('action');
      var $icon = $('<span>').addClass('icon').html('&nbsp;');

      $action.addClass(id);
      $action.attr({
        alt: action.label,
        title: action.label
      });
      $action.append($icon);

      cloudUI.event.register({
        id: 'list-view-action',
        $elem: $action,
        data: {
          $action: $action,
          $tr: $tr,
          action: action,
          list: list,
          listArgs: listArgs
        }
      });

      return $action;
    }
  };
  
  cloudUI.event.handler({
    'list-table-header-row': {
      init: function(args) {
        var $tr = args.$tr;
        var $actionsHeader = $('<th>').addClass('actions').html('Actions');

        $tr.append($actionsHeader);
      }
    },
    
    'list-table-row': {
      init: function(args) {
        var $tr = args.$tr;
        var list = args.list;
        var listArgs = args.listArgs;
        var $actions = $('<td>').addClass('actions');
        var actions = args.listArgs.actions;

        $tr.append($actions);

        _.each(actions, function(action, id) {
          var $action = elems.action({
            id: id,
            action: action,
            list: list,
            listArgs: listArgs,
            $tr: $tr
          });

          $action.appendTo($actions);
        });
      }
    },

    'list-view-action': {
      click: function(args) {
        // Call action event
      }
    }
  });
}(jQuery, _, cloudUI));

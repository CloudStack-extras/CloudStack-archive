(function($, _, cloudUI) {
  cloudUI.event.handler({
    'list-table-item': {
      init: function(args) {
        var $td = args.$td;
        var list = args.list;
        var listArgs = args.listArgs;
        var field = args.field;
        var fieldID = args.fieldID;
        var dataItem = args.dataItem;
        var indicator = field.indicator;

        if (!indicator) return;

        $td
          .addClass('state')
          .addClass(indicator[dataItem[fieldID]]);
      }
    }
  });
}(jQuery, _, cloudUI));

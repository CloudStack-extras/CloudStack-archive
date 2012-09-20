(function($, _, cloudUI) {
  // Application list view->detail view flow
  cloudUI.event.handler({
    'list-table-item': {
      click: function(args) {
        var $td = args.$td;
        var $tr = $td.closest('tr');
        var application = args.listArgs.application;
        var section = args.listArgs.section;
        var browser = application.widgets.browser;
        var detailArgs = args.listArgs.section.details;
        var list = args.list;
        var data = list.getItemData($tr);
        var context = cloudUI.context(args.context, {
          id: args.listArgs.id,
          data: data
        });

        if (!detailArgs) return;

        // Only first cell column shows detail view
        if ($td.hasClass('first')) {
          $tr.addClass('selected')
            .siblings().removeClass('selected');
          browser.selectPanel({
            $panel: $td.closest('.panel')
          });
          browser.addPanel({
            title: $td.find('span').html(),
            content: function($panel) {
              var details;

              // Initialize detail view
              details = cloudUI.widgets.details(_.extend(
                _.clone(detailArgs), {
                  context: context,
                  $container: $panel
                }
              ));
            }
          });
        }
      }
    }
  });
}(jQuery, _, cloudUI));

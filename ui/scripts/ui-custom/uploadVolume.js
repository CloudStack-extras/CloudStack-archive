(function(cloudStack, $) {
  cloudStack.uiCustom.uploadVolume = function(args) {
    var listView = args.listView;
    var action = args.action;

    return function(args) {
      var context = args.context;

      var vmList = function(args) {
        // Create a listing of instances, based on limited information
        // from main instances list view
        var $listView;
        var instances = $.extend(true, {}, args.listView, {
          context: context,
          uiCustom: true
        });

        instances.listView.actions = {
          select: {
            label: _l('label.select.instance'),
            type: 'radio',
            action: {
              uiCustom: function(args) {
                var $item = args.$item;
                var $input = $item.find('td.actions input:visible');

                if ($input.attr('type') == 'checkbox') {
                  if ($input.is(':checked'))
                    $item.addClass('multi-edit-selected');
                  else
                    $item.removeClass('multi-edit-selected');
                } else {
                  $item.siblings().removeClass('multi-edit-selected');
                  $item.addClass('multi-edit-selected');
                }
              }
            }
          }
        };

        $listView = $('<div>').listView(instances);

        // Change action label
        $listView.find('th.actions').html(_l('label.select'));

        return $listView;
      };

      var $dataList = vmList({
        listView: listView
      }).dialog({
        dialogClass: 'multi-edit-add-list panel',
        width: 825,
        title: _l('label.upload.volume'),
        buttons: [
          {
            text: _l('label.apply'),
            'class': 'ok',
            click: function() {
              if (!$dataList.find(
                'input[type=radio]:checked, input[type=checkbox]:checked'
              ).size()) {
                cloudStack.dialog.notice({ message: _l('message.select.instance')});

                return false;
              }

              var complete = args.complete;
              var $loading = $('<div>').addClass('loading-overlay');

              $loading.appendTo($dataList);
              action({
                context: $.extend(true, {}, context, {
                  instances: [
                    $dataList.find('tr.multi-edit-selected').data('json-obj')
                  ]
                }),
                response: {
                  success: function(args) {
                    $dataList.fadeOut(function() {
                      $dataList.remove();
                      $(window).trigger('cloudStack.fullRefresh');
                    });
                    $('div.overlay').fadeOut(function() {
                      $('div.overlay').remove();
                    }); 
                    complete({
                      $item: $('<div>'),
                      _custom: args._custom
                    });
                  },
                  error: function(args) {
                    cloudStack.dialog.notice({ message: args });
                  }
                }
              }); 
            }
          },
          {
            text: _l('label.cancel'),
            'class': 'cancel',
            click: function() {
              $dataList.fadeOut(function() {
                $dataList.remove();
              });
              $('div.overlay').fadeOut(function() {
                $('div.overlay').remove();
              });
            }
          }
        ]
      }).parent('.ui-dialog').overlay();
    };
  };
}(cloudStack, jQuery)); 

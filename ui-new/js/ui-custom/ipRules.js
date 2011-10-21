(function($, cloudStack) {
  cloudStack.ipRules = function(args) {
    return function(detailArgs) {
      var context = detailArgs.context;
      
      var portMultiEdit = function(args) {
        return $('<div>').multiEdit(args);
      };

      var netChart = function(args) {
        var $chart = $('#template').find('.network-chart').clone();

        $chart.find('.view-details').click(function() {
          var targetId = $(this).attr('net-target');
          var targetName = $(this).parent().find('.name').find('span').html();
          var target = args[targetId];
          var $browser = $(this).closest('.detail-view').data('view-args').$browser;

          $browser.cloudBrowser('addPanel', {
            title: targetName,
            maximizeIfSelected: true,
            complete: function($newPanel) {
              $newPanel.detailView({
                $browser: $browser,
                name: targetId,
                context: context,
                tabs: {
                  network: {
                    title: targetName,
                    custom: function(args) {
                      return portMultiEdit($.extend(target, {
                        context: context
                      }));
                    }
                  }
                }
              });
            }
          });
        });

        return $chart;
      };

      return netChart(args);
    };
  };
})(jQuery, cloudStack);

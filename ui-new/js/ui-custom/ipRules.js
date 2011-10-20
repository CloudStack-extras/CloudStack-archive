(function($, cloudStack) {
  cloudStack.ipRules = function(args) {
    return function() {
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
            maximizeIfSelected: targetId == 'firewall',
            complete: function($newPanel) {
              $newPanel.detailView({
                $browser: $browser,
                name: targetId,
                tabs: {
                  network: {
                    title: targetName,
                    custom: function(args) {
                      return portMultiEdit(target);
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

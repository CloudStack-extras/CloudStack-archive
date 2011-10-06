(function($, cloudStack) {
  /**
   * Make system zone 'org' chart
   */
  cloudStack.zoneChart = function(args) {
    return function(listViewArgs) {
      var $browser = listViewArgs.$browser;
      var $chartView = $('<div>').addClass('system-chart-view')
            .append(
              $('<div>').addClass('toolbar')
            )
            .append(
              $('#template').find('div.zone-chart').clone()
            );
      args.dataProvider({
        id: listViewArgs.id,
        jsonObj: listViewArgs.jsonObj,
        context: { zones: listViewArgs.context.physicalResources },
        response: {
          success: function(dataProviderArgs) {
            var data = dataProviderArgs.data;
            var name = data.name;
            
            // Replace cell contents
            $chartView.find('li.zone div.name span').html(name);

            // Events
            $chartView.click(function(event) {
              var $target = $(event.target);
              var $panel;

              // View zone details button
              if ($target.is('ul li div.view-details')) {
                $panel = $browser.cloudBrowser('addPanel', {
                  title: 'Zone Details',
                  data: '',
                  noSelectPanel: true,
                  maximizeIfSelected: true,
                  complete: function($newPanel) {
                    // Create detail view
                    $.extend(args.detailView, {
                      id: listViewArgs.id,
                      context: { zones: listViewArgs.context.physicalResources },
                      $browser: listViewArgs.$browser
                    });

                    $panel.detailView(args.detailView);
                  }
                });

                return false;
              }

              // View all
              if ($target.is('ul li div.view-all')) {
                $panel = $browser.cloudBrowser('addPanel', {
                  title: $target.closest('li').find('div.name span').html(),
                  data: '',
                  noSelectPanel: true,
                  maximizeIfSelected: true,
                  complete: function($newPanel) {
                    $panel.listView(
                      $.extend(cloudStack.sections.system.sections.physicalResources.subsections[
                        $target.attr('zone-target')
                      ], {
                        $browser: $browser,
                        $chartView: $chartView,
                        ref: { zoneID: listViewArgs.id },                        
                        context: { zones: [listViewArgs.jsonObj] }
                      })
                    );
                  }
                });

                return false;
              };

              return true;
            }); 
          }          
        }
      });

      return $chartView;
    };
  };
})(jQuery, cloudStack);

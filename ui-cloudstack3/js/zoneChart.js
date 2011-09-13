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

      $(['pod', 'cluster', 'primary-storage', 'host']).each(function() {
        $chartView.find('div.toolbar').append(
          $('<div>').addClass('button add').addClass('add-' + this).append(
            $('<span>').html('Add ' + this.replace('-', ' '))
          )
        );
      });
      
      args.dataProvider({
        id: listViewArgs.id,
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
                  title: $target.closest('li').find('div.name span').html(),
                  data: '',
                  noSelectPanel: true,
                  maximizeIfSelected: true
                });
                $browser.cloudBrowser('toggleMaximizePanel', {
                  panel: $panel
                });

                // Create detail view
                $.extend(args.detailView, {
                  id: listViewArgs.id,
                  $browser: listViewArgs.$browser
                });
                $panel.detailView(args.detailView);

                return false;
              }

              // View all
              if ($target.is('ul li div.view-all')) {
                $panel = $browser.cloudBrowser('addPanel', {
                  title: $target.closest('li').find('div.name span').html(),
                  data: '',
                  noSelectPanel: true,
                  maximizeIfSelected: true
                });
                $browser.cloudBrowser('toggleMaximizePanel', {
                  panel: $panel
                });

                $panel.listView(
                  $.extend(cloudStack.sections.system.sections.physicalResources.subsections[
                    $target.attr('zone-target')
                  ], {
                    $browser: $browser,
                    $chartView: $chartView
                  })
                );

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
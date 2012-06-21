(function($, cloudStack) {
  var elems = {
    router: function() {
      var $router = $('<li>').addClass('tier virtual-router');
      var $title = $('<span>').addClass('title').html('Virtual Router');

      $router.append($title);

      // Append horizontal chart line
      $router.append($('<div>').addClass('connect-line'));

      return $router;
    },
    tier: function(args) {
      var name = args.name;
      var cidr = args.cidr;
      var isPlaceholder = args.isPlaceholder;
      var $tier = $('<li>').addClass('tier');
      var $title = $('<span>').addClass('title');
      var $cidr = $('<span>').addClass('cidr');

      if (isPlaceholder) {
        $tier.addClass('placeholder');
        $title.html('Create Tier');
      } else {
        $title.html(name);
        $cidr.html(cidr);
      }

      $tier.append($title, $cidr);

      // Append horizontal chart line
      $tier.append($('<div>').addClass('connect-line'));

      return $tier;
    },
    chart: function(args) {
      var tiers = args.tiers;
      var $tiers = $('<ul>').addClass('tiers');
      var $router = elems.router();
      var $chart = $('<div>').addClass('vpc-chart');

      if (tiers.length) {
        $(tiers).map(function(index, tier) {
          var $tier = elems.tier({
            name: tier.name,
            cidr: tier.cidr
          });

          $tier.appendTo($tiers);
        });
        
      }
      
      elems.tier({ isPlaceholder: true }).appendTo($tiers);
      $tiers.prepend($router);
      $chart.append($tiers);

      return $chart;
    }
  };

  cloudStack.uiCustom.vpc = function(args) {
    return function(args) {
      var $browser = $('#browser .container');
      var vpc = args.context.vpc[0];
      var $chart = elems.chart({
        tiers: [
          {
            name: 'tier1',
            cidr: '0.0.0.0/0'
          },
          {
            name: 'tier2',
            cidr: '10.0.0.0/24'
          }
        ]
      });

      $browser.cloudBrowser('addPanel', {
        maximizeIfSelected: true,
        title: 'Configure VPC: ' + vpc.name,
        complete: function($panel) {
          $panel.append($chart);
          cloudStack.dialog.createForm({
            form: {
              title: 'Add new tier',
              desc: 'Please fill in the following to add a new VPC tier.',
              fields: {
                name: { label: 'Name', validation: { required: true } }
              }
            },
            after: function(args) {}
          });
        }
      });
    };
  };
}(jQuery, cloudStack));
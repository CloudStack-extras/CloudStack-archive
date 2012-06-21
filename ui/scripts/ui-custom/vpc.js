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
      var virtualMachines = args.virtualMachines;
      var $tier = $('<li>').addClass('tier');
      var $title = $('<span>').addClass('title');
      var $cidr = $('<span>').addClass('cidr');
      var $vmCount = $('<span>').addClass('vm-count');

      if (isPlaceholder) {
        $tier.addClass('placeholder');
        $title.html('Create Tier');
      } else {
        $title.html(name);
        $cidr.html(cidr);
        $vmCount.append(
          $('<span>').addClass('total').html(virtualMachines.length),
          ' VMs'
        );
      }

      $tier.append($title, $cidr, $vmCount);

      // Append horizontal chart line
      $tier.append($('<div>').addClass('connect-line'));

      return $tier;
    },
    chart: function(args) {
      var tiers = args.tiers;
      var vpcName = args.vpcName;
      var $tiers = $('<ul>').addClass('tiers');
      var $router = elems.router();
      var $chart = $('<div>').addClass('vpc-chart');
      var $title = $('<div>').addClass('vpc-title').html(vpcName);

      if (tiers.length) {
        $(tiers).map(function(index, tier) {
          var $tier = elems.tier({
            name: tier.name,
            cidr: tier.cidr,
            virtualMachines: tier.virtualMachines
          });

          $tier.appendTo($tiers);
        });
        
      }
      
      elems.tier({ isPlaceholder: true }).appendTo($tiers)
        .click(addTierDialog);
      $tiers.prepend($router);
      $chart.append($title, $tiers);

      return $chart;
    }
  };

  var addTierDialog = function() {
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
  };

  cloudStack.uiCustom.vpc = function(args) {
    return function(args) {
      var $browser = $('#browser .container');
      var $toolbar = $('<div>').addClass('toolbar');
      var tiers = [ // Dummy content
        {
          name: 'tier1',
          cidr: '0.0.0.0/0',
          virtualMachines: [
            { name: 'i-2-VM' },
            { name: 'i-3-VM' }
          ]
        },
        {
          name: 'tier2',
          cidr: '10.0.0.0/24',
          virtualMachines: []
        }
      ];
      var vpc = args.context.vpc[0];
      var $chart = elems.chart({
        vpcName: vpc.name,
        tiers: tiers
      });

      $browser.cloudBrowser('addPanel', {
        maximizeIfSelected: true,
        title: 'Configure VPC: ' + vpc.name,
        complete: function($panel) {
          $panel.append($toolbar, $chart);

          if (!tiers || !tiers.length) {
            addTierDialog();
          }
        }
      });
    };
  };
}(jQuery, cloudStack));
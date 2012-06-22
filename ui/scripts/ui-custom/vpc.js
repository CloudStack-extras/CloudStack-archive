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
      var actions = $.map(
        args.actions ? args.actions : {}, function(value, key) {
          return { id: key, action: value };
        }
      );
      var isPlaceholder = args.isPlaceholder;
      var virtualMachines = args.virtualMachines;
      var $tier = $('<li>').addClass('tier');
      var $title = $('<span>').addClass('title');
      var $cidr = $('<span>').addClass('cidr');
      var $vmCount = $('<span>').addClass('vm-count');
      var $actions = $('<div>').addClass('actions');

      // Ignore special actions
      // -- Add tier action is handled separately
      actions = $.grep(actions, function(action) {
        return action.id != 'add';
      });

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
        $tier.append($actions);

        // Add actions
        $(actions).map(function(index, action) {
          var $action = $('<div>').addClass('action');

          $action.addClass(action.id);
          $action.append($('<span>').addClass('icon').html('&nbsp;'));
          $actions.append($action);

          // Action event
          $action.click(function() {
            tierAction({
              action: action,
              $tier: $tier
            });
          });
        });
      }

      $tier.prepend($title, $cidr, $vmCount);

      // Append horizontal chart line
      $tier.append($('<div>').addClass('connect-line'));

      return $tier;
    },
    chart: function(args) {
      var tiers = args.tiers;
      var actions = args.actions;
      var vpcName = args.vpcName;
      var $tiers = $('<ul>').addClass('tiers');
      var $router = elems.router();
      var $chart = $('<div>').addClass('vpc-chart');
      var $title = $('<div>').addClass('vpc-title').html(vpcName);

      var showAddTierDialog = function() {
        addTierDialog({
          $tiers: $tiers,
          actions: actions
        });
      };

      if (tiers.length) {
        $(tiers).map(function(index, tier) {
          var $tier = elems.tier({
            name: tier.name,
            cidr: tier.cidr,
            virtualMachines: tier.virtualMachines,
            actions: actions
          });

          $tier.appendTo($tiers);
        });

      }

      elems.tier({ isPlaceholder: true }).appendTo($tiers)
        .click(showAddTierDialog);
      $tiers.prepend($router);
      $chart.append($title, $tiers);

      if (!tiers || !tiers.length) {
        showAddTierDialog();
      }

      return $chart;
    }
  };

  // Handles tier action, including UI effects
  var tierAction = function(args) {
    var $tier = args.$tier;
    var $loading = $('<div>').addClass('loading-overlay').appendTo($tier);
    var actionArgs = args.action.action;
    var action = actionArgs.action;
    var notification = actionArgs.notification;
    var label = actionArgs.label;

    action({
      response: {
        success: function(args) {
          cloudStack.ui.notifications.add(
            // Notification
            {
              desc: label,
              poll: notification.poll
            },

            // Success
            function(args) {
              $loading.remove();
            },

            {},

            // Error
            function(args) {
              $loading.remove();
            }
          );
        },
        error: function(args) { $loading.remove(); }
      }
    });
  };

  // Appends a new tier to chart
  var addNewTier = function(args) {
    var actions = args.actions;
    var tier = $.extend(args.tier, {
      actions: actions,
      virtualMachines: []
    });
    var $tiers = args.$tiers;

    $tiers.find('li.placeholder')
      .before(
        elems.tier(tier)
          .hide()
          .fadeIn('slow')
      );
  };

  // Renders the add tier form, in a dialog
  var addTierDialog = function(args) {
    var actions = args.actions;
    var $tiers = args.$tiers;

    cloudStack.dialog.createForm({
      form: actions.add.createForm,
      after: function(args) {
        var $loading = $('<div>').addClass('loading-overlay').prependTo($tiers.find('li.placeholder'));
        actions.add.action({
          data: args.data,
          response: {
            success: function(args) {
              var tier = args.data;

              $loading.remove();
              addNewTier({
                tier: tier,
                $tiers: $tiers,
                actions: actions
              });
            }
          }
        });
      }
    });
  };

  cloudStack.uiCustom.vpc = function(args) {
    var tierArgs = args.tiers;

    return function(args) {
      var $browser = $('#browser .container');
      var $toolbar = $('<div>').addClass('toolbar');
      var vpc = args.context.vpc[0];

      $browser.cloudBrowser('addPanel', {
        maximizeIfSelected: true,
        title: 'Configure VPC: ' + vpc.name,
        complete: function($panel) {
          var $loading = $('<div>').addClass('loading-overlay').appendTo($panel);

          $panel.append($toolbar);

          // Load data
          tierArgs.dataProvider({
            response: {
              success: function(args) {
                var tiers = args.data.tiers;
                var $chart = elems.chart({
                  actions: tierArgs.actions,
                  vpcName: vpc.name,
                  tiers: tiers
                }).appendTo($panel);

                $loading.remove();
                $chart.fadeIn(function() {
                });
              }
            }
          });
        }
      });
    };
  };
}(jQuery, cloudStack));
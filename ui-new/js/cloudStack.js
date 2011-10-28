(function($, testData) {
  $.extend(window.cloudStack, testData, {
    home: 'dashboard',

    sections: {
      /**
       * Dashboard
       */
      dashboard: {},
      //'dashboard-user': {},
      instances: {},
      storage: {},
      network: {},
      templates: {},
      accounts: {},
      domains: {},
      events: {},
      system: {},
      'global-settings': {},
      configuration: {}
    }
  });

  $(function() {
    var $container = $('#cloudStack3-container');
    
    cloudStack.uiCustom.login({
      $container: $container,
      
      // Use this for checking the session, to bypass login screen
      bypassLoginCheck: function(args) {
        return false;
        return {
          user: {
            login: 'wchan',
            name: 'Will Chan'
          }
        };
      },

      // Actual login process, via form
      loginAction: function(args) {
        if (args.data.username != 'invalid') {
          login();

          // Store capabilities
          $.ajax({
            url: createURL('listCapabilities'),
            dataType: 'json',
            async: false,
            success: function(data) {
              cloudStack._capabilities = data.listcapabilitiesresponse.capability;
            }
          });
          return args.response.success();
        }

        return args.response.error();
      },

      // Show cloudStack main UI widget
      complete: function(args) {
        $container.cloudStack($.extend(cloudStack, {
          context: {
            users: [
              {
                name: args.user.name,
                login: args.user.login
              }
            ]
          }
        }));
      }
    });
  });
})(jQuery, testData);

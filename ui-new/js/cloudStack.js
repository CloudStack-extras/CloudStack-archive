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

  $(function() {
    $('#cloudStack3-container').cloudStack(cloudStack);
  });
})(jQuery, testData);

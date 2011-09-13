(function($, testData) {
  $.extend(window.cloudStack, testData, {
    home: 'dashboard',

    sections: {
      /**
       * Dashboard
       */
      dashboard: {},
      'dashboard-user': {},
      instances: {},
      storage: {},
      network: {},
      templates: {},
      accounts: {},
      domains: {},
      events: {},
      system: {},
      configuration: {}
    }
  });

  $(function() {
    $('#cloudStack3-container').cloudStack(cloudStack);
  });
})(jQuery, testData);

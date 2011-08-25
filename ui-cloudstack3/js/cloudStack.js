(function($, testData) {
  window.cloudStack = {
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
      events: {},
      system: {},
      accounts: {},
      configuration: {}
    }
  };

  $(function() {
    $('#cloudStack3-container').cloudStack(cloudStack);
  });
})(jQuery, window.testData);

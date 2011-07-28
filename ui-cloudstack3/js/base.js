/**
 * CloudStack 3.0 UI functionality
 */
(function($, window) {
  var $browser = null;

  var _cloudStack = {
    ui: {
      $browser: null
    },
    data: {},

    init: function() {
      var setupBrowser = function() {
        $browser = _cloudStack.ui.$browser = $('#browser div.container');
        $browser.cloudBrowser();
      };

      var setupEvents = function() {
        $(document).bind('click change keypress', function(event) {
          _cloudStack.ui.api.notifications.hideSample();

          var $target = $(event.target);
          var $button = $target.closest('[ui-id]');
          if (!$button.size()) return true;

          var buttonID = $button.attr('ui-id');
          if (!buttonID) return true;

          var action = _cloudStack.ui.elems[buttonID].actions[event.type];
          if (action) return action(event, $button);

          return true;
        });
      };

      setupEvents();
      setupBrowser();

      // Store template data
      _cloudStack.ui.$template = $('#template').remove();

      // Show first nav item on page load
      _cloudStack.ui.api.showSamplePage($('#navigation').find('ul li:first'));

      return true;
    }
  };

  window._cloudStack = _cloudStack;

  $(_cloudStack.init);
})(jQuery, window);

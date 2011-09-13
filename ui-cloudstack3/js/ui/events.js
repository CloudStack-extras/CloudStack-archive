(function($, cloudStack) {
  var event = cloudStack.ui.event = {};
  
  // Attach element to a specific event type
  event.elem = function(widget, elem, $elem, extraData) {
    // Setup DOM metadata
    var data = { cloudStack: {} };
    data.cloudStack[widget] = {
      elem: elem
    };
    if (extraData) $.extend(data.cloudStack[widget], extraData);

    return $elem
      .addClass('cloudStack-elem')
      .addClass(widget)
      .data(data);
  };

  // Create widget-based event
  event.bind = function(widget, events) {
    return function(event) {
      var $target = $(event.target);
      var $widget;
      var data, elem;

      if (!$target.closest('.cloudStack-elem.' + widget).size())
        return false;

      $widget = $('.cloudStack-widget.' + widget);
      data = $target.data('cloudStack')[widget];
      elem = data.elem;

      events[elem]($target, $widget, data);
    };
  };
})(jQuery, cloudStack);

(function($, _, cloudUI) {
  // Add even/odd pattern as CSS class for specified elems
  cloudUI.evenOdd = function($elems) {
    var isEven = true;

    // Cleanup
    $elems.removeClass('even odd');

    _.map($elems, function(elem) {
      var $elem = $(elem);
      var type;

      if (isEven) {
        type = 'even';
        isEven = false;
      } else {
        type = 'odd';
        isEven = true;
      }

      $elem.addClass(type);
    });

    return $elems;
  };

  // Given a list of multiple keys, return object w/ first matching key
  //
  // -- used primarily to support multiple synonymous option naming,
  //    for legacy support
  cloudUI.which = function(obj, keys) {
    var targetKey;
    
    _.map(_.keys(obj), function(key) {
      if (_.indexOf(keys, key) > -1) {
        targetKey = key;

        return false;
      }

      return true;
    });

    return obj[targetKey];
  };
}(jQuery, _, cloudUI));

(function($, cloudStack) {
  cloudStack.uiCustom.uploadVolume = function(args) {
    return function(args) {
      cloudStack.dialog.notice({ message: 'upload volume' });
    }
  };
}(jQuery, cloudStack));

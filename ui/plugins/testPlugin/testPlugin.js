(function($, cloudStack) {
  cloudStack.sections.testPlugin = {
    title: 'Test Plugin',
    id: 'testPlugin',
    show: function() {
      return $('<div>').html('<h2>This is my test plugin!</h2>');
    }   
  };
}(jQuery, cloudStack));

(function($, cloudStack) {
  //
  // To add a new section to the navigation, use this format
  //
  // cloudStack.sections.[pluginID] = {
  //   title: 'Plugin Title',
  //   id: 'pluginID',
  //   show: function() {
  //     [your JS code here that does stuff];
  //   }
  // }
  //
  cloudStack.sections.testPlugin = {
    title: 'Test Plugin', // Title that appears on the sidebar
    id: 'testPlugin', // Usually the same name as the plugin folder
    show: function() {
      // Returning something will render it in the browser panel
      //
      // -- it can be a jQuery object, text, or a DOM object
      return '<h2>This is my test plugin!</h2>';
    }   
  };
}(jQuery, cloudStack));

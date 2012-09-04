cloudStack.sections.clickHere = {
  title: 'Click Here!',
  id: 'clickHere',
  show: function() {
    window.open('http://www.google.com/', '_blank');

    return $('<div>').html('Loading google...');
  }
};

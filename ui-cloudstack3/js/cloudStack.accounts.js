(function(cloudStack) {
  cloudStack.sections.accounts = {
    title: 'Accounts',
    id: 'accounts',
    listView: {
      fields: {
        name: { label: 'Name', editable: true },
        domain: { label: 'Domain' },
        state: { label: 'State' }
      },
      filters: {
        mine: { label: 'My Accounts' },
        all: { label: 'All Accounts' }
      },
      dataProvider: testData.dataProvider.listView('accounts')
    }
  };  
})(cloudStack);

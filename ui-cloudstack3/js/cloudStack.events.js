(function(cloudStack) {
  cloudStack.sections.events = {
    title: 'Events',
    sections: {
      events: {
        title: 'Events',
        listView: {
          label: 'Events',
          fields: {
            type: { label: 'Type' },
            description: { label: 'Description' },
            username: { label: 'Initiated By' },
            created: { label: 'Date' }
          },
          dataProvider: testData.dataProvider.listView('events')
        }
      },
      alerts: {
        title: 'Alerts',
        listView: {
          label: 'Alerts',
          fields: {
            description: { label: 'Description' },
            sent: { label: 'Date' }
          },
          dataProvider: testData.dataProvider.listView('alerts')
        }
      }
    }
  };
})(cloudStack);

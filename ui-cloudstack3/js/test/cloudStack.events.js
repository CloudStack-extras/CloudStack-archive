(function(cloudStack) {
  cloudStack.sections.events = {
    title: 'Events',
    id: 'events',
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
            type: { label: 'Type' },
            description: { label: 'Description' },
            sent: { label: 'Date' }
          },
          dataProvider: testData.dataProvider.listView('alerts'),
          detailView: {
            name: 'Alert details',
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    type: { label: 'Type' },
                    description: { label: 'Description' },
                    created: { label: 'Sent' }
                  }
                ],
                dataProvider: testData.dataProvider.detailView('alerts')
              },
            }
          }
        }
      }
    }
  };
})(cloudStack);

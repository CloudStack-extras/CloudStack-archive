(function(cloudStack, testData) {
  cloudStack.sections.templates = {
    title: 'Templates',
    id: 'templates',
    sections: {
      templates: {
        title: 'Templates',
        listView: {
          label: 'Templates',
          fields: {
            name: { label: 'Name', editable: true },
            id: { label: 'ID' },
            zonename: { label: 'Zone' },
            hypervisor: { label: 'Hypervisor' }
          },
          actions: {
            edit: {
              label: 'Edit template name',
              action: function(args) {
                args.response.success(args.data[0]);
              }
            }
          },
          dataProvider: testData.dataProvider.listView('templates')
        }
      },
      isos: {
        title: 'ISOs',
        listView: {
          label: 'ISOs',
          fields: {
            displaytext: { label: 'Name' },
            id: { label: 'ID' },
            size: { label: 'Size' },
            zonename: { label: 'Zone' }
          },
          dataProvider: testData.dataProvider.listView('isos')
        }
      }
    }
  };  
})(cloudStack, testData);

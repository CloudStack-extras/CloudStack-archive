(function(cloudStack) {
  cloudStack.sections['global-settings'] = {
    title: 'Global Settings',
    id: 'global-settings',
    listView: {
      label: 'Global Settings',
      actions: {
        edit: {
          label: 'Change value',
          action: function(args) {
            args.response.success();
          }
        }
      },
      fields: {
        name: { label: 'Name', id: true },
        description: { label: 'Description' },
        value: { label: 'Value', editable: true }
      },
      dataProvider: function(args) {
        $.ajax({
          url: createURL("listConfigurations&page="+args.page+"&pagesize="+pageSize),
          dataType: "json",
          async: true,
          success: function(json) {
            var items = json.listconfigurationsresponse.configuration;
            args.response.success({ data: items });
          }
        });
      }
    }
  };
})(cloudStack);

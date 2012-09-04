(function($, cloudStack) {
  // Instance details -- shapshots tab
  var instanceSnapshotsTab = {
    title: 'Snapshots',
    multiple: true,
    fields: {
      volumename: { label: 'Volume name' },
      account: { label: 'Account' },
      state: { label: 'State' }
    },
    dataProvider: function(args) {
      $.ajax({
        url: createURL('listSnapshots'),
        success: function(json) {
          args.response.success({ data: json.listsnapshotsresponse.snapshot });
        }
      });
    }
  };

  var snapshotSection = {
    title: 'Snapshots',
    id: 'snapshots',
    listView: {
      section: 'snapshots',
      fields: {
        volumename: { label: 'Volume name' },
        account: { label: 'Account' },
        state: { label: 'State' }        
      },
      dataProvider: function(args) {
        $.ajax({
          url: createURL('listSnapshots'),
          success: function(json) {
            args.response.success({ data: json.listsnapshotsresponse.snapshot });
          }
        });        
      }
    }
  };

  var sectionPreFilter = cloudStack.sectionPreFilter;
  
  $.extend(cloudStack, {
    sectionPreFilter: function(args) {
      var preFilter = sectionPreFilter(args);

      preFilter.push('snapshots');

      return preFilter;
    }
  });

  $.extend(cloudStack.sections, {
    snapshots: snapshotSection
  });

  $.extend(cloudStack.sections.instances.listView.detailView.tabs, {
    snapshots: instanceSnapshotsTab
  });
}(jQuery, cloudStack));
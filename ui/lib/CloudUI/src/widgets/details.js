(function($, _, cloudUI) {
  // Make jQuery UI-compatible ID for tab link and div container
  var tabRef = function(tabID) {
    return 'details-tab-'+  tabID;
  };

  // Re-initialize jQuery UI tabs for specified detail container
  var initTabs = function(args) {
    var $container = args.$container;
    var detailArgs = args.detailArgs;
    
    $container.tabs('destroy');
    $container.tabs({
      show: function(event, ui) {
        var tabID = $(ui.tab).closest('li').attr('details-tab');
        
        cloudUI.event.call('loadTab', $container, {
          tabID: tabID,
          tab: detailArgs.tabs[tabID],
          $tabContent: $(ui.panel)
        });
      }
    });
  };

  // Update tab data for specific field
  var updateFieldData = function(args) {
    var fieldID = args.fieldID;
    var value = args.value;
    var $tab = args.$tab;
    var $field;

    $field = $tab.find('tr').filter(function() {
      return $(this).hasClass(fieldID);
    });
    $field.find('td.value').html(
      '<span>' + value + '</span>'
    );
  };

  // Update all tab data fields
  var updateTabData = function(args) {
    var $tab = args.$tab;
    var data = args.data;

    _.each(data, function(value, fieldID) {
      updateFieldData({
        $tab: $tab,
        fieldID: fieldID,
        value: value
      });
    });
  };
  
  var elems = {
    fieldGroup: function(args) {
      var $fieldGroup = $('<div>').addClass('detail-group');
      var $table = $('<table>');
      var $tbody = $('<tbody>');
      var fieldGroup = args.fieldGroup;
      var fieldDisplay = args.fieldDisplay ?
            args.fieldDisplay() : _.keys(args.fieldGroup);

      _.each(fieldDisplay, function(fieldID) {
        var field = fieldGroup[fieldID];
        var $field = $('<tr>').addClass(fieldID);
        var $name = $('<td>').addClass('name');
        var $value = $('<td>').addClass('value');
        var $valueText = $('<span>');

        $name.html(field.label);
        $field.append($name, $value);
        $field.appendTo($tbody);
      });

      $table.append($tbody);
      $fieldGroup.append($table);
      cloudUI.evenOdd($fieldGroup.find('tr'));

      return $fieldGroup;
    },
    
    // Tab nav item
    tab: function(args) {
      var $li = $('<li>');
      var $link = $('<a>');

      $li.attr('details-tab', args.tabID);
      $link.attr('href', '#' + tabRef(args.tabID));
      $link.html(args.tab.title ? args.tab.title : args.tabID);
      $li.append($link);

      cloudUI.event.register({
        id: 'details-tab',
        $elem: $li,
        data: args
      });

      return $li;
    },

    // Tab content group
    tabContent: function(args) {
      var $tabContent = $('<div>').addClass('detail-group');
      var $mainGroups = $('<div>').addClass('main-groups');
      var tab = args.tab;
      var fields = args.tab.fields;

      $tabContent.attr('id', tabRef(args.tabID));
      $tabContent.append($mainGroups);

      _.each(fields, function(fieldGroup) {
        var $fieldGroup = elems.fieldGroup({
          tab: tab,
          fieldGroup: fieldGroup
        });

        $fieldGroup.appendTo($mainGroups);
      });

      cloudUI.event.register({
        id: 'details-tab-content',
        $elem: $tabContent,
        data: args
      });

      return $tabContent;
    }
  };
      
  cloudUI.widgets.details = cloudUI.widget({
    methods: {
      _init: function(details, detailArgs) {
        var $details = detailArgs.$container;
        var $toolbar = $('<div>').addClass('toolbar');
        var $tabs = $('<ul>');
        var tabs = detailArgs.tabs ? detailArgs.tabs : {};
        var tabDisplay = detailArgs.tabDisplay ?
              detailArgs.tabDisplay() :
              _.keys(tabs);

        $details.addClass('detail-view details');
        $details.append($toolbar, $tabs);

        _.each(tabDisplay, function(tabID) {
          details.addTab({
            id: tabID,
            tab: tabs[tabID]
          });
        });

        cloudUI.event.register({
          id: 'details-container',
          $elem: $details,
          data: {
            details: details,
            detailArgs: detailArgs
          }
        });

        // First tab event
        if (tabDisplay.length) {
          cloudUI.event.call('loadTab', $details, {
            tabID: tabDisplay[0],
            tab: detailArgs.tabs[tabDisplay[0]],
            $tabContent: $details.find('div.detail-group:first')
          });
        }
      },

      addTab: function(details, detailArgs, args) {
        var tabID = args.id;
        var tab = args.tab;
        var $container = detailArgs.$container;
        var $ul = $container.find('ul');
        var $tab = elems.tab({
          tabID: tabID,
          tab: tab,
          details: details,
          detailArgs: detailArgs
        });
        var $tabContent = elems.tabContent({
          tabID: tabID,
          tab: tab,
          details: details,
          detailArgs: detailArgs
        });

        $tab.appendTo($ul);
        $tabContent.appendTo($container);

        // Initialize UI tabs
        initTabs({
          $container: $container,
          detailArgs: detailArgs
        });
        
        $ul.find('li').removeClass('first last');
        $ul.find('li:first').addClass('first');
        $ul.find('li:last').addClass('last');
      },

      // Populate field data for tab
      loadData: function(details, detailArgs, args) {
        var tabID = args.tabID;
        var data = args.data;
        var $tab = detailArgs.$container.find('#' + tabRef(tabID));
        
        updateTabData({
          $tab: $tab,
          data: data
        });
      }
    }
  });

  cloudUI.event.handler({
    'details-container': {
      loadTab: function(args) {
        // Populate data from provider
        var tab = args.tab;
        var dataProvider = args.tab.dataProvider;
        var details = args.details;
        var detailArgs = args.detailArgs;
        var tabID = args.tabID;

        if (!dataProvider) return false;
        
        return cloudUI.dataProvider({
          context: detailArgs.context,
          dataProvider: dataProvider,
          success: function(args) {
            details.loadData({
              tabID: tabID,
              data: args.data
            });
          },
          error: function(args) {}
        });
      }
    }
  });
}(jQuery, _, cloudUI));

(function($, _, cloudUI) {
  var resizeHeaders = function($table) {
    var $thead = $table.closest('div.data-table').find('thead');
    var $tbody = $table.find('tbody');
    var $ths = $thead.find('th');
    var $tds = $tbody.find('tr:first td');

    if ($ths.size() > $tds.size()) {
      $ths.width(
        $table.width() / $ths.size()
      );
      return false;
    }

    $ths.each(function() {
      var $th = $(this);
      var $td = $tds.filter(function() {
        return $(this).index() == $th.index();
      });

      $th.width($td.width());

      return true;
    });

    return $ths;
  };

  var elems = {
    // Main table wrapper
    table: function(args) {
      var list = args.list;
      var listArgs = args.listArgs;
      var fields = args.fields;
      var fieldDisplay = args.fieldDisplay;
      var $wrapper = $('<div>').addClass('data-table');
      var $fixedHeader = elems.fixedHeader({
        list: list,
        listArgs: listArgs,
        fields: fields,
        fieldDisplay: fieldDisplay
      });
      var $bodyTable = elems.bodyTable();

      $wrapper.append($fixedHeader, $bodyTable);

      return $wrapper;
    },

    // Single data row
    tableRow: function(args) {
      var list = args.list;
      var listArgs = args.listArgs;
      var fields = args.fields;
      var dataItem = args.dataItem;
      var $tr = $('<tr>');
      var fieldDisplay = args.fieldDisplay;

      // Store data in row
      cloudUI.data($tr).jsonObj = dataItem;

      _.map(fieldDisplay, function(fieldID) {
        var field = fields[fieldID];
        var $td = $('<td>');
        var $span = $('<span>');

        cloudUI.event.register({
          id: 'list-table-item',
          $elem: $td,
          data: {
            $td: $td,
            fieldID: fieldID,
            field: field,
            list: list,
            listArgs: listArgs,
            dataItem: dataItem
          }
        });

        $td.addClass(fieldID);
        $span.html(dataItem[fieldID]).appendTo($td);
        $td.appendTo($tr);
      });

      cloudUI.event.register({
        id: 'list-table-row',
        $elem: $tr,
        data: {
          $tr: $tr,
          fields: fields,
          list: list,
          listArgs: listArgs
        }
      });

      $tr.find('td:first').addClass('first');
      $tr.find('td:last').addClass('last');

      return $tr;
    },

    // 'No contents' row
    emptyRow: function() {
      var $emptyRow = $('<tr>').addClass('nocontents');
      var $emptyCell = $('<td>').html('<span>No contents</span>');

      return $emptyRow.append($emptyCell);
    },

    // Table body
    bodyTable: function() {
      var $table = $('<table>').addClass('body');
      var $tbody = $('<tbody>');
      
      return $table.append($tbody);
    },

    // Header area
    fixedHeader: function(args) {
      var list = args.list;
      var listArgs = args.listArgs;
      var fields = args.fields;
      var $fixedHeader = $('<div>').addClass('fixed-header');
      var $table = $('<table>').attr('nowrap', 'nowrap');
      var $thead = $('<thead>');
      var $tr = $('<tr>');
      var fieldDisplay = args.fieldDisplay;

      // Add fields
      if (fields) {
        _.map(fieldDisplay, function(fieldID) {
          var field = fields[fieldID];
          var $th = $('<th>');

          $th.addClass(fieldID);
          $th.html(field.label);
          $th.appendTo($tr);
        });
      } else {
        $tr.append('<th>&nbsp;</th>');
      }

      cloudUI.event.register({
        id: 'list-table-header-row',
        $elem: $tr,
        data: {
          $header: $fixedHeader,
          $tr: $tr,
          fields: fields,
          list: list,
          listArgs: listArgs
        }
      });

      return $fixedHeader.append(
        $table.append(
          $thead.append($tr)
        )
      );
    }
  };

  // Table list-related actions
  var table = {
    // Add multiple rows to list
    // -- defaults to append, unless prepend: true is set
    addRows: function(args) {
      var list = args.list;
      var listArgs = args.listArgs;
      var fields = args.fields;
      var data = args.data;
      var fieldDisplay = args.fieldDisplay;
      var $tbody = args.$tbody;
      var prepend = args.prepend;

      // Cleanup
      $tbody.find('tr.nocontents').remove();

      // Make rows
      _.map(data, function(dataItem) {
        var $tr = elems.tableRow({
          list: list,
          listArgs: listArgs,
          fields: fields,
          dataItem: dataItem,
          fieldDisplay: fieldDisplay
        });

        if (prepend) {
          $tr.prependTo($tbody);
        } else {
          $tr.appendTo($tbody);
        }
      });

      cloudUI.evenOdd($tbody.find('tr'));
    }
  };

  // Get field order from args
  var fieldDisplay = function(listArgs) {
    var fields = listArgs.fields;
    var fieldDisplay = listArgs.fieldDisplay;

    return fieldDisplay ? fieldDisplay :
      (fields ? _.keys(fields) : []); // if no order specified
  };

  cloudUI.widgets.list = cloudUI.widget({
    methods: {
      _init: function(list, listArgs) {
        var $list = listArgs.$list;
        var id = listArgs.id;
        var fields = listArgs.fields;
        var dataProvider = listArgs.dataProvider;

        // Draw basic list layout
        $list.addClass('view list-view');
        $list.addClass(id);
        $list.append(elems.table({
          list: list,
          listArgs: listArgs,
          fields: fields,
          fieldDisplay: fieldDisplay(listArgs)
        }));

        // Load data
        if (dataProvider) {
          cloudUI.dataProvider({
            dataProvider: dataProvider,
            success: function(args) {
              var data = args.data;

              if (data.length) {
                table.addRows({
                  list: list,
                  listArgs: listArgs,
                  fields: fields,
                  data: data,
                  fieldDisplay: fieldDisplay(listArgs),
                  $tbody: $list.find('tbody')
                });

                resizeHeaders($list.find('table'));
              } else {
                $list.find('tbody').append(elems.emptyRow());
              }
            },

            error: function(args) {}
          });
        } else {
          $list.find('tbody').append(elems.emptyRow());
        }
      },

      // Append rows at bottom of table
      appendRows: function(list, listArgs, args) {
        var data = args.data;
        var fields = listArgs.fields;
        var $list = listArgs.$list;

        table.addRows({
          list: list,
          listArgs: listArgs,
          fields: fields,
          data: data,
          fieldDisplay: fieldDisplay(listArgs),
          $tbody: $list.find('tbody')
        });
      },

      // Prepend rows at the top of table
      prependRows: function(list, listArgs, args) {
        var data = args.data;
        var fields = listArgs.fields;
        var $list = listArgs.$list;

        table.addRows({
          list: list,
          listArgs: listArgs,
          prepend: true,
          fields: fields,
          data: data,
          fieldDisplay: fieldDisplay(listArgs),
          $tbody: $list.find('tbody')
        });
      },

      // Return JSON object stored in specified list row
      getItemData: function(list, listArgs, $tr) {
        return cloudUI.data($tr).jsonObj;
      }
    }
  });
}(jQuery, _, cloudUI));

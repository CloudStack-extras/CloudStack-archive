(function($, cloudStack) {
  /**
   * Port forwarding custom tab
   */
  cloudStack.portForwarding = function(args) {
    var dataProvider = args.dataProvider;
    
    return function(args) {
      var $portForwarding = $('<div>').addClass('port-forwarding');
      var $inputTable = $('<table>').addClass('multi-edit').appendTo($portForwarding);
      var $dataTable = $('<table>').addClass('data').appendTo($portForwarding);
      var fields = [
        ['start-port', 'Start Port'],
        ['end-port', 'End Port'],
        ['protocol', 'Protocol'],
        ['add-vm', 'Add VM'], 
        ['actions', 'Actions']
      ];

      var $thead = $('<tr>').appendTo(
        $('<thead>').appendTo($inputTable) 
      );
      var $inputForm = $('<tr>').appendTo(
        $('<tbody>').appendTo($inputTable)
      );
      var $dataBody = $('<tbody>').appendTo($dataTable);

      // Setup input table headers
      $(fields).each(function() {
        $thead.append(
          $('<th>').html(this[1].toString())
        );

        var $td = $('<td>').addClass(this[0]).appendTo($inputForm);

        if ($.inArray(this[0], ['start-port', 'end-port']) != -1) {
          // Port fields
          $td.append(
            $('<input>').attr({
              name: this[0],
              type: 'text'
            })
          );
        } else if (this[0] == 'protocol') {
          // Protocol select
          $td.append(
            $('<select>').attr({
              name: this[0]
            })
              .append($('<option>').val('tcp').html('TCP'))
              .append($('<option>').val('udp').html('UDP'))
          );
        } else if (this[0] == 'add-vm') {
          // Add VM button
          $td.append(
            $('<div>').addClass('button add-vm').html('Add Instance')
          );
        }
      });

      // Setup input table body
      dataProvider({
        response: {
          success: function(args) {
            $(args.data).each(function() {
              var dataItem = this;
              var $tr = $('<tr>').appendTo($dataTable);

              $(fields).each(function() {
                var $td = $('<td>').addClass(this[0]);

                if (this[0] == 'actions') {
                  $td.append(
                    $('<div>').addClass('action destroy')
                  );
                } else {
                  debugger;
                  $td.append(
                    $('<span>').html(dataItem[this[0]])
                  );                  
                }
              });
            });
          }
        }
      });

      return $portForwarding;
    };
  };
})(jQuery, cloudStack);

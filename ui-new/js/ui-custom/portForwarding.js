(function($, cloudStack) {
  /**
   * Port forwarding custom tab
   */
  var _pf = cloudStack.uiCustom.portForwarding = {
    /**
     * Append item to port forwarded list
     */
    addItem: function(vmData, $portForwarding, options) {
      if (!options) options = {};

      var $tr = $('<tr>')
        .data('port-forwarding-vm-data', vmData)
        .append($('<td>').addClass('start-port').html(
          // Start port
          $portForwarding.find('input[name=start-port]').val()
        ))
        .append($('<td>').addClass('end-port').html(
          // End port
          $portForwarding.find('input[name=end-port]').val()
        ))
        .append($('<td>').addClass('protocol').html(
          // Protocol
          $portForwarding.find('select[name=protocol] option:selected').html()
        ))
        .append($('<td>').addClass('add-vm').html(
          options.multipleVMs ?
            vmData.length + ' VMs' :
            vmData[0].name
        ).click(function() {
          var $browser = $(this).closest('.detail-view').data('view-args').$browser;

          if (options.multipleVMs) {
            _pf.loadBalancing.details(vmData, $browser);
          } else {
            _pf.details(vmData[0], $browser);
          }
        }))
        .append($('<td>').addClass('actions').append(
          // Actions
          $('<div>').addClass('action destroy').append(
            $('<span>').addClass('icon')
          )
        ));

      // Add VM list expandable
      if (options.multipleVMs) {
        var $vmList = $('<div>').addClass('vm-box')
              .appendTo($tr).hide()
              .append(
                $('<div>').addClass('content')
              );
        var $vmTable = $('<tbody>').appendTo(
          $('<table>').appendTo($vmList.find('.content'))
            .append(
              $('<thead>')
                .append($('<th>').html('Name'))
                .append($('<th>').html('Status'))
                .append($('<th>').html('Actions'))
            )
        );

        $(vmData).each(function() {
          var $tr = $('<tr>').appendTo($vmTable);

          $tr
            .append($('<td>').append($('<span>').html(this.name)))
            .append($('<td>').append($('<span>').html(this.state)))
            .append($('<td>'));
        });

        $tr.find('td:first').prepend(
          $('<div>').addClass('expand')
            .click(function() {
              $vmList.slideToggle();
              $vmList.find('table').dataTable('refresh');
            })
        );

        $vmList.find('table').dataTable();
      }

      return $tr;
    },
    details: function(vmData, $browser) {
      var detailViewArgs, $detailView;

      detailViewArgs = $.extend(true, {}, cloudStack.sections.instances.listView.detailView);
      detailViewArgs.actions = null;
      detailViewArgs.$browser = $browser;
      detailViewArgs.id = vmData.id;
      detailViewArgs.jsonObj = vmData;

      $browser.cloudBrowser('addPanel', {
        title: 'Port forwarded VM',
        complete: function($newPanel) {
          $newPanel.detailView(detailViewArgs);
        }
      });
    },
    loadBalancing: {
      /**
       * Show listing of load balanced VMs
       */
      details: function(vmData, $browser) {
        var listViewArgs, $listView;

        // Setup list view
        listViewArgs = $.extend(true, {}, cloudStack.sections.instances);
        listViewArgs.listView.actions = null;
        listViewArgs.listView.filters = null;
        listViewArgs.$browser = $browser;
        listViewArgs.listView.detailView.actions = null;
        listViewArgs.listView.dataProvider = function(args) {
          setTimeout(function() {
            args.response.success({
              data: vmData
            });
          }, 50);
        };
        $listView = $('<div>').listView(listViewArgs);

        // Show list view of selected VMs
        $browser.cloudBrowser('addPanel', {
          title: 'Load Balanced VMs',
          data: '',
          noSelectPanel: true,
          maximizeIfSelected: true,
          complete: function($newPanel) {
            return $newPanel.listView(listViewArgs);
          }
        });
      }
    }
  };

  cloudStack.portForwarding = function(args) {
    var dataProvider = args.dataProvider;
    var multipleVMs = args.type == 'multiple';

    return function(args) {
      var $portForwardingForm = $('<form>');
      var $portForwarding = $('<div>').addClass('port-forwarding').appendTo(
        $portForwardingForm
      );
      var $inputTable = $('<table>').addClass('multi-edit').appendTo($portForwarding);
      var $dataTable = $('<table>').addClass('data').appendTo($portForwarding);
      var $addVM;

      $dataTable.dataTable();

      var fields = {
        'start-port': 'Start Port',
        'end-port': 'End Port',
        'protocol': 'Protocol',
        'add-vm': multipleVMs ? 'Add VMs' : 'Add VM',
        'actions': 'Actions'
      };

      var $thead = $('<tr>').appendTo(
        $('<thead>').appendTo($inputTable)
      );
      var $inputForm = $('<tr>').appendTo(
        $('<tbody>').appendTo($inputTable)
      );
      var $dataBody = $('<tbody>').appendTo($dataTable);

      // Setup input table headers
      $.each(fields, function(field, label) {
        $thead.append(
          $('<th>').html(label.toString())
        );

        var $td = $('<td>').addClass(field).appendTo($inputForm);

        if ($.inArray(field, ['start-port', 'end-port']) != -1) {
          // Port fields
          $td.append(
            $('<input>').attr({
              name: field,
              type: 'text'
            }).addClass('required')
          );
        } else if (field == 'protocol') {
          // Protocol select
          $td.append(
            $('<select>').attr({
              name: field
            })
              .append($('<option>').val('tcp').html('TCP'))
              .append($('<option>').val('udp').html('UDP'))
          );
        } else if (field == 'add-vm') {
          // Add VM button
          $addVM = $('<div>').addClass('button add-vm').html(
            multipleVMs ? 'Add VMs' : 'Add Instance'
          )
            .appendTo($td);
        }
      });


      // Setup input table body
      dataProvider({
        response: {
          success: function(args) {
            $(args.data).each(function() {
              var dataItem = this;
              var $tr = $('<tr>').appendTo($dataTable);

              $.each(fields, function(field) {
                var $td = $('<td>').addClass(field).appendTo($tr);

                if (field == 'actions') {
                  $td.append(
                    $('<div>').addClass('action destroy').append($('<span>').addClass('icon'))
                  );
                } else {
                  $td.append(
                    $('<span>').html(dataItem[field])
                  );
                }
              });

              $dataTable.dataTable('refresh');
            });
          }
        }
      });

      var vmList = function() {
        // Create a listing of instances, based on limited information
        // from main instances list view
        var $listView;
        var instances = $.extend(true, {}, cloudStack.sections.instances, {
          listView: {
            uiCustom: true
          }
        });

        instances.listView.actions = {
          select: {
            label: 'Select instance',
            type: multipleVMs ? 'checkbox' : 'radio',
            action: {
              uiCustom: function(args) {
                var $item = args.$item;
                var $input = $item.find('td.actions input:visible');

                if ($input.attr('type') == 'checkbox') {
                  if ($input.is(':checked'))
                    $item.addClass('port-forwarding-selected');
                  else
                    $item.removeClass('port-forwarding-selected');
                } else {
                  $item.siblings().removeClass('port-forwarding-selected');
                  $item.addClass('port-forwarding-selected');
                }
              }
            }
          }
        };

        $listView = $('<div>').listView(instances);

        // Change action label
        $listView.find('th.actions').html('Select');

        return $listView;
      };

      $addVM.bind('click', function() {
        if (!$portForwardingForm.valid()) return true;

        var $vmList = vmList($portForwarding).dialog({
          dialogClass: 'add-vm-list panel',
          width: 825,
          title: 'Add VM',
          buttons: [
            {
              text: 'Done',
              'class': 'ok',
              click: function() {
                $vmList.fadeOut(function() {
                  _pf.addItem(
                    $.map(
                      $vmList.find('tr.port-forwarding-selected'),

                      // Attach VM data to row
                      function(elem) {
                        return $(elem).data('json-obj');
                      }
                    ),
                    $portForwarding,
                    {
                      multipleVMs: multipleVMs
                    }
                  ).appendTo($dataTable);

                  $dataTable.dataTable('refresh');
                  $portForwarding.find('input').val('');
                  $vmList.remove();
                });
                $('div.overlay').fadeOut(function() {
                  $('div.overlay').remove();
                });
              }
            },
            {
              text: 'Cancel',
              'class': 'cancel',
              click: function() {
                $vmList.fadeOut(function() {
                  $vmList.remove();
                });
                $('div.overlay').fadeOut(function() {
                  $('div.overlay').remove();
                });
              }
            }
          ]
        }).parent('.ui-dialog').overlay();

        return true;
      });

      $portForwardingForm.validate();
      return $portForwardingForm;
    };
  };
})(jQuery, cloudStack);

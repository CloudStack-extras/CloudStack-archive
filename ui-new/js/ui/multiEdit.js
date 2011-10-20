(function($, cloudStack) {
  var _medit = cloudStack.ui.widgets.multiEdit = {
    /**
     * Append item to list
     */
    addItem: function(data, fields, $multi, options) {
      if (!options) options = {};

      var $tr = $('<tr>').data('multi-edit-data', data);

      $.each(fields, function(fieldName, field) {
        var $td = $('<td></td>').addClass(fieldName).appendTo($tr);

        // Get edit textfield value
        if (field.edit) {
          $td
            .append(
              $('<span></span>').html(
                $multi.find('input').filter(function() {
                  return $(this).attr('name') == fieldName;
                }).val()
              )
            );
        } else if (field.select) {
          $td
            .append(
              $('<span></span>').html(
                $multi.find('select').filter(
                  function() {
                    return $(this).attr('name') == fieldName;
                  }).find('option:selected').html()
              )
            );
        } else if (field.addButton && data.length) {
          $td
            .html(
              options.multipleAdd ?
                data.length + ' VMs' : data[0].name
            )
            .click(function() {
              var $browser = $(this).closest('.detail-view').data('view-args').$browser;

              if (options.multipleAdd) {
                _medit.multiItem.details(data, $browser);
              } else {
                _medit.details(data[0], $browser);
              }
            });
        } else {
          $td.html(
            $('<span></span>').html(field)
          );
        }

        // Align width to main header
        var targetWidth = $multi.find('th.' + fieldName).width() + 5;
        $td.width(targetWidth);

        return true;
      });

      return $tr;
    },
    details: function(data, $browser) {
      var detailViewArgs, $detailView;

      detailViewArgs = $.extend(true, {}, cloudStack.sections.instances.listView.detailView);
      detailViewArgs.actions = null;
      detailViewArgs.$browser = $browser;
      detailViewArgs.id = data.id;
      detailViewArgs.jsonObj = data;

      $browser.cloudBrowser('addPanel', {
        title: data.name,
        complete: function($newPanel) {
          $newPanel.detailView(detailViewArgs);
        }
      });
    },
    multiItem: {
      /**
       * Show listing of load balanced VMs
       */
      details: function(data, $browser) {
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
              data: data
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
      },

      expandable: function(data) {
        var $expandable = $('<div>').addClass('expandable-listing');
        var $tbody = $('<tbody>').appendTo(
          $('<table>').appendTo($expandable)
        );

        $(data).each(function() {
          var field = this;
          var $tr = $('<tr>').appendTo($tbody);

          $tr.append(
            $('<td></td>').appendTo($tr).html(field.name)
          );
        });

        cloudStack.evenOdd($tbody, 'tr', {
          even: function($elem) {
            $elem.addClass('even');
          },
          odd: function($elem) {
            $elem.addClass('odd');
          }
        });

        return $expandable.hide();
      }
    }
  };

  $.fn.multiEdit = function(args) {
    var dataProvider = args.dataProvider;
    var multipleAdd = args.multipleAdd;
    var $multiForm = $('<form>').appendTo(this);
    var $multi = $('<div>').addClass('multi-edit').appendTo(
      $multiForm
    );
    var $inputTable = $('<table>').addClass('multi-edit').appendTo($multi);
    var $dataTable = $('<div>').addClass('data').appendTo($multi);
    var $addVM;

    var $thead = $('<tr>').appendTo(
      $('<thead>').appendTo($inputTable)
    );
    var $inputForm = $('<tr>').appendTo(
      $('<tbody>').appendTo($inputTable)
    );
    var $dataBody = $('<div>').addClass('data-body').appendTo($dataTable);

    // Setup input table headers
    $.each(args.fields, function(fieldName, field) {
      $thead.append(
        $('<th>').addClass(fieldName).html(field.label.toString())
      );

      var $td = $('<td>').addClass(fieldName).appendTo($inputForm);

      if (field.select) {
        var $select = $('<select>')
          .attr({
            name: fieldName
          })
          .appendTo($td);

        field.select({
          $select: $select,
          $form: $multi,
          response: {
            success: function(args) {
              $(args.data).each(function() {
                $('<option>').val(this.name).html(this.description)
                  .appendTo($select);
              });
            }
          }
        });
      } else if (field.edit) {
        $('<input>')
          .attr({
            name: fieldName,
            type: 'text'
          })
          .addClass('required')
          .attr('disabled', field.isDisabled ? 'disabled' : false)
          .appendTo($td);
      } else if (field.addButton) {
        $addVM = $('<div>').addClass('button add-vm').html(
          args.addTitle
        )
          .appendTo($td);
      }
    });

    if (args.actions) {
      $thead.append($('<th>Actions</th>').addClass('multi-actions'));
      $inputForm.append($('<td></td>').addClass('multi-actions'));
    }

    var vmList = function() {
      // Create a listing of instances, based on limited information
      // from main instances list view
      var $listView;
      var instances = $.extend(true, {}, args.listView, {
        listView: {
          uiCustom: true
        }
      });

      instances.listView.actions = {
        select: {
          label: 'Select instance',
          type: multipleAdd ? 'checkbox' : 'radio',
          action: {
            uiCustom: function(args) {
              var $item = args.$item;
              var $input = $item.find('td.actions input:visible');

              if ($input.attr('type') == 'checkbox') {
                if ($input.is(':checked'))
                  $item.addClass('multi-edit-selected');
                else
                  $item.removeClass('multi-edit-selected');
              } else {
                $item.siblings().removeClass('multi-edit-selected');
                $item.addClass('multi-edit-selected');
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
      var $item = $('<tbody>').appendTo(
        $('<table>').appendTo(
          $('<div>').addClass('data-item').appendTo($dataBody)
        )
      );
      var $dataList;
      var addItem = function(data) {
        _medit.addItem(
          data,
          args.fields,
          $multi,
          {
            multipleAdd: multipleAdd
          }
        ).appendTo($item);

        $multi.find('input').val(''); // Clear out fields

        // Append actions
        $actions = $('<td>').addClass('multi-actions').appendTo(
          $item.find('tr')
        );

        $actions.width(
          $multi.find('th.multi-actions').width() + 4
        );

        $.each(args.actions, function(actionID, action) {
          $actions.append(
            $('<div>')
              .addClass('action')
              .addClass(actionID)
              .append($('<span>').addClass('icon'))
              .attr({ title: action.label })
              .click(function() {
                if ($(this).hasClass('destroy')) {
                  $(this).closest('.data-item').remove();
                }
              })
          );
        });

        // Add expandable listing, for multiple-item
        if (multipleAdd) {
          $item.find('td:first').prepend(
            $('<div></div>').addClass('expand')
              .click(function() {
                $item.closest('.data-item')
                  .find('.expandable-listing').slideToggle();
              }
            )
          );

          _medit.multiItem.expandable(
            $item.find('tr').data('multi-edit-data')
          )
            .appendTo($item.closest('.data-item'));
        }
      };

      if (!$multiForm.valid()) return true;
      if (args.noSelect) {
        addItem([]);

        return true;
      }

      $dataList = vmList($multi).dialog({
        dialogClass: 'multi-edit-add-list panel',
        width: 825,
        title: args.addTitle,
        buttons: [
          {
            text: 'Done',
            'class': 'ok',
            click: function() {
              $dataList.fadeOut(function() {
                addItem($.map(
                  $dataList.find('tr.multi-edit-selected'),

                  // Attach VM data to row
                  function(elem) {
                    return $(elem).data('json-obj');
                  }
                ));
                $dataList.remove();
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
              $dataList.fadeOut(function() {
                $dataList.remove();
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

    $multiForm.validate();

    return this;
  };
})(jQuery, cloudStack);

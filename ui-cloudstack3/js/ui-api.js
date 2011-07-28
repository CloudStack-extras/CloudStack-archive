(function($, _cloudStack) {
  var _ui = {
    fromTemplate: function(templateType, templateID) {
      return _cloudStack.ui.$template.find('div').filter(function() {
        var $template = $(this);

        var thisTemplateType = $template.attr('template-type');
        var thisTemplateID = $template.attr('template-id');

        return thisTemplateType === templateType && thisTemplateID === templateID;
      }).clone();
    },

    wizard: {

    },

    /**
     * Set navigation bar to highlight specific section as active
     *
     * @param sectionID Section ID of nav item, specified as 'class' in the HTML
     */
    setActiveSection: function(sectionID) {
      var $navigationItems = $('#navigation').find('ul li');

      $navigationItems.removeClass('active');

      return $navigationItems.filter(function() {
        return $(this).hasClass(sectionID);
      }).addClass('active');
    },

    /**
     * Browser-related functionality
     */
    browser: {
      /**
       * Clear out all existing panels, replacing with new panel
       *
       * @param panelData Panel args to pass; same as cloudBrowser('addPanel' {...} )
       */
      resetTo: function(panelData) {
        _cloudStack.ui.$browser.cloudBrowser('removeAllPanels');
        return _cloudStack.ui.$browser.cloudBrowser('addPanel', panelData);
      }
    },
    notifications: {
      /**
       * Show sample notification (no actual content)
       */
      showSample: function() {
        if ($('div.notification-sample').size()) return _ui.notifications.hideSample();

        var $notification = $('<div></div>').addClass('notification-sample').hide();
        var $notificationImg = $('<img>').attr({
          src: 'images/bg-notifications-sample.png'
        }).appendTo($notification);

        $('#container').children().css({ opacity: 0.5 });

        $notification.appendTo('#container').fadeIn('fast');

        return true;
      },

      /**
       * Hide sample notification area
       */
      hideSample: function() {
        if ($('div.notification-sample').size()) {
          $('div.notification-sample').remove();
          $('#container').children().css({ opacity: 1 });

          return true;
        }

        return false;
      }
    },

    /**
     * Enable table to have drag and drop support
     *
     * @param $table Table to enable drag support for
     */
    setTableDragging: function($table) {
      return $table.find('tbody td').draggable({
        start: function(event, ui) {
          $(event.target).parent().addClass('selected');
        },
        helper: function(event, ui) {
          var $dragContainer = $('<div></div>').html($(this).html());

          return $dragContainer.remove().appendTo('html body');
        }
      });
    },

    /**
     * Functionality related to projects
     */
    projects: {
      /**
       * Bring session back to default (non-project) state
       */
      setDefaultView: function() {
        return $('#header').find('div.button.view-switcher').removeClass('alt');
      },

      /**
       * Bring up project selection screen
       */
      selectView: function() {
        return $('#header').find('div.button.view-switcher').addClass('alt');
      }
    },

    // Instances section functions
    instances: {
      /**
       * Show details pane for specified instance
       *
       * @param instanceID ID of instance to show details for
       */
      showDetails: function(instanceID) {
        var $details = _ui.fromTemplate('instances', 'details');
        var $toolbar = _ui.fromTemplate('toolbar', 'instance-details');

        _cloudStack.ui.$browser.cloudBrowser('removePanelChildren', {
          panel: $('div.panel:first')
        });

        var $panel = _cloudStack.ui.$browser.cloudBrowser('addPanel', {
          title: instanceID,
          data: '',
          withToolbar: $toolbar
        }).append($details);

        $details.tabs();

        // Populate information
        var populateFields = function(props, $table) {
          $.each(props, function(key, value) {
            var $tr = $table.find('[prop-id=' + key + ']');

            if (!value) value = '<span class="none">(None)</span>';

            $tr.find('td.value, th.value').hide().html(value).fadeIn('fast');
          });
        };

        _cloudStack.data.api.instances.details(instanceID, function(instance) {
          $details.find('span.id').html(instance.name);

          $('div[detail-set]').each(function() {
            var $tab = $(this);
            var properties = instance[$tab.attr('detail-set')];

            if ($tab.hasClass('group-multiple')) {
              var $detailGroupTmpl = $tab.find('div.detail-group').remove();
              $(properties).each(function() {
                var $detailGroup = $detailGroupTmpl.clone().appendTo($tab);
                populateFields(this, $detailGroup.find('table'));
              });
            } else {
              populateFields(properties, $tab.find('table'));
            }
          });
        });

        return $details;
      },

      /**
       * Show edit field for instance name
       *
       * @param $instanceRow {jQuery} table row to show edit field
       */
      toggleRowEditField: function($instanceRow) {
        var $td = $instanceRow.find('td.name');
        var $edit = $td.find('div.edit');
        var $editInput = $edit.find('input');
        var $label = $td.find('span');

        // Hide label, show edit field
        var showEditField = function() {
          $edit.css({ opacity: 1 });
          $label.fadeOut('fast', function() {
            $edit.fadeIn();
            $editInput.focus();
          });
        };

        // Hide edit field, validate and save changes
        var showLabel = function(val) {
          if (val) $label.html(val);

          $edit.hide();
          $label.fadeIn();
        };

        if ($label.is(':visible')) {
          showEditField();
        } else if ($editInput.val() != $label.html()) {
          $edit.animate({ opacity: 0.5 });

          _cloudStack.data.api.instances.save([
            {
              name: $editInput.val(),
              ip: $instanceRow.find('td.ip').html(),
              owner: $instanceRow.find('td.owner').html(),
              zone: $instanceRow.find('td.zone').html(),
              status: $instanceRow.find('td.status').html()
            }
          ], function(data) { showLabel(data[0].name); });
        } else {
          showLabel();
        }

        return $instanceRow;
      },

      /**
       * Generate table from instance data
       *
       * @param instanceData {Array} Instances to render in table
       */
      makeInstanceTable: function(instanceData) {
        var $instances = $('<div>').addClass('view instances');
        var $instanceTable = $('<table>').appendTo($instances);
        var $thead = $('<thead></thead>').appendTo($instanceTable);
        var $header = $('<tr></tr>').appendTo($thead);

        // Make header
        $(['Name', 'IP Address', 'Owner', 'Zone', 'Status', 'Actions']).each(function() {
          var $th = $('<th>').html(this.toString()).appendTo($header);
        });

        // Make body
        var $tbody = $('<tbody>').appendTo($instanceTable);
        var fields = ['name', 'ip', 'owner', 'zone', 'status'];

        $(instanceData).each(function() {
          var instance = this;

          // Assign instance ID to row as ref
          var $row = $('<tr>').attr({
            'instance-id': instance.name
          }).appendTo($tbody);

          // Put label and hidden edit field in each cell
          $(fields).each(function() {
            var value = instance[this];
            var $td = $('<td></td>').addClass(this);

            // Non-editable
            var $label = $('<span></span>').attr({
              'ui-id': 'show-instance-details'
            }).html(value);
            $label.appendTo($td);

            // Edit text field
            var $editArea = $('<div></div>').addClass('edit');
            var $editField = $('<input />').addClass('edit').attr({
              type: 'text',
              'ui-id': 'instance-table-edit',
              value: value
            });
            var $actionButton = $('<div></div>').addClass('action');
            var $saveButton = $actionButton.clone().addClass('save').attr({
              'ui-id': 'instance-table-edit',
              'title': 'Save'
            });
            var $cancelButton = $actionButton.clone().addClass('cancel').attr({
              'ui-id': 'instance-table-edit',
              'title': 'Cancel edit'
            });

            $([$editField, $saveButton, $cancelButton]).each(function() {
              this.appendTo($editArea);
            });

            $editArea.hide().appendTo($td);

            $td.appendTo($row);
          });

          // Create actions column
          var $actions = $('<td></td>').addClass('actions');
          var $editIcon = $('<div></div>').addClass('action edit').attr({
            'ui-id': 'edit-instance-name-table'
          });
          var $destroyIcon = $('<div></div>').addClass('action destroy').attr({
            'ui-id': 'remove-instance'
          });

          $([$editIcon, $destroyIcon]).each(function() {
            this.appendTo($actions);
          });
          $actions.appendTo($row);
        });

        $instanceTable.dataTable();

        // Setup 'reduced view' for table
        $instanceTable.find('thead th:not(:first)').addClass('reduced-hide');
        $instanceTable.find('tbody tr').each(function() {
          $(this).find('td:not(:first)').addClass('reduced-hide');
        });

        return $instances;
      },

      /**
       * Show a list of all instances
       */
      show: function() {
        _ui.setActiveSection('instances');

        var $panel = _ui.browser.resetTo({
          title: 'Instances',
          data: '',
          withToolbar: _ui.fromTemplate('toolbar', 'instances')
        });

        _cloudStack.ui.$browser.cloudBrowser('toggleMaximizePanel', {
          panel: $panel,
          noAnimate: true
        });

        // Generate instance table
        _cloudStack.data.api.instances.all(function(data) {
          _ui.instances.makeInstanceTable(data).appendTo($panel);
        });
      }
    },

    /**
     * Instance groups functionality
     */
    groups: {
      /**
       * Highlight current group, showing contents in left pane
       *
       * @param $group Group element to activate
       */
      selectGroup: function($group) {
        var $targetPanel = $('div.panel:first');
        var $instanceContainer = $targetPanel.find('div.view.instances');

        if ($group.hasClass('new')) return false;

        $group.siblings().removeClass('active');
        $group.addClass('active');

        $instanceContainer.animate({ opacity: 0.5 });

        // Retrieve group's instances, put in first panel's table
        _cloudStack.data.api.groups.instances($group.attr('group-id'), function(data) {
          // Remake table
          $instanceContainer.animate({ opacity: 1 });
          $instanceContainer.html('').append(_ui.groups.makeGroupDetailsPage({items: data}));

          // Configure data table behavior
          var $targetTable = $instanceContainer.find('table');
          $targetTable.dataTable();
          _ui.setTableDragging($targetTable);
        });

        return $group;
      },

      /**
       * Remove group; also removes all visible elems relating to group
       *
       * @param groupID ID of group to remove
       */
      removeGroup: function(groupID) {
        if (confirm('Are you sure you want to remove this group?')) {
          var $groupElems = $('[group-id=' + groupID + ']');
          $groupElems.animate({ opacity: 0.5 });
          _cloudStack.data.api.groups.remove(groupID, function() {
            $groupElems.fadeOut(function() {
              $groupElems.remove('slow');
            });
          });
        }
      },

      /**
       * Generate group icon/thumbnail
       *
       * @param groupData Data to be shown in icon element
       */
      makeGroupThumbnail: function(groupData) {
        var $group = $('<li>').attr({
          'ui-id': 'group-icon',
          'group-id': groupData.id
        });
        var $groupName = $('<span>').addClass('name');
        var $vmCount = $('<span>').addClass('vm-count');
        var $vmValue = $('<span>').addClass('value');

        $groupName.html(groupData.name);
        $vmValue.html(groupData.vmCount);
        $vmCount.append($vmValue);
        $vmCount.append(' VMs');

        $group.append($groupName).append($vmCount);

        return $group;
      },

      /**
       * Generate table for group details page, replacing data in instance table
       *
       * @param args
       *
       * Required arguments:
       * - items -> Instance item data to pass to table; currently
       *            only instance name is displayed
       *
       */
      makeGroupDetailsPage: function(args) {
        return _ui.instances.makeInstanceTable(args.items);
      },

      /**
       * Increases/decreases VM count display
       * -- The target group's count is increases, while the active group's is
       *    decreased (i.e., the group whose contents is being dragged away)
       * -- The VM instance which was dragged will be removed from its respective table
       *
       * @param $target Target group element to increment VM count for
       * @param $instance Instance to remove from list
       */
      incrementVMTotal: function($target, $instance) {
        if ($target.hasClass('active')) return false;

        var $vmCount = $target.find('span.vm-count span.value');
        var $table = $instance.closest('table');

        $vmCount.html(parseInt($vmCount.html(), null) + 1);
        $table.dataTable('removeRow', $instance.index());

        var $activeGroup = $target.siblings().filter('.active');
        if ($activeGroup.size()) {
          var $activeGroupVMCount = $activeGroup.find('span.vm-count span.value');
          $activeGroupVMCount.html(parseInt($activeGroupVMCount.html(), null));
        }

        return $target;
      },

      /**
       * Turns a 'temporary' group into a full group;
       * similar functionality as incrementVMTotal(...)
       *
       * @param $target Target group element to increment VM count for
       * @param $instance Instance to remove from list
       */
      convertTempToReal: function($target, $instance) {
        $target.fadeOut('fast', function() {
          var $groupName = $('<span>').addClass('name').html('My New Group');
          var $groupVMCount = $('<span>').addClass('vm-count').html('<span class="value">0</span> VMs');

          $target.append($groupName);
          $target.append($groupVMCount);
          $target.removeClass('new');

          _ui.groups.incrementVMTotal($target, $instance);

          $target.fadeIn('fast');
        });
      },

      /**
       * Setup drag-and-drop for specified group item
       */
      setupDragAndDrop: function($target) {
        var $table = $('table:visible');

        _ui.setTableDragging($table);

        $target.each(function() {
          var $item = $(this);

          $item.droppable({
            hoverClass: 'drop-hover',

            drop: function(event, ui) {
              if ($item.hasClass('new'))
                _ui.groups.convertTempToReal($item, ui.draggable.parent());
              else
                _ui.groups.incrementVMTotal($item, ui.draggable.parent());
            }
          });
        });
      },

      addNewGroup: function() {
        var $groups = $('div.panel:last').find('ul.groups');

        if ($groups.find('li.new').size()) {
          return false;
        }

        var doAddNewGroup = function() {
          var $newGroup = $('<li>').addClass('new').attr({
            'ui-id': 'group-icon'
          });

          $newGroup.hide();
          $groups.prepend($newGroup);
          $newGroup.fadeIn(1000);
          _ui.groups.setupDragAndDrop($newGroup);
        };

        if (!$('div.panel div.view.group-thumbnail:visible').size()) {
          _ui.groups.showGroupPane({ complete: _ui.groups.addNewGroup });

          return true;
        }

        doAddNewGroup();

        return true;
      },

      /**
       * Display list of groups, in thumbnail form
       *
       * @param args {object} Additional arguments:
       *   - complete: callback to run after pane and group thumbnails are drawn
       */
      showGroupPane: function(args) {
        // Don't reload group pane if it is already open
        if ($('div.panel:last').find('div.view.group-thumbnail').size()) {
          _cloudStack.ui.$browser.cloudBrowser('toggleMaximizePanel', {
            panel: $('div.panel:first')
          });

          return false;
        }

        _ui.setActiveSection('instances');

        if (!args) args = {
          complete: _ui.groups.addNewGroup
        };

        var $groupContainer = $('<div>').addClass('view group-thumbnail');
        var $groups = $('<ul>').addClass('groups');

        $groups.appendTo($groupContainer);

        // Split panel and create 'reduced' instances panel
        _cloudStack.ui.$browser.cloudBrowser('removePanelChildren', {
          panel: $('div.panel:first')
        });

        var $panel = _cloudStack.ui.$browser.cloudBrowser('addPanel', {
          title: 'Groups',
          data: '',
          withToolbar: _ui.fromTemplate('toolbar', 'groups')
        });

        $panel.append($groupContainer);

        // Get instance data
        _cloudStack.data.api.groups.all(function(data) {
          $(data).each(function() {
            var $groupThumbnail = _ui.groups.makeGroupThumbnail(this);
            $groupThumbnail.appendTo($groups);
          });

          var groupData = $('<div>').append(
            $('div.panel:first div.view.group-thumbnail').clone().remove()
          ).html();

          $panel.find('div.view.group-thumbnail').fadeIn('slow');

          _ui.groups.setupDragAndDrop($groups.find('li'));

          args.complete();
        });

        return $groups;
      }
    },

    showSamplePage: function($elem) {
      _cloudStack.ui.$browser.cloudBrowser('removeAllPanels');
      $('#navigation').find('ul li').removeClass('active');
      $elem.addClass('active');

      var title = $($elem.find('span')[1]).html();
      var $panel = _cloudStack.ui.$browser.cloudBrowser('addPanel', {
        title: title,
        data: '',
        breadcrumbEvent: function() {
          _ui.showSamplePage($elem);
        }
      });

      // Add sample screen image
      $panel.append(
        $('<img>').attr({
          src: 'images/screens/' + title + '.jpg',
          'ui-id': 'sample-screen',
          'sample-image-id': title
        })
      );

      _cloudStack.ui.$browser.cloudBrowser('toggleMaximizePanel', {
        panel: $panel,
        noAnimate: true
      });
    },

    showSampleDetailsPage: function(sampleDetailsID) {
      _cloudStack.ui.$browser.cloudBrowser('removePanelChildren', {
        panel: $('div.panel:first')
      });

      var $panel = _cloudStack.ui.$browser.cloudBrowser('addPanel', {
        title: sampleDetailsID + ' Details',
        data: '',
        noSelectPanel: true
      });

      $panel.append(
        $('<img>').attr({
          src: 'images/screens/' + sampleDetailsID + '-Details.jpg'
        })
      );

      _ui.panelSample.makeQuarterSize($panel);
    }
  };

  _cloudStack.ui.api = _ui;
})(jQuery, _cloudStack);

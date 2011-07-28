(function($, _cloudStack) {
  var elems = {
    'notifications-sample': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.notifications.showSample();
        }
      }
    },
    switcher: {
      actions: {
        click: function(event, $elem) {
          var $target = $(event.target);

          if ($target.hasClass('select')) {
            _cloudStack.ui.api.projects.selectView();
          } else {
            _cloudStack.ui.api.projects.setDefaultView();
          }

          return false;
        }
      }
    },

    // Panel controls
    'toggle-expand-panel': {
      actions: {
        click: function(event, $elem) {
          var $panel = $elem.closest('div.panel');

          _cloudStack.ui.$browser.cloudBrowser('toggleMaximizePanel', {
            panel: $panel
          });
        }
      }
    },

    'sample-screen': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.showSampleDetailsPage($elem.attr('sample-image-id'));
        }
      }
    },

    // Groups
    'group-icon': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.groups.selectGroup($elem);
        }
      }
    },

    'show-groups': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.groups.showGroupPane();
        }
      }
    },

    // Navigation
    'nav-dashboard': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.showSamplePage($elem);
        }
      }
    },
    'nav-instances': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.instances.show();
        }
      }
    },
    'nav-storage': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.showSamplePage($elem);
        }
      }
    },
    'nav-network': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.showSamplePage($elem);
        }
      }
    },
    'nav-templates': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.showSamplePage($elem);
        }
      }
    },
    'nav-projects': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.showSamplePage($elem);
        }
      }
    },
    'nav-events': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.showSamplePage($elem);
        }
      }
    },

    // Instances
    'add-instance': {
      actions: {
        click: function() {
          alert('Add instance');
        }
      }
    },

    // Instances-specific panel controls
    'show-instance-details': {
      actions: {
        click: function(event, $elem) {
          var instanceID = $elem.closest('tr').attr('instance-id');
          _cloudStack.ui.api.instances.showDetails(instanceID);
        }
      }
    },
    'instance-table-edit': {
      actions: {
        keypress: function(event, $elem) {
          if (!$elem.is('input')) return false;

          var keyCode = event.which || event.keyCode;

          // On enter
          if (keyCode === 13) {
            _cloudStack.ui.api.instances.toggleRowEditField($elem.closest('tr'));
            return false;
          }

          return true;
        },
        click: function(event, $elem) {
          if ($elem.hasClass('cancel')) {
            // Reset label input
            $elem.siblings('input').val(
              $elem.closest('tr').find('span').html()
            );
          }

          if ($elem.hasClass('action'))
            _cloudStack.ui.api.instances.toggleRowEditField($elem.closest('tr'));
        }
      }
    },
    'expand-panel-instance': {
      actions: {
        click: function() {
          $('#navigation li.instances').click();
        }
      }
    },
    'edit-instance-name-table': {
      actions: {
        click: function(event, $elem) {
          _cloudStack.ui.api.instances.toggleRowEditField($elem.closest('tr'));
        }
      }
    },
    'remove-instance': {
      actions: {
        click: function(event, $elem) {
          if (!confirm('Are you sure you want to remove this instance?'))
            return false;

          var $table = $elem.closest('table');
          var $row = $elem.closest('tr');

          $row.toggleClass('to-remove', 'slow');
          setTimeout(function() {
            $table.dataTable('removeRow', $row.index());
          }, 1000);
        }
      }
    }
  };

  _cloudStack.ui.elems = elems;
})(jQuery, _cloudStack);

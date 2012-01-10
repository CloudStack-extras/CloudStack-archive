(function($, cloudStack) {
  /**
   * Zone wizard
   */
  cloudStack.zoneWizard = function(args) {
    /**
     * Handles validation for custom UI components
     */
    var customValidation = {
      networkRanges: function($form) {
        if ($form.closest('.multi-edit').find('.data-item').size()) {
          return true;
        }

        cloudStack.dialog.notice({
          message: 'Please add at lease one traffic range.'
        });
        return false;
      },

      trafficTypes: function($form) {
        var requiredTrafficTypes = [
          'management',
          'guest'
        ];
        var $requiredTrafficTypes = $form.find('li.traffic-type-draggable').filter(function() {
          return $.inArray($(this).attr('traffic-type-id'), requiredTrafficTypes) > -1
        });

        if ($requiredTrafficTypes.size() == requiredTrafficTypes.length) {
          return true;
        }
        
        cloudStack.dialog.notice({
          message: 'Please assign all required traffic types to a network.'
        });
        return false;
      }
    };

    /**
     * Determine if UI components in step should be custom-validated
     * (i.e., not a standard form)
     */
    var checkCustomValidation = function($step) {
      var $multiEditForm = $step.find('.multi-edit form');
      var $physicalNetworks = $step.find('.select-container.multi');
      var isCustomValidated;
      
      if ($multiEditForm.size()) {
        isCustomValidated = customValidation.networkRanges($multiEditForm);
      } else if ($physicalNetworks.size()) {
        isCustomValidated = customValidation.trafficTypes($physicalNetworks);
      } else {
        isCustomValidated = true;
      }

      return isCustomValidated;
    };

    /**
     * Physical network step: Renumber network form items
     */
    var renumberPhysicalNetworkForm = function($container) {
      var $items = $container.find('.select-container.multi');

      $items.each(function() {
        var $item = $(this);
        var $networkName = $item.find('.field.name input[type=text]');
        var $networkTypes = $item.find('.field.network-types input');
        var index = $item.index();

        $networkName.attr('name', 'physicalNetworks[' + index + ']' + '.name');
        $networkTypes.val(index);
      });
    };

    /**
     * Physical network step: Generate new network element
     */
    var addPhysicalNetwork = function($wizard) {
      var $container = $wizard.find('.setup-physical-network .content.input-area form');
      var $physicalNetworkItem = $('<div>').addClass('select-container multi');
      var $deleteButton = $('<div>').addClass('button remove physical-network')
        .attr({ title: 'Remove this physical network' })
        .append('<span>').addClass('icon').html('&nbsp;');
      var $nameField = $('<div>').addClass('field name').append(
        $('<div>').addClass('name').append(
          $('<span>').html('Network Name')
        ),
        $('<div>').addClass('value').append(
          $('<input>').attr({ type: 'text' }).addClass('required')
        )
      );
      var $dropContainer = $('<div>').addClass('drop-container').append(
        $('<span>').addClass('empty-message').html(
          'Drag and drop traffic types you would like to add here.'
        ),
        $('<ul>').hide()
      ).droppable({
        over: function(event, ui) {
          var $ul = $(this).find('ul');

          $ul.addClass('active');

          if (!$ul.find('li').size()) {
            $ul.fadeIn();
          }
        },

        out: function(event, ui) {
          var $ul = $(this).find('ul');

          $ul.removeClass('active');

          if (!$ul.find('li').size()) {
            $ul.fadeOut();
          }
        },

        drop: function(event, ui) {
          var $ul = $(this).find('ul');

          $ul.removeClass('active');
          ui.draggable.appendTo($ul);

          $ul.closest('.select-container.multi').siblings().each(function() {
            var $ul = $(this).find('.drop-container ul');

            if (!$ul.find('li').size()) {
              $ul.fadeOut();
            }
          });
        }
      });

      // Initialize new default network form elem
      $physicalNetworkItem.append(
        $deleteButton,
        $nameField,
        $dropContainer
      );
      $physicalNetworkItem.hide().appendTo($container).fadeIn('fast');
      renumberPhysicalNetworkForm($container);

      // Remove network action
      $physicalNetworkItem.find('.button.remove.physical-network').click(function() {
        removePhysicalNetwork($physicalNetworkItem);
      });
    };

    /**
     * Physical network step: Remove specified network element
     */
    var removePhysicalNetwork = function($item) {
      var $container = $item.closest('.setup-physical-network .content.input-area form');

      if (!$item.siblings().size()) {
        cloudStack.dialog.notice({
          message: 'You must have at least 1 physical network'
        });
      } else if ($item.find('input[type=radio]:checked').size()) {
        cloudStack.dialog.notice({
          message: 'Please select a different public and/or management network before removing'
        });
      } else {
        // Put any traffic type symbols back in original container
        $item.find('li.traffic-type-draggable').each(function() {
          var $draggable = $(this);
          var $originalContainer = $('.traffic-types-drag-area:visible > ul > li')
            .filter(function() {
              return $(this).hasClass($draggable.attr('traffic-type-id'));
            });

          $draggable.appendTo($originalContainer.find('ul'));
        });

        $item.remove();
      }

      $container.validate('refresh');
    };

    return function(listViewArgs) {
      var $wizard = $('#template').find('div.zone-wizard').clone();
      var $progress = $wizard.find('div.progress ul li');
      var $steps = $wizard.find('div.steps').children().hide().filter(':not(.disabled)');

      // Close wizard
      var close = function() {
        $wizard.dialog('destroy');
        $('div.overlay').fadeOut(function() { $('div.overlay').remove(); });
      };

      // Save and close wizard
      var completeAction = function() {
        var data = cloudStack.serializeForm($wizard.find('form'));
        args.action({
          data: data,
          response: {
            success: function(args) {
              var $item = $('.list-view').listView('prependItem', {
                data: [data],
                actionFilter: function(args) { return []; }
              });

              listViewArgs.complete({
                _custom: args._custom,
                $item: $item,
                messageArgs: {
                  name: $wizard.find('div.review div.vm-instance-name input').val()
                }
              });

              close();
            },
            error: function(message) {
              $wizard.remove();
              $('div.overlay').remove();

              if (message) {
                cloudStack.dialog.notice({ message: message });
              }
            }
          }
        });
      };

      /**
       * Generate dynamic form, based on ID of form object given
       */
      var makeForm = function(id, formState) {
        var form = cloudStack.dialog.createForm({
          noDialog: true,
          context: $.extend(true, {}, cloudStack.context, {
            zones: [formState]
          }),
          form: {
            title: '',
            desc: '',
            fields: args.forms[id].fields
          },
          after: function(args) {}
        });

        var $form = form.$formContainer.find('form');

        // Cleanup form to follow zone wizard CSS naming
        $form.find('input[type=submit]').remove();
        $form.find('.form-item').addClass('field').removeClass('form-item');
        $form.find('label.error').hide();
        $form.find('.form-item .name').each(function() {
          $(this).html($(this).find('label'));
        });

        $form.find('select, input').change(function() {
          cloudStack.evenOdd($form, '.field:visible', {
            even: function($row) {
              $row.removeClass('odd');
            },
            odd: function($row) {
              $row.addClass('odd');
            }
          });
        });

        return $form;
      };

      // Go to specified step in wizard,
      // updating nav items and diagram
      var showStep = function(index) {
        var targetIndex = index - 1;

        if (index <= 1) targetIndex = 0;
        if (targetIndex == $steps.size()) {
          completeAction();
        }

        $steps.hide();

        var $targetStep = $($steps[targetIndex]).show();
        var formState = cloudStack.serializeForm($wizard.find('form'));

        var formID = $targetStep.attr('zone-wizard-form');
        var $uiCustom = $targetStep.find('[ui-custom]');

        if (formID) {
          if (!$targetStep.find('form').size()) {
            makeForm(formID, formState).appendTo($targetStep.find('.content.input-area .select-container'));

            cloudStack.evenOdd($targetStep, '.field:visible', {
              even: function() {},
              odd: function($row) {
                $row.addClass('odd');
              }
            });
          }

          if (args.forms[formID].preFilter) {
            var preFilter = args.forms[formID].preFilter({
              $form: $targetStep.find('form'),
              data: formState
            });
          }
        }

        if ($uiCustom.size()) {
          $uiCustom.each(function() {
            var $item = $(this);
            var id = $item.attr('ui-custom');

            $item.replaceWith(
              args.customUI[id]({
                data: formState,
                context: cloudStack.context
              })
            )
          });
        }

        if (!targetIndex) {
          $wizard.find('.button.previous').hide();
        } else {
          $wizard.find('.button.previous').show();
        }

        var $nextButton = $wizard.find('.button.next');
        $nextButton.find('span').html('Next');
        $nextButton.removeClass('final');

        // Show launch button if last step
        if ($targetStep.index() == $steps.size() - 1) {
          $nextButton.find('span').html('Add zone');
          $nextButton.addClass('final');
        }

        // Update progress bar
        var $targetProgress = $progress.removeClass('active').filter(function() {
          return $(this).index() <= targetIndex;
        }).toggleClass('active');

        setTimeout(function() {
          if (!$targetStep.find('input[type=radio]:checked').size()) {
            $targetStep.find('input[type=radio]:first').click();
          }
        }, 50);

        $targetStep.find('form').validate();
      };

      // Events
      $wizard.find('select').change(function(event) {
        // Conditional selects (on step 4 mainly)
        var $target = $(this);
        var $tagged = $wizard.find('.conditional.vlan-type-tagged');
        var $untagged = $wizard.find('.conditional.vlan-type-untagged');
        var $accountSpecific = $wizard.find('.field.conditional.ip-scope-account-specific');

        // VLAN - tagged
        if ($target.is('[name=vlan-type]')) {
          $tagged.hide();
          $untagged.hide();
          $accountSpecific.hide();

          if ($target.val() == 'tagged') {
            $untagged.hide();
            $tagged.show();
          }
          else if ($target.val() == 'untagged') {
            $tagged.hide();
            $untagged.show();
          }

          $.merge($tagged, $untagged).find('select:visible').trigger('change');

          cloudStack.evenOdd($wizard, '.field:visible', {
            even: function($elem) { $elem.removeClass('odd'); $elem.addClass('even'); },
            odd: function($elem) { $elem.removeClass('even'); $elem.addClass('odd'); }
          });

          return true;
        }

        // IP Scope - acct. specific
        if ($target.is('select.ip-scope')) {
          $accountSpecific.hide();
          if ($target.val() == 'account-specific') $accountSpecific.show();

          cloudStack.evenOdd($wizard, '.field:visible', {
            even: function($elem) { $elem.removeClass('odd'); $elem.addClass('even'); },
            odd: function($elem) { $elem.removeClass('even'); $elem.addClass('odd'); }
          });
        }

        return true;
      });

      $wizard.click(function(event) {
        var $target = $(event.target);

        // Radio button
        if ($target.is('[type=radio]')) {

          if ($target.attr('name') == 'network-model') {
            var $inputs = $wizard.find('.isolation-mode').find('input[name=isolation-mode]').attr({
              disabled: 'disabled'
            });

            if ($target.val() == 'Advanced') {
              $inputs.attr('disabled', false);
            }
          }

          return true;
        }

        // Checkbox
        if ($target.is('[type=checkbox]:checked')) {
          $('div.conditional.' + $target.attr('name')).hide();

          return true;
        } else if ($target.is('[type=checkbox]:unchecked')) {
          $('div.conditional.' + $target.attr('name')).show();

          return true;
        }

        // Next button
        if ($target.closest('div.button.next').size()) {
          var $step = $steps.filter(':visible');
          // Validation
          var $form = $step.find('form').filter(function() {
            // Don't include multi-edit (validation happens separately)
            return !$(this).closest('.multi-edit').size();
          });

          // Handle validation for custom UI components
          var isCustomValidated = checkCustomValidation($step);
          if (($form.size() && !$form.valid()) || !isCustomValidated) {
            if (($form && $form.find('.error:visible').size()) || !isCustomValidated)
              return false;
          }

          showStep($steps.filter(':visible').index() + 2);

          return false;
        }

        // Previous button
        if ($target.closest('div.button.previous').size()) {
          showStep($steps.filter(':visible').index());

          return false;
        }

        // Close button
        if ($target.closest('div.button.cancel').size()) {
          close();

          return false;
        }

        // Edit link
        if ($target.closest('div.edit').size()) {
          var $edit = $target.closest('div.edit');

          showStep($edit.find('a').attr('href'));

          return false;
        }

        return true;
      });

      // Add/remove network action
      $wizard.find('.button.add.new-physical-network').click(function() {
        addPhysicalNetwork($wizard);
      });

      // Setup traffic type draggables
      $wizard.find('.traffic-type-draggable').draggable({
        appendTo: $wizard,
        helper: 'clone',

        // Events
        start: function(event, ui) {
          $(this).addClass('disabled');
        },

        stop: function(event, ui) {
          $(this).removeClass('disabled');
        }
      });

      $wizard.find('.traffic-types-drag-area').droppable({
        drop: function(event, ui) {
          var $ul = $(this).find('ul').filter(function() {
            return $(this).parent().hasClass(
              ui.draggable.attr('traffic-type-id')
            );
          });

          $ul.append(ui.draggable);
        }
      });

      // Initialize first physical network item
      addPhysicalNetwork($wizard);

      showStep(1);

      return $wizard.dialog({
        title: 'Add zone',
        width: 750,
        height: 665,
        zIndex: 5000,
        resizable: false
      }).closest('.ui-dialog').overlay();
    };
  };
})(jQuery, cloudStack);

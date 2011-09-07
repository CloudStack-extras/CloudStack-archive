(function($, cloudStack) {
  /**
   * Zone wizard
   */
  cloudStack.zoneWizard = function(args) {
    return function(listViewArgs) {
      var $wizard = $('#template').find('div.zone-wizard').clone();
      var $progress = $wizard.find('div.progress ul li');
      var $steps = $wizard.find('div.steps').children().hide();
      var $diagramParts = $wizard.find('div.diagram').children().hide();

      // Close wizard
      var close = function() {
        $wizard.dialog('destroy');
        $('div.overlay').fadeOut(function() { $('div.overlay').remove(); });
      };

      // Save and close wizard
      var completeAction = function() {
        args.complete({
          data: cloudStack.serializeForm($wizard.find('form')),
          response: {
            success: function(args) {
              listViewArgs.complete({
                messageArgs: {
                  name: $wizard.find('div.review div.vm-instance-name input').val()
                }
              });
              close();
            }
          }
        });
      };

      // Go to specified step in wizard,
      // updating nav items and diagram
      var showStep = function(index) {
        var targetIndex = index - 1;

        if (index <= 1) targetIndex = 0;
        if (targetIndex == $steps.size()) {
          completeAction();
        }

        var $targetStep = $($steps.hide()[targetIndex]).show();

        // Show launch vm button if last step
        var $nextButton = $wizard.find('.button.next');
        $nextButton.find('span').html('Next');
        $nextButton.removeClass('final');
        if ($targetStep.index() == $steps.size() - 1) {
          $nextButton.find('span').html('Add zone');
          $nextButton.addClass('final');
        }

        // Show relevant conditional sub-step if present
        if ($targetStep.has('.wizard-step-conditional')) {
          $targetStep.find('.wizard-step-conditional').hide();
          $targetStep.find('.wizard-step-conditional.select-network').show();
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
      $wizard.click(function(event) {
        var $target = $(event.target);

        // Next button
        if ($target.closest('div.button.next').size()) {
          // Check validation first
          var $form = $steps.filter(':visible').find('form');
          if ($form.size() && !$form.valid()) return false;

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

      showStep(1);

      // Setup tabs and slider
      $wizard.find('.tab-view').tabs();
      $wizard.find('.slider').slider({
        min: 1,
        max: 100,
        start: function(event) {
          $wizard.find('div.data-disk-offering div.custom-size input[type=radio]').click();
        },
        slide: function(event, ui) {
          $wizard.find('div.data-disk-offering div.custom-size input[type=text]').val(
            ui.value
          );
        }
      });

      return $wizard.dialog({
        title: 'Add zone',
        width: 665,
        height: 665,
        zIndex: 5000,
        resizable: false
      })
        .closest('.ui-dialog').overlay();
    };
  };
})(jQuery, cloudStack);
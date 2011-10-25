(function($, cloudStack) {
  cloudStack.dialog = {
    /**
     * Dialog with form
     */
    createForm: function(args) {
      var $formContainer = $('<div>').addClass('form-container');
      var $message = $('<span>').addClass('message').appendTo($formContainer).html(args.form.desc);
      var $form = $('<form>').appendTo($formContainer);

      $.each(args.form.fields, function(key) {
        var $formItem = $('<div>')
              .addClass('form-item')
              .attr({ rel: key });

        if (this.hidden || this.isHidden) $formItem.hide();

        $formItem.appendTo($form);

        // Label field
        var $name = $('<div>').addClass('name')
              .appendTo($formItem)
              .append(
                $('<label>').html(this.label + ':')
              );

        // Input area
        var $value = $('<div>').addClass('value')
              .appendTo($formItem);
        var $input, $dependsOn, selectFn, selectArgs;
        var dependsOn = this.dependsOn;

        // Depends on fields
        if (this.dependsOn) {
          $formItem.attr('depends-on', dependsOn);
          $dependsOn = $form.find('input, select').filter(function() {
            return $(this).attr('name') === dependsOn;
          });

          if ($dependsOn.is('[type=checkbox]')) {
            $dependsOn.bind('click', function(event) {
              var $target = $(this);
              var $dependent = $form.find('[depends-on=' + dependsOn + ']');

              if ($target.is(':checked')) {
                $dependent.css('display', 'inline-block');
                $dependent.each(function() {
                  if ($(this).data('dialog-select-fn')) {
                    $(this).data('dialog-select-fn')();
                  }
                });
              } else {
                $dependent.hide();
                $dependent.find('input[type=checkbox]').trigger('click');
              }

              $dependent.find('input[type=checkbox]').attr('checked', false);

              return true;
            });
          }
        }

        // Determine field type of input
        if (this.select) {
          selectArgs = {
            context: args.context,
            response: {
              success: function(args) {
                $(args.data).each(function() {
                  var id = this.id;
                  var description = this.description;

                  if (args.descriptionField)
                    description = this[args.descriptionField];
                  else
                    description = this.description;

                  var $option = $('<option>')
                        .appendTo($input)
                        .val(id)
                        .html(description);
                });

                $input.trigger('change');
              }
            }
          };
          selectFn = this.select;
          $input = $('<select>')
            .attr({ name: key })
            .data('dialog-select-fn', function() {
              selectFn(selectArgs);
            })
            .appendTo($value);

          // Pass form item to provider for additional manipulation
          $.extend(selectArgs, { $select: $input });

          if (dependsOn) {
            $dependsOn = $form.find('input, select').filter(function() {
              return $(this).attr('name') === dependsOn;
            });
            $dependsOn.bind('change', function(event) {
              var $target = $(this);

              if (!$dependsOn.is('select')) return true;
              
              var dependsOnArgs = {};

              $input.find('option').remove();
              $input.trigger('change');

              if (!$target.children().size()) return true;

              dependsOnArgs[dependsOn] = $target.val();
              selectFn($.extend(selectArgs, dependsOnArgs));

              return true;
            });

            if (!$dependsOn.is('select')) {
              selectFn(selectArgs);
            }
          } else {
            selectFn(selectArgs);
          }
        } else if (this.isBoolean) {
          $input = $('<input>').attr({ name: key, type: 'checkbox' }).appendTo($value);
        } else {
          $input = $('<input>').attr({ 
            name: key, 
            type: this.password || this.isPassword ? 'password' : 'text'
          }).appendTo($value);
        }

        $input.data('validation-rules', this.validation);
        $('<label>').addClass('error').appendTo($value).html('*required');
      });

      $form.find('select').trigger('change');

      var getFormValues = function() {
        var formValues = {};
        $.each(args.form.fields, function(key) {
        });
      };

      // Setup form validation
      $formContainer.find('form').validate();
      $formContainer.find('input, select').each(function() {
        if ($(this).data('validation-rules')) {
          $(this).rules('add', $(this).data('validation-rules'));
        }
      });

      var complete = function($formContainer) {
        var $form = $formContainer.find('form');
        var data = cloudStack.serializeForm($form);

        if (!$formContainer.find('form').valid()) {
          // Ignore hidden field validation
          if ($formContainer.find('input.error:visible').size()) {
            return false;
          }
        }

        args.after({
          data: data,
          ref: args.ref, // For backwards compatibility; use context
          context: args.context,
          $form: $form
        });

        return true;
      };

      if (args.noDialog) {
        return {
          $formContainer: $formContainer,
          completeAction: complete
        };
      }

      return $formContainer.dialog({
        dialogClass: 'create-form',
        width: 400,
        title: args.form.title,
        open: function() {
          if (args.form.preFilter) {
            args.form.preFilter({ $form: $form, context: args.context });
          }
        },
        buttons: [
          {
            text: 'Create',
            'class': 'ok',
            click: function() {
              if (!complete($formContainer)) { return false; }

              $('div.overlay').remove();
              $(this).dialog('destroy');

              return true;
            }
          },
          {
            text: 'Cancel',
            'class': 'cancel',
            click: function() {
              $('div.overlay').remove();
              $(this).dialog('destroy');
            }
          }
        ]
      }).closest('.ui-dialog').overlay();
    },

    /**
     * Confirmation dialog
     */
    confirm: function(args) {
      return $(
        $('<span>').addClass('message').html(
          args.message
        )
      ).dialog({
        title: 'Confirm',
        dialogClass: 'confirm',
        zIndex: 5000,
        buttons: [
          {
            text: 'Cancel',
            'class': 'cancel',
            click: function() {
              $(this).dialog('destroy');
              $('div.overlay').remove();
            }
          },
          {
            text: 'Yes',
            'class': 'ok',
            click: function() {
              args.action();
              $(this).dialog('destroy');
              $('div.overlay').remove();
            }
          }
        ]
      }).closest('.ui-dialog').overlay();
    },

    /**
     * Notice dialog
     */
    notice: function(args) {
      return $(
        $('<span>').addClass('message').html(
          args.message
        )
      ).dialog({
        title: 'Status',
        dialogClass: 'notice',
        zIndex: 5000,
        buttons: [
          {
            text: 'Close',
            'class': 'close',
            click: function() {
              $(this).dialog('destroy');
            }
          }
        ]
      });
    }
  };
})(jQuery, cloudStack);

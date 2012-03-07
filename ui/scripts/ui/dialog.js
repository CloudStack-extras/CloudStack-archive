(function($, cloudStack) {
  cloudStack.dialog = {
    /**
     * Error message form
     *
     * Returns callback, that can be plugged into a standard data provider response
     */
    error: function(callback) {
      return function(args) {
        var message = args.message ? args.message : args;
        if (message) cloudStack.dialog.notice({ message: message });

        if (callback) callback();
      };
    },

    /**
     * Dialog with form
     */
    createForm: function(args) {
      var $formContainer = $('<div>').addClass('form-container');
      var $message = $('<span>').addClass('message').appendTo($formContainer).html(
        _l(args.form.desc)
      );
      var $form = $('<form>').appendTo($formContainer)
            .submit(function() {
              $(this).closest('.ui-dialog').find('button.ok').click();
              
              return false;
            });
      
      var createLabel = _l(args.form.createLabel);
      var $submit = $('<input>')
            .attr({
              type: 'submit'
            })
            .hide()
            .appendTo($form);

      // Render fields and events
      var fields = $.map(args.form.fields, function(value, key) {
        return key;
      })
      $(fields).each(function() {
        var key = this;
        var field = args.form.fields[key];
        
        var $formItem = $('<div>')
          .addClass('form-item')
          .attr({ rel: key });

        if (field.hidden || field.isHidden) $formItem.hide();

        $formItem.appendTo($form);

        // Label field
        var $name = $('<div>').addClass('name')
          .appendTo($formItem)
          .append(
            $('<label>').html(_l(field.label) + ':')
          );

        // Add 'required asterisk' if field is required
        if (field.validation && field.validation.required) {
          $name.find('label').prepend($('<span>').addClass('field-required').html('*'));
        }

        // Tooltip description
        if (field.desc) {
          $formItem.attr({ title: _l(field.desc) });
        }

        // Input area
        var $value = $('<div>').addClass('value')
          .appendTo($formItem);
        var $input, $dependsOn, selectFn, selectArgs;
        var dependsOn = field.dependsOn;

        // Depends on fields
        if (field.dependsOn) {
          $formItem.attr('depends-on', dependsOn);
          $dependsOn = $form.find('input, select').filter(function() {
            return $(this).attr('name') === dependsOn;
          });

          if ($dependsOn.is('[type=checkbox]')) {
            var isReverse = args.form.fields[dependsOn].isReverse;

            // Checkbox
            $dependsOn.bind('click', function(event) {
              var $target = $(this);
              var $dependent = $target.closest('form').find('[depends-on=\'' + dependsOn + '\']');

              if (($target.is(':checked') && !isReverse) ||
                  ($target.is(':unchecked') && isReverse)) {
                $dependent.css('display', 'inline-block');
                $dependent.each(function() {
                  if ($(this).data('dialog-select-fn')) {
                    $(this).data('dialog-select-fn')();
                  }
                });
              } else if (($target.is(':unchecked') && !isReverse) ||
                         ($target.is(':checked') && isReverse)) {
                $dependent.hide();
              }

              $dependent.find('input[type=checkbox]').click();

              if (!isReverse) {
                $dependent.find('input[type=checkbox]').attr('checked', false);
              } else {
                $dependent.find('input[type=checkbox]').attr('checked', true);
              }

              return true;
            });

            // Show fields by default if it is reverse checkbox
            if (isReverse) {
              $dependsOn.click();
            }
          }
        }

        // Determine field type of input
        if (field.select) {
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

                if (field.defaultValue) {
                  $input.val(field.defaultValue);
                }

                $input.trigger('change');
              }
            }
          };

          selectFn = field.select;
          $input = $('<select>')
            .attr({ name: key })
            .data('dialog-select-fn', function(args) {
              selectFn(args ?
                       $.extend(true, {}, selectArgs, args) : selectArgs);
            })
            .appendTo($value);

          // Pass form item to provider for additional manipulation
          $.extend(selectArgs, { $select: $input });

          if (dependsOn) {
            $dependsOn = $input.closest('form').find('input, select').filter(function() {
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
        } else if (field.isBoolean) {
          if (field.multiArray) {
            $input = $('<div>')
              .addClass('multi-array').addClass(key).appendTo($value);

            $.each(field.multiArray, function(itemKey, itemValue) {
              $input.append(
                $('<div>').addClass('item')
                  .append(
                    $.merge(
                      $('<div>').addClass('name').html(_l(itemValue.label)),
                      $('<div>').addClass('value').append(
                        $('<input>').attr({ name: itemKey, type: 'checkbox' }).appendTo($value)
                      )
                    )
                  )
              );
            });

          } else {
            $input = $('<input>').attr({ name: key, type: 'checkbox' }).appendTo($value);
            if (field.isChecked) {
              $input.attr('checked', 'checked');
            }
          }
        } else if (field.dynamic) {
          // Generate a 'sub-create-form' -- append resulting fields
          $input = $('<div>').addClass('dynamic-input').appendTo($value);
          $form.hide();

          field.dynamic({
            response: {
              success: function(args) {
                var form = cloudStack.dialog.createForm({
                  noDialog: true,
                  form: {
                    title: '',
                    fields: args.fields
                  }
                });

                var $fields = form.$formContainer.find('.form-item').appendTo($input);
                $form.show();

                // Form should be slightly wider
                $form.closest(':ui-dialog').dialog('option', { position: 'center' });
              }
            }
          });
				} else if(field.isTextarea) {				 
					$input = $('<textarea>').attr({
						name: key
					}).appendTo($value);

					if (field.defaultValue) {
						$input.val(field.defaultValue);
					}					
        } else {
          // Text field
          if (field.range) {
            $input = $.merge(
              // Range start
              $('<input>').attr({
                type: 'text',
                name: field.range[0]
              }),

              // Range end
              $('<input>').attr({
                type: 'text',
                name: field.range[1]
              })
            ).appendTo(
              $('<div>').addClass('range-edit').appendTo($value)
            );

            $input.wrap($('<div>').addClass('range-item'));
          } else {
            $input = $('<input>').attr({
              name: key,
              type: field.password || field.isPassword ? 'password' : 'text'
            }).appendTo($value);

            if (field.defaultValue) {
              $input.val(field.defaultValue);
            }
          }
        }

        $input.data('validation-rules', field.validation);
        $('<label>').addClass('error').appendTo($value).html('*' + _l('label.required'));
      });

      $form.find('select').trigger('change');

      var getFormValues = function() {
        var formValues = {};
        $.each(args.form.fields, function(key) {});
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
          if ($formContainer.find('input.error:visible, select.error:visible').size()) {
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
        title: _l(args.form.title),
        open: function() {
          if (args.form.preFilter) {
            args.form.preFilter({ $form: $form, context: args.context });
          }
        },
        buttons: [
          {
            text: createLabel ? createLabel : _l('label.ok'),
            'class': 'ok',
            click: function() {
              if (!complete($formContainer)) { return false; }

              $('div.overlay').remove();
              $formContainer.remove();
              $(this).dialog('destroy');

              return true;
            }
          },
          {
            text: _l('label.cancel'),
            'class': 'cancel',
            click: function() {
              $('div.overlay').remove();
              $formContainer.remove();
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
          _l(args.message)
        )
      ).dialog({
        title: _l('label.confirmation'),
        dialogClass: 'confirm',
        zIndex: 5000,
        buttons: [
          {
            text: _l('label.no'),
            'class': 'cancel',
            click: function() {
              $(this).dialog('destroy');
              $('div.overlay').remove();
              if (args.cancelAction) { args.cancelAction(); }
            }
          },
          {
            text: _l('label.yes'),
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
          _l(args.message)
        )
      ).dialog({
        title: _l('label.status'),
        dialogClass: 'notice',
        zIndex: 5000,
        buttons: [
          {
            text: _l('Close'),
            'class': 'close',
            click: function() {
              $(this).dialog('destroy');
              if (args.clickAction) args.clickAction();
            }
          }
        ]
      });
    }
  };
})(window.jQuery, window.cloudStack);

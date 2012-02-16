(function($, cloudStack) {
  var installWizard = function(args) {
    var context = args.context;
    var $installWizard = $('<div>').addClass('install-wizard');
    var $container = args.$container;
    var state = {}; // Hold wizard form state

    var launchStart; // Holds last launch callback, in case of error
    var $launchState;

    /**
     * Successful installation action
     */
    var complete = function() {
      $installWizard.remove();
      $('html body').removeClass('install-wizard');

      args.complete();
    };

    /**
     * Retrive copy text and append to element -- async
     * @param id
     * @param $elem
     */
    var getCopy = function(id, $elem) {
      cloudStack.installWizard.copy[id]({
        response: {
          success: function(args) {
            $elem.append(args.text);
          }
        }
      });

      return $elem;
    };

    /**
     * Go to specified step in flow -- for use in individual steps
     * @param stateStepID ID to group state elements in (i.e., zone, pod, cluster, ...)
     * @param $elem (optional) Element containing <form>, to serialize for state
     */
    var goTo = cloudStack._goto = function(stepID, stateID, $elem, options) {
      if (!options) options = {};

      var $body = $installWizard.find('.body');

      if (stateID && $elem) {
        state[stateID] = cloudStack.serializeForm($elem.is('form') ? $elem : $elem.find('form'));
      }

      $body.children().fadeOut('fast', function() {
        var $nextStep = steps[stepID]({ nextStep: options.nextStep });

        $body.children().detach();
        $nextStep.appendTo($body).hide();
        $nextStep.addClass('step').fadeIn();
      });
    };

    /**
     * Generic page elements
     */
    var elems = {
      /**
       * A standard intro text wizard step template
       */
      stepIntro: function(args) {
        var title = args.title;
        var subtitle = args.subtitle;
        var copyID = args.copyID;
        var prevStepID = args.prevStepID;
        var nextStepID = args.nextStepID;
        var diagram = args.diagram;

        var $intro = $('<div></div>').addClass('intro');
        var $title = $('<div></div>').addClass('title')
              .html(title);
        var $subtitle = $('<div></div>').addClass('subtitle')
              .html(subtitle);
        var $copy = getCopy(copyID, $('<p></p>'));
        var $prev = elems.prevButton('Go back');
        var $continue = elems.nextButton('OK');

        $continue.click(function() {
          goTo(nextStepID);

          return false;
        });

        $prev.click(function() {
          goTo(prevStepID);
        });

        return function(args) {
          showDiagram(diagram);

          return $intro.append(
            $title, $subtitle,
            $copy,
            $prev,
            $continue
          );
        };
      },

      /**
       * A standard form-based wizard step template
       * -- relies on createForm for form generation
       */
      step: function(args) {
        var title = args.title;
        var formData = args.form;
        var diagram = args.diagram;
        var id = args.id;
        var stateID = args.stateID;
        var tooltipID = args.tooltipID;
        var prevStepID = args.prevStepID;
        var nextStepID = args.nextStepID;
        var form;

        var $container = $('<div></div>').addClass(id);
        var $form = $('<div>').addClass('setup-form');
        var $save = elems.nextButton('Continue', { type: 'submit' });
        var $prev = elems.prevButton('Go back');
        var $title = $('<div></div>').addClass('title').html(title);

        // Generate form
        form = cloudStack.dialog.createForm({
          noDialog: true,
          form: {
            title: title,
            desc: '',
            fields: formData
          },
          after: function(args) {
            goTo(nextStepID, stateID, $form);
          }
        });

        $form.append(form.$formContainer);
        $form.find('.form-item').addClass('field');
        $prev.appendTo($form.find('form'));
        $save.appendTo($form.find('form'));

        // Submit handler
        $form.find('form').submit(function() {
          form.completeAction($form);

          return false;
        });

        // Go back handler
        $prev.click(function(event) {
          goTo(prevStepID);
        });

        // Cleanup
        $form.find('.message').remove();
        $form.find('label.error').hide();
        $container.append($form.prepend($title));

        showTooltip($form, tooltipID);

        return function(args) {
          var overrideGotoEvent = function(event) {
            goTo(args.nextStep, stateID, $form);

            return false;
          };

          if (args && args.nextStep) {
            $save.unbind('click');
            $save.click(overrideGotoEvent);
          }

          // Setup diagram, tooltips
          showDiagram(diagram);
          setTimeout(function() {
            $form.find('input[type=text]:first').focus();
          }, 600);

          return $container;
        };
      },

      /**
       * A form item tooltip
       */
      tooltip: function(title, content) {
        return $('<div>').addClass('tooltip-info').append(
          $('<div>').addClass('arrow'),
          $('<div>').addClass('title').html(title),
          $('<div>').addClass('content').append($('<p>').html(content))
        );
      },

      /**
       * The main header
       */
      header: function() {
        return $('<div></div>').addClass('header')
          .append(
            $.merge(
              $('<h2></h2>').html(_l('label.installWizard.title')),
              $('<h3></h3>').html('This tour will aid you in setting up your CloudStack installation')
            )
        );
      },

      /**
       * The wizard body (contains form)
       */
      body: function() {
        return $('<div></div>').addClass('body');
      },

      /**
       * A standard next button
       */
      nextButton: function(label, options) {
        var $button = options && !options.type ?
          $('<div>').addClass('button goTo').html(label) :
          $('<input>').attr({ type: 'submit' }).addClass('button goTo').val(label);

        return $button;
      },

      /**
       * A standard previous/go back button
       */
      prevButton: function(label) {
        return $('<div>').addClass('button go-back').html(label);
      },

      diagramParts: function() {
        return $('<div>').addClass('diagram').append(
          $('<div>').addClass('part zone'),
          $('<div>').addClass('part pod'),
          $('<div>').addClass('part cluster'),
          $('<div>').addClass('part host'),
          $('<div>').addClass('part primaryStorage'),
          $('<div>').addClass('part secondaryStorage'),
          $('<div>').addClass('part loading').append($('<div>').addClass('icon'))
        );
      }
    };

    var $diagramParts = elems.diagramParts();

    var showDiagram = function(parts) {
      $diagramParts.children().hide();
      $diagramParts.find(parts).show();
    };

    /**
     * Show tooltip for focused form elements
     */
    var showTooltip = function($formContainer, sectionID) {
      var $tooltip = elems.tooltip('Hints', '');

      $formContainer.find('input[type=text]').focus(function() {
        var $input = $(this);

        $tooltip.find('p').html('');
        $tooltip.appendTo($formContainer);
        $tooltip.css({
          top: $(this).position().top - 20
        });

        var content = getCopy(
          'tooltip.' + sectionID + '.' + $input.attr('name'),
          $tooltip.find('p')
        );
      });

      $formContainer.find('input').blur(function() {
        $tooltip.remove();
      });
    };

    /**
     * Layout/behavior for each step in wizard
     */
    var steps = {
      eula: function(args) {
        var $intro = $('<div></div>').addClass('intro eula');
        var $title = $('<div></div>').addClass('title').html(_l('label.license.agreement'));
        var $subtitle = $('<div></div>').addClass('subtitle').html(_l('label.license.agreement.subtitle'));
        var $copy = getCopy('eula', $('<div></div>').addClass('eula-copy'));
        var $continue = elems.nextButton(_l('label.agree'));

        $continue.click(function() {
          goTo('intro');

          return false;
        });
        
        return $intro.append($title, $subtitle,
                             $copy,
                             $continue);
      },
      
      intro: function(args) {
        var $intro = $('<div></div>').addClass('intro what-is-cloudstack');
        var $title = $('<div></div>').addClass('title').html('What is CloudStack&#8482?');
        var $subtitle = $('<div></div>').addClass('subtitle').html('Introduction to CloudStack&#8482');
        var $copy = getCopy('whatIsCloudStack', $('<p></p>'));
        var $continue = elems.nextButton('Continue with basic installation');
        var $advanced = elems.nextButton('I have used Cloudstack before, skip this guide').addClass('advanced-installation');

        $continue.click(function() {
          goTo('changeUser');

          return false;
        });

        $advanced.click(function() {
          complete();

          return false;
        });

        return $intro.append($title, $subtitle,
                             $copy,
                             $advanced,
                             $continue);
      },

      changeUser: function(args) {
        var $changeUser = $('<div></div>').addClass('step change-user');
        var $form = $('<form></form>').appendTo($changeUser);

        // Fields
        var $password = $('<input>').addClass('required').attr({ id: 'password', type: 'password', name: 'password' });
        var $passwordConfirm = $('<input>').addClass('required').attr({ id: 'password-confirm', type: 'password', name: 'password-confirm' });
        var $save = elems.nextButton('Save and continue', { type: 'submit' });

        $form.append(
          $('<div></div>').addClass('title').html('Please change your password.'),
          $('<div></div>').addClass('field').append(
            $('<label>New Password:</label>'), $password
          ),
          $('<div></div>').addClass('field').append(
            $('<label>Confirm Password:</label>'), $passwordConfirm
          ),
          $save
        );

        $form.validate({
          rules: {
            'password-confirm': {
              equalTo: '#password'
            }
          },
          messages: {
            'password-confirm': {
              equalTo: 'The password fields do not match.'
            }
          }
        });

        // Save event
        $form.submit(function() {
          if (!$form.valid()) return false;

          var $loading = $('<div></div>').addClass('loading-overlay').prependTo($form);
          cloudStack.installWizard.changeUser({
            data: cloudStack.serializeForm($form),
            response: {
              success: function(args) {
                goTo('addZoneIntro', 'newUser', $form);
              }
            }
          });

          return false;
        });

        return $changeUser;
      },

      /**
       * Add zone intro text
       * @param args
       */
      addZoneIntro: elems.stepIntro({
        title: "Let's add a zone",
        subtitle: 'What is a zone?',
        copyID: 'whatIsAZone',
        prevStepID: 'changeUser',
        nextStepID: 'addZone',
        diagram: '.part.zone'
      }),

      /**
       * Add zone form
       */
      addZone: elems.step({
        title: 'Add zone',
        id: 'add-zone',
        stateID: 'zone',
        tooltipID: 'addZone',
        diagram: '.part.zone',
        prevStepID: 'addZoneIntro',
        nextStepID: 'addPodIntro',
        form: {
          name: { label: 'Name', validation: { required: true } },
          dns1: { label: 'DNS 1', validation: { required: true } },
          dns2: { label: 'DNS 2' },
          internaldns1: { label: 'Internal DNS 1', validation: { required: true } },
          internaldns2: { label: 'Internal DNS 2' }
        }
      }),

      /**
       * Add pod intro text
       * @param args
       */
      addPodIntro: elems.stepIntro({
        title: "Let's add a pod.",
        subtitle: 'What is a pod?',
        copyID: 'whatIsAPod',
        prevStepID: 'addZone',
        nextStepID: 'addPod',
        diagram: '.part.zone, .part.pod'
      }),

      /**
       * Add pod form
       * @param args
       */
      addPod: elems.step({
        title: 'Add pod',
        id: 'add-pod',
        stateID: 'pod',
        tooltipID: 'addPod',
        diagram: '.part.zone, .part.pod',
        prevStepID: 'addPodIntro',
        nextStepID: 'configureGuestTraffic',
        form: {
          name: { label: 'Name', validation: { required: true }},
          reservedSystemGateway: { label: 'Gateway', validation: { required: true }},
          reservedSystemNetmask: { label: 'Netmask', validation: { required: true }},
          ipRange: {
            label: 'IP Range',
            range: ['reservedSystemStartIp', 'reservedSystemEndIp'],
            validation: { required: true }
          }
        }
      }),

      /**
       * Add guest network form
       */
      configureGuestTraffic: elems.step({
        title: 'Add guest network',
        id: 'add-guest-network',
        stateID: 'guestTraffic',
        tooltipID: 'configureGuestTraffic',
        diagram: '.part.zone, .part.pod',
        prevStepID: 'addPod',
        nextStepID: 'addClusterIntro',
        form: {
          guestGateway: { label: 'Gateway', validation: { required: true } },
          guestNetmask: { label: 'Netmask', validation: { required: true } },
          guestIPRange: { label: 'IP Range', range: ['guestStartIp', 'guestEndIp'], validation: { required: true } }
        }
      }),

      /**
       * Add cluster intro text
       * @param args
       */
      addClusterIntro: elems.stepIntro({
        title: "Let's add a cluster.",
        subtitle: 'What is a cluster?',
        copyID: 'whatIsACluster',
        prevStepID: 'configureGuestTraffic',
        nextStepID: 'addCluster',
        diagram: '.part.zone, .part.cluster'
      }),

      /**
       * Add cluster form
       * @param args
       */
      addCluster: elems.step({
        title: 'Add cluster',
        id: 'add-cluster',
        stateID: 'cluster',
        tooltipID: 'addCluster',
        prevStepID: 'addClusterIntro',
        nextStepID: 'addHostIntro',
        diagram: '.part.zone, .part.cluster',
        form: {
          hypervisor: {
            label: 'Hypervisor',
            select: function(args) {
              args.response.success({ data: [
                { id: 'XenServer', description: 'XenServer' },
								{ id: 'KVM', description: 'KVM'}
              ]});
            }
          },
          name: { label: 'Name', validation: { required: true }}
        }
      }),

      /**
       * Add host intro text
       * @param args
       */
      addHostIntro: elems.stepIntro({
        title: "Let's add a host.",
        subtitle: 'What is a host?',
        copyID: 'whatIsAHost',
        prevStepID: 'addCluster',
        nextStepID: 'addHost',
        diagram: '.part.zone, .part.host'
      }),

      /**
       * Add host form
       * @param args
       */
      addHost: elems.step({
        title: 'Add host',
        id: 'add-host',
        stateID: 'host',
        tooltipID: 'addHost',
        prevStepID: 'addHostIntro',
        nextStepID: 'addPrimaryStorageIntro',
        diagram: '.part.zone, .part.host',
        form: {
          hostname: {
            label: 'Host name',
            validation: { required: true }
          },

          username: {
            label: 'User name',
            validation: { required: true }
          },

          password: {
            label: 'Password',
            validation: { required: true },
            isPassword: true
          }
        }
      }),

      /**
       * Add primary storage intro text
       * @param args
       */
      addPrimaryStorageIntro: elems.stepIntro({
        title: "Let's add primary storage.",
        subtitle: 'What is primary storage?',
        copyID: 'whatIsPrimaryStorage',
        prevStepID: 'addHost',
        nextStepID: 'addPrimaryStorage',
        diagram: '.part.zone, .part.primaryStorage'
      }),

      /**
       * Add primary storage
       * @param args
       */
      addPrimaryStorage: elems.step({
        title: 'Add primary storage',
        id: 'add-primary-storage',
        stateID: 'primaryStorage',
        tooltipID: 'addPrimaryStorage',
        prevStepID: 'addPrimaryStorageIntro',
        nextStepID: 'addSecondaryStorageIntro',
        diagram: '.part.zone, .part.primaryStorage',
        form: {
          name: {
            label: 'Name',
            validation: { required: true }
          },

          protocol: {
            label: 'Protocol',
            select: function(args) {
              args.response.success({
                data: { id: 'nfs', description: 'NFS' }
              });
            }
          },

          server: {
            label: 'Server',
            validation: { required: true }
          },

          path: {
            label: 'Path',
            validation: { required: true }
          }
        }
      }),

      /**
       * Add secondary storage intro text
       * @param args
       */
      addSecondaryStorageIntro: elems.stepIntro({
        title: "Let's add secondary storage.",
        subtitle: 'What is secondary storage?',
        copyID: 'whatIsSecondaryStorage',
        prevStepID: 'addPrimaryStorage',
        nextStepID: 'addSecondaryStorage',
        diagram: '.part.zone, .part.secondaryStorage'
      }),

      /**
       * Add secondary storage
       * @param args
       */
      addSecondaryStorage: elems.step({
        title: 'Add secondary storage',
        id: 'add-secondary-storage',
        stateID: 'secondaryStorage',
        tooltipID: 'addSecondaryStorage',
        prevStepID: 'addSecondaryStorageIntro',
        nextStepID: 'launchInfo',
        diagram: '.part.zone, .part.secondaryStorage',
        form: {
          nfsServer: {
            label: 'NFS Server',
            validation: { required: true }
          },
          path: {
            label: 'Path',
            validation: { required: true }
          }
        }
      }),

      /**
       * Pre-launch text
       */
      launchInfo: function(args) {
        var $intro = $('<div></div>').addClass('intro');
        var $title = $('<div></div>').addClass('title')
          .html('Congratulations!.');
        var $subtitle = $('<div></div>').addClass('subtitle')
          .html('Click the launch button.');
        var $continue = elems.nextButton('Launch');
        var $prev = elems.prevButton('Go back');

        $continue.click(function() {
          goTo('launch');

          return false;
        });

        $prev.click(function() {
          goTo('addSecondaryStorage');
        });

        showDiagram('.part.zone, .part.secondaryStorage');

        return $intro.append(
          $title, $subtitle,
          $prev, $continue
        );
      },

      /**
       * Pre-launch test -- after error correction
       */
      launchInfoError: function(args) {
        var $intro = $('<div></div>').addClass('intro');
        var $title = $('<div></div>').addClass('title')
              .html('Corrections saved.');
        var $subtitle = $('<div></div>').addClass('subtitle')
              .html('Click the button to retry launch.');
        var $continue = elems.nextButton('Launch');

        $continue.click(function() {
          goTo('launch');

          return false;
        });

        showDiagram('.part.zone, .part.secondaryStorage');

        return $intro.append(
          $title, $subtitle,
          $continue
        );
      },

      /**
       * Initiates launch tasks
       */
      launch: function(args) {
        var $intro = $('<div>').addClass('intro');
        var $title = $('<div>').addClass('title')
              .html('Now building your cloud...');
        var $subtitle = $('<div></div>').addClass('subtitle');

        showDiagram('.part.loading');
        $intro.append(
          $title, $subtitle
        );

        cloudStack.installWizard.action({
          data: state,
          startFn: launchStart,
          response: {
            message: function(msg) {
              var $li = $('<li>').html(msg);

              $subtitle.append($li);
              $li.siblings().addClass('complete');
            },
            success: function() {
              goTo('complete');
            },
            error: function(stepID, message, callback) {
              launchStart = callback;
              $subtitle.find('li:last').addClass('error');

              $subtitle.append(
                $('<p>').html(
                  'Something went wrong; you may go back and correct any errors:<br/>'
                    + message
                ),
                $('<div>').addClass('button').append(
                  $('<span>').html('Go back')
                ).click(function() {
                  goTo(stepID, null, null, {
                    nextStep: 'launchInfoError'
                  });
                })
              );
            }
          }
        });

        return $intro;
      },

      complete: function(args) {
        var $intro = $('<div></div>').addClass('intro');
        var $title = $('<div></div>').addClass('title')
              .html('Cloud setup successful');
        var $subtitle = $('<div></div>').addClass('subtitle')
              .html('You may now continue.');
        var $continue = elems.nextButton('Launch');

        showDiagram('');

        $continue.click(function() {
          $installWizard.fadeOut(function() {
            complete();
          });
        });

        return $intro.append(
          $title, $subtitle, $continue
        );
      }
    };

    var initialStep = steps.eula().addClass('step');
    showDiagram('');
    $('html body').addClass('install-wizard');

    $installWizard.append(
      elems.header(),
      elems.body().append(initialStep),
      $diagramParts
    ).appendTo($container);
  };

  cloudStack.uiCustom.installWizard = installWizard;
}(jQuery, cloudStack));

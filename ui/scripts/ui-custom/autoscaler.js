// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
(function($, cloudStack) {
  cloudStack.uiCustom.autoscaler = function(args) {
    // Place outer args here as local variables
    // i.e, -- var dataProvider = args.dataProvider
    var topfields = args.forms.topFields;
    var bottomfields = args.forms.bottomFields;
    var scaleuppolicy = args.forms.scaleUpPolicy;
    var scaledownpolicy = args.forms.scaleDownPolicy;
    
    return function(args) {
      var context = args.context;
      var $autoscalerDialog = $('<div>').addClass('autoscaler');
      var $topFields = $('<div>').addClass('field-group top-fields');
      var $bottomFields = $('<div>').addClass('field-group bottom-fields');
      var $scaleUpPolicy = $('<div>').addClass('scale-up-policy');
      var $slideScaleUp = $('<div></div>').addClass('expand');
      var $hideScaleUp = $('<div></div>').addClass('hide');
      var $scaleUpLabel= $('<div>Show</div>').addClass('slide-label');
      var $scaleUpHideLabel=$('<div>Hide</div>').addClass('slide-label');
      var $scaleDownHideLabel=$('<div>Hide</div>').addClass('slide-label');
      var $scaleDownLabel=$('<div>Show</div>').addClass('slide-label');
      var $slideScaleDown = $('<div></div>').addClass('expand');
      var $hideScaleDown = $('<div></div>').addClass('hide');
      var $scaleUpDivider = $('<hr></hr>').addClass('policy-divider');
      var $scaleDownDivider = $('<hr></hr>').addClass('policy-divider');
      var $bottomFieldDivider = $('<hr></hr>').addClass('policy-divider');
      var $scaleDownPolicy = $('<div>').addClass('scale-down-policy');
      var $scaleUpPolicyTitle = $('<div>').addClass('scale-up-policy-title').html("Scale Up Policy");
      var $scaleDownPolicyTitle = $('<div>').addClass('scale-down-policy-title').html("Scale Down Policy");
      var topFieldForm, $topFieldForm,
          bottomFieldForm, $bottomFieldForm,
          scaleUpPolicyTitleForm, $scaleUpPolicyTitleForm,
          scaleDownPolicyTitleForm, $scaleDownPolicyTitleForm;

      // Create and append top fields
      // -- uses create form to generate fields
      topFieldForm = cloudStack.dialog.createForm({
        context: context,
        noDialog: true, // Don't render a dialog, just return $formContainer
        form: {
          title: '',
          fields: topfields
        }
      });
      $topFieldForm = topFieldForm.$formContainer;
      $topFieldForm.appendTo($topFields);

      scaleUpPolicyTitleForm = cloudStack.dialog.createForm({
      	context: context,
      	noDialog: true,
      	form: {
    			title: '',
    			fields: {scaleUpDuration: { label: 'Duration', validation: { required: true } } }
      	}
      });
			$scaleUpPolicyTitleForm = scaleUpPolicyTitleForm.$formContainer;
			$scaleUpPolicyTitleForm.appendTo($scaleUpPolicyTitle);
			
     
      scaleDownPolicyTitleForm = cloudStack.dialog.createForm({
      	context: context,
      	noDialog: true,
      	form: {
    			title: '',
    			fields: {scaleDownDuration: { label: 'Duration', validation: { required: true } } }
      	}
      });
			$scaleDownPolicyTitleForm = scaleDownPolicyTitleForm.$formContainer;
			$scaleDownPolicyTitleForm.appendTo($scaleDownPolicyTitle)
			
      // Make multi-edits
      // $scaleUpPolicy.multiEdit(...)
      scaleUpPolicyForm = $scaleUpPolicy.multiEdit(
        $.extend(true, {}, scaleuppolicy, { context: context }));
      // $scaleDownPolicy.multiEdit(...)
      scaleDownPolicyForm = $scaleDownPolicy.multiEdit(
        $.extend(true, {}, scaledownpolicy, { context: context }));

      // Create and append bottom fields
      bottomFieldForm = cloudStack.dialog.createForm({
        context: context,
        noDialog: true, // Don't render a dialog, just return $formContainer
        form: {
          title: '',
          fields: bottomfields 
        }
      });
      $bottomFieldForm = bottomFieldForm.$formContainer;
      $bottomFieldForm.appendTo($bottomFields);

      // Append main div elements
      $autoscalerDialog.append(
        $topFields,
        $scaleUpPolicyTitle,
        $scaleUpPolicy,
        $scaleDownPolicyTitle,
        $scaleDownPolicy,
        $bottomFields
      );

      // Render dialog
      $autoscalerDialog.dialog({
          title: 'AutoScale Configuration Wizard',
          width: 825,
          height: 'auto',
          draggable: true,
          closeonEscape: false,
          overflow:'auto',
          open:function() {
              $("button").each(function(){
                            $(this).attr("style", "left: 600px; position: relative; margin-right: 5px; "); 
                        });

            },
            buttons: [
              {
                text: _l('label.cancel'),
                'class': 'cancel',
                click: function() {
                  $(this).dialog('close');
                  $('.overlay').remove(); 
                }
              },
              {
                text: _l('Apply'),
                'class': 'ok',
                click: function() {
              		var data = cloudStack.serializeForm($('form'));
              		cloudStack.autoscaler.actions.add({data: data,context: context});
                  $autoscalerDialog.dialog('close');
                  $('.overlay').remove();
                  $autoscalerDialog.closest(':ui-dialog').remove();
                }
              }
            ]
        }).closest('.ui-dialog').overlay();
         $('.ui-dialog div.autoscaler div.form-container').find('.form-item[rel=templateNames] label').hide();

         /* Duration Fields*/
         //$('div.ui-dialog div.autoscaler').find('div.scale-up-policy-title').append("<br></br>").append($inputLabel = $('<label>').html('Duration').attr({left:'200'})).append($('<input>').attr({ name: 'username' }));
         //$('div.ui-dialog div.autoscaler').find('div.scale-down-policy-title').append("<br></br>").append($inputLabel = $('<label>').html('Duration').attr({left:'200'})).append($('<input>').attr({ name: 'username' }));
        
         /*Dividers*/
         $('div.ui-dialog div.autoscaler').find('div.scale-up-policy-title').prepend($scaleUpDivider);
         $('div.ui-dialog div.autoscaler').find('div.scale-down-policy-title').prepend($scaleDownDivider);
         $('div.ui-dialog div.autoscaler').find('div.field-group.bottom-fields').prepend($bottomFieldDivider);
          
         /* Hide effects for multi-edit table*/
         $('div.ui-dialog div.autoscaler div.scale-up-policy').prepend($hideScaleUp);
         $('div.ui-dialog div.autoscaler div.scale-down-policy ').prepend($hideScaleDown);
         $('div.ui-dialog div.autoscaler div.scale-up-policy').prepend($scaleUpHideLabel);
         $('div.ui-dialog div.autoscaler div.scale-down-policy').prepend($scaleDownHideLabel);

         /*Toggling the labels and data-item table - SCALE UP POLICY*/
         $('div.ui-dialog div.autoscaler div.scale-up-policy div.hide').click(function() { 
             $('div.ui-dialog div.autoscaler div.scale-up-policy div.multi-edit div.data div.data-item').slideToggle(); 
             $scaleUpLabel = $('div.ui-dialog div.autoscaler div.scale-up-policy div.slide-label').replaceWith($scaleUpLabel);

          });
           
          /*Toggling the images */
          $('div.ui-dialog div.autoscaler div.scale-up-policy div.hide').click(function() {
                    $(this).toggleClass('expand hide');
              }); 
                    
           $('div.ui-dialog div.autoscaler div.scale-down-policy div.hide').click(function() {
                    $(this).toggleClass('expand hide');
              });
 
          /*Toggling the labels and data-item table - SCALE DOWN POLICY*/
             $('div.ui-dialog div.autoscaler div.scale-down-policy div.hide').click(function() {
              $('div.ui-dialog div.autoscaler div.scale-down-policy div.multi-edit div.data div.data-item').slideToggle();
              $scaleDownLabel = $('div.ui-dialog div.autoscaler div.scale-down-policy div.slide-label').replaceWith($scaleDownLabel);
          });
        
          $('div.ui-dialog div.autoscaler div.scale-down-policy div.multi-edit div.data div.expand').click(function() { $('div.ui-dialog div.autoscaler div.scale-down-policy div.multi-edit div.data div.data-item').slideToggle(); });
       
      }
    }
}(jQuery, cloudStack));

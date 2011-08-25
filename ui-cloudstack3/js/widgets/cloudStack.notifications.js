(function($) {
  /**
   * Notification handling
   */
  var notifications = {
    activeTasks: [],
    cornerAlert: function(args) {
      var $cornerAlert = $('<div>').addClass('notification corner-alert')
            .hide()
            .appendTo('html body')
            .append(
              $('<div>').addClass('title').append(
                $('<span>').html('Task completed')
              )
            )
            .append(
              $('<div>').addClass('message')
                .append(
                  $('<span>').html(args.message)                  
                )
            );

      $cornerAlert
        .fadeIn()
        .css({
          position: 'absolute',
          top: $(window).height(),
          left: $(window).width() - $cornerAlert.width()
        })
        .animate({
          top: $(window).height() - $cornerAlert.height()
        }, {
          complete: function() {
            setTimeout(function() {
              $cornerAlert.fadeOut('fast', function() {
                $cornerAlert.remove();
              });
            }, 5000);            
          }
        });
    },
    add: function(args, $popup, $total) {
      var currentTotal = parseInt($total.html());
      var newTotal = currentTotal + 1;
      var desc = args.desc;
      
      var $item = $('<li>')
            .append(
              $('<span>').html(args.desc)
            )
            .append(
              $('<div>').addClass('remove')
            );

      $popup.find('ul').append($item);
      $total.html(newTotal);
      $total.parent().addClass('pending');
      $item.addClass('pending');

      // Setup timer
      var pollTimer = setInterval(function() {
        args.poll({
          complete: function(args) {
            clearInterval(pollTimer);

            notifications.cornerAlert({ message: desc });
            notifications.activeTasks.pop(pollTimer);
            $item.removeClass('pending');

            if (!notifications.activeTasks.length) {
              $total.parent().removeClass('pending');
            }
          },
          incomplete: function(args) {}
        });
      }, args.interval);
      notifications.activeTasks.push(pollTimer);

      return $total;
    },
    removeItem: function($popup, $item) {
      if ($item.closest('li').hasClass('pending')) return false;

      var $total = $popup.data('notifications-attach-to').find('div.total span');
      $item.remove();
      $total.html(parseInt($total.html()) - 1);
      
      return true;
    },
    clear: function($popup) {
      $popup.find('ul li').each(function() {
        var $item = $(this);

        if (!$item.hasClass('pending')) {
          notifications.removeItem($popup, $item);
        }
      });
    },
    popup: {
      create: function($attachTo) {
        var $popup = $('<div>')
              .addClass('notification-box')
              .append(
                // Header
                $('<h3>').html('Notifications')
              )
              .append(
                // Container
                $('<div>').addClass('container')
                  .append(
                    // Notification list
                    $('<ul>')
                  )
              )
              .append(
                // Buttons
                $('<div>').addClass('buttons')
                  .append(
                    // Clear list
                    $('<div>').addClass('button clear-list')
                  )
                  .append(
                    $('<div>').addClass('button close')
                  )
              )
              .css({ position: 'absolute' })
              .data('notifications-attach-to', $attachTo)
              .hide();

        if (!$attachTo.hasClass('notifications')) $attachTo.addClass('notifications');
        $attachTo.data('notifications-popup', $popup);

        return $popup;
      },
      show: function($popup, $attachTo) {
        return notifications.popup.reposition($popup, $attachTo)
          .overlay({
            closeAction: function() {
              notifications.popup.hide($popup);
            }
          })
          .fadeIn();
      },
      hide: function($popup) {
        $popup.fadeOut();      
      },
      reposition: function($popup, $attachTo) {
        return $popup
          .css({
            zIndex: 10000,
            top: $attachTo.offset().top + $attachTo.height() + 10,
            left: $attachTo.offset().left - $attachTo.width()
          });        
      }
    }
  };

  $.fn.notifications = function(method, args) {
    var $attachTo = this;
    var $total = $attachTo.find('div.total span');
    var $popup;

    var init = function() {
      $popup = notifications.popup.create($attachTo).appendTo('html body');
    };

    if (method == 'add')
      notifications.add(args, $attachTo.data('notifications-popup'), $total);
    else
      init();

    return this;
  };

  // Events
  $(document).click(function(event) {
    var $target = $(event.target);
    var $attachTo, $popup;

    // Notifications header area
    if ($target.closest('.notifications').size()) {
      $attachTo = $target.closest('.notifications');
      $popup = $attachTo.data('notifications-popup');
      notifications.popup.show($popup, $attachTo);

      return false;
    }

    // Popup
    if ($target.closest('div.notification-box').size()) {
      $popup = $target.closest('div.notification-box');

      // Clear list
      if ($target.hasClass('button clear-list'))
        notifications.clear($popup);
      // Remove instance item
      else if ($target.hasClass('remove'))
        notifications.removeItem($popup, $target.closest('li'));
      // Close button
      else if ($target.hasClass('button close'))
        $('div.overlay').click();

      return false;
    }

    return true;
  });

  $(window).resize(function(event) {
    var $popup = $('div.notification-box:visible');

    if ($popup.size())
      notifications.popup.reposition($popup, $popup.data('notifications-attach-to'));
  });
})(window.jQuery);

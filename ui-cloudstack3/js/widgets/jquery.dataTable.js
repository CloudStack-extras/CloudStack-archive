(function($) {
  /**
   * Convert table to be resizable and sortable
   *
   * @author Brian Federle
   */
  $.fn.dataTable = function(method) {
    var $table = this;

    /**
     * Check if position is in 'resize zone'
     *
     * @return boolean, true if position is within bounds
     */
    var withinResizeBounds = function($elem, posX) {
      var leftBound = $elem.offset().left + $elem.width() / 1.2;

      return posX > leftBound;
    };

    /**
     * Handles actual resizing of table headers
     */
    var resizeDragEvent = function(event) {
      var $elem = $(this);

      if (event.type == 'mousedown') {
        $elem.addClass('dragging');

        return false;
      } else if (event.type == 'mouseup') {
        $table.find('th').removeClass('dragging');

        return false;
      }

      var isDraggable = $elem.hasClass('dragging');

      if (!isDraggable) {
        return false;
      }

      var columnIndex = $elem.index();

      // Get all TDs from column
      var columnCells = [$elem];
      $table.find('thead tr').each(function() {
        var targetCell = $($(this).find('th')[columnIndex]);

        columnCells.push(targetCell);
      });

      var tolerance = 25;
      var targetWidth = event.pageX - $elem.offset().left + tolerance;
      $(columnCells).each(function() {
        $(this).css({
          width: targetWidth
        });
      });

      return true;
    };

    /**
     * Event to set resizable appearance on hover
     */
    var hoverResizableEvent = function(event) {
      var $elem = $(this);
      var posX = event.pageX;

      if (event.type != 'mouseout' && withinResizeBounds($elem, posX)) {
        $elem.addClass('resizable');
      } else {
        $elem.removeClass('resizable');
      }

      return true;
    }

    /**
     * Make row at specified index selected or unselected
     *
     * @param rowIndex Row's index, starting at 1
     */
    var toggleSelectRow = function(rowIndex) {
      var $rows = $table.find('tbody tr');
      var $row = $($rows[rowIndex]);

      $rows.filter(
        function() {
          return this != $row[0];
        }).removeClass('selected');
      return $row.toggleClass('selected');
    };

    var computeEvenOddRows = function() {
      var currentRowType = 'even';
      $table.find('tbody tr').each(function() {
        var $row = $(this);

        $row.removeClass('even').removeClass('odd');
        $row.addClass(currentRowType);

        if (currentRowType == 'even') currentRowType = 'odd';
        else currentRowType = 'even';
      });
    };

    /**
     * Sort table by column
     *
     * @param columnIndex Index of column (starting at 0) to sort by
     */
    var sortTable = function(columnIndex) {
      var direction = 'asc';

      if ($table.find('thead th').hasClass('sorted ' + direction)) {
        direction = 'desc';
      }

      $table.find('thead th').removeClass('sorted desc asc');
      $($table.find('thead th')[columnIndex]).addClass('sorted').addClass(direction);

      var $elems = $table.find('tbody td').filter(function() {
        return $(this).index() == columnIndex;
      });

      var sortData = [];
      $elems.each(function() {
        sortData.push($(this).html());
        sortData.sort();

        if (direction == 'asc') {
          sortData.reverse();
        }
      });

      $(sortData).each(function() {
        var sortKey = this;
        var $targetCell = $elems.filter(function() {
          return $(this).html() == sortKey;
        });
        var $targetContainer = $targetCell.parent();

        $targetContainer.remove().appendTo($table.find('tbody'));
      });

      computeEvenOddRows();
    };

    var methods = {
      removeRow: function(rowIndex) {
        var $row = $($table.find('tbody tr')[rowIndex]);

        $row.fadeOut(function() {
          $row.remove();
          computeEvenOddRows();
        });

        return $row;
      }
    };

    var init = function() {
      $table.find('th').bind('mousemove mouseout', hoverResizableEvent);
      $table.find('th').bind('mousedown mousemove mouseup mouseout', resizeDragEvent);
      $table.find('th').bind('click', function(event) {
        if ($(this).hasClass('resizable')) {
          return false;
        }

        sortTable($(event.target).index());
      });

      $table.find('tbody tr').bind('click', function(event) {
        var rowIndex = $(this).index();

        toggleSelectRow(rowIndex);
      });

      computeEvenOddRows();

      $('th, td').width(120);
    };

    if (methods[method]) {
      return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (!method) {
      init();
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.dataTable');
    }
  };
}(jQuery));

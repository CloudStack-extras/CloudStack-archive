(function($, cloudStack) {
  var sectionPreFilter = cloudStack.sectionPreFilter;

  // You have to override this in order for the new nav item
  // to display
  cloudStack.sectionPreFilter = function(args) {
    var preFilter = sectionPreFilter(args);

    preFilter.push('customGroups');
    
    return preFilter;
  };
  
  // This adds a new section to the navigation bar
  cloudStack.sections.customGroups = {
    id: 'custom-groups',
    title: 'Groups', // The text that appears in the left nav bar

    // This defines a table view
    //
    // You can change the displayed columns and how the data is retrived,
    // as well as the actions performed.
    listView: {
      id: 'customGroups',

      // The columns displayed in the table
      fields: {
        uuid: { label: 'UUID' },
        ipaddress: { label: 'IP Address' }

        // ... add more fields that you need here
      },

      // The actions listed here will display in a separate column,
      // to the right of each row
      actions: {
        viewConsole: {
          label: 'Launch Group',
          action: {
            externalLink: {
              title: function() {
                return 'External link'; // What you want the new window's title to be
              },
              
              // This will launch a new browser window,
              // to whichever URL string that is returned
              url: function(args) {
                // All selected item data is stored in this variable
                var group = args.context.customGroups[0];

                // Fields are stored in the object, under the same name
                // as the key specified in 'fields'
                var uuid = group.uuid;
                var ipaddress = group.ipaddress;
                
                var url = 'http://www.google.com/';

                return url;
              },

              // Dimensions of the browser window (in pixels)
              width: 800,
              height: 600
            }
          }
        }
      },

      // This is called to retrieve all data
      //
      // It runs asynchonously; when you have received your data
      // from an ajax call, etc. then just call args.response.success,
      // and pass your data there
      //
      //
      // -- See example of hard-coded data below
      dataProvider: function(args) {
        args.response.success({
          data: [
            // Item 1
            {
              // Fields passed in the object should be named the same
              // as the keys in 'fields' declared above
              uuid: '1234',
              ipaddress: '10.0.0.1'
            },

            // Item 2
            {
              uuid: '4567',
              ipaddress: '10.0.0.2'
            },

            // Item 3
            {
              uuid: '8910',
              ipaddress: '10.0.0.3'
            }

            // ...etc.
          ]
        });
      }
    }
  };
}(jQuery, cloudStack));
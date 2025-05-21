// Call the dataTables jQuery plugin
$(document).ready(function() {
  console.log('📋 Initializing DataTables plugin');
  
  try {
    const table = $('#dataTable');
    
    if (table.length === 0) {
      console.error('❌ DataTable element not found in DOM (#dataTable)');
      return;
    }
    
    console.log(`✅ DataTable element found with ${table.find('tr').length - 1} rows of data`);
    
    // Initialize DataTable with logging callbacks
    const dataTable = table.DataTable({
      initComplete: function(settings, json) {
        console.log('✅ DataTable successfully initialized');
        console.log(`📊 DataTable contains ${this.api().rows().count()} rows`);
      },
      drawCallback: function(settings) {
        console.log(`🔄 DataTable redraw - showing page ${this.api().page.info().page + 1} of ${this.api().page.info().pages}`);
      },
      rowCallback: function(row, data, index) {
        // Log row creation but limit to prevent console flooding
        if (index < 5) {
          console.log(`📝 DataTable row ${index} processed`);
        } else if (index === 5) {
          console.log(`... additional rows processed (limiting log output)`);
        }
      }
    });
    
    // Log search events
    table.on('search.dt', function() {
      const searchTerm = dataTable.search();
      if (searchTerm) {
        console.log(`🔍 DataTable search performed: "${searchTerm}"`);
        console.log(`📊 Search results: ${dataTable.rows({search: 'applied'}).count()} matching rows`);
      }
    });
    
    // Log page change events
    table.on('page.dt', function() {
      console.log(`📄 DataTable page changed to ${dataTable.page.info().page + 1}`);
    });
    
    // Log length change events (rows per page)
    table.on('length.dt', function(e, settings, len) {
      console.log(`👁️ DataTable rows per page changed to ${len}`);
    });
    
    // Log sort events
    table.on('order.dt', function() {
      const order = dataTable.order();
      const columnName = $(dataTable.column(order[0][0]).header()).text().trim();
      const direction = order[0][1];
      console.log(`🔀 DataTable sorted by "${columnName}" (${direction})`);
    });
    
  } catch (error) {
    console.error('❌ Error initializing DataTable:', error.message);
  }
});

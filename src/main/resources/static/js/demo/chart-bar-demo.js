// Bar Chart Example with Highcharts
console.log('📊 Initializing Bar Chart with Highcharts');

// Check if the chart container exists
var chartContainer = document.getElementById("myBarChart");
if (!chartContainer) {
  console.error('❌ Bar Chart container element not found in DOM');
} else {
  console.log('✅ Bar Chart container element found');

  console.log('📊 Creating Bar Chart with revenue data for 6 months');

  // Monthly data for the chart
  const months = ["January", "February", "March", "April", "May", "June"];
  const revenueData = [4215, 5312, 6251, 7841, 9821, 14984];

  // Create the Highcharts bar chart
  Highcharts.chart('myBarChart', {
    chart: {
      type: 'column',
      events: {
        load: function() {
          console.log('✅ Bar Chart loaded and rendered');
        },
        click: function(event) {
          console.log('👆 Bar Chart clicked (no specific bar)');
        }
      }
    },
    title: {
      text: null
    },
    xAxis: {
      categories: months,
      labels: {
        style: {
          fontFamily: 'Nunito, -apple-system, system-ui, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
          color: '#858796'
        }
      },
      tickmarkPlacement: 'on',
      title: {
        enabled: false
      },
      lineWidth: 0,
      gridLineWidth: 0
    },
    yAxis: {
      title: {
        text: null
      },
      labels: {
        style: {
          fontFamily: 'Nunito, -apple-system, system-ui, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
          color: '#858796'
        },
        formatter: function() {
          const formattedValue = '$' + Highcharts.numberFormat(this.value, 0);
          console.log(`💲 Y-axis tick formatted: ${formattedValue}`);
          return formattedValue;
        }
      },
      gridLineColor: 'rgb(234, 236, 244)',
      gridLineDashStyle: 'Dash',
      gridLineWidth: 1,
      min: 0,
      max: 15000,
      tickAmount: 5
    },
    tooltip: {
      backgroundColor: 'rgb(255, 255, 255)',
      borderColor: '#dddfeb',
      borderWidth: 1,
      style: {
        fontFamily: 'Nunito, -apple-system, system-ui, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
        color: '#858796'
      },
      formatter: function() {
        const formattedLabel = 'Revenue: $' + Highcharts.numberFormat(this.y, 0);
        console.log(`🔍 Bar Chart tooltip displayed: ${formattedLabel} for ${this.x}`);
        return `<b>${this.x}</b><br>${formattedLabel}`;
      }
    },
    plotOptions: {
      column: {
        pointPadding: 0.2,
        borderWidth: 0,
        maxPointWidth: 25,
        color: '#4e73df',
        states: {
          hover: {
            color: '#2e59d9'
          }
        },
        events: {
          mouseOver: function(event) {
            const point = event.target;
            console.log(`👆 Bar Chart hover: ${point.category} (${point.y})`);
          },
          click: function(event) {
            const point = event.point;
            console.log(`👆 Bar Chart bar clicked: ${point.category} (${point.y})`);
          }
        }
      }
    },
    series: [{
      name: 'Revenue',
      data: revenueData,
      showInLegend: false
    }],
    credits: {
      enabled: false
    }
  });

  console.log('✅ Bar Chart successfully initialized with Highcharts');
}

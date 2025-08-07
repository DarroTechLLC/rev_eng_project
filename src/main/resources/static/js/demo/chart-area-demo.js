// Area Chart Example with Highcharts
console.log('📊 Initializing Area Chart with Highcharts');

// Check if the chart container exists
var chartContainer = document.getElementById("myAreaChart");
if (!chartContainer) {
  console.error('❌ Area Chart container element not found in DOM');
} else {
  console.log('✅ Area Chart container element found');

  console.log('📊 Creating Area Chart with sample earnings data');

  // Monthly data for the chart
  const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
  const earningsData = [0, 10000, 5000, 15000, 10000, 20000, 15000, 25000, 20000, 30000, 25000, 40000];

  // Create the Highcharts area chart
  Highcharts.chart('myAreaChart', {
    chart: {
      type: 'area',
      events: {
        load: function() {
          console.log('✅ Area Chart loaded and rendered');
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
      max: 6 // Show only 7 labels (0-6)
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
      max: null
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
        const formattedLabel = 'Earnings: $' + Highcharts.numberFormat(this.y, 0);
        console.log(`🔍 Tooltip displayed: ${formattedLabel}`);
        return formattedLabel;
      }
    },
    plotOptions: {
      area: {
        fillOpacity: 0.05,
        marker: {
          radius: 3,
          fillColor: 'rgba(78, 115, 223, 1)',
          lineWidth: 2,
          lineColor: 'rgba(78, 115, 223, 1)',
          states: {
            hover: {
              radius: 4
            }
          }
        },
        lineWidth: 2,
        states: {
          hover: {
            lineWidth: 3
          }
        },
        threshold: null,
        events: {
          mouseOver: function() {
            console.log('👆 Mouse over area chart');
          }
        }
      }
    },
    series: [{
      name: 'Earnings',
      color: 'rgba(78, 115, 223, 1)',
      data: earningsData,
      showInLegend: false
    }],
    credits: {
      enabled: false
    }
  });

  console.log('✅ Area Chart successfully initialized with Highcharts');
}

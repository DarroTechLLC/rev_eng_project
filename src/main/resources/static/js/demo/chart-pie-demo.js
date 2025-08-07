// Pie Chart Example with Highcharts
console.log('🥧 Initializing Pie Chart with Highcharts');

// Check if the chart container exists
var chartContainer = document.getElementById("myPieChart");
if (!chartContainer) {
  console.error('❌ Pie Chart container element not found in DOM');
} else {
  console.log('✅ Pie Chart container element found');

  console.log('📊 Initializing Pie Chart with data [Direct: 55%, Referral: 30%, Social: 15%]');

  // Data for the pie chart
  const pieData = [
    {
      name: 'Direct',
      y: 55,
      color: '#4e73df',
      sliced: false,
      selected: false
    },
    {
      name: 'Referral',
      y: 30,
      color: '#1cc88a',
      sliced: false,
      selected: false
    },
    {
      name: 'Social',
      y: 15,
      color: '#36b9cc',
      sliced: false,
      selected: false
    }
  ];

  // Create the Highcharts pie chart
  Highcharts.chart('myPieChart', {
    chart: {
      type: 'pie',
      events: {
        load: function() {
          console.log('✅ Pie Chart loaded and rendered');
        }
      }
    },
    title: {
      text: null
    },
    tooltip: {
      backgroundColor: 'rgb(255, 255, 255)',
      borderColor: '#dddfeb',
      borderWidth: 1,
      style: {
        fontFamily: 'Nunito, -apple-system, system-ui, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
        color: '#858796'
      },
      pointFormat: '{point.name}: {point.y}%',
      formatter: function() {
        const formattedLabel = `${this.point.name}: ${this.point.y}%`;
        console.log(`🔍 Pie Chart tooltip displayed: ${formattedLabel}`);
        return formattedLabel;
      }
    },
    plotOptions: {
      pie: {
        innerSize: '80%', // Equivalent to cutoutPercentage: 80 in Chart.js
        depth: 45,
        dataLabels: {
          enabled: false
        },
        borderWidth: 0,
        states: {
          hover: {
            brightness: -0.1
          }
        },
        point: {
          events: {
            mouseOver: function() {
              console.log(`👆 Pie Chart segment hover: ${this.name} (${this.y}%)`);
            },
            click: function() {
              console.log(`👆 Pie Chart segment clicked: ${this.name} (${this.y}%)`);
            }
          }
        }
      }
    },
    series: [{
      name: 'Traffic Source',
      colorByPoint: true,
      data: pieData
    }],
    credits: {
      enabled: false
    }
  });

  console.log('✅ Pie Chart successfully initialized with Highcharts');
}

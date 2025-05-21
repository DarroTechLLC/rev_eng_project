// Set new default font family and font color to mimic Bootstrap's default styling
console.log('🥧 Initializing Pie Chart with custom font settings');
Chart.defaults.global.defaultFontFamily = 'Nunito', '-apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
Chart.defaults.global.defaultFontColor = '#858796';
console.log('✅ Pie Chart global font settings applied');

// Pie Chart Example
console.log('🥧 Creating Pie Chart');
var ctx = document.getElementById("myPieChart");
if (!ctx) {
  console.error('❌ Pie Chart canvas element not found in DOM');
} else {
  console.log('✅ Pie Chart canvas element found');
  
  console.log('📊 Initializing Pie Chart with data [Direct: 55%, Referral: 30%, Social: 15%]');
  var myPieChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ["Direct", "Referral", "Social"],
      datasets: [{
        data: [55, 30, 15],
        backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc'],
        hoverBackgroundColor: ['#2e59d9', '#17a673', '#2c9faf'],
        hoverBorderColor: "rgba(234, 236, 244, 1)",
      }],
    },
    options: {
      maintainAspectRatio: false,
      tooltips: {
        backgroundColor: "rgb(255,255,255)",
        bodyFontColor: "#858796",
        borderColor: '#dddfeb',
        borderWidth: 1,
        xPadding: 15,
        yPadding: 15,
        displayColors: false,
        caretPadding: 10,
        callbacks: {
          label: function(tooltipItem, data) {
            const label = data.labels[tooltipItem.index];
            const value = data.datasets[0].data[tooltipItem.index];
            const formattedLabel = `${label}: ${value}%`;
            console.log(`🔍 Pie Chart tooltip displayed: ${formattedLabel}`);
            return formattedLabel;
          }
        }
      },
      legend: {
        display: false
      },
      cutoutPercentage: 80,
      // Log animation completion
      animation: {
        onComplete: function() {
          console.log('✅ Pie Chart animation completed');
        }
      },
      // Log when hover events occur
      onHover: function(event, elements) {
        if (elements && elements.length) {
          const index = elements[0]._index;
          const label = this.data.labels[index];
          const value = this.data.datasets[0].data[index];
          console.log(`👆 Pie Chart segment hover: ${label} (${value}%)`);
        }
      },
      // Log when click events occur
      onClick: function(event, elements) {
        if (elements && elements.length) {
          const index = elements[0]._index;
          const label = this.data.labels[index];
          const value = this.data.datasets[0].data[index];
          console.log(`👆 Pie Chart segment clicked: ${label} (${value}%)`);
        }
      }
    },
  });
  
  console.log('✅ Pie Chart successfully initialized');
}

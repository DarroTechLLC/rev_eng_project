// Set new default font family and font color to mimic Bootstrap's default styling
console.log('📊 Initializing Bar Chart with custom font settings');
Chart.defaults.global.defaultFontFamily = 'Nunito', '-apple-system,system-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';
Chart.defaults.global.defaultFontColor = '#858796';
console.log('✅ Bar Chart global font settings applied');

function number_format(number, decimals, dec_point, thousands_sep) {
  // *     example: number_format(1234.56, 2, ',', ' ');
  // *     return: '1 234,56'
  console.log(`🔢 Formatting number: ${number} with ${decimals} decimals`);
  number = (number + '').replace(',', '').replace(' ', '');
  var n = !isFinite(+number) ? 0 : +number,
    prec = !isFinite(+decimals) ? 0 : Math.abs(decimals),
    sep = (typeof thousands_sep === 'undefined') ? ',' : thousands_sep,
    dec = (typeof dec_point === 'undefined') ? '.' : dec_point,
    s = '',
    toFixedFix = function(n, prec) {
      var k = Math.pow(10, prec);
      return '' + Math.round(n * k) / k;
    };
  // Fix for IE parseFloat(0.55).toFixed(0) = 0;
  s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
  if (s[0].length > 3) {
    s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep);
  }
  if ((s[1] || '').length < prec) {
    s[1] = s[1] || '';
    s[1] += new Array(prec - s[1].length + 1).join('0');
  }
  const result = s.join(dec);
  console.log(`✅ Number formatted: ${result}`);
  return result;
}

// Bar Chart Example
console.log('📊 Creating Bar Chart');
var ctx = document.getElementById("myBarChart");
if (!ctx) {
  console.error('❌ Bar Chart canvas element not found in DOM');
} else {
  console.log('✅ Bar Chart canvas element found');
  
  console.log('📊 Initializing Bar Chart with revenue data for 6 months');
  var myBarChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ["January", "February", "March", "April", "May", "June"],
      datasets: [{
        label: "Revenue",
        backgroundColor: "#4e73df",
        hoverBackgroundColor: "#2e59d9",
        borderColor: "#4e73df",
        data: [4215, 5312, 6251, 7841, 9821, 14984],
      }],
    },
    options: {
      maintainAspectRatio: false,
      layout: {
        padding: {
          left: 10,
          right: 25,
          top: 25,
          bottom: 0
        }
      },
      scales: {
        xAxes: [{
          time: {
            unit: 'month'
          },
          gridLines: {
            display: false,
            drawBorder: false
          },
          ticks: {
            maxTicksLimit: 6
          },
          maxBarThickness: 25,
        }],
        yAxes: [{
          ticks: {
            min: 0,
            max: 15000,
            maxTicksLimit: 5,
            padding: 10,
            // Include a dollar sign in the ticks
            callback: function(value, index, values) {
              const formattedValue = '$' + number_format(value);
              console.log(`💲 Y-axis tick formatted: ${formattedValue}`);
              return formattedValue;
            }
          },
          gridLines: {
            color: "rgb(234, 236, 244)",
            zeroLineColor: "rgb(234, 236, 244)",
            drawBorder: false,
            borderDash: [2],
            zeroLineBorderDash: [2]
          }
        }],
      },
      legend: {
        display: false
      },
      tooltips: {
        titleMarginBottom: 10,
        titleFontColor: '#6e707e',
        titleFontSize: 14,
        backgroundColor: "rgb(255,255,255)",
        bodyFontColor: "#858796",
        borderColor: '#dddfeb',
        borderWidth: 1,
        xPadding: 15,
        yPadding: 15,
        displayColors: false,
        caretPadding: 10,
        callbacks: {
          label: function(tooltipItem, chart) {
            var datasetLabel = chart.datasets[tooltipItem.datasetIndex].label || '';
            const formattedLabel = datasetLabel + ': $' + number_format(tooltipItem.yLabel);
            console.log(`🔍 Bar Chart tooltip displayed: ${formattedLabel} for ${chart.labels[tooltipItem.index]}`);
            return formattedLabel;
          },
          title: function(tooltipItems, data) {
            const title = data.labels[tooltipItems[0].index];
            console.log(`🔍 Bar Chart tooltip title: ${title}`);
            return title;
          }
        }
      },
      // Log animation completion
      animation: {
        onComplete: function() {
          console.log('✅ Bar Chart animation completed');
        }
      },
      // Log when hover events occur
      onHover: function(event, elements) {
        if (elements && elements.length) {
          const index = elements[0]._index;
          const label = this.data.labels[index];
          const value = this.data.datasets[0].data[index];
          console.log(`👆 Bar Chart hover: ${label} (${value})`);
        }
      },
      // Log when click events occur
      onClick: function(event, elements) {
        if (elements && elements.length) {
          const index = elements[0]._index;
          const label = this.data.labels[index];
          const value = this.data.datasets[0].data[index];
          console.log(`👆 Bar Chart bar clicked: ${label} (${value})`);
        } else {
          console.log('👆 Bar Chart clicked (no specific bar)');
        }
      }
    }
  });
  
  console.log('✅ Bar Chart successfully initialized');
}

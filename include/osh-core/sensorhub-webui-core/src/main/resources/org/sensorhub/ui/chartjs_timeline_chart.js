var config = {
  type: 'bar',
  data: {
    datasets: [{
      label: 'Record Counts',
      backgroundColor: '#197de1aa',
      data: data,
      type: 'bar',
      borderWidth: 0
    }]
  },
  options: {
    maintainAspectRatio: false,
    legend: {
      display: false
    },
    scales: {
      xAxes: [{
        type: 'time',
        //offset: true,
        time: {
            displayFormats: {
                day: 'YYYY-MM-DD',
                hour: "HH:mm[Z]",
                minute: "HH:mm[Z]",
                second: 'HH:mm:ss[Z]'
            }
        },
        distribution: 'linear',
        barPercentage: 1.0,
        categoryPercentage: 0.8,
        ticks: {
          source: 'auto',
          autoSkip: true,
          maxRotation: 0,
          callback: insertDayTicks
        },
        gridLines: {
          display: false,
          tickMarkLength: 5
        }
      }],
      yAxes: [{
        display: false,
        ticks: {
          beginAtZero: true
        }
      }]
    },
    tooltips: {
      intersect: false,
      mode: 'index',
      callbacks: {
        label: function(tooltipItem, myData) {
          var label = myData.datasets[tooltipItem.datasetIndex].label || '';
          if (label) {
            label += ': ';
          }
          label += parseFloat(tooltipItem.value).toFixed(2);
          return label;
        }
      }
    }
  }
}

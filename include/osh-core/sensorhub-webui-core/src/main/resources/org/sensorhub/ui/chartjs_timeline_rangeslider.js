var config = {
  type: 'line',
  data: {
    datasets: [{
      backgroundColor: '#ccccccaa',
      borderColor: '#222222aa',
      data: data,
      type: 'bar',
      fill: false,
      borderWidth: 1
    }]
  },
  options: {
    maintainAspectRatio: false,
    layout: {
      padding: {
        top: 5
      }
    },
    legend: {
      display: false
    },
    tooltips: {
      enabled: false
    },
    scales: {
      xAxes: [{
        type: 'time',
        bounds: 'data',
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
          //display: false,
          source: 'auto',
          autoSkip: true,
          maxRotation: 0,
          callback: insertDayTicks
        },
        gridLines: {
          display: true,
          tickMarkLength: 8
        }
      }],
      yAxes: [{
        bounds: 'data',
        display: false,
        ticks: {
          beginAtZero: true
        }
      }]
    }
  }
}
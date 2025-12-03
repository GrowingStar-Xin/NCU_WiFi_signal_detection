export default function initOption(geoCoordMap, ZSData) {
  var convertData = function(data) {


    var res1 = []
    var maxvalue=0
    for (var i = 0; i < data.length; i++) {
      var dataItem = data[i]
      var fromCoord = geoCoordMap[dataItem[0].name]
      var toCoord = geoCoordMap[dataItem[1].name]
      var value= dataItem[1].value
      if (maxvalue<value) {
        maxvalue=value
      }
      // console.log(Math.ceil(value/5));
      if (fromCoord && toCoord) {
        res1.push({
          fromName: dataItem[0].name,
          toName: dataItem[1].name,
          coords: [fromCoord, toCoord],
          value:value==0?1:value
        })
       }
     }
    return {data:res1,maxvalue:maxvalue}
  }

  var series = []
  var sourcedata=convertData(ZSData)
  var data=sourcedata.data
  var maxval=sourcedata.maxvalue
  var color = ['#a6c84c', '#ffa022', '#46bee9'];
  var planePath ='path://M1705.06,1318.313v-89.254l-319.9-221.799l0.073-208.063c0.521-84.662-26.629-121.796-63.961-121.491c-37.332-0.305-64.482,36.829-63.961,121.491l0.073,208.063l-319.9,221.799v89.254l330.343-157.288l12.238,241.308l-134.449,92.931l0.531,42.034l175.125-42.917l175.125,42.917l0.531-42.034l-134.449-92.931l12.238-241.308L1705.06,1318.313z';
  // console.log("sourcedata",sourcedata);
  for (let i = 0; i < data.length; i++) {
    if (i==0) {
      series=[]
    }
    // console.log("index",i);
    series.push({
    name: data[i].fromName,//路线名称
    coordinateSystem: 'GLMap',
    type: 'lines',
    zlevel: 1,
    animation: true,
    effect: {
      show: true,//是否显示动点
      color:'#FF69B4',//icon颜色
      period: 3, //icon移动速度值越小移动越快
      trailLength: 0.1, //icon移动时的阴影
      symbol: planePath, //icon
      symbolSize:25 //icon大小
    },
    lineStyle: {
      normal: {
        //width: sourcedata[i].value/5 >6?6:sourcedata[i].value/5,
        width:3,//路径宽度
        color:'#FF69B4',//路径颜色
        opacity: 1,//路径透明度
        curveness: 0.2//路径弯曲度
      }
    },
    data: [data[i]]
    })
    
  }
  series.push( {
    name: '数据',
    type: 'effectScatter',
    coordinateSystem: 'GLMap',
    zlevel: 2,
    rippleEffect: {
      period: 3,//波纹动画速度速度值越小越快
      scale: 4,//起点与终点波纹大小
      brushType: 'fill'
    },
    label: {
      normal: {
        show: false
      }
    },
    itemStyle: {
      normal: {
        color: '#800080'//波纹颜色
      }
    },
    data: ZSData.map(function(dataItem) {
      return {
        name: dataItem[1].name,
        value: geoCoordMap[dataItem[1].name].concat([
          dataItem[1].value
        ])
      }
    })
  }, {
    name: '点位',
    type: 'effectScatter',
    coordinateSystem: 'GLMap',
    zlevel: 2,
    rippleEffect: {
      brushType: 'fill'
    },
    label: {
      normal: {
        show: false
      }
    },
    itemStyle: {
      normal: {
        color:'#FF69B4'//起点与终点颜色
      }
    },
    data: Object.keys(geoCoordMap).map(function(key) {
      return {
        name: key,
        value: geoCoordMap[key].concat([
          100
        ])
      }
    })
  })
  
  const option = {
    GLMap: {
      roam: true
    },
    coordinateSystem: 'GLMap',
    title: {
      show: false
    },
    tooltip: {
      trigger: 'item'//显示点与路线信息
    },
    legend: {
      show: false
    },
    geo: {
      map: 'GLMap',
      label: {
        emphasis: {
          show: false
        }
      },
      roam: true,
      itemStyle: {
        normal: {
          areaColor: '#323c48',
          borderColor: '#404a59'
        },
        emphasis: {
          areaColor: '#2a333d'
        }
      }
    },
    series: series
  }
  return option
}

<template>
  <div class="map-container">
    <div class="control-panel">
      <el-card>
        <h3>南昌大学前湖校区 WiFi轨迹可视化</h3>
        <p style="color: #666; font-size: 14px; margin-bottom: 15px;">仅显示校区范围内的轨迹数据</p>
        <el-select v-model="selectedTrack" placeholder="选择轨迹" @change="loadTrackData">
          <el-option
            v-for="track in trackList"
            :key="track.id"
            :label="track.name"
            :value="track.id"
          />
        </el-select>
        <el-button type="primary" @click="showAllTracks" style="margin-top: 10px;">
          显示所有轨迹
        </el-button>
        <el-button @click="clearTracks" style="margin-top: 10px;">
          清除轨迹
        </el-button>
      </el-card>
      
      <el-card style="margin-top: 20px;" v-if="trackList.length > 0">
        <h3>轨迹图例</h3>
        <div class="legend-item" v-for="(track, index) in trackList" :key="track.id">
          <div 
            class="legend-color" 
            :style="{ backgroundColor: getTrackColor(index) }"
          ></div>
          <span class="legend-text">{{ track.name }}</span>
        </div>
      </el-card>
    </div>
    <div id="map-container" class="map"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { loadTrackData as loadTrackDataAPI, getTrackList } from '@/utils/api'
import type { TrackData, TrackPoint } from '@/types/track'

const selectedTrack = ref('')
const trackList = ref<TrackData[]>([])
let map: any = null
let trackLayers: any[] = []

// 南昌大学前湖校区坐标
const NCU_CENTER = [115.8342, 28.6329]

onMounted(async () => {
  await nextTick()
  
  // 等待AMap API加载完成
  const waitForAMap = () => {
    return new Promise<void>((resolve) => {
      if ((window as any).AMap) {
        resolve()
      } else {
        const checkAMap = () => {
          if ((window as any).AMap) {
            resolve()
          } else {
            setTimeout(checkAMap, 100)
          }
        }
        checkAMap()
      }
    })
  }
  
  await waitForAMap()
  initMap()
  await loadTrackList()
})

const initMap = () => {
  // 检查AMap是否已加载
  if (!(window as any).AMap) {
    console.error('高德地图API未加载')
    return
  }
  
  // 初始化高德地图
  map = new (window as any).AMap.Map('map-container', {
    zoom: 17,
    center: NCU_CENTER,
    mapStyle: 'amap://styles/normal',
    // 限制地图范围在南昌大学前湖校区
    limitBounds: new (window as any).AMap.Bounds(
      [115.825, 28.625], // 西南角
      [115.845, 28.640]  // 东北角
    ),
    // 限制缩放级别
    zooms: [15, 19],
    // 禁用拖拽出边界
    dragEnable: true,
    // 禁用双击放大
    doubleClickZoom: false
  })
  
  // 添加地图控件
  try {
    if ((window as any).AMap.Scale) {
      const scale = new (window as any).AMap.Scale({
        position: 'LB'
      })
      map.addControl(scale)
    }
    
    // 简化工具栏，只保留缩放控件
    if ((window as any).AMap.ToolBar) {
      const toolBar = new (window as any).AMap.ToolBar({
        position: 'RT',
        ruler: false,
        direction: false,
        locate: false
      })
      map.addControl(toolBar)
    }
  } catch (error) {
    console.warn('地图控件加载失败:', error)
  }
  
  // 监听地图移动事件，确保不超出校区范围
  map.on('moveend', () => {
    const bounds = map.getBounds()
    const campusBounds = new (window as any).AMap.Bounds(
      [115.825, 28.625], // 西南角
      [115.845, 28.640]  // 东北角
    )
    
    if (!campusBounds.contains(bounds.getSouthWest()) || !campusBounds.contains(bounds.getNorthEast())) {
      map.setBounds(campusBounds)
    }
  })
}

const loadTrackList = async () => {
  try {
    const response = await getTrackList()
    console.log('原始轨迹列表数据:', response.data)
    
    // 过滤有效的轨迹数据
    const validTracks = response.data.filter(track => {
      if (!track.id || track.id.trim() === '') {
        console.warn('发现无效轨迹数据:', track)
        return false
      }
      return true
    })
    
    trackList.value = validTracks
    console.log('过滤后的轨迹列表:', validTracks)
  } catch (error) {
    ElMessage.error('加载轨迹列表失败')
    console.error(error)
  }
}

const loadTrackData = async () => {
  if (!selectedTrack.value) return
  
  try {
    const response = await loadTrackDataAPI(selectedTrack.value)
    const trackData = response.data
    drawTrack(trackData, selectedTrack.value, true)
    ElMessage.success('轨迹加载成功')
  } catch (error) {
    ElMessage.error('加载轨迹数据失败')
    console.error(error)
  }
}

const drawTrack = (trackData: TrackPoint[], trackId?: string, clearPrevious: boolean = true) => {
  if (!map) return
  
  // 根据参数决定是否清除之前的轨迹
  if (clearPrevious) {
    clearTracks()
  }
  
  // 为不同轨迹生成不同颜色
  const colors = ['#FF5722', '#2196F3', '#4CAF50', '#FF9800', '#9C27B0', '#607D8B']
  const colorIndex = trackList.value.findIndex(track => track.id === trackId) % colors.length
  const trackColor = colors[colorIndex] || '#3366FF'
  
  // 过滤有效的轨迹点（有经纬度的点）
  const validPoints = trackData.filter(point => 
    point.longitude && point.latitude && 
    !isNaN(point.longitude) && !isNaN(point.latitude)
  )
  
  if (validPoints.length === 0) {
    ElMessage.warning('该轨迹没有有效的坐标数据')
    return
  }
  
  // 创建轨迹线（不显示过多的点标记，只显示轨迹线）
  const path = validPoints.map(point => [point.longitude, point.latitude])
  const polyline = new (window as any).AMap.Polyline({
    path: path,
    strokeColor: trackColor,
    strokeWeight: 4,
    strokeOpacity: 0.8,
    lineJoin: 'round',
    lineCap: 'round'
  })
  
  // 添加起点和终点标记
  const startPoint = validPoints[0]
  const endPoint = validPoints[validPoints.length - 1]
  
  const startMarker = new (window as any).AMap.Marker({
    position: [startPoint.longitude, startPoint.latitude],
    title: '起点',
    icon: new (window as any).AMap.Icon({
      size: new (window as any).AMap.Size(32, 32),
      image: 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">
          <circle cx="16" cy="16" r="12" fill="${trackColor}" stroke="white" stroke-width="3"/>
          <text x="16" y="20" text-anchor="middle" fill="white" font-size="12" font-weight="bold">起</text>
        </svg>
      `)
    })
  })
  
  const endMarker = new (window as any).AMap.Marker({
    position: [endPoint.longitude, endPoint.latitude],
    title: '终点',
    icon: new (window as any).AMap.Icon({
      size: new (window as any).AMap.Size(32, 32),
      image: 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(`
        <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">
          <circle cx="16" cy="16" r="12" fill="${trackColor}" stroke="white" stroke-width="3"/>
          <text x="16" y="20" text-anchor="middle" fill="white" font-size="12" font-weight="bold">终</text>
        </svg>
      `)
    })
  })
  
  // 添加点击事件
  startMarker.on('click', () => {
    const infoWindow = new (window as any).AMap.InfoWindow({
      content: `
        <div>
          <h4>轨迹起点</h4>
          <p><strong>时间:</strong> ${startPoint.timestamp}</p>
          <p><strong>坐标:</strong> ${startPoint.longitude}, ${startPoint.latitude}</p>
          <p><strong>账号:</strong> ${startPoint.accountId || '未知'}</p>
        </div>
      `
    })
    infoWindow.open(map, startMarker.getPosition())
  })
  
  endMarker.on('click', () => {
    const infoWindow = new (window as any).AMap.InfoWindow({
      content: `
        <div>
          <h4>轨迹终点</h4>
          <p><strong>时间:</strong> ${endPoint.timestamp}</p>
          <p><strong>坐标:</strong> ${endPoint.longitude}, ${endPoint.latitude}</p>
          <p><strong>账号:</strong> ${endPoint.accountId || '未知'}</p>
        </div>
      `
    })
    infoWindow.open(map, endMarker.getPosition())
  })
  
  // 添加到地图
  const trackElements = [polyline, startMarker, endMarker]
  map.add(trackElements)
  trackLayers.push(...trackElements)
  
  // 调整地图视野，但限制在校区范围内
  if (clearPrevious && path.length > 0) {
    const campusBounds = new (window as any).AMap.Bounds(
      [115.825, 28.625], // 西南角
      [115.845, 28.640]  // 东北角
    )
    
    // 计算轨迹边界
    const trackBounds = new (window as any).AMap.Bounds()
    path.forEach(point => {
      trackBounds.extend(point)
    })
    
    // 如果轨迹在校区范围内，则适应轨迹；否则保持校区视野
    if (campusBounds.contains(trackBounds.getSouthWest()) && campusBounds.contains(trackBounds.getNorthEast())) {
      map.setFitView(trackLayers, false, [50, 50, 50, 50])
    } else {
      map.setBounds(campusBounds)
    }
  }
}

const showAllTracks = async () => {
  try {
    // 清除现有轨迹
    clearTracks()
    
    for (let i = 0; i < trackList.value.length; i++) {
      const track = trackList.value[i]
      
      // 验证trackId是否有效
      if (!track.id || track.id.trim() === '') {
        console.warn('跳过无效的轨迹ID:', track)
        continue
      }
      
      try {
        const response = await loadTrackDataAPI(track.id)
        const trackData = response.data
        // 第一个轨迹清除之前的，后续轨迹不清除
        drawTrack(trackData, track.id, false)
      } catch (error) {
        console.error(`加载轨迹 ${track.id} 失败:`, error)
        ElMessage.warning(`轨迹 ${track.name || track.id} 加载失败`)
      }
    }
    
    // 调整地图视野，确保在校区范围内
    if (trackLayers.length > 0) {
      const campusBounds = new (window as any).AMap.Bounds(
        [115.825, 28.625], // 西南角
        [115.845, 28.640]  // 东北角
      )
      
      // 尝试适应所有轨迹，但不超出校区边界
      try {
        map.setFitView(trackLayers, false, [50, 50, 50, 50])
        
        // 检查当前视野是否超出校区范围
        const currentBounds = map.getBounds()
        if (!campusBounds.contains(currentBounds.getSouthWest()) || !campusBounds.contains(currentBounds.getNorthEast())) {
          map.setBounds(campusBounds)
        }
      } catch (error) {
        // 如果适应视野失败，直接设置为校区范围
        map.setBounds(campusBounds)
      }
    }
    
    ElMessage.success('所有轨迹加载完成')
  } catch (error) {
    ElMessage.error('加载所有轨迹失败')
    console.error(error)
  }
}

const clearTracks = () => {
  if (map && trackLayers.length > 0) {
    map.remove(trackLayers)
    trackLayers = []
  }
}

const getTrackColor = (index: number) => {
  const colors = ['#FF5722', '#2196F3', '#4CAF50', '#FF9800', '#9C27B0', '#607D8B']
  return colors[index % colors.length] || '#3366FF'
}
</script>

<style scoped>
.map-container {
  display: flex;
  height: 100%;
}

.control-panel {
  width: 300px;
  padding: 20px;
  background-color: #f5f5f5;
  overflow-y: auto;
}

.map {
  flex: 1;
  height: 100%;
}

.el-select {
  width: 100%;
}

.el-button {
  width: 100%;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin-right: 8px;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.3);
}

.legend-text {
  font-size: 12px;
  color: #666;
  flex: 1;
}
</style>
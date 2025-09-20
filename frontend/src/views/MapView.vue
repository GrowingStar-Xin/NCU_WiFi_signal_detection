<template>
  <div class="map-container">
    <!-- 左侧控制面板 -->
    <div class="control-panel">
      <!-- 校区切换功能 -->
      <el-card>
        <template #header>
          <div class="card-header">
            <span>校区选择</span>
          </div>
        </template>
        <el-select v-model="selectedCampus" placeholder="选择校区" @change="handleCampusChange" style="width: 100%">
          <el-option
            v-for="campus in campusList"
            :key="campus.id"
            :label="campus.name"
            :value="campus.id"
          />
        </el-select>
        <div v-if="currentCampus" class="campus-info">
          <p><strong>{{ currentCampus.name }}</strong></p>
          <p>{{ currentCampus.description }}</p>
          <p>建筑数量: {{ currentCampus.buildingCount }}</p>
          <p>AP数量: {{ currentCampus.apCount }}</p>
        </div>
      </el-card>

      <!-- 用户信息检索功能 -->
      <el-card>
        <template #header>
          <div class="card-header">
            <span>用户检索</span>
          </div>
        </template>
        <el-input
          v-model="searchQuery"
          placeholder="输入用户ID、姓名或学号"
          @input="handleSearch"
          clearable
        >
          <template #append>
            <el-button @click="performSearch">搜索</el-button>
          </template>
        </el-input>
        <div v-if="searchResults.length > 0" class="search-results">
          <div 
            v-for="user in searchResults" 
            :key="user.id"
            class="user-item"
            @click="selectUser(user)"
          >
            <strong>{{ user.name || user.id }}</strong>
            <p>轨迹: {{ user.trackCount }}条</p>
          </div>
        </div>
      </el-card>

      <!-- 原有轨迹控制 -->
      <el-card>
        <template #header>
          <div class="card-header">
            <span>轨迹控制</span>
          </div>
        </template>
        <el-select v-model="selectedTrack" placeholder="选择轨迹" @change="loadTrackData">
          <el-option
            v-for="track in trackList"
            :key="track.id"
            :label="track.name"
            :value="track.id"
          />
        </el-select>
        <div style="margin-top: 10px;">
          <el-button type="primary" @click="showAllTracks" size="small">
            显示所有轨迹
          </el-button>
          <el-button @click="clearTracks" size="small">
            清除轨迹
          </el-button>
        </div>
      </el-card>
      
      <!-- 轨迹图例 -->
      <el-card v-if="trackList.length > 0">
        <template #header>
          <div class="card-header">
            <span>轨迹图例</span>
          </div>
        </template>
        <div class="legend-item" v-for="(track, index) in trackList" :key="track.id">
          <div 
            class="legend-color" 
            :style="{ backgroundColor: getTrackColor(index) }"
          ></div>
          <span class="legend-text">{{ track.name }}</span>
        </div>
      </el-card>
    </div>
    
    <!-- 地图容器 -->
    <div id="map-container" class="map"></div>
    
    <!-- 右侧信息面板 -->
    <div class="info-panel" v-if="selectedUserInfo">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>用户详情</span>
            <el-button size="small" @click="closeInfoPanel">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </template>
        <div class="user-detail">
          <h4>{{ selectedUserInfo.name || selectedUserInfo.id }}</h4>
          <p>用户ID: {{ selectedUserInfo.id }}</p>
          <p>轨迹数量: {{ selectedUserInfo.trackCount }}</p>
          <p>最后活动: {{ formatTime(selectedUserInfo.lastActivity) }}</p>
          <div class="user-actions">
            <el-button type="primary" size="small" @click="focusOnUser">
              定位到用户
            </el-button>
            <el-button size="small" @click="exportUserTracks">
              导出轨迹
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import { loadTrackData as loadTrackDataAPI, getTrackList, getAllTracks } from '../utils/api'
import type { TrackData, TrackPoint } from '../types/track'

// 临时注释组件导入，使用基础功能
// import CampusSelector from '../components/CampusSelector.vue'
// import UserSearch from '../components/UserSearch.vue'
// import MapControls from '../components/MapControls.vue'

interface Campus {
  id: string
  name: string
  description: string
  center: [number, number]
  bounds: [[number, number], [number, number]]
  buildingCount: number
  apCount: number
}

interface UserInfo {
  id: string
  name?: string
  studentId?: string
  trackCount: number
  lastActivity: string
  campuses: string[]
}

// 基础状态
const selectedTrack = ref('')
const trackList = ref<TrackData[]>([])
const selectedUserInfo = ref<UserInfo | null>(null)

// 校区相关状态
const selectedCampus = ref('')
const currentCampus = ref<Campus | null>(null)
const campusList = ref<Campus[]>([
  {
    id: 'qianhu',
    name: '前湖校区',
    description: '南昌大学主校区',
    center: [115.8342, 28.6329] as [number, number],
    bounds: [[115.825, 28.625], [115.845, 28.640]] as [[number, number], [number, number]],
    buildingCount: 45,
    apCount: 320
  },
  {
    id: 'qingshanhu',
    name: '青山湖校区',
    description: '医学院校区',
    center: [115.9342, 28.7329] as [number, number],
    bounds: [[115.920, 28.720], [115.950, 28.750]] as [[number, number], [number, number]],
    buildingCount: 25,
    apCount: 180
  }
])

// 用户搜索相关状态
const searchQuery = ref('')
const searchResults = ref<UserInfo[]>([])
const isSearching = ref(false)

// 地图相关变量
let map: any = null
let trackLayers: any[] = []
let userMarkers: any[] = []

// 默认南昌大学前湖校区坐标
const DEFAULT_CENTER = [115.8342, 28.6329]

onMounted(async () => {
  await nextTick()
  
  // 初始化校区数据
  initCampusData()
  
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

// 初始化校区数据
const initCampusData = () => {
  campusList.value = [
    {
      id: 'qianhu',
      name: '前湖校区',
      description: '南昌大学主校区，位于前湖新区',
      center: [115.8342, 28.6329],
      bounds: [
        [115.8200, 28.6200], // 西南角
        [115.8500, 28.6450]  // 东北角
      ],
      buildingCount: 120,
      apCount: 850
    },
    {
      id: 'qingshan',
      name: '青山湖校区',
      description: '南昌大学青山湖校区',
      center: [115.9342, 28.6829],
      bounds: [
        [115.9200, 28.6700],
        [115.9500, 28.6950]
      ],
      buildingCount: 80,
      apCount: 420
    },
    {
      id: 'donghu',
      name: '东湖校区',
      description: '南昌大学东湖校区',
      center: [115.8842, 28.6929],
      bounds: [
        [115.8700, 28.6800],
        [115.9000, 28.7050]
      ],
      buildingCount: 45,
      apCount: 280
    }
  ]
  
  // 设置默认校区
  currentCampus.value = campusList.value[0]
  selectedCampus.value = campusList.value[0].id
}

const initMap = () => {
  // 检查AMap是否已加载
  if (!(window as any).AMap) {
    console.error('高德地图API未加载')
    ElMessage.error('地图API加载失败，请刷新页面重试')
    return
  }
  
  // 检查地图容器是否存在
  const mapContainer = document.getElementById('map-container')
  if (!mapContainer) {
    console.error('地图容器未找到')
    ElMessage.error('地图容器初始化失败')
    return
  }
  
  try {
    // 初始化高德地图
    map = new (window as any).AMap.Map('map-container', {
      zoom: 17,
      center: DEFAULT_CENTER,
      mapStyle: 'amap://styles/normal',
      zooms: [15, 19],
      dragEnable: true,
      doubleClickZoom: false,
      resizeEnable: true
    })
    
    console.log('地图初始化成功')
    ElMessage.success('地图加载成功')
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图初始化失败，请检查网络连接')
    return
  }
  
  // 添加地图控件
  try {
    if ((window as any).AMap.Scale) {
      const scale = new (window as any).AMap.Scale({
        position: 'LB'
      })
      map.addControl(scale)
    }
    
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

  // 监听地图事件
  map.on('zoomend', () => {
    console.log('地图缩放级别:', map.getZoom())
  })

  map.on('moveend', () => {
    const center = map.getCenter()
    console.log('地图中心:', [center.lng, center.lat])
  })
}

// 校区切换处理
const handleCampusChange = (campusId: string) => {
  const campus = campusList.value.find(c => c.id === campusId)
  if (!campus) {
    ElMessage.error('校区信息未找到')
    return
  }
  
  currentCampus.value = campus
  selectedCampus.value = campusId
  
  if (!map) {
    ElMessage.error('地图未初始化')
    return
  }
  
  try {
    // 更新地图中心和缩放级别
    map.setCenter(campus.center)
    map.setZoom(16)
    
    // 设置地图边界
    const bounds = new (window as any).AMap.Bounds(
      campus.bounds[0], // 西南角
      campus.bounds[1]  // 东北角
    )
    map.setLimitBounds(bounds)
    
    // 清除之前的用户标记
    clearUserMarkers()
    
    // 重置用户信息
    selectedUserInfo.value = null
    searchQuery.value = ''
    searchResults.value = []
    
    ElMessage.success(`已切换到${campus.name}`)
    console.log(`校区切换成功: ${campus.name}`, campus)
  } catch (error) {
    console.error('校区切换失败:', error)
    ElMessage.error('校区切换失败，请重试')
  }
}

// 用户搜索处理
const handleSearch = () => {
  // 防抖处理，避免频繁搜索
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  
  searchTimeout = setTimeout(() => {
    performSearch()
  }, 500)
}

let searchTimeout: any = null

const performSearch = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  
  try {
    isSearching.value = true
    
    // 从轨迹数据中获取真实用户
    const response: any = await getAllTracks()
    if (response && response.code === 200 && response.data) {
      // 获取所有唯一的用户ID
       const allUserIds = [...new Set(response.data.map((point: any) => point.accountId))] as string[]
       
       // 根据搜索关键词过滤用户
       const filteredUserIds = allUserIds.filter((userId: string) => 
         userId && userId.toLowerCase().includes(searchQuery.value.toLowerCase())
       )
       
       // 为每个用户创建用户信息
       const users: UserInfo[] = []
       for (const userId of filteredUserIds) {
        // 计算该用户的轨迹点数量
        const userPoints = response.data.filter((point: any) => point.accountId === userId)
        const trackCount = userPoints.length
        
        // 获取最后活动时间
        const lastActivity = userPoints.length > 0 
          ? userPoints.sort((a: any, b: any) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())[0].timestamp
          : new Date().toISOString()
        
        users.push({
          id: userId,
          name: `用户 ${userId}`,
          studentId: userId.replace('user', '2021'),
          trackCount: trackCount,
          lastActivity: lastActivity,
          campuses: [currentCampus.value?.id || 'qianhu']
        })
      }
      
      searchResults.value = users
      
      if (users.length === 0) {
        ElMessage.info('未找到匹配的用户')
      }
    } else {
      ElMessage.error('获取用户数据失败')
      searchResults.value = []
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请重试')
    searchResults.value = []
  } finally {
    isSearching.value = false
  }
}

const selectUser = (user: UserInfo) => {
  selectedUserInfo.value = user
  loadUserTracks(user.id)
  highlightUser(user.id)
}

const selectTrack = (track: TrackData) => {
  selectedTrack.value = track.id
  loadTrackData(track.id)
}

// 高亮用户处理
const highlightUser = async (userId: string) => {
  try {
    // 清除之前的用户标记
    clearUserMarkers()
    
    // 从后端API获取所有轨迹点数据
    const response: any = await getAllTracks()
    if (response && response.code === 200 && response.data) {
      // 过滤出该用户的轨迹点
      const userPoints = response.data.filter((point: any) => 
        point.accountId === userId
      )
      
      if (userPoints.length === 0) {
        ElMessage.warning(`未找到用户 ${userId} 的轨迹数据`)
        return
      }
      
      // 添加用户轨迹标记
      userPoints.forEach((point: any, index: number) => {
        // 检查经纬度是否有效
        if (!point.latitude || !point.longitude || 
            isNaN(point.latitude) || isNaN(point.longitude)) {
          console.warn('跳过无效的轨迹点:', point)
          return
        }
        
        const marker = new (window as any).AMap.Marker({
          position: [point.longitude, point.latitude],
          title: `${userId} - ${point.timestamp}`,
          icon: new (window as any).AMap.Icon({
            size: new (window as any).AMap.Size(20, 20),
            image: 'data:image/svg+xml;base64,' + btoa(`
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20">
                <circle cx="10" cy="10" r="8" fill="#ff4757" stroke="#fff" stroke-width="2"/>
                <text x="10" y="14" text-anchor="middle" fill="white" font-size="10">${index + 1}</text>
              </svg>
            `)
          })
        })
        
        map.add(marker)
        userMarkers.push(marker)
      })
      
      // 调整地图视野以包含所有标记
      if (userMarkers.length > 0) {
        const bounds = new (window as any).AMap.Bounds()
        userPoints.forEach((point: any) => {
          if (point.latitude && point.longitude && 
              !isNaN(point.latitude) && !isNaN(point.longitude)) {
            bounds.extend([point.longitude, point.latitude])
          }
        })
        map.setBounds(bounds)
      }
      
      ElMessage.success(`成功加载用户 ${userId} 的 ${userPoints.length} 个轨迹点`)
    }
  } catch (error) {
    console.error('获取用户轨迹数据失败:', error)
    ElMessage.error('获取用户轨迹数据失败')
  }
}





// 原有功能保持不变
const loadTrackList = async () => {
  try {
    const response = await getTrackList()
    console.log('原始轨迹列表数据:', response.data)
    
    const validTracks = response.data.filter((track: any) => {
      if (!track.id || track.id.trim() === '') {
        console.warn('发现无效轨迹数据:', track)
        return false
      }
      return true
    })
    
    trackList.value = validTracks
    console.log('有效轨迹数据:', validTracks)
    
    if (validTracks.length === 0) {
      ElMessage.warning('暂无有效轨迹数据')
    }
  } catch (error) {
    console.error('加载轨迹列表失败:', error)
    ElMessage.error('加载轨迹列表失败')
  }
}

const loadTrackData = async (trackId: string) => {
  if (!trackId) return
  
  try {
    const response = await loadTrackDataAPI(trackId)
    console.log('轨迹数据:', response.data)
    
    if (response.data && response.data.length > 0) {
      displayTrackOnMap(response.data, trackId)
      ElMessage.success(`成功加载轨迹 ${trackId}`)
    } else {
      ElMessage.warning('该轨迹暂无数据点')
    }
  } catch (error) {
    console.error('加载轨迹数据失败:', error)
    ElMessage.error('加载轨迹数据失败')
  }
}

const displayTrackOnMap = (points: TrackPoint[], trackId: string) => {
  if (!map || !points || points.length === 0) return
  
  clearTracks()
  
  const validPoints = points.filter(point => 
    point.latitude && point.longitude &&
    point.latitude >= 28.6 && point.latitude <= 28.7 &&
    point.longitude >= 115.8 && point.longitude <= 115.95
  )
  
  if (validPoints.length === 0) {
    ElMessage.warning('该轨迹没有有效的坐标点')
    return
  }
  
  const trackIndex = trackList.value.findIndex(track => track.id === trackId)
  const color = getTrackColor(trackIndex)
  
  const path = validPoints.map(point => [point.longitude, point.latitude])
  
  const polyline = new (window as any).AMap.Polyline({
    path: path,
    strokeColor: color,
    strokeWeight: 3,
    strokeOpacity: 0.8
  })
  
  map.add(polyline)
  trackLayers.push(polyline)
  
  validPoints.forEach((point, index) => {
    const marker = new (window as any).AMap.Marker({
      position: [point.longitude, point.latitude],
      title: `点 ${index + 1}: ${point.timestamp}`,
      icon: new (window as any).AMap.Icon({
        size: new (window as any).AMap.Size(8, 8),
        image: 'data:image/svg+xml;base64,' + btoa(`
          <svg xmlns="http://www.w3.org/2000/svg" width="8" height="8" viewBox="0 0 8 8">
            <circle cx="4" cy="4" r="3" fill="${color}" stroke="#fff" stroke-width="1"/>
          </svg>
        `)
      })
    })
    
    map.add(marker)
    trackLayers.push(marker)
  })
  
  const bounds = new (window as any).AMap.Bounds()
  validPoints.forEach(point => {
    bounds.extend([point.longitude, point.latitude])
  })
  map.setBounds(bounds)
}

const showAllTracks = async () => {
  clearTracks()
  
  for (const track of trackList.value) {
    await loadTrackData(track.id)
  }
}

const clearTracks = () => {
  trackLayers.forEach(layer => {
    map.remove(layer)
  })
  trackLayers = []
}

const clearUserMarkers = () => {
  userMarkers.forEach(marker => {
    map.remove(marker)
  })
  userMarkers = []
}

const getTrackColor = (index: number) => {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F']
  return colors[index % colors.length]
}

// 新增功能
const loadUserTracks = (userId: string) => {
  // 模拟加载用户轨迹
  console.log('加载用户轨迹:', userId)
}

const closeInfoPanel = () => {
  selectedUserInfo.value = null
  clearUserMarkers()
}

const focusOnUser = () => {
  if (selectedUserInfo.value) {
    highlightUser(selectedUserInfo.value.id)
  }
}

const exportUserTracks = () => {
  if (selectedUserInfo.value) {
    ElMessage.success('用户轨迹导出功能开发中...')
  }
}

const formatTime = (timeStr: string) => {
  return new Date(timeStr).toLocaleString('zh-CN')
}
</script>

<style scoped>
.map-container {
  display: flex;
  height: 100vh;
  position: relative;
}

.control-panel {
  width: 350px;
  height: 100vh;
  overflow-y: hidden;
  background-color: #f5f7fa;
  padding: 20px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.control-panel .el-card {
  flex-shrink: 0;
}

.control-panel .el-card:last-child {
  flex: 1;
  min-height: 0;
}

.control-panel .el-card:last-child .el-card__body {
  height: 100%;
  overflow-y: auto;
}

/* 优化卡片样式 */
.control-panel .el-card {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.control-panel .el-card__header {
  background-color: #fafafa;
  border-bottom: 1px solid #e4e7ed;
  padding: 12px 16px;
}

.control-panel .el-card__body {
  padding: 16px;
}

/* 优化校区信息显示 */
.campus-info {
  margin-top: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #409EFF;
}

.campus-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #606266;
}

.campus-info p:first-child {
  color: #303133;
  font-weight: 500;
}

/* 优化搜索结果样式 */
.search-results {
  margin-top: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.user-item {
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: #fff;
}

.user-item:hover {
  border-color: #409EFF;
  background-color: #f0f9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.user-item strong {
  color: #303133;
  font-size: 14px;
}

.user-item p {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #909399;
}

.map {
  flex: 1;
  height: 100vh;
  position: relative;
}

#map-container {
  width: 100%;
  height: 100%;
  background-color: #f0f0f0;
}

.info-panel {
  width: 300px;
  height: 100vh;
  overflow-y: auto;
  background-color: #f5f7fa;
  padding: 20px;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
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
}

.legend-text {
  font-size: 14px;
  color: #606266;
}

.user-detail h4 {
  margin: 0 0 15px 0;
  color: #409EFF;
}

.user-detail p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.user-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .control-panel {
    width: 300px;
  }
  
  .info-panel {
    width: 250px;
  }
}

@media (max-width: 768px) {
  .map-container {
    flex-direction: column;
  }
  
  .control-panel {
    width: 100%;
    height: auto;
    max-height: 40vh;
  }
  
  .map {
    height: 60vh;
  }
  
  .info-panel {
    width: 100%;
    height: auto;
    max-height: 30vh;
  }
}
</style>
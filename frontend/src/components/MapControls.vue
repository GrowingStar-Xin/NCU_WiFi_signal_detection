<template>
  <div class="map-controls">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>地图控制</span>
        </div>
      </template>
      
      <!-- 地图样式切换 -->
      <div class="control-section">
        <h4>地图样式</h4>
        <el-radio-group v-model="mapStyle" @change="handleMapStyleChange">
          <el-radio-button 
            v-for="style in mapStyles" 
            :key="style.value" 
            :label="style.value"
          >
            {{ style.label }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 图层控制 -->
      <div class="control-section">
        <h4>图层显示</h4>
        <el-checkbox-group v-model="visibleLayers" @change="handleLayerChange">
          <el-checkbox 
            v-for="layer in availableLayers" 
            :key="layer.value" 
            :label="layer.value"
          >
            {{ layer.label }}
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <!-- 显示选项 -->
      <div class="control-section">
        <h4>显示选项</h4>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-switch
              v-model="showBuildings"
              @change="handleBuildingToggle"
              active-text="显示建筑"
              inactive-text="隐藏建筑"
            />
          </el-col>
          <el-col :span="12">
            <el-switch
              v-model="showAPPoints"
              @change="handleAPToggle"
              active-text="显示AP点"
              inactive-text="隐藏AP点"
            />
          </el-col>
        </el-row>
        <el-row :gutter="10" style="margin-top: 10px;">
          <el-col :span="12">
            <el-switch
              v-model="showHeatmap"
              @change="handleHeatmapToggle"
              active-text="热力图"
              inactive-text="关闭热力图"
            />
          </el-col>
          <el-col :span="12">
            <el-switch
              v-model="showTrafficLayer"
              @change="handleTrafficToggle"
              active-text="实时路况"
              inactive-text="关闭路况"
            />
          </el-col>
        </el-row>
      </div>

      <!-- 地图工具 -->
      <div class="control-section">
        <h4>地图工具</h4>
        <el-row :gutter="10">
          <el-col :span="12">
            <el-button size="small" @click="resetMapView" style="width: 100%">
              <el-icon><Refresh /></el-icon>
              重置视图
            </el-button>
          </el-col>
          <el-col :span="12">
            <el-button size="small" @click="toggleFullscreen" style="width: 100%">
              <el-icon><FullScreen /></el-icon>
              全屏
            </el-button>
          </el-col>
        </el-row>
        <el-row :gutter="10" style="margin-top: 10px;">
          <el-col :span="12">
            <el-button size="small" @click="measureDistance" style="width: 100%">
              <el-icon><Position /></el-icon>
              测距
            </el-button>
          </el-col>
          <el-col :span="12">
            <el-button size="small" @click="captureMap" style="width: 100%">
              <el-icon><Camera /></el-icon>
              截图
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 缩放控制 -->
      <div class="control-section">
        <h4>缩放级别: {{ currentZoom }}</h4>
        <el-slider
          v-model="currentZoom"
          :min="15"
          :max="19"
          :step="1"
          @change="handleZoomChange"
          show-stops
        />
      </div>

      <!-- 地图信息 -->
      <div class="control-section">
        <h4>地图信息</h4>
        <el-descriptions :column="1" size="small">
          <el-descriptions-item label="中心坐标">
            {{ mapCenter[0].toFixed(6) }}, {{ mapCenter[1].toFixed(6) }}
          </el-descriptions-item>
          <el-descriptions-item label="缩放级别">
            {{ currentZoom }}
          </el-descriptions-item>
          <el-descriptions-item label="地图样式">
            {{ mapStyles.find(s => s.value === mapStyle)?.label }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, FullScreen, Position, Camera } from '@element-plus/icons-vue'

interface MapStyle {
  value: string
  label: string
}

interface Layer {
  value: string
  label: string
}

const emit = defineEmits<{
  mapStyleChange: [style: string]
  layerChange: [layers: string[]]
  buildingToggle: [show: boolean]
  apToggle: [show: boolean]
  heatmapToggle: [show: boolean]
  trafficToggle: [show: boolean]
  resetView: []
  zoomChange: [zoom: number]
  measureDistance: []
  captureMap: []
}>()

const mapStyle = ref('normal')
const visibleLayers = ref(['tracks', 'points'])
const showBuildings = ref(true)
const showAPPoints = ref(true)
const showHeatmap = ref(false)
const showTrafficLayer = ref(false)
const currentZoom = ref(17)
const mapCenter = ref([115.8342, 28.6329])

const mapStyles: MapStyle[] = [
  { value: 'normal', label: '标准' },
  { value: 'satellite', label: '卫星' },
  { value: 'dark', label: '暗色' },
  { value: 'light', label: '浅色' },
  { value: 'fresh', label: '清新' }
]

const availableLayers: Layer[] = [
  { value: 'tracks', label: '轨迹线' },
  { value: 'points', label: '轨迹点' },
  { value: 'buildings', label: '建筑物' },
  { value: 'roads', label: '道路' },
  { value: 'labels', label: '标注' }
]

const handleMapStyleChange = (style: string) => {
  emit('mapStyleChange', style)
  ElMessage.success(`已切换到${mapStyles.find(s => s.value === style)?.label}地图`)
}

const handleLayerChange = (layers: string[]) => {
  emit('layerChange', layers)
  ElMessage.success('图层显示已更新')
}

const handleBuildingToggle = (show: boolean) => {
  emit('buildingToggle', show)
  ElMessage.success(show ? '已显示建筑物' : '已隐藏建筑物')
}

const handleAPToggle = (show: boolean) => {
  emit('apToggle', show)
  ElMessage.success(show ? '已显示AP点' : '已隐藏AP点')
}

const handleHeatmapToggle = (show: boolean) => {
  emit('heatmapToggle', show)
  ElMessage.success(show ? '已开启热力图' : '已关闭热力图')
}

const handleTrafficToggle = (show: boolean) => {
  emit('trafficToggle', show)
  ElMessage.success(show ? '已开启实时路况' : '已关闭实时路况')
}

const resetMapView = () => {
  currentZoom.value = 17
  mapCenter.value = [115.8342, 28.6329]
  emit('resetView')
  ElMessage.success('地图视图已重置')
}

const handleZoomChange = (zoom: number) => {
  emit('zoomChange', zoom)
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    ElMessage.success('已进入全屏模式')
  } else {
    document.exitFullscreen()
    ElMessage.success('已退出全屏模式')
  }
}

const measureDistance = () => {
  emit('measureDistance')
  ElMessage.info('请在地图上点击两点进行测距')
}

const captureMap = () => {
  emit('captureMap')
  ElMessage.success('地图截图已保存')
}

// 更新地图中心坐标的方法
const updateMapCenter = (center: [number, number]) => {
  mapCenter.value = center
}

// 更新缩放级别的方法
const updateZoom = (zoom: number) => {
  currentZoom.value = zoom
}

// 暴露方法给父组件
defineExpose({
  updateMapCenter,
  updateZoom
})
</script>

<style scoped>
.map-controls {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.control-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.control-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.control-section h4 {
  margin: 0 0 10px 0;
  color: #409EFF;
  font-size: 14px;
}

.el-radio-group {
  width: 100%;
}

.el-radio-button {
  margin-bottom: 5px;
}

.el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.el-switch {
  margin-bottom: 10px;
}

:deep(.el-slider) {
  margin: 10px 0;
}

:deep(.el-descriptions) {
  margin-top: 10px;
}

:deep(.el-descriptions-item__label) {
  font-weight: normal;
  color: #666;
}

:deep(.el-descriptions-item__content) {
  color: #303133;
}
</style>
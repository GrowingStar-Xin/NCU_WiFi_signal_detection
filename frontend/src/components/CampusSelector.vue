<template>
  <div class="campus-selector">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>校区选择</span>
        </div>
      </template>
      <el-select 
        v-model="selectedCampus" 
        placeholder="选择校区" 
        @change="handleCampusChange"
        style="width: 100%"
      >
        <el-option
          v-for="campus in campusList"
          :key="campus.id"
          :label="campus.name"
          :value="campus.id"
        >
          <span style="float: left">{{ campus.name }}</span>
          <span style="float: right; color: #8492a6; font-size: 13px">
            {{ campus.description }}
          </span>
        </el-option>
      </el-select>
      
      <div class="campus-info" v-if="currentCampus">
        <h4>{{ currentCampus.name }}</h4>
        <p>{{ currentCampus.description }}</p>
        <div class="campus-stats">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="stat-item">
                <span class="stat-label">建筑数量:</span>
                <span class="stat-value">{{ currentCampus.buildingCount }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-item">
                <span class="stat-label">AP数量:</span>
                <span class="stat-value">{{ currentCampus.apCount }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

interface Campus {
  id: string
  name: string
  description: string
  center: [number, number]
  bounds: [[number, number], [number, number]]
  buildingCount: number
  apCount: number
}

const emit = defineEmits<{
  campusChange: [campus: Campus]
}>()

const selectedCampus = ref('qianhu-north')

const campusList = ref<Campus[]>([
  {
    id: 'qianhu-north',
    name: '前湖北校区',
    description: '主校区，包含大部分学院',
    center: [115.8342, 28.6329],
    bounds: [[115.825, 28.625], [115.845, 28.640]],
    buildingCount: 25,
    apCount: 150
  },
  {
    id: 'qingshan-north',
    name: '青山湖北校区',
    description: '理工科学院集中区域',
    center: [115.918, 28.6785],
    bounds: [[115.910, 28.670], [115.925, 28.685]],
    buildingCount: 18,
    apCount: 120
  },
  {
    id: 'qingshan-south',
    name: '青山湖南校区',
    description: '医学院校区',
    center: [115.919, 28.679],
    bounds: [[115.912, 28.672], [115.926, 28.686]],
    buildingCount: 12,
    apCount: 80
  }
])

const currentCampus = computed(() => {
  return campusList.value.find(campus => campus.id === selectedCampus.value)
})

const handleCampusChange = (campusId: string) => {
  const campus = campusList.value.find(c => c.id === campusId)
  if (campus) {
    emit('campusChange', campus)
    ElMessage.success(`已切换到${campus.name}`)
  }
}

onMounted(() => {
  // 默认选择前湖北校区
  const defaultCampus = currentCampus.value
  if (defaultCampus) {
    emit('campusChange', defaultCampus)
  }
})
</script>

<style scoped>
.campus-selector {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.campus-info {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.campus-info h4 {
  margin: 0 0 8px 0;
  color: #409EFF;
}

.campus-info p {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 14px;
}

.campus-stats {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

.stat-value {
  font-weight: bold;
  color: #409EFF;
}
</style>
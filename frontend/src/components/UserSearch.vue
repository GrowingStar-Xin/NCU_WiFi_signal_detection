<template>
  <div class="user-search">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户信息检索</span>
        </div>
      </template>
      
      <div class="search-form">
        <el-input
          v-model="searchQuery"
          placeholder="输入用户ID或姓名"
          @keyup.enter="searchUser"
          clearable
        >
          <template #append>
            <el-button @click="searchUser" :loading="searching">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <div class="search-filters" style="margin-top: 15px;">
        <el-row :gutter="10">
          <el-col :span="12">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              size="small"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="12">
            <el-select v-model="searchType" placeholder="搜索类型" size="small" style="width: 100%">
              <el-option label="用户ID" value="userId" />
              <el-option label="姓名" value="name" />
              <el-option label="学号" value="studentId" />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <div class="user-results" v-if="userResults.length > 0">
        <h4>搜索结果 ({{ userResults.length }})</h4>
        <div class="user-list">
          <div 
            v-for="user in userResults" 
            :key="user.id"
            class="user-item"
            :class="{ active: selectedUser?.id === user.id }"
            @click="selectUser(user)"
          >
            <div class="user-info">
              <div class="user-name">{{ user.name || user.id }}</div>
              <div class="user-details">
                <span class="user-id">ID: {{ user.id }}</span>
                <span class="track-count">轨迹: {{ user.trackCount }}条</span>
              </div>
              <div class="user-time">
                最后活动: {{ formatTime(user.lastActivity) }}
              </div>
            </div>
            <div class="user-actions">
              <el-button size="small" @click.stop="viewUserTracks(user)">
                查看轨迹
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="selected-user" v-if="selectedUser">
        <h4>选中用户详情</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ selectedUser.id }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ selectedUser.name || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="轨迹数量">{{ selectedUser.trackCount }}</el-descriptions-item>
          <el-descriptions-item label="最后活动">{{ formatTime(selectedUser.lastActivity) }}</el-descriptions-item>
          <el-descriptions-item label="活动校区" :span="2">
            <el-tag v-for="campus in selectedUser.campuses" :key="campus" size="small" style="margin-right: 5px;">
              {{ campus }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 15px;">
          <el-button type="primary" @click="highlightUserOnMap">在地图上高亮显示</el-button>
          <el-button @click="exportUserData">导出用户数据</el-button>
        </div>
      </div>

      <el-empty v-if="searchQuery && !searching && userResults.length === 0" description="未找到匹配的用户" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

interface UserInfo {
  id: string
  name?: string
  studentId?: string
  trackCount: number
  lastActivity: string
  campuses: string[]
}

const emit = defineEmits<{
  userSelected: [user: UserInfo]
  highlightUser: [userId: string]
}>()

const searchQuery = ref('')
const searchType = ref('userId')
const dateRange = ref<[string, string] | null>(null)
const searching = ref(false)
const userResults = ref<UserInfo[]>([])
const selectedUser = ref<UserInfo | null>(null)

// 模拟用户数据
const mockUsers: UserInfo[] = [
  {
    id: 'user001',
    name: '张三',
    studentId: '2021001',
    trackCount: 15,
    lastActivity: '2024-01-15T14:30:00',
    campuses: ['前湖北校区', '青山湖北校区']
  },
  {
    id: 'user002',
    name: '李四',
    studentId: '2021002',
    trackCount: 8,
    lastActivity: '2024-01-15T12:15:00',
    campuses: ['前湖北校区']
  },
  {
    id: 'user003',
    name: '王五',
    studentId: '2021003',
    trackCount: 22,
    lastActivity: '2024-01-15T16:45:00',
    campuses: ['青山湖北校区', '青山湖南校区']
  }
]

const searchUser = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }

  searching.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟搜索逻辑
    const query = searchQuery.value.toLowerCase()
    userResults.value = mockUsers.filter(user => {
      switch (searchType.value) {
        case 'userId':
          return user.id.toLowerCase().includes(query)
        case 'name':
          return user.name?.toLowerCase().includes(query)
        case 'studentId':
          return user.studentId?.toLowerCase().includes(query)
        default:
          return false
      }
    })

    if (userResults.value.length === 0) {
      ElMessage.info('未找到匹配的用户')
    } else {
      ElMessage.success(`找到 ${userResults.value.length} 个用户`)
    }
  } catch (error) {
    ElMessage.error('搜索失败，请重试')
  } finally {
    searching.value = false
  }
}

const selectUser = (user: UserInfo) => {
  selectedUser.value = user
  emit('userSelected', user)
}

const viewUserTracks = (user: UserInfo) => {
  selectUser(user)
  ElMessage.success(`正在加载 ${user.name || user.id} 的轨迹数据`)
}

const highlightUserOnMap = () => {
  if (selectedUser.value) {
    emit('highlightUser', selectedUser.value.id)
    ElMessage.success('已在地图上高亮显示用户轨迹')
  }
}

const exportUserData = () => {
  if (selectedUser.value) {
    // 模拟导出功能
    const data = JSON.stringify(selectedUser.value, null, 2)
    const blob = new Blob([data], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `user_${selectedUser.value.id}_data.json`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('用户数据已导出')
  }
}

const formatTime = (timeStr: string) => {
  return new Date(timeStr).toLocaleString('zh-CN')
}
</script>

<style scoped>
.user-search {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.search-form {
  margin-bottom: 15px;
}

.user-results {
  margin-top: 20px;
}

.user-results h4 {
  margin: 0 0 15px 0;
  color: #409EFF;
}

.user-list {
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-item:hover {
  background-color: #f5f7fa;
  border-color: #409EFF;
}

.user-item.active {
  background-color: #ecf5ff;
  border-color: #409EFF;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.user-details {
  display: flex;
  gap: 15px;
  margin-bottom: 4px;
}

.user-id, .track-count {
  font-size: 12px;
  color: #666;
}

.user-time {
  font-size: 12px;
  color: #999;
}

.selected-user {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.selected-user h4 {
  margin: 0 0 15px 0;
  color: #409EFF;
}
</style>
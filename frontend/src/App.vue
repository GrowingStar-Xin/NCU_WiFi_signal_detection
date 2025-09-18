<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>南昌大学前湖校区轨迹平台</h1>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
// App组件逻辑
</script>

<style scoped>
#app {
  height: 100vh;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

.el-header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.el-header h1 {
  margin: 0;
  font-size: 24px;
}

.el-main {
  padding: 0;
  height: calc(100vh - 60px);
}
</style>
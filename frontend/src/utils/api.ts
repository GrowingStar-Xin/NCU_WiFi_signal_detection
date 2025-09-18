import axios from 'axios'
import type { TrackData, TrackPoint, ApiResponse } from '@/types/track'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

// 获取轨迹列表
export const getTrackList = (): Promise<ApiResponse<TrackData[]>> => {
  return api.get('/tracks')
}

// 获取指定轨迹数据
export const loadTrackData = (trackId: string): Promise<ApiResponse<TrackPoint[]>> => {
  return api.get(`/tracks/${trackId}/points`)
}

// 获取所有轨迹数据
export const getAllTracks = (): Promise<ApiResponse<TrackData[]>> => {
  return api.get('/tracks/all')
}

// 上传轨迹数据
export const uploadTrackData = (file: File): Promise<ApiResponse<string>> => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/tracks/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
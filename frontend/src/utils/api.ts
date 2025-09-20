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
export const getTrackList = () => {
  return api.get('/tracks')
}

// 加载轨迹数据
export const loadTrackData = (trackId: string) => {
  return api.get(`/tracks/${trackId}/points`)
}

// 用户信息相关API
export const searchUsers = (query: string) => {
  return api.get(`/users/search?q=${encodeURIComponent(query)}`)
}

export const getUserInfo = (userId: string) => {
  return api.get(`/users/${userId}`)
}

export const getUserTracks = (userId: string) => {
  return api.get(`/users/${userId}/tracks`)
}

// 校区信息相关API
export const getCampusList = () => {
  return api.get('/campuses')
}

export const getCampusInfo = (campusId: string) => {
  return api.get(`/campuses/${campusId}`)
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
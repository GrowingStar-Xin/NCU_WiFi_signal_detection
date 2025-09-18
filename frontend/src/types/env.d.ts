/// <reference types="vite/client" />

declare module '@/utils/api' {
  import type { TrackData, TrackPoint, ApiResponse } from '@/types/track'
  
  export function getTrackList(): Promise<ApiResponse<TrackData[]>>
  export function loadTrackData(trackId: string): Promise<ApiResponse<TrackPoint[]>>
  export function getAllTracks(): Promise<ApiResponse<TrackData[]>>
  export function uploadTrackData(file: File): Promise<ApiResponse<string>>
}

declare module '@/types/track' {
  export interface TrackPoint {
    id: number
    longitude: number
    latitude: number
    timestamp: string
    accountId: string
    trackId: string
  }
  
  export interface TrackData {
    id: string
    name: string
    accountId: string
    createdAt: string
  }
  
  export interface ApiResponse<T> {
    code: number
    data: T
    message?: string
  }
}
export interface TrackPoint {
  id?: string
  accountId: string
  latitude: number
  longitude: number
  timestamp: string
  accuracy?: number
  speed?: number
}

export interface TrackData {
  id: string
  name: string
  accountId: string
  points: TrackPoint[]
  startTime: string
  endTime: string
  totalPoints: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
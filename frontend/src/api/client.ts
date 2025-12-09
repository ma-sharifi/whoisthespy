import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface User {
  id: string
  username: string
  createdAt: string
}

export interface Game {
  id: string
  joinCode: string
  hostUserId: string
  players: string[]
  numberOfSpies?: number
  spyUserIds?: string[]
  currentTurnIndex: number
  civilianWord?: string
  spyWord?: string
  currentImageUrl?: string
  gameState: 'WAITING' | 'RUNNING' | 'FINISHED'
}

export const userApi = {
  create: async (username: string): Promise<User> => {
    const response = await apiClient.post<User>('/users', { username })
    return response.data
  },
  
  get: async (id: string): Promise<User> => {
    const response = await apiClient.get<User>(`/users/${id}`)
    return response.data
  },
  
  update: async (id: string, username: string): Promise<User> => {
    const response = await apiClient.put<User>(`/users/${id}`, { username })
    return response.data
  },
  
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/users/${id}`)
  },
}

export const gameApi = {
  create: async (hostUserId: string): Promise<Game> => {
    const response = await apiClient.post<Game>('/game/create', { hostUserId })
    return response.data
  },
  
  join: async (joinCode: string, userId: string): Promise<Game> => {
    const response = await apiClient.post<Game>('/game/join', { joinCode, userId })
    return response.data
  },
  
  start: async (gameId: string, hostUserId: string, numberOfSpies: number): Promise<Game> => {
    const response = await apiClient.post<Game>('/game/start', {
      gameId,
      hostUserId,
      numberOfSpies,
    })
    return response.data
  },
  
  get: async (gameId: string): Promise<Game> => {
    const response = await apiClient.get<Game>(`/game/${gameId}`)
    return response.data
  },
  
  nextTurn: async (gameId: string, hostUserId: string): Promise<Game> => {
    const response = await apiClient.post<Game>(`/game/${gameId}/nextTurn`, { hostUserId })
    return response.data
  },
  
  generateImage: async (gameId: string, hostUserId: string, word?: string, role?: string): Promise<{ imageUrl: string }> => {
    const response = await apiClient.post<{ imageUrl: string }>(`/game/${gameId}/generateImage`, {
      hostUserId,
      word,
      role,
    })
    return response.data
  },
}


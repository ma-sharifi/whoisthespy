import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws'

export class WebSocketService {
  private client: Client | null = null
  private subscribers: Map<string, ((data: any) => void)[]> = new Map()

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => new SockJS(WS_URL) as any,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log('WebSocket connected')
          resolve()
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame)
          reject(new Error(frame.headers['message'] || 'WebSocket connection failed'))
        },
        onWebSocketError: (event) => {
          console.error('WebSocket error:', event)
          reject(event)
        },
      })

      this.client.activate()
    })
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.client = null
      this.subscribers.clear()
    }
  }

  subscribe(topic: string, callback: (data: any) => void) {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      return () => {}
    }

    if (!this.subscribers.has(topic)) {
      this.subscribers.set(topic, [])
    }

    this.subscribers.get(topic)!.push(callback)

    const subscription = this.client.subscribe(topic, (message) => {
      try {
        const data = JSON.parse(message.body)
        callback(data)
      } catch (error) {
        console.error('Error parsing WebSocket message:', error)
      }
    })

    return () => {
      subscription.unsubscribe()
      const callbacks = this.subscribers.get(topic)
      if (callbacks) {
        const index = callbacks.indexOf(callback)
        if (index > -1) {
          callbacks.splice(index, 1)
        }
      }
    }
  }

  isConnected(): boolean {
    return this.client?.connected || false
  }
}

export const wsService = new WebSocketService()


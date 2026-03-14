import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

type MessageHandler = (message: unknown) => void;

class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private messageHandlers: Map<string, Set<MessageHandler>> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;

  connect(token: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const wsUrl = import.meta.env.VITE_WS_URL || '/ws';

      this.client = new Client({
        webSocketFactory: () => new SockJS(wsUrl) as unknown as WebSocket,
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
          if (import.meta.env.DEV) {
            console.log('[WS]', str);
          }
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          this.reconnectAttempts = 0;
          resolve();
        },
        onStompError: (frame) => {
          console.error('[WS] STOMP error:', frame.headers['message']);
          reject(new Error(frame.headers['message']));
        },
        onDisconnect: () => {
          console.log('[WS] Disconnected');
        },
        onWebSocketClose: () => {
          this.reconnectAttempts++;
          if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('[WS] Max reconnect attempts reached');
            this.disconnect();
          }
        },
      });

      this.client.activate();
    });
  }

  disconnect(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    this.subscriptions.clear();
    this.messageHandlers.clear();

    if (this.client?.active) {
      this.client.deactivate();
    }
    this.client = null;
    this.reconnectAttempts = 0;
  }

  subscribe(destination: string, handler: MessageHandler): () => void {
    if (!this.client?.active) {
      console.warn('[WS] Cannot subscribe, client not connected');
      return () => {};
    }

    // Track handlers
    if (!this.messageHandlers.has(destination)) {
      this.messageHandlers.set(destination, new Set());
    }
    this.messageHandlers.get(destination)!.add(handler);

    // If we haven't subscribed to this destination yet, create STOMP subscription
    if (!this.subscriptions.has(destination)) {
      const subscription = this.client.subscribe(destination, (message: IMessage) => {
        const data = JSON.parse(message.body);
        const handlers = this.messageHandlers.get(destination);
        handlers?.forEach((h) => h(data));
      });
      this.subscriptions.set(destination, subscription);
    }

    // Return unsubscribe function
    return () => {
      const handlers = this.messageHandlers.get(destination);
      handlers?.delete(handler);

      if (handlers?.size === 0) {
        this.subscriptions.get(destination)?.unsubscribe();
        this.subscriptions.delete(destination);
        this.messageHandlers.delete(destination);
      }
    };
  }

  subscribeToUserNotifications(userId: string, handler: MessageHandler): () => void {
    return this.subscribe(`/user/${userId}/notifications`, handler);
  }

  subscribeToFoodUpdates(handler: MessageHandler): () => void {
    return this.subscribe('/topic/food-updates', handler);
  }

  subscribeToPickupUpdates(pickupId: string, handler: MessageHandler): () => void {
    return this.subscribe(`/topic/pickups/${pickupId}`, handler);
  }

  send(destination: string, body: unknown): void {
    if (!this.client?.active) {
      console.warn('[WS] Cannot send, client not connected');
      return;
    }
    this.client.publish({
      destination,
      body: JSON.stringify(body),
    });
  }

  get isConnected(): boolean {
    return this.client?.active ?? false;
  }
}

export const wsService = new WebSocketService();
export default wsService;

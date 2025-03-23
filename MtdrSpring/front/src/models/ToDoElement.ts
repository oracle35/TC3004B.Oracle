export interface ToDoElement {
  id: number;
  description: string;
  creation_ts: Date;
  delivery_ts?: Date; // Optional
  done: boolean;
}

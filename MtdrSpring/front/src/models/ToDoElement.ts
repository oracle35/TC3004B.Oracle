/**
 * Legacy element used in the earliest stages of the project.
 * It is currently implemented due to compatibility and debugging purposes but it is no longer properly used within the project.
 */

export interface ToDoElement {
  id: number;
  description: string;
  creation_ts: Date;
  delivery_ts?: Date; // Optional
  done: boolean;
}

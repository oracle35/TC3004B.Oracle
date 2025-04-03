/**
 * Task model
 * This model represents a task in the system.
 * It contains information about the task such as its ID, description, state, estimated hours,
 * real hours spent, project ID, assigned user ID, and timestamps for creation, update, and finish.
 */

export interface Task {
  id: number;
  description: string;
  state: string;
  hours_estimated: number;
  hours_real?: number;
  id_project: number;
  assigned_to: number; // User ID
  createdAt: Date;
  updatedAt?: Date;
  finishesAt?: Date;
}

/**
 * Task model
 * This model represents a task in the system.
 * It contains information about the task such as its ID, description, state, estimated hours,
 * real hours spent, project ID, assigned user ID, and timestamps for creation, update, and finish.
 */

export interface Task {
  id_Task: number;
  description: string;
  state: string;
  hoursEstimated: number | null;
  hoursReal: number;
  assignedTo: number;
  id_Sprint: number;
  createdAt: Date | null;
  updatedAt: Date | null;
  finishesAt: Date | null;
}

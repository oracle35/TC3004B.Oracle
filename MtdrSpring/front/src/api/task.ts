import { Task } from "../models/Task";

export const API_TASKS = "/task";

export async function getTasks() {
  try {
    const response = await fetch(API_TASKS);
    if (!response.ok) {
      throw new Error("Error fetching tasks");
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error fetching tasks!", error);
  }
}

export async function createTask(
  task: Omit<Task, "id" | "createdAt" | "updatedAt" | "finishesAt">,
) {
  try {
    const response = await fetch(API_TASKS, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(task),
    });
    if (!response.ok) {
      throw new Error("Error creating task");
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error creating the task!", error);
  }
}

export async function updateTask(id: number, task: Task) {
  try {
    const response = await fetch(`${API_TASKS}/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(task),
    });
    if (!response.ok) {
      throw new Error(`Error updating task with id ${id}`);
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error updating the task!", error);
  }
}

export async function deleteTask(id: number) {
  try {
    const response = await fetch(`${API_TASKS}/${id}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
    });
    if (!response.ok) {
      throw new Error(`Error deleting task with id ${id}`);
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error deleting the task!", error);
  }
}

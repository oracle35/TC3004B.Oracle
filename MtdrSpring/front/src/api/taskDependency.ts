import { TaskDependency } from "../models/TaskDependency";

const API_URL = "/taskDependency";

export const createTaskDependency = async (
  dependency: Omit<TaskDependency, "id_Dependency">,
): Promise<TaskDependency> => {
  const response = await fetch(API_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(dependency),
  });

  if (!response.ok) {
    throw new Error("Failed to create task dependency");
  }

  return response.json();
};

export const getTaskDependencies = async (
  parentTaskId: number,
): Promise<TaskDependency[]> => {
  const response = await fetch(`${API_URL}/${parentTaskId}`);

  if (!response.ok) {
    throw new Error("Failed to fetch task dependencies");
  }

  return response.json();
};

export const deleteTaskDependency = async (id: number): Promise<void> => {
  const response = await fetch(`${API_URL}/${id}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error("Failed to delete task dependency");
  }
};

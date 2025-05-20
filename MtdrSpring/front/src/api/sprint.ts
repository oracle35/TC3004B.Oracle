import { Sprint } from "../models/Sprint";

export const API_SPRINTS = "/sprint";

// This function fetches all sprints from the API
export async function getSprints(): Promise<Sprint[]> {
  try {
    const response = await fetch(API_SPRINTS);
    if (!response.ok) {
      throw new Error("Error fetching sprints");
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error fetching sprints!", error);
    return [];
  }
}

// This function fetches a single sprint by its ID from the API
export async function getSprintById(id: number): Promise<Sprint | null> {
  try {
    const response = await fetch(`${API_SPRINTS}/${id}`);
    if (!response.ok) {
      throw new Error(`Error fetching sprint with id ${id}`);
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error fetching the sprint!", error);
    return null;
  }
}

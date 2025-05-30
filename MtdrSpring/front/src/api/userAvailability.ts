import { UserAvailability } from "../models/UserAvailability";

// This file contains functions to interact with the user availability API
export const API_USER_AVAILABILITY = "/userAvailability";
export async function getUserAvailability(): Promise<UserAvailability[]> {
  try {
    const response = await fetch(API_USER_AVAILABILITY);
    if (!response.ok) {
      throw new Error("Error fetching user availability");
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error fetching user availability!", error);
    return [];
  }
}

// This function fetches a single user availability by its ID from the API
export async function getUserAvailabilityById(
  id: number,
): Promise<UserAvailability | null> {
  try {
    const response = await fetch(`${API_USER_AVAILABILITY}/${id}`);
    if (!response.ok) {
      throw new Error(`Error fetching user availability with id ${id}`);
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error fetching the user availability!", error);
    return null;
  }
}

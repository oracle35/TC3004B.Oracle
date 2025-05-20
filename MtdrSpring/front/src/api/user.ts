import { User } from "../models/User";

export const API_USERS = "/user";

// Fetches all users from the API
export async function getUsers(): Promise<User[]> {
  try {
    const response = await fetch(API_USERS);
    if (!response.ok) {
      throw new Error("Error fetching users");
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error fetching users!", error);
    return [];
  }
}

// Fetches a single user by its ID from the API
export async function getUserById(id: number): Promise<User | null> {
  try {
    const response = await fetch(`${API_USERS}/${id}`);
    if (!response.ok) {
      throw new Error(`Error fetching user with id ${id}`);
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error fetching the user!", error);
    return null;
  }
}

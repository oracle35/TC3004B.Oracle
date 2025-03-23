/**
 * TO-DO List API
 */

export const API_LIST = "/todolist";

export async function deleteItem(deleteId: number) {
  try {
    const response = await fetch(`${API_LIST}/${deleteId}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
    });
    if (!response.ok) {
      throw new Error(`Error deleting item with id ${deleteId}`);
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error!", error);
  }
}

export async function modifyItem(
  id: number,
  description: string,
  done: boolean
) {
  try {
    const response = await fetch(`${API_LIST}/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ description, done }),
    });
    if (!response.ok) {
      throw new Error(`Error modifying item with id ${id}`);
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error!", error);
  }
}

export async function addItem(description: string) {
  try {
    const response = await fetch(API_LIST, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ description }),
    });
    if (!response.ok) {
      throw new Error("Error adding item");
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error!", error);
  }
}

export async function getItems() {
  try {
    const response = await fetch(API_LIST);
    if (!response.ok) {
      throw new Error("Error fetching items");
    }
    return await response.json();
  } catch (error) {
    console.error("There was an error!", error);
  }
}

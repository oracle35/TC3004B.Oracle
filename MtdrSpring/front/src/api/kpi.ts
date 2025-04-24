export const API_KPI = "/api/kpi"; // Match backend controller path

export async function getAiSummary(): Promise<string> {
  try {
    const response = await fetch(`${API_KPI}/summary`);

    const responseBody = await response.text(); // Get text directly

    if (!response.ok) {
       // Use the body as the error message if available
      throw new Error(responseBody || `Error fetching AI summary: ${response.statusText}`);
    }
    return responseBody;
  } catch (error) {
    console.error("There was an error fetching the AI summary!", error);
    // Re-throw the error to be caught by the component
    throw error;
  }
}
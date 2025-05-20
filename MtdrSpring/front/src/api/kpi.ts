export const API_KPI = "/api/kpi"; // Match backend controller path

export async function getAiSummary(): Promise<string> {
  try {
    const response = await fetch(`${API_KPI}/summary`);
    const responseBody = await response.text(); // Get text directly due to it being a direct comment.
    if (!response.ok) {
      throw new Error(
        responseBody || `Error fetching AI summary: ${response.statusText}`,
      );
    }
    return responseBody;
  } catch (error) {
    console.error("There was an error fetching the AI summary!", error);
    throw error;
  }
}

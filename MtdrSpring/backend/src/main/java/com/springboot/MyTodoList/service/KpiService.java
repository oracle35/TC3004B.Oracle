package com.springboot.MyTodoList.service;

// Remove Vertex AI imports
// import com.google.cloud.vertexai.VertexAI;
// import com.google.cloud.vertexai.api.GenerateContentResponse;
// import com.google.cloud.vertexai.generativeai.GenerativeModel;
// import com.google.cloud.vertexai.generativeai.ResponseHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.User;

@Service
public class KpiService {

    private static final Logger logger = LoggerFactory.getLogger(KpiService.class);
    private static final String GEMINI_API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private SprintService sprintService;

    @Value("${gemini.api.key:#{null}}") // Read API key from properties/env
    private String geminiApiKey;


    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20)) // Connection timeout
            .build();

    
            public String generateKpiSummary() throws IOException, InterruptedException {
                // Check if the API key is configured - REMOVE the check against the specific key value
                if (geminiApiKey == null || geminiApiKey.isBlank() || geminiApiKey.equals("YOUR_FALLBACK_API_KEY")) { // Keep checks for null, blank, or a generic placeholder
                    logger.warn("Gemini API Key not configured or is using a placeholder value. AI features disabled.");
                    return "AI Summary feature is disabled (API key missing or invalid).";
                }
        
                // ... rest of the method remains the same ...
                List<Task> tasks = taskService.findAll();
                List<User> users = userService.findAll();
                List<Sprint> sprints = sprintService.findAll().stream()
                        .sorted(Comparator.comparing(Sprint::getName))
                        .collect(Collectors.toList());
        
                if (tasks.isEmpty() || users.isEmpty() || sprints.isEmpty()) {
                    logger.info("Insufficient data (tasks, users, or sprints) to generate AI summary.");
                    return "Insufficient data to generate AI summary.";
                }
        
                String prompt = buildPrompt(tasks, users, sprints);
                String requestBody;
                try {
                    requestBody = buildJsonRequestBody(prompt);
                } catch (JSONException e) {
                    logger.error("Error building JSON request body: " + e.getMessage(), e);
                    throw new IOException("Error building JSON request body", e);
                }
                String apiUrlWithKey = GEMINI_API_ENDPOINT + "?key=" + geminiApiKey;
        
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrlWithKey))
                        .timeout(Duration.ofMinutes(2))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
        
                try {
                    logger.info("Sending request to Gemini API endpoint: {}", GEMINI_API_ENDPOINT);
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
                    if (response.statusCode() == 200) {
                        logger.info("Received successful response from Gemini API.");
                        String summary = parseGeminiResponse(response.body());
                        return summary;
                    } else {
                        logger.error("Error response from Gemini API. Status: {}, Body: {}", response.statusCode(), response.body());
                        String errorMsg = parseGeminiError(response.body());
                        throw new IOException("Failed to generate AI summary. API Error: " + (errorMsg != null ? errorMsg : "Status code " + response.statusCode()));
                    }
                } catch (IOException | InterruptedException e) {
                    logger.error("Error sending request to Gemini API: {}", e.getMessage(), e);
                     if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                     }
                    throw e;
                }
            }

    /**
     * Builds the JSON request body string for the Gemini API.
     * @param prompt The text prompt to send to the model.
     * @return A JSON string representing the request body.
     */
    private String buildJsonRequestBody(String prompt) throws JSONException {
        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);

        JSONObject content = new JSONObject();
        content.put("parts", new JSONArray().put(textPart));

        JSONObject requestJson = new JSONObject();
        requestJson.put("contents", new JSONArray().put(content));

        // Optional: Add generation configuration if needed (e.g., temperature, max tokens)
        // JSONObject generationConfig = new JSONObject();
        // generationConfig.put("temperature", 0.7);
        // generationConfig.put("maxOutputTokens", 256);
        // requestJson.put("generationConfig", generationConfig);

        return requestJson.toString();
    }

    /**
     * Parses the successful JSON response from the Gemini API to extract the generated text.
     * @param responseBody The JSON string response body.
     * @return The extracted text summary, or an error message if parsing fails.
     */
    private String parseGeminiResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            // Navigate the expected JSON structure: response -> candidates -> content -> parts -> text
            JSONArray candidates = jsonResponse.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject firstCandidate = candidates.optJSONObject(0);
                if (firstCandidate != null) {
                    JSONObject content = firstCandidate.optJSONObject("content");
                    if (content != null) {
                        JSONArray parts = content.optJSONArray("parts");
                        if (parts != null && parts.length() > 0) {
                            JSONObject firstPart = parts.optJSONObject(0);
                            if (firstPart != null && firstPart.has("text")) {
                                return firstPart.getString("text");
                            }
                        }
                    }
                }
            }
            // Log a warning if the expected text part wasn't found
            logger.warn("Could not find 'text' part in Gemini response structure: {}", responseBody);
            return "AI response received but content was empty or in unexpected format.";
        } catch (Exception e) {
            // Log any errors during JSON parsing
            logger.error("Error parsing Gemini JSON response: {}", e.getMessage(), e);
            return "Error parsing AI response.";
        }
    }

     /**
      * Attempts to parse an error message from a failed Gemini API JSON response.
      * @param responseBody The JSON string response body from a failed request.
      * @return The extracted error message, or null if it cannot be found.
      */
     private String parseGeminiError(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            // Check for the standard 'error' object in Google API responses
            if (jsonResponse.has("error")) {
                JSONObject error = jsonResponse.getJSONObject("error");
                if (error.has("message")) {
                    return error.getString("message"); // Return the specific error message
                }
            }
        } catch (Exception e) {
            // Log if parsing the error structure fails
            logger.error("Could not parse error details from Gemini response body: {}", e.getMessage());
        }
        return null; // Return null if no specific error message could be extracted
    }


    // --- Helper methods to calculate summaries and build the prompt ---

    /**
     * Constructs the main text prompt for the Gemini model based on calculated KPI data.
     */
    private String buildPrompt(List<Task> tasks, List<User> users, List<Sprint> sprints) {
        String teamPerfSummary = calculateTeamPerformance(tasks, sprints);
        String individualPerfSummary = calculateIndividualPerformance(tasks, users, sprints);
        String estimationAccSummary = calculateEstimationAccuracy(tasks, sprints);

        // Using String.format for clarity, ensuring proper newline handling
        return String.format(
            "Please provide a brief (2-3 sentences) summary of the following project Key Performance Indicators (KPIs). Focus on overall trends in team performance, individual contributions, and estimation accuracy across sprints.\n\n" +
            "Team Performance per Sprint (Completed Tasks, Total Real Hours):\n%s\n\n" +
            "Individual Performance per Sprint (Completed Tasks, Real Hours):\n%s\n\n" +
            "Estimation Accuracy per Sprint (Estimated vs Real Hours):\n%s\n\n" +
            "Generate a concise summary:\n",
            teamPerfSummary.isEmpty() ? "No team performance data available." : teamPerfSummary,
            individualPerfSummary.isEmpty() ? "No individual performance data available." : individualPerfSummary,
            estimationAccSummary.isEmpty() ? "No estimation accuracy data available." : estimationAccSummary
        );
    }

    /**
     * Helper method to get the name of a sprint by its ID.
     */
    private String getSprintName(int sprintId, List<Sprint> sprints) {
        if (sprintId == -1) return "Backlog / Unassigned";
        return sprints.stream()
                .filter(s -> s.getID_Sprint() == sprintId)
                .map(Sprint::getName)
                .findFirst()
                .orElse("Sprint " + sprintId); // Fallback if sprint ID not found
    }

    /**
     * Helper method to get the name of a user by their ID.
     */
    private String getUserName(int userId, List<User> users) {
        return users.stream()
                .filter(u -> u.getID_User() == userId)
                .map(User::getName)
                .findFirst()
                .orElse("Unknown User"); // Fallback if user ID not found
    }

    /**
     * Calculates and formats the team performance summary string.
     */
    private String calculateTeamPerformance(List<Task> tasks, List<Sprint> sprints) {
        Map<String, TeamPerfData> stats = new HashMap<>();
        // Initialize stats for all known sprints and backlog
        sprints.forEach(s -> stats.put(s.getName(), new TeamPerfData(s.getName())));
        stats.put("Backlog / Unassigned", new TeamPerfData("Backlog / Unassigned"));

        // Aggregate data from completed tasks
        tasks.stream()
             .filter(t -> "DONE".equalsIgnoreCase(t.getState()))
             .forEach(task -> {
                 String sprintName = getSprintName(task.getID_Sprint(), sprints);
                 TeamPerfData data = stats.get(sprintName);
                 if (data != null) {
                     data.completedTasks++;
                     data.totalRealHours += (task.getHoursReal() != null ? task.getHoursReal() : 0.0); // Ensure double addition
                 } else {
                     logger.warn("Task {} references sprint ID {} which was not found in the sprints list.", task.getID_Task(), task.getID_Sprint());
                 }
             });

        // Format the results into a string, sorted by sprint name
        return stats.values().stream()
                .filter(d -> d.completedTasks > 0 || d.totalRealHours > 0) // Only include sprints with activity
                .sorted(Comparator.comparing(d -> d.sprintName))
                .map(d -> String.format("- %s: %d tasks completed, %.1f total real hours.",
                        d.sprintName, d.completedTasks, d.totalRealHours))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Calculates and formats the individual performance summary string.
     */
     private String calculateIndividualPerformance(List<Task> tasks, List<User> users, List<Sprint> sprints) {
        Map<String, Map<String, IndividualPerfData>> performance = new HashMap<>();

        // Initialize structure for all sprints and users
        sprints.forEach(sprint -> {
            performance.put(sprint.getName(), new HashMap<>());
            users.forEach(user -> performance.get(sprint.getName()).put(user.getName(), new IndividualPerfData()));
        });
        performance.put("Backlog / Unassigned", new HashMap<>());
         users.forEach(user -> performance.get("Backlog / Unassigned").put(user.getName(), new IndividualPerfData()));


        // Populate data from completed tasks
        tasks.stream()
             .filter(t -> "DONE".equalsIgnoreCase(t.getState()))
             .forEach(task -> {
                 String sprintName = getSprintName(task.getID_Sprint(), sprints);
                 String userName = getUserName(task.getAssignedTo(), users);
                 Map<String, IndividualPerfData> sprintData = performance.get(sprintName);
                 if (sprintData != null) {
                     IndividualPerfData userData = sprintData.get(userName);
                     if (userData != null) {
                         userData.completedTasks++;
                         userData.realHours += (task.getHoursReal() != null ? task.getHoursReal() : 0.0); // Ensure double addition
                     } else {
                          logger.warn("Task {} references user ID {} which was not found in the users list for sprint {}.", task.getID_Task(), task.getAssignedTo(), sprintName);
                     }
                 } else {
                      logger.warn("Task {} references sprint ID {} which was not found in the sprints list.", task.getID_Task(), task.getID_Sprint());
                 }
             });

         // Format the results into a string, grouped by sprint and sorted
         StringBuilder sb = new StringBuilder();
         performance.entrySet().stream()
             .sorted(Map.Entry.comparingByKey()) // Sort sprints by name
             .forEach(sprintEntry -> {
                 String sprintName = sprintEntry.getKey();
                 Map<String, IndividualPerfData> usersData = sprintEntry.getValue();
                 // Format lines for users with activity in this sprint
                 String userLines = usersData.entrySet().stream()
                     .filter(userEntry -> userEntry.getValue().completedTasks > 0 || userEntry.getValue().realHours > 0) // Only include users with activity
                     .sorted(Map.Entry.comparingByKey()) // Sort users by name
                     .map(userEntry -> String.format("  - %s: %d tasks, %.1fh",
                             userEntry.getKey(), userEntry.getValue().completedTasks, userEntry.getValue().realHours))
                     .collect(Collectors.joining("\n"));

                 // Append to the final string if there were active users
                 if (!userLines.isEmpty()) {
                     if (sb.length() > 0) sb.append("\n\n"); // Add spacing between sprints
                     sb.append(sprintName).append(":\n").append(userLines);
                 }
             });
         return sb.toString();
    }

    /**
     * Calculates and formats the estimation accuracy summary string.
     */
    private String calculateEstimationAccuracy(List<Task> tasks, List<Sprint> sprints) {
         Map<String, EstimationData> stats = new HashMap<>();
         // Initialize stats for all known sprints and backlog
         sprints.forEach(s -> stats.put(s.getName(), new EstimationData(s.getName())));
         stats.put("Backlog / Unassigned", new EstimationData("Backlog / Unassigned"));

         // Aggregate data from completed tasks
         tasks.stream()
              .filter(t -> "DONE".equalsIgnoreCase(t.getState()))
              .forEach(task -> {
                  String sprintName = getSprintName(task.getID_Sprint(), sprints);
                  EstimationData data = stats.get(sprintName);
                  if (data != null) {
                      data.totalEstimated += (task.getHoursEstimated() != null ? task.getHoursEstimated() : 0.0); // Ensure double addition
                      data.totalReal += (task.getHoursReal() != null ? task.getHoursReal() : 0.0); // Ensure double addition
                  } else {
                       logger.warn("Task {} references sprint ID {} which was not found in the sprints list.", task.getID_Task(), task.getID_Sprint());
                  }
              });

         // Format the results into a string, sorted by sprint name
         return stats.values().stream()
                 .filter(d -> d.totalEstimated > 0 || d.totalReal > 0) // Only include sprints with estimated or real hours
                 .sorted(Comparator.comparing(d -> d.sprintName))
                 .map(d -> String.format("- %s: Est. %.1fh, Real %.1fh",
                         d.sprintName, d.totalEstimated, d.totalReal))
                 .collect(Collectors.joining("\n"));
    }

    // --- Helper Data Classes for Aggregation ---
    private static class TeamPerfData {
        String sprintName;
        int completedTasks = 0;
        double totalRealHours = 0.0;
        TeamPerfData(String name) { this.sprintName = name; }
    }

    private static class IndividualPerfData {
        int completedTasks = 0;
        double realHours = 0.0;
    }

     private static class EstimationData {
         String sprintName;
         double totalEstimated = 0.0;
         double totalReal = 0.0;
         EstimationData(String name) { this.sprintName = name; }
     }
}
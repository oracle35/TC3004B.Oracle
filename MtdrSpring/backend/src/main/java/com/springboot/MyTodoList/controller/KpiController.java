package com.springboot.MyTodoList.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.MyTodoList.service.KpiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/kpi")
@Tag(name = "KPI", description = "Endpoints related to Key Performance Indicators")
public class KpiController {

  private static final Logger logger = LoggerFactory.getLogger(KpiController.class);

  @Autowired private KpiService kpiService;

  @GetMapping("/summary")
  @Operation(
      summary = "Generate AI Summary",
      description = "Generates a text summary of project KPIs using AI.")
  @ApiResponse(responseCode = "200", description = "Summary generated successfully")
  @ApiResponse(responseCode = "500", description = "Error generating summary")
  @ApiResponse(responseCode = "503", description = "AI Service unavailable or not configured")
  public ResponseEntity<String> getAiSummary() {
    try {
      String summary = kpiService.generateKpiSummary();
      if (summary.startsWith("AI Summary feature is disabled")) {
        // Return a specific status or message if AI is disabled
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(summary);
      }
      if (summary.startsWith("Insufficient data")) {
        return ResponseEntity.ok(summary); // Or maybe a 204 No Content?
      }
      return ResponseEntity.ok(summary);
    } catch (Exception e) {
      logger.error("Failed to get AI summary: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error generating AI summary: " + e.getMessage());
    }
  }
}

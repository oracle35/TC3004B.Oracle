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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para KPIs del sistema.
 */
@RestController
@RequestMapping("/api/kpi")
@Tag(name = "KPI", description = "Endpoints relacionados con Indicadores Clave de Desempeño (KPI)")
public class KpiController {

    private static final Logger logger = LoggerFactory.getLogger(KpiController.class);

    @Autowired
    private KpiService kpiService;

    @GetMapping("/summary")
    @Operation(
        summary = "Generar resumen con IA",
        description = "Genera un resumen textual de los indicadores clave de proyectos utilizando inteligencia artificial."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumen generado exitosamente"),
        @ApiResponse(responseCode = "204", description = "Datos insuficientes para generar resumen"),
        @ApiResponse(responseCode = "503", description = "La funcionalidad de IA está deshabilitada o no disponible"),
        @ApiResponse(responseCode = "500", description = "Error interno al generar el resumen")
    })
    public ResponseEntity<String> getAiSummary() {
        try {
            String summary = kpiService.generateKpiSummary();

            if (summary.startsWith("AI Summary feature is disabled")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(summary);
            }

            if (summary.startsWith("Insufficient data")) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(summary);
            }

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error al generar el resumen de KPIs con IA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generando el resumen con IA: " + e.getMessage());
        }
    }
}

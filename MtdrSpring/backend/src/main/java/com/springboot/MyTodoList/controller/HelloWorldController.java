package com.springboot.MyTodoList.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HelloWorldController class
 * Simple controller to test the application
 */
@RestController
@RequestMapping("/test")
@Tag(name = "Test", description = "Controlador para probar la API")
public class HelloWorldController {

    @GetMapping
    @Operation(
            summary = "Saludo de prueba",
            description =
                    "Devuelve un mensaje 'Hello World!' para verificar que la aplicación funciona"
                            + " correctamente.")
    public String helloWorld() {
        return "Hello World!";
    }
}

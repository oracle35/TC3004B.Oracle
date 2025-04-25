package com.springboot.MyTodoList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Project API", version = "v1"))
@Import({TelegramBotStarterConfiguration.class})
public class MyTodoListApplication {

  private static final Logger logger = LoggerFactory.getLogger(MyTodoListApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(MyTodoListApplication.class, args);
  }
}

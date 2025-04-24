package com.springboot.MyTodoList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.starter.TelegramBotStarterConfiguration;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

// import com.springboot.MyTodoList.controller.ToDoItemBotController;
import com.springboot.MyTodoList.service.ProjectService;
import com.springboot.MyTodoList.service.ToDoItemService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Project API", version = "v1"))
@Import({TelegramBotStarterConfiguration.class})
public class MyTodoListApplication {

	private static final Logger logger = LoggerFactory.getLogger(MyTodoListApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(MyTodoListApplication.class, args);
	}

	// @Override
	// public void run(String... args) throws Exception {
	// 	try {
	// 		if (env.getProperty("telegram_name") != null) {
	// 			botName = env.getProperty("telegram_name");
	// 		}
	// 		if (env.getProperty("telegram_token") != null) {
	// 			telegramBotToken = env.getProperty("telegram_token");
	// 		}
	// 		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
	// 		telegramBotsApi
	// 				.registerBot(new ToDoItemBotController(telegramBotToken, botName, toDoItemService, projectService));
	// 		logger.info(BotMessages.BOT_REGISTERED_STARTED.getMessage());
	// 	} catch (TelegramApiException e) {
	// 		e.printStackTrace();
	// 	}
	// }
}

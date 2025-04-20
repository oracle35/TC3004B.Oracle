package com.springboot.MyTodoList.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.model.Project;
import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.service.ProjectService;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para el Bot de Telegram que gestiona las tareas (ToDoItems) y proyectos.
 * Este controlador extiende TelegramLongPollingBot para interactuar con la API de Telegram
 * y proporciona métodos para gestionar tareas y proyectos.
 */
@Tag(name = "Bot de Telegram", description = "API para gestionar tareas y proyectos a través de un bot de Telegram")
public class ToDoItemBotController extends TelegramLongPollingBot {

	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	private ToDoItemService toDoItemService;
	private String botName;
	private ProjectService projectService;

	// Map to store pending new ToDo items for each chat conversation
	private Map<Long, ToDoItem> pendingNewItems = new HashMap<>();
	private Map<Long, ToDoItem> pendingDoneItems = new HashMap<>();

	// allowed users
	long allowedUserId = 8161138802L;


	public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, ProjectService projectService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.toDoItemService = toDoItemService;
		this.projectService = projectService;
		this.botName = botName;
	}

	/**
	 * Método que se ejecuta cuando el bot recibe un mensaje o actualización.
	 * Procesa los comandos y mensajes del usuario.
	 * 
	 * @param update Actualización recibida del servidor de Telegram
	 */
	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageTextFromTelegram = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();

			// User authentication
			Long userId = update.getMessage().getFrom().getId();
			if (!userId.equals(allowedUserId)) {
				SendMessage deniedMessage = new SendMessage();
				deniedMessage.setChatId(chatId);
				deniedMessage.setText("Access denied.");
				try {
					execute(deniedMessage);
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}
				return;
			}

			// Check if the user is in the middle of adding a new task
			if (pendingNewItems.containsKey(chatId)) {
				ToDoItem pendingItem = pendingNewItems.get(chatId);
				// First step: description not yet set
				if (pendingItem.getDescription() == null) {
					pendingItem.setDescription(messageTextFromTelegram);
					// Ask for the delivery date in YYYY-MM-DD format
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram
							.setText("Please enter the delivery date in the format YYYY-MM-DD (e.g. 2025-03-15):");
					messageToTelegram.setReplyMarkup(new ReplyKeyboardRemove(true));
					try {
						execute(messageToTelegram);
					} catch (TelegramApiException e) {
						logger.error(e.getLocalizedMessage(), e);
					}
					return;
				}
				// Second step: delivery timestamp not yet set
				else if (pendingItem.getFinishesAt() == null) {
					try {
						// Append "T00:00:00+00:00" to the input date string to mimic the frontend
						// behavior
						String fullDateTime = messageTextFromTelegram + "T00:00:00+00:00";
						OffsetDateTime deliveryTs = OffsetDateTime.parse(fullDateTime);
						pendingItem.setFinishesAt(deliveryTs);
						pendingItem.setCreatedAt(OffsetDateTime.now());
						pendingItem.setState("NOT_STARTED");
						
						// Ask for estimated hours
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram
								.setText("Please enter the estimated hours: ");
						messageToTelegram.setReplyMarkup(new ReplyKeyboardRemove(true));
						try {
							execute(messageToTelegram);
						} catch (TelegramApiException e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText(
								"Invalid date format. Please enter the delivery date in the format YYYY-MM-DD (e.g. 2025-03-15):");
						try {
							execute(messageToTelegram);
						} catch (TelegramApiException ex) {
							logger.error(ex.getLocalizedMessage(), ex);
						}
					}
					return;
				}
				// Third step: estimated hours not set yet
				else if (pendingItem.getHoursEstimated() == null) {
					try {
						int hoursEstimated = Integer.parseInt(messageTextFromTelegram);
						if (hoursEstimated > 4 || hoursEstimated <= 0) {
							throw new NumberFormatException("Hours must be between 1 and 4.");
						}
						pendingItem.setHoursEstimated(hoursEstimated);
						
						// Save the new ToDo item
						addToDoItem(pendingItem);
						// Confirm creation to the user
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("New item added with delivery date: " + pendingItem.getFinishesAt());
						execute(messageToTelegram);
						// Remove the pending task for this chat
						pendingNewItems.remove(chatId);
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText(
								"Invalid input. Please enter a number between 1 and 4 for estimated hours:");
						try {
							execute(messageToTelegram);
						} catch (TelegramApiException ex) {
							logger.error(ex.getLocalizedMessage(), ex);
						}
					}
					return;
				}
			}

			// Process commands normally if not in the middle of adding a new task
			if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {

				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());

				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboard = new ArrayList<>();

				// First row
				KeyboardRow row = new KeyboardRow();
				row.add(BotLabels.LIST_ALL_ITEMS.getLabel());
				row.add(BotLabels.ADD_NEW_ITEM.getLabel());
				keyboard.add(row);

				// Second row
				row = new KeyboardRow();
				row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
				row.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
				keyboard.add(row);

				keyboardMarkup.setKeyboard(keyboard);
				messageToTelegram.setReplyMarkup(keyboardMarkup);

				try {
					execute(messageToTelegram);
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.indexOf(BotLabels.DONE.getLabel()) != -1) {
				try {
					String done = messageTextFromTelegram.substring(0, messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
					Integer id = Integer.valueOf(done);
			
					ToDoItem item = getToDoItemById(id).getBody();
					item.setState("DONE");
			
					// Save change on pendingDoneItems
					pendingDoneItems.put(chatId, item);
			
					// Ask user for real hours spent
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText("Please enter the hours it took (max 4):");
					messageToTelegram.setReplyMarkup(new ReplyKeyboardRemove(true));
			
					execute(messageToTelegram);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
				return;
			} else if (pendingDoneItems.containsKey(chatId)) {
				try {
					int hoursReal = Integer.parseInt(messageTextFromTelegram);
					ToDoItem item = pendingDoneItems.get(chatId);
					item.setHoursReal(hoursReal);
			
					// save changes
					updateToDoItem(item, item.getID_Task());
					pendingDoneItems.remove(chatId);

					BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DONE.getMessage(), this);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
				return;
			} else if (messageTextFromTelegram.indexOf(BotLabels.UNDO.getLabel()) != -1) {

				String undo = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(undo);

				try {
					ToDoItem item = getToDoItemById(id).getBody();
					item.setState("IN_PROGRESS");
					updateToDoItem(item, id);
					BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_UNDONE.getMessage(), this);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.indexOf(BotLabels.DELETE.getLabel()) != -1) {

				String delete = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(delete);

				try {
					deleteToDoItem(id).getBody();
					BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DELETED.getMessage(), this);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.equals(BotCommands.HIDE_COMMAND.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.HIDE_MAIN_SCREEN.getLabel())) {

				BotHelper.sendMessageToTelegram(chatId, BotMessages.BYE.getMessage(), this);

			} else if (messageTextFromTelegram.equals(BotCommands.TODO_LIST.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.LIST_ALL_ITEMS.getLabel())
					|| messageTextFromTelegram.equals(BotLabels.MY_TODO_LIST.getLabel())) {

				List<ToDoItem> allItems = getAllToDoItems();
				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboard = new ArrayList<>();

				// Command to go back to the main screen
				KeyboardRow mainScreenRowTop = new KeyboardRow();
				mainScreenRowTop.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
				keyboard.add(mainScreenRowTop);

				KeyboardRow firstRow = new KeyboardRow();
				firstRow.add(BotLabels.ADD_NEW_ITEM.getLabel());
				keyboard.add(firstRow);

				KeyboardRow myTodoListTitleRow = new KeyboardRow();
				myTodoListTitleRow.add(BotLabels.MY_TODO_LIST.getLabel());
				keyboard.add(myTodoListTitleRow);

				List<ToDoItem> activeItems = allItems.stream()
						.filter(item -> "IN_PROGRESS".equals(item.getState()))
						.collect(Collectors.toList());

				for (ToDoItem item : activeItems) {
					KeyboardRow currentRow = new KeyboardRow();
					currentRow.add(item.getDescription());
					currentRow.add(item.getID_Task() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
					keyboard.add(currentRow);
				}

				List<ToDoItem> doneItems = allItems.stream()
						.filter(item -> "DONE".equals(item.getState()))
						.collect(Collectors.toList());

				for (ToDoItem item : doneItems) {
					KeyboardRow currentRow = new KeyboardRow();
					currentRow.add(item.getDescription());
					currentRow.add(item.getID_Task() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
					currentRow.add(item.getID_Task() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
					keyboard.add(currentRow);
				}
				// Command to go back to the main screen
				KeyboardRow mainScreenRowBottom = new KeyboardRow();
				mainScreenRowBottom.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
				keyboard.add(mainScreenRowBottom);

				keyboardMarkup.setKeyboard(keyboard);

				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(BotLabels.MY_TODO_LIST.getLabel());
				messageToTelegram.setReplyMarkup(keyboardMarkup);

				try {
					execute(messageToTelegram);
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.equals(BotCommands.ADD_ITEM.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.ADD_NEW_ITEM.getLabel())) {
				// Begin the process for adding a new task by creating a pending item
				pendingNewItems.put(chatId, new ToDoItem());
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("Please enter the description for the new task:");
				messageToTelegram.setReplyMarkup(new ReplyKeyboardRemove(true));

				try {
					execute(messageToTelegram);
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			} else {
				// For unrecognized messages, send a default response
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText("Command not recognized. Please use one of the available commands.");
				try {
					execute(messageToTelegram);
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	@Override
	public String getBotUsername() {
		return botName;
	}

	/**
	 * Obtiene todos los elementos de la lista de tareas
	 * 
	 * @return Lista de todas las tareas
	 */
	@Operation(summary = "Obtener todas las tareas", description = "Recupera todas las tareas de la lista")
	@ApiResponse(responseCode = "200", description = "Lista de tareas recuperada correctamente", 
	             content = @Content(mediaType = "application/json", 
	             schema = @Schema(implementation = ToDoItem.class)))
	public List<ToDoItem> getAllToDoItems() {
		return toDoItemService.findAll();
	}

	/**
	 * Obtiene una tarea por su ID
	 * 
	 * @param id ID de la tarea a recuperar
	 * @return ResponseEntity con la tarea encontrada o error 404 si no existe
	 */
	@Operation(summary = "Obtener tarea por ID", description = "Busca y recupera una tarea específica por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tarea encontrada",
				content = @Content(mediaType = "application/json", 
				schema = @Schema(implementation = ToDoItem.class))),
		@ApiResponse(responseCode = "404", description = "Tarea no encontrada",
				content = @Content)
	})
	public ResponseEntity<ToDoItem> getToDoItemById(
			@Parameter(description = "ID de la tarea a buscar", required = true) @PathVariable int id) {
		try {
			ResponseEntity<ToDoItem> responseEntity = toDoItemService.getItemById(id);
			return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Añade una nueva tarea a la lista
	 * 
	 * @param todoItem Tarea a añadir
	 * @return ResponseEntity con la ubicación de la nueva tarea creada
	 * @throws Exception Si ocurre un error al añadir la tarea
	 */
	@Operation(summary = "Crear nueva tarea", description = "Añade una nueva tarea a la lista")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tarea creada correctamente",
				content = @Content),
		@ApiResponse(responseCode = "400", description = "Datos de tarea inválidos",
				content = @Content)
	})
	public ResponseEntity addToDoItem(
			@Parameter(description = "Datos de la nueva tarea", required = true) 
			@RequestBody ToDoItem todoItem) throws Exception {
		ToDoItem td = toDoItemService.addToDoItem(todoItem);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("location", "" + td.getID_Task());
		responseHeaders.set("Access-Control-Expose-Headers", "location");
		return ResponseEntity.ok().headers(responseHeaders).build();
	}

	/**
	 * Actualiza una tarea existente
	 * 
	 * @param toDoItem Nuevos datos de la tarea
	 * @param id ID de la tarea a actualizar
	 * @return ResponseEntity con la tarea actualizada o error 404 si no existe
	 */
	@Operation(summary = "Actualizar tarea", description = "Actualiza una tarea existente según su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tarea actualizada correctamente",
				content = @Content(mediaType = "application/json", 
				schema = @Schema(implementation = ToDoItem.class))),
		@ApiResponse(responseCode = "404", description = "Tarea no encontrada",
				content = @Content)
	})
	public ResponseEntity updateToDoItem(
			@Parameter(description = "Nuevos datos de la tarea", required = true) 
			@RequestBody ToDoItem toDoItem, 
			@Parameter(description = "ID de la tarea a actualizar", required = true) 
			@PathVariable int id) {
		try {
			ToDoItem toDoItem1 = toDoItemService.updateToDoItem(id, toDoItem);
			System.out.println(toDoItem1.toString());
			return new ResponseEntity<>(toDoItem1, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Elimina una tarea por su ID
	 * 
	 * @param id ID de la tarea a eliminar
	 * @return ResponseEntity con el resultado de la operación
	 */
	@Operation(summary = "Eliminar tarea", description = "Elimina una tarea específica por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Tarea eliminada correctamente",
				content = @Content),
		@ApiResponse(responseCode = "404", description = "Tarea no encontrada",
				content = @Content)
	})
	public ResponseEntity<Boolean> deleteToDoItem(
			@Parameter(description = "ID de la tarea a eliminar", required = true) 
			@PathVariable("id") int id) {
		Boolean flag = false;
		try {
			flag = toDoItemService.deleteToDoItem(id);
			return new ResponseEntity<>(flag, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(flag, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Obtiene todos los proyectos
	 * 
	 * @return ResponseEntity con los proyectos o error 404 si no existen
	 */
	@Operation(summary = "Obtener proyectos", description = "Recupera todos los proyectos disponibles")
	@ApiResponse(responseCode = "200", description = "Proyectos recuperados correctamente", 
	             content = @Content(mediaType = "application/json", 
	             schema = @Schema(implementation = Project.class)))
	public ResponseEntity<Project> getAllProjects() {
		try {
			ResponseEntity<Project> responseEntity = projectService.getItemById(1);
			return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Obtiene un proyecto por su ID
	 * 
	 * @param id ID del proyecto a buscar
	 * @return ResponseEntity con el proyecto encontrado o error 404 si no existe
	 */
	@Operation(summary = "Obtener proyecto por ID", description = "Busca y recupera un proyecto específico por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Proyecto encontrado",
				content = @Content(mediaType = "application/json", 
				schema = @Schema(implementation = Project.class))),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
				content = @Content)
	})
	public ResponseEntity<Project> getProjectById(
			@Parameter(description = "ID del proyecto a buscar", required = true) 
			@PathVariable int id) {
		try {
			ResponseEntity<Project> responseEntity = projectService.getItemById(id);
			return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Añade un nuevo proyecto
	 * 
	 * @param project Proyecto a añadir
	 * @return ResponseEntity con la ubicación del nuevo proyecto creado
	 * @throws Exception Si ocurre un error al añadir el proyecto
	 */
	@Operation(summary = "Crear nuevo proyecto", description = "Añade un nuevo proyecto")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Proyecto creado correctamente",
				content = @Content),
		@ApiResponse(responseCode = "400", description = "Datos de proyecto inválidos",
				content = @Content)
	})
	public ResponseEntity addProject(
			@Parameter(description = "Datos del nuevo proyecto", required = true) 
			@RequestBody Project project) throws Exception {
		Project pr = projectService.addProject(project);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("location", "" + pr.getID_Project());
		responseHeaders.set("Access-Control-Expose-Headers", "location");
		return ResponseEntity.ok().headers(responseHeaders).build();
	}

	/**
	 * Actualiza un proyecto existente
	 * 
	 * @param project Nuevos datos del proyecto
	 * @param id ID del proyecto a actualizar
	 * @return ResponseEntity con el proyecto actualizado o error 404 si no existe
	 */
	@Operation(summary = "Actualizar proyecto", description = "Actualiza un proyecto existente según su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Proyecto actualizado correctamente",
				content = @Content(mediaType = "application/json", 
				schema = @Schema(implementation = Project.class))),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
				content = @Content)
	})
	public ResponseEntity updateProject(
			@Parameter(description = "Nuevos datos del proyecto", required = true) 
			@RequestBody Project project, 
			@Parameter(description = "ID del proyecto a actualizar", required = true) 
			@PathVariable int id) {
		try {
			Project project1 = projectService.updateProject(id, project);
			System.out.println(project1.toString());
			return new ResponseEntity<>(project1, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Elimina un proyecto por su ID
	 * 
	 * @param id ID del proyecto a eliminar
	 * @return ResponseEntity con el resultado de la operación
	 */
	@Operation(summary = "Eliminar proyecto", description = "Elimina un proyecto específico por su ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Proyecto eliminado correctamente",
				content = @Content),
		@ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
				content = @Content)
	})
	public ResponseEntity deleteProject(
			@Parameter(description = "ID del proyecto a eliminar", required = true) 
			@PathVariable int id) {
		try {
			projectService.deleteProject(id);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
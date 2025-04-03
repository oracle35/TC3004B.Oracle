package com.springboot.MyTodoList.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.springboot.MyTodoList.model.Project;
import com.springboot.MyTodoList.service.ProjectService;
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

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {

	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	private ToDoItemService toDoItemService;
	private String botName;
	private ProjectService projectService;

	// Map to store pending new ToDo items for each chat conversation
	private Map<Long, ToDoItem> pendingNewItems = new HashMap<>();


	public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, ProjectService projectService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.toDoItemService = toDoItemService;
		this.projectService = projectService;
		this.botName = botName;
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageTextFromTelegram = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();

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
				else if (pendingItem.getDelivery_ts() == null) {
					try {
						// Append "T00:00:00+00:00" to the input date string to mimic the frontend
						// behavior
						String fullDateTime = messageTextFromTelegram + "T00:00:00+00:00";
						OffsetDateTime deliveryTs = OffsetDateTime.parse(fullDateTime);
						pendingItem.setDelivery_ts(deliveryTs);
						pendingItem.setCreation_ts(OffsetDateTime.now());
						pendingItem.setDone(false);
						// Save the new ToDo item
						addToDoItem(pendingItem);
						// Confirm creation to the user
						SendMessage messageToTelegram = new SendMessage();
						messageToTelegram.setChatId(chatId);
						messageToTelegram.setText("New item added with delivery date: " + messageTextFromTelegram);
						execute(messageToTelegram);
						// Remove the pending task for this chat
						pendingNewItems.remove(chatId);
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

				String done = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(done);

				try {
					ToDoItem item = getToDoItemById(id).getBody();
					item.setDone(true);
					updateToDoItem(item, id);
					BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DONE.getMessage(), this);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.indexOf(BotLabels.UNDO.getLabel()) != -1) {

				String undo = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(undo);

				try {
					ToDoItem item = getToDoItemById(id).getBody();
					item.setDone(false);
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

				List<ToDoItem> activeItems = allItems.stream().filter(item -> !item.isDone())
						.collect(Collectors.toList());

				for (ToDoItem item : activeItems) {
					KeyboardRow currentRow = new KeyboardRow();
					currentRow.add(item.getDescription());
					currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
					keyboard.add(currentRow);
				}

				List<ToDoItem> doneItems = allItems.stream().filter(item -> item.isDone())
						.collect(Collectors.toList());

				for (ToDoItem item : doneItems) {
					KeyboardRow currentRow = new KeyboardRow();
					currentRow.add(item.getDescription());
					currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
					currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
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

	// GET /todolist
	public List<ToDoItem> getAllToDoItems() {
		return toDoItemService.findAll();
	}

	// GET BY ID /todolist/{id}
	public ResponseEntity<ToDoItem> getToDoItemById(@PathVariable int id) {
		try {
			ResponseEntity<ToDoItem> responseEntity = toDoItemService.getItemById(id);
			return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	// POST /todolist (used to add a new item)
	public ResponseEntity addToDoItem(@RequestBody ToDoItem todoItem) throws Exception {
		ToDoItem td = toDoItemService.addToDoItem(todoItem);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("location", "" + td.getID());
		responseHeaders.set("Access-Control-Expose-Headers", "location");
		return ResponseEntity.ok().headers(responseHeaders).build();
	}

	// UPDATE /todolist/{id}
	public ResponseEntity updateToDoItem(@RequestBody ToDoItem toDoItem, @PathVariable int id) {
		try {
			ToDoItem toDoItem1 = toDoItemService.updateToDoItem(id, toDoItem);
			System.out.println(toDoItem1.toString());
			return new ResponseEntity<>(toDoItem1, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	// DELETE /todolist/{id}
	public ResponseEntity<Boolean> deleteToDoItem(@PathVariable("id") int id) {
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
	 * Get all projects
	 * @return List of projects
	 */

	public ResponseEntity<Project> getAllProjects() {
		try {
			ResponseEntity<Project> responseEntity = projectService.getItemById(1);
			return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<Project> getProjectById(@PathVariable int id) {
		try {
			ResponseEntity<Project> responseEntity = projectService.getItemById(id);
			return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity addProject(@RequestBody Project project) throws Exception {
		Project pr = projectService.addProject(project);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("location", "" + pr.getID_Project());
		responseHeaders.set("Access-Control-Expose-Headers", "location");
		return ResponseEntity.ok().headers(responseHeaders).build();
	}

	public ResponseEntity updateProject(@RequestBody Project project, @PathVariable int id) {
		try {
			Project project1 = projectService.updateProject(id, project);
			System.out.println(project1.toString());
			return new ResponseEntity<>(project1, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity deleteProject(@PathVariable int id) {
		try {
			projectService.deleteProject(id);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}

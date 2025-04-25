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

import java.time.format.DateTimeFormatter;

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

	// Map to store pending state for developer search
        private Map<Long, Boolean> pendingDeveloperSearch = new HashMap<>();
	private Map<Long, Boolean> pendingDeveloperAssign = new HashMap<>();

	// Map to store developer names and their User IDs
        private static final Map<String, Long> developers = new HashMap<>();
        static {
             	developers.put("Jean", 21L);
             	developers.put("Jacob G.", 42L);
         }

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

	 		if (pendingDeveloperSearch.containsKey(chatId)) {
                                handleDeveloperSelection(chatId, messageTextFromTelegram);
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
						pendingItem.setState("IN_PROGRESS");

						// Assign a developer
        					SendMessage messageToTelegram = new SendMessage();
        				 	messageToTelegram.setChatId(chatId);
        				 	messageToTelegram.setText("Please select a developer to assign the task:");
                                         	
        				 	ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        				 	List<KeyboardRow> keyboard = new ArrayList<>();
                                         	
        				 	// Create buttons for each developer
        				 	KeyboardRow devRow = new KeyboardRow();
        				 	int count = 0;
        				 	for (String devName : developers.keySet()) {
        				 	    devRow.add(devName);
        				 	    count++;
        				 	    if (count % 2 == 0) {
        				 	        keyboard.add(devRow);
        				 	        devRow = new KeyboardRow();
        				 	    }
        				 	}
        				 	// Add the last row if it wasn't fully populated
        				 	if (!devRow.isEmpty()) {
        				 	    keyboard.add(devRow);
        				 	}
                                         	
        				 	keyboardMarkup.setKeyboard(keyboard);
        				 	keyboardMarkup.setResizeKeyboard(true);
        				 	keyboardMarkup.setOneTimeKeyboard(true);
        				 	messageToTelegram.setReplyMarkup(keyboardMarkup);
                                         	
        				 	try {
        				 	    execute(messageToTelegram);
        				 	} catch (TelegramApiException e) {
        				 	    logger.error("Error initiating developer search: {}", e.getMessage(), e);
        				 	    pendingDeveloperAssign.remove(chatId);
        				 	    // BotHelper.sendMessageToTelegram(chatId, "Error setting up developer search.", this);
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
				// NEW Third step: assign the user
				else if (pendingItem.getAssignedTo() == null) {
					try {
						// AMOGUS assign a developer

						String selectedDeveloperName = messageTextFromTelegram;
						Long selectedUserId = null;

						selectedUserId = developers.get(selectedDeveloperName);
             					logger.info("Searching tasks for developer: {} (ID: {})", selectedDeveloperName, selectedUserId);

						pendingItem.setAssignedTo(selectedUserId.intValue());

						// if (developers.containsKey(selectedDeveloperName)) {
         					//     selectedUserId = developers.get(selectedDeveloperName);
						// } else {
						//     throw new Exception("Damn, you suck fr");
						// }

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
								"Developer not found, please select a developer from the list");
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

						showMainMenu(chatId);
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

				// Third row
                                KeyboardRow thirdRow = new KeyboardRow();
                                thirdRow.add("Search Tasks by developer");
                                keyboard.add(thirdRow);

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

			} else if (messageTextFromTelegram.equals("Search Tasks by developer")) {
                             	initiateDeveloperSearch(chatId);
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

	private void showMainMenu(long chatId) {
         SendMessage messageToTelegram = new SendMessage();
         messageToTelegram.setChatId(chatId);
         messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());

         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
         List<KeyboardRow> keyboard = new ArrayList<>();

         // First row
         KeyboardRow row1 = new KeyboardRow();
         row1.add(BotLabels.LIST_ALL_ITEMS.getLabel());
         row1.add(BotLabels.ADD_NEW_ITEM.getLabel());
         keyboard.add(row1);

         // Second row - Developer Search
         KeyboardRow row2 = new KeyboardRow();
         row2.add("Search Tasks by developer");
         keyboard.add(row2);

         // Third row
         KeyboardRow row3 = new KeyboardRow();
         row3.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
         row3.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
         keyboard.add(row3);

         keyboardMarkup.setKeyboard(keyboard);
         keyboardMarkup.setResizeKeyboard(true);
         keyboardMarkup.setOneTimeKeyboard(false);
         messageToTelegram.setReplyMarkup(keyboardMarkup);

         try {
             execute(messageToTelegram);
         } catch (TelegramApiException e) {
             logger.error("Error showing main menu: {}", e.getMessage(), e);
         }
     }

     // Helper method to show the ToDo List
      private void showToDoList(long chatId) {
         List<ToDoItem> allItems = getAllToDoItems();
         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
         List<KeyboardRow> keyboard = new ArrayList<>();
         StringBuilder messageText = new StringBuilder("📋 *Your ToDo List*\n\n");

         // Command to go back to the main screen
         KeyboardRow mainScreenRowTop = new KeyboardRow();
         mainScreenRowTop.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
         keyboard.add(mainScreenRowTop);

         KeyboardRow firstRow = new KeyboardRow();
         firstRow.add(BotLabels.ADD_NEW_ITEM.getLabel());
         keyboard.add(firstRow);

         // Filter items
         List<ToDoItem> activeItems = allItems.stream()
                 .filter(item -> !"DONE".equals(item.getState()))
                 .collect(Collectors.toList());

         List<ToDoItem> doneItems = allItems.stream()
                 .filter(item -> "DONE".equals(item.getState()))
                 .collect(Collectors.toList());

         messageText.append("*Active Tasks:*\n");
         if (activeItems.isEmpty()) {
             messageText.append("_No active tasks._\n");
         } else {
             for (ToDoItem item : activeItems) {
                 messageText.append("- ").append(item.getDescription())
                            .append(" (Due: ").append(item.getFinishesAt() != null ? item.getFinishesAt().toLocalDate() : "N/A")
                            .append(")\n");
                 KeyboardRow currentRow = new KeyboardRow();
                 // Make description button shorter if needed
                 String shortDesc = item.getDescription().length() > 20 ? item.getDescription().substring(0, 17) + "..." : item.getDescription();
                 currentRow.add(shortDesc); // Button shows description
                 currentRow.add(item.getID_Task() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
                 keyboard.add(currentRow);
             }
         }

         messageText.append("\n*Completed Tasks:*\n");
          if (doneItems.isEmpty()) {
             messageText.append("_No completed tasks._\n");
         } else {
             for (ToDoItem item : doneItems) {
                  messageText.append("- ~").append(item.getDescription()).append("~ (ID: ").append(item.getID_Task()).append(")\n"); // Strikethrough for done items
                 KeyboardRow currentRow = new KeyboardRow();
                 String shortDesc = item.getDescription().length() > 15 ? item.getDescription().substring(0, 12) + "..." : item.getDescription();
                 currentRow.add("~" + shortDesc + "~"); // Button shows description (strikethrough)
                 currentRow.add(item.getID_Task() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
                 currentRow.add(item.getID_Task() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
                 keyboard.add(currentRow);
             }
         }

         keyboardMarkup.setKeyboard(keyboard);
         keyboardMarkup.setResizeKeyboard(true);
         keyboardMarkup.setOneTimeKeyboard(true);

         SendMessage messageToTelegram = new SendMessage();
         messageToTelegram.setChatId(chatId);
         messageToTelegram.setText(messageText.toString());
         messageToTelegram.setParseMode("Markdown");
         messageToTelegram.setReplyMarkup(keyboardMarkup);

         try {
             execute(messageToTelegram);
         } catch (TelegramApiException e) {
             logger.error("Error showing ToDo list: {}", e.getMessage(), e);
         }
     }

     // Method to initiate the developer search
     private void initiateDeveloperSearch(long chatId) {
         pendingDeveloperSearch.put(chatId, true);
         SendMessage messageToTelegram = new SendMessage();
         messageToTelegram.setChatId(chatId);
         messageToTelegram.setText("Please select a developer to view their tasks:");

         ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
         List<KeyboardRow> keyboard = new ArrayList<>();

         // Create buttons for each developer
         KeyboardRow devRow = new KeyboardRow();
         int count = 0;
         for (String devName : developers.keySet()) {
             devRow.add(devName);
             count++;
             if (count % 2 == 0) {
                 keyboard.add(devRow);
                 devRow = new KeyboardRow();
             }
         }
         // Add the last row if it wasn't fully populated
         if (!devRow.isEmpty()) {
             keyboard.add(devRow);
         }

         // Add a cancel/back button
         KeyboardRow cancelRow = new KeyboardRow();
         cancelRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
         keyboard.add(cancelRow);


         keyboardMarkup.setKeyboard(keyboard);
         keyboardMarkup.setResizeKeyboard(true);
         keyboardMarkup.setOneTimeKeyboard(true);
         messageToTelegram.setReplyMarkup(keyboardMarkup);

         try {
             execute(messageToTelegram);
         } catch (TelegramApiException e) {
             logger.error("Error initiating developer search: {}", e.getMessage(), e);
             pendingDeveloperSearch.remove(chatId);
             // BotHelper.sendMessageToTelegram(chatId, "Error setting up developer search.", this);
         }
     }

     // Method to handle the developer selection
     private void handleDeveloperSelection(long chatId, String selectedDeveloperName) {
         // Check if the selected name is a valid developer
         if (developers.containsKey(selectedDeveloperName)) {
             Long selectedUserId = developers.get(selectedDeveloperName);
             logger.info("Searching tasks for developer: {} (ID: {})", selectedDeveloperName, selectedUserId);

             try {
                 List<ToDoItem> assignedItems = toDoItemService.getItemsByAssignedTo(selectedUserId.intValue());
		 logger.info("AMOGUS-1");

                 StringBuilder responseText = new StringBuilder("Tasks assigned to *" + selectedDeveloperName + "*:\n\n");
                 if (assignedItems == null || assignedItems.isEmpty()) {
			 logger.info("AMOGUS-2");
                     responseText.append("_No tasks found for this developer._");
                 } else {
			 logger.info("AMOGUS-3");
                     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                     for (ToDoItem item : assignedItems) {
                         responseText.append("- ").append(item.getDescription())
                                     .append(" (State: ").append(item.getState())
                                     .append(", Due: ").append(item.getFinishesAt() != null ? item.getFinishesAt().format(formatter) : "N/A")
                                     .append(")\n");
                     }
                 }
		 logger.info("AMOGUS-4");

                 // Send the results back to the user
                 SendMessage resultMessage = new SendMessage();
                 resultMessage.setChatId(chatId);
                 resultMessage.setText(responseText.toString());
                 // resultMessage.setParseMode("Markdown");
                 // resultMessage.setReplyMarkup(new ReplyKeyboardRemove(true));

                 execute(resultMessage);

                 showMainMenu(chatId);


             } catch (Exception e) {
                 logger.error("Error searching tasks for developer ID " + selectedUserId, e);
                 BotHelper.sendMessageToTelegram(chatId, "An error occurred while searching for tasks.", this);
                 showMainMenu(chatId);
             } finally {
                 pendingDeveloperSearch.remove(chatId);
             }

         } else if (selectedDeveloperName.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
              // User clicked the cancel/back button
              logger.info("Developer search cancelled by user {}", chatId);
              pendingDeveloperSearch.remove(chatId); 
              showMainMenu(chatId);

         } else {
             // Invalid input received while expecting a developer name
             logger.warn("Invalid input received during developer selection for chat {}: {}", chatId, selectedDeveloperName);
             BotHelper.sendMessageToTelegram(chatId, "Invalid selection. Please choose a developer from the keyboard or cancel.", this);
             initiateDeveloperSearch(chatId);
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

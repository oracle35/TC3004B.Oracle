 # Telegram Command API

 This document describes how to use the `TelegramCommand` API to implement Telegram bot commands in the backend application.

 ## Location

 Core classes are located in:
 ```text
 MtdrSpring/backend/src/main/java/com/springboot/MyTodoList/bot/command/core/
 ```
 - **TelegramCommand.java**: base abstract class with utilities for messaging and callbacks
 - **AuthenticatedTelegramCommand.java**: extends `TelegramCommand` to enforce user authentication
 - **CommandContext.java**: request context and helper methods for commands
 - **CommandResult.java**: controls command flow (finish, continue, execute)
 - **CommandRegistry.java**: registers and looks up commands
 - **CommandProcessor.java**: routes updates to commands

 ## Overview

 `TelegramCommand` provides common functionality for sending messages, actions, and handling callback queries. To add a new bot command, extend `TelegramCommand` (or `AuthenticatedTelegramCommand` for protected commands) and register it in the bot.

 ## Extending TelegramCommand

 ```java
 public class MyCommand extends TelegramCommand {
   public MyCommand(TelegramClient client) {
     super(client);
   }

   @Override
   public String getDescription() {
     return "Description shown in /help";
   }

   @Override
   public CommandResult execute(CommandContext context) {
     // Your command logic here
     sendMessage(context, "Hello, world!");
     return CommandResult.finish();
   }
 }
```

 To require a registered user, extend `AuthenticatedTelegramCommand` instead and implement:

 ```java
 @Override
 public CommandResult executeAuthenticated(CommandContext context) {
   // Protected logic here
 }
```

 ## Registering Commands

 In `TelegramBot.registerCommands()`:
```java
registry.registerCommand("/mycmd", new MyCommand(client));
```
 The command name (e.g., `/mycmd`) triggers your command when received as a message or callback prefix.

 ## Command Flow Control

 Control how the processor handles the next update by returning a `CommandResult`:
- `CommandResult.finish()`: end this command; subsequent messages are routed normally.
- `CommandResult.continu()`: keep the current command active; next message goes to the same command.
- `CommandResult.execute(String[] args)`: immediately invoke another command with provided args.

 ## CommandContext

 Provides access to the incoming update and utilities:
- `context.getChatId()`: chat identifier
- `context.getMessage()`: the incoming `Message`
- `context.getSender()`: the `org.telegram.telegrambots.meta.api.objects.User`
- `context.getArguments()`: message text split on whitespace
- `context.hasArguments()`: check for extra args
- `context.getRegistry()`: `CommandRegistry` instance
- `context.isAuthenticated()`: whether a user is registered
- `context.getAuthenticatedUser()`: application `User` entity (if present)
- `context.getBotUsername()`: bot's username (for deep links)

 ## Utility Methods

 ### Sending Messages
- `sendMessage(context, String text)`: simple send
- `sendMessage(context, builder -> builder.text(...).parseMode(...).build())`: full builder support
  Returns `Optional<Message>`.

 ### Sending Chat Actions
- `sendAction(context, ActionType.TYPING)` (or other `ActionType`)

 ### Deep Linking
Generate a `t.me` link to launch the bot with parameters:
```java
String link = linkCommand(context, "task", "42");
// https://t.me/YourBot?start=task_42
```

 ### Escaping MarkdownV2
Escape special characters before sending in `MARKDOWNV2` mode:
```java
String safe = escapeMarkdownV2(userInput);
sendMessage(context, msg -> msg.text(safe).parseMode(ParseMode.MARKDOWNV2).build());
```

 ### Handling Callback Queries
- `answerCallbackQuery(callbackQuery, String text)`: reply to inline button presses
- `answerCallbackQuery(AnswerCallbackQuery answer)`: send a prepared answer
- `callbackQuery(query)`: default unknown-action response

 ## Example: Echo Command

 ```java
 public class EchoCommand extends TelegramCommand {
   public EchoCommand(TelegramClient client) { super(client); }

   @Override
   public String getDescription() {
     return "Echo back the user's message";
   }

   @Override
   public CommandResult execute(CommandContext context) {
     String text = context.getMessage().getText();
     sendMessage(context, text);
     return CommandResult.finish();
   }
 }

 // Registration
 registry.registerCommand("/echo", new EchoCommand(client));
 ```
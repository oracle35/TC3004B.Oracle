# Prompt: Unit Testing Analysis for Java Telegram Bot Task Tracker

## What I Need: Analysis & Advice Only (Not Implementation)
I need analysis and recommendations for implementing unit tests, not for you to write the actual tests. Please focus on providing a testing strategy that:

1. **Analyzes testability**: Evaluate the current codebase structure for testability, focusing on the Telegram bot command system. Identify which parts can be easily tested with Mockito and which might need special approaches.

2. **Designs a testing scheme**: Propose a structure for systematically testing commands, including patterns for:
   - Mocking external dependencies (database, Telegram API)
   - Testing command flow control (`CommandResult` handling)
   - Testing authenticated vs. non-authenticated commands
   - Verifying proper responses to different types of user input

3. **Creates a reference for future engineers**: Outline best practices for testing new commands as they're developed, including:
   - Templates or patterns to follow
   - Common pitfalls to avoid
   - How to approach testing complex user interactions

## Constraints & Requirements
- **Minimize codebase modifications**: The testing approach should require minimal changes to the existing codebase
- **Cost efficiency**: The testing approach should be efficient and avoid unnecessary resource usage
- **Use Mockito**: The testing framework should be based on Mockito for mocking dependencies
- **Focus on unit tests**: While integration tests might be mentioned, the primary focus should be on unit testing individual commands and their interactions

## Project Details
The core of the Telegram bot functionality is in the command system:
- `TelegramCommand`: Base abstract class with utilities for messaging and callbacks
- `AuthenticatedTelegramCommand`: Extends `TelegramCommand` to enforce user authentication
- `CommandContext`: Request context and helper methods for commands
- `CommandResult`: Controls command flow (finish, continue, execute)
- `CommandRegistry`: Registers and looks up commands
- `CommandProcessor`: Routes updates to commands

Commands are registered with the bot and triggered by messages or callbacks with specific prefixes. Commands can send messages, actions, and handle callback queries.

## Output Requested
Please provide:
1. An analysis of the testability of the current command structure
2. A proposed testing scheme for the Telegram bot commands
3. Recommendations for test organization and structure
4. Example test patterns (not complete tests) showing how to approach different testing scenarios
5. Best practices for future engineers to follow when writing tests for new commands
The output should be written to `claude/` as a Markdown document for future reference.

Your analysis should focus on practical advice that can be implemented without changing the core structure of the application, while ensuring thorough test coverage of command functionality.

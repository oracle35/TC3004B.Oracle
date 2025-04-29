package com.springboot.MyTodoList.bot.command.core;

import java.util.Optional;

/*
 * Represents data passed to the CommandProcessor to determine
 * the next state.
 */
public class CommandResult {
  /**
   * Enum to manage the CommandProcessor's state machine.
   */
  public enum CommandState {
    /*
     * The next message will be routed to this command.
     */
    CONTINUE,
    /*
     * Tell the CommandProcessor to start handling other
     * commands on the next message.
     */
    FINISH,
    /*
     * Execute the specified command as if the user
     * sent the message immediately.
     */
    EXECUTE,
  }

  private final CommandState state;
  private final String[] forwardedCommand;

  private CommandResult(CommandState state) {
    this.state = state;
    this.forwardedCommand = null;
  }

  private CommandResult(CommandState state, String[] args) {
    this.state = state;
    this.forwardedCommand = args;
  }

  public CommandState getState() {
    return this.state;
  }

  public Optional<String[]> getExecutedCommand() {
    return Optional.ofNullable(this.forwardedCommand);
  }

  /*
   * Return this value from TelegramCommand.execute
   * to mark this command as finished. Next messages will be
   * routed to appropiate commands depending on the called command.
   */
  public static CommandResult finish() {
    return new CommandResult(CommandState.FINISH);
  }

  /*
   * Return this value from TelegramCommand.execute
   * to redirect the next message back to the current command
   * to continue a command chain (e.g /newtask)
   */
  // continue is a reserved token :(
  public static CommandResult continu() {
    return new CommandResult(CommandState.CONTINUE);
  }

  /*
   * Return this value from TelegramCommand.execute
   * to execute the specified command with arguments
   * immediately.
   */
  public static CommandResult execute(String[] args) {
    return new CommandResult(CommandState.EXECUTE, args);
  }
}

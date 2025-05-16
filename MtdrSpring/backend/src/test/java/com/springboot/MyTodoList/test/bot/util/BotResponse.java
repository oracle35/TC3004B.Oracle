package com.springboot.MyTodoList.test.bot.util;

import java.util.List;
import java.util.stream.Collectors;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.springboot.MyTodoList.bot.command.core.CommandResult;

public class BotResponse {
  private final List<SendMessage> response;
  private final CommandResult result;

  public BotResponse(List<SendMessage> response, CommandResult result) {
    this.response = response;
    this.result = result;
  }

  public BotResponse assertResult(CommandResult target) {
    // TODO: handle commandstate EXECUTE
    if (target.getState() == result.getState()) {
      return this;
    }

    throw new AssertionError(
        "Expected CommandResult " +
        target.getState().name() +
        ", but got: " +
        result.getState().name());
  }

  /**
     * Check if the response contains the expected text.
     * 
     * @param expectedText The text to look for
     * @return This BotResponse for method chaining
     * @throws AssertionError if the text is not found
     */
    public BotResponse assertContains(String expectedText) {
        for (SendMessage message : response) {
            if (message.getText().contains(expectedText)) {
                return this;
            }
        }
        throw new AssertionError(
            "Expected response to contain: " + expectedText +
            "\nBot response:\n" + response.stream().map(msg -> msg.getText()).collect(Collectors.joining("\n")));
    }
}


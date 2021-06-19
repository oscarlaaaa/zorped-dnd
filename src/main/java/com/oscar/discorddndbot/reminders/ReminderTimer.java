package com.oscar.discorddndbot.reminders;

import java.util.Timer;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

import io.github.cdimascio.dotenv.Dotenv;

public class ReminderTimer {

  /** Discord client to grab channels and such from */
  private GatewayDiscordClient client;

  /** Output channel for reminders */
  private TextChannel botChannelText;

  public ReminderTimer(GatewayDiscordClient disc) {
    client = disc;
    Dotenv hello = Dotenv.load();
    String output = System.getenv("DISCORD_OUTPUT_CHANNEL");
    // String output = hello.get("DISCORD_OUTPUT_CHANNEL");
    Snowflake x = Snowflake.of(output);
    Mono<Channel> botChannelRaw = this.client.getChannelById(x);
    botChannelText = botChannelRaw.cast(TextChannel.class).block();
  }

  /**
   * Starts the timer to repopulate the reminder list every 24 hours.
   * @throws Exception - no client set
   */
  public void startPopulateList() throws Exception {
    if (client == null) throw new Exception("Client has not been set yet.");

    Timer populateReminderListTimer = new Timer();
    int day = 86400;
    // int day = 10; // for Testing purposes
    PopulateTask populateList = new PopulateTask();
    populateReminderListTimer.schedule(populateList, 0, day * 1000);
  }

    /**
   * Starts the timer to check the reminder list every hour for events within 5 hours of being due.
   * @throws Exception - no client set
   */
  public void startEventTimers() throws Exception {
    if (client == null) throw new Exception("Client has not been set yet.");

    Timer eventReminderTimer = new Timer();
    int sec = 3600;
    // int sec = 15;  // for Testing purposes
    ReminderTask checkReminders = new ReminderTask(botChannelText);
    eventReminderTimer.schedule(checkReminders, 0, sec * 1000);
  }
}

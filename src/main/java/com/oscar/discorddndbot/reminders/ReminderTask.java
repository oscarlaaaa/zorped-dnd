package com.oscar.discorddndbot.reminders;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import discord4j.core.object.entity.channel.TextChannel;

public class ReminderTask extends TimerTask {
  TextChannel channel;

  public ReminderTask(TextChannel channel) {
    this.channel = channel;
  }

  public void run() {
    List<Reminder> events = Schedule.getEvents();
    ZoneId zone = ZoneId.systemDefault();
    LocalDateTime now = LocalDateTime.now(zone);
    StringBuilder display = new StringBuilder("```\nUpcoming Reminders\n--------------------\n");
    
    if (events.size() != 0) {
      for (int i = 0; i < events.size(); i++) {
        LocalDateTime eventTime = events.get(i).getDateTime();
        Long diff = Duration.between(now, eventTime).toMinutes();
        System.out.println(diff);
        if (diff < 300) {
          display.append(events.get(i).toString());
          display.append("\n");
        }
      }

      display.append("```");
      channel.createMessage(display.toString()).block();

    } else {
      channel.createMessage("No reminders to remind yet.").block();
    }
  }
}

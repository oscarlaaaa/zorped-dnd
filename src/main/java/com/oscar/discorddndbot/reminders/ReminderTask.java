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
    Schedule.populateEvents();
    List<Reminder> events = Schedule.getEvents();
    ZoneId zone = ZoneId.systemDefault();
    LocalDateTime now = LocalDateTime.now(zone);
    
    if (events.size() != 0) {
      for (int i = 0; i < events.size(); i++) {
        LocalDateTime eventTime = events.get(i).getDateTime();
        Long diff = Duration.between(now, eventTime).toHours();
        if (diff < 5) {
          channel.createMessage(events.get(i).toString());
        }
      }
    } else {
      channel.createMessage("No reminders to remind yet.").block();
    }
  }
}

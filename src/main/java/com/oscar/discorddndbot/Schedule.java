package com.oscar.discorddndbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.time.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;

public class Schedule {

  private static List<Reminder> events;

  static {
    events = new ArrayList<Reminder>();
  }

  public Schedule() {
    // Not to be used
  }

  public static List<Reminder> getEvents() {
    return events;
  }

  public static void reminderHelp(MessageCreateEvent event) {
    event.getMessage().getChannel().block().createMessage("<!setreminder - sets a reminder with a date, time, and message to be reminded of on the day of.>").block();
    event.getMessage().getChannel().block().createMessage("To use setreminder: !setreminder YYYY-MM-DD(numerical) HH:MM(24-hour clock) message-goes-here").block();
    event.getMessage().getChannel().block().createMessage("----------------------------------------------------------------------------------------------").block();
    event.getMessage().getChannel().block().createMessage("<!deletereminder - deletes a reminder with the first word of the message being the input.>").block();
    event.getMessage().getChannel().block().createMessage("To use deletereminder: !deletereminder keyword").block();
    event.getMessage().getChannel().block().createMessage("----------------------------------------------------------------------------------------------").block();
    event.getMessage().getChannel().block().createMessage("<!displayreminders - displays a list of all stored reminders.>").block();
    event.getMessage().getChannel().block().createMessage("To use displayreminders: !displayreminders").block();
  }

  public static void setReminder(MessageCreateEvent event) {
    String content = event.getMessage().getContent();
    Scanner scan = new Scanner(content);

    // Get rid of initial "schedule" command
    scan.next();

    // Extract the date input
    String rawDate = scan.next();
    List<String> date = Arrays.asList(rawDate.split("-"));
    int year = Integer.parseInt(date.get(0));
    int month = Integer.parseInt(date.get(1));
    int day = Integer.parseInt(date.get(2));

    // Extract the time input (as a string)
    String time = scan.next();

    // Extract the message
    String message = "\"";
    
    while(scan.hasNext()) {
      message += scan.next() + " ";
    }

    message += "\"";

    // Create a new reminder, add it to events list, and sort the list so that earliest events come first
    Reminder scheduledReminder = new Reminder(year, month, day, time, message);
    events.add(scheduledReminder);
    events.sort(null);

    scan.close();

    event.getMessage().getChannel().block().createMessage("Reminder" + scheduledReminder.toString() + " added!").block();
  }

  public static void deleteReminder(MessageCreateEvent event) {
    String content = event.getMessage().getContent();
    Scanner scan = new Scanner(content);

    // Get rid of initial "deleteReminder" command
    scan.next();

    // Default value if no event found
    int eventToDelete = Integer.MAX_VALUE;

    if (scan.hasNext()) {
      for (int i = 0; i < events.size(); i++) {
        if (events.get(i).getMessage().startsWith(scan.next())) {
          eventToDelete = i;
          break;
        }
      }
    } 
    Reminder del = events.get(eventToDelete);

    if (eventToDelete != Integer.MAX_VALUE) {
      events.remove(eventToDelete);
      event.getMessage().getChannel().block().createMessage("Reminder " + del.toString() + " deleted!").block();
    } 

    scan.close();
  }

  public static void displayReminders(MessageCreateEvent event) {
    MessageChannel channel = event.getMessage().getChannel().block();
    if (events.size() == 0) {
      channel.createMessage("You have no reminders scheduled.").block();
    } else {
      channel.createMessage("Here is a list of all your scheduled events:").block();
      channel.createMessage("--------------------------------------------").block();
      for (Reminder r : events) {
        channel.createMessage(r.toString()).block();
      }
    }
  }

  public static ZonedDateTime currentTime() {
    return ZonedDateTime.now(ZoneId.of("GMT-7"));
  }

}

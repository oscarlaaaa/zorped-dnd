package com.oscar.discorddndbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class Schedule {

  private static List<Reminder> events;

  static {
    events = new ArrayList<Reminder>();
  }

  public Schedule() {
    // Not to be used
  }

  public static void setReminder(MessageCreateEvent event) {
    String content = event.getMessage().getContent();
    Scanner scan = new Scanner(content);

    // Get rid of initial "schedule" command
    String rawDate = scan.next();

    // Extract the date input
    rawDate = scan.next();
    List<String> date = Arrays.asList(rawDate.split("/"));
    int year = Integer.parseInt(date.get(0));
    int month = Integer.parseInt(date.get(1));
    int day = Integer.parseInt(date.get(2));

    // Extract the time input (as a string)
    String time = scan.next();

    // Extract the message
    String message = scan.next();

    // Create a new reminder, add it to events list, and sort the list so that earliest events come first
    Reminder scheduledReminder = new Reminder(year, month, day, time, message);
    events.add(scheduledReminder);
    events.sort(null);

    scan.close();

    event.getMessage().getChannel().block().createMessage("Reminder for " + message + " at " 
    + date + " " + time + " added!").block();
  }

}

package com.oscar.discorddndbot.reminders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.*;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;

public class Schedule {

  /**
   * Holds the loaded events for the current day + the next day as Reminder
   * objects.
   */
  private static List<Reminder> events;

  private static Connection myConnection;

  static {
    // Instantiate the events arraylist
    events = new ArrayList<Reminder>();

    // Connecting to SQL Database
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      myConnection = DriverManager.getConnection(
          "jdbc:mysql://b659e9727d513b:47503661@us-cdbr-east-04.cleardb.com/heroku_f64027d6875ce57?reconnect=true");

      System.out.println("Connection with SQL database established!");

      // Create SQL table if does not exist
      Statement createTables = myConnection.createStatement();
      createTables.executeUpdate("CREATE TABLE IF NOT EXISTS reminders(" 
        + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
        + "due_date DATE NOT NULL," 
        + "due_time TIME NOT NULL," 
        + "message TEXT" 
        + ");");

    } catch (Exception e) {
      System.out.println("There was an error with connecting to the database.");
    }

  }

  public Schedule() {
    // Not to be used
  }

  public static List<Reminder> getEvents() {
    return events;
  }

  public static void populateEvents() {

    // Clears event list of previous events
    events.clear();

    try {
      String reminderSql = "SELECT * FROM reminders WHERE due_date >= CURDATE() AND due_date < CUR_DATE() + INTERVAL 1 DAY ORDER BY due_date DESC, due_time DESC;";
      Statement grabNearbyEvents = myConnection.createStatement();
      ResultSet queryResults = grabNearbyEvents.executeQuery(reminderSql);

      while (queryResults.next()) {
        LocalDate date = queryResults.getDate("due_date").toLocalDate();
        LocalTime time = queryResults.getTime("due_time").toLocalTime();
        String message = queryResults.getString("message");

        LocalDateTime dateTime = LocalDateTime.of(date, time);

        Reminder reminder = new Reminder(dateTime, message);
        events.add(reminder);
      }

      System.out.println("Events added!");

    } catch (Exception e) {
      System.out.println("Error with grabbing events.");
    }

  }

  public static void reminderHelp(MessageCreateEvent event) {
    event.getMessage().getChannel().block()
        .createMessage(
            "```\n<!setreminder - sets a reminder with a date, time, and message to be reminded of on the day of.>\n"
                + "\tTo use setreminder: !setreminder YYYY-MM-DD(numerical) HH:MM(24-hour clock) message-goes-here\n\n"
                + "<!deletereminder - deletes a reminder with the first word of the message being the input.>\n"
                + "\tTo use deletereminder: !deletereminder keyword\n\n"
                + "<!displayreminders - displays a list of all stored reminders.>\n"
                + "\tTo use displayreminders: !displayreminders\n```")
        .block();
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
    LocalDate inputDate = LocalDate.of(year, month, day);
    Date sqlDate = Date.valueOf(inputDate);

    // Extract the time input (as a string)
    String time = scan.next();
    List<String> splittime = Arrays.asList(time.split(":"));
    int hour = Integer.parseInt(splittime.get(0));
    int min = Integer.parseInt(splittime.get(1));
    LocalTime inputTime = LocalTime.of(hour, min);
    Time sqlTime = Time.valueOf(inputTime);

    // Extract the message
    String message = "\"";

    while (scan.hasNext()) {
      message += scan.next() + " ";
    }

    message += "\"";

    try {
      PreparedStatement input = myConnection.prepareStatement("INSERT INTO reminders (due_date, due_time, message) values (?, ?, ?);");
      input.setDate(1, sqlDate);
      input.setTime(2, sqlTime);
      input.setString(3, message);
      input.executeUpdate();
      input.close();
      
      event.getMessage().getChannel().block().createMessage("Reminder added!").block();
    } catch (SQLException e) {
      event.getMessage().getChannel().block().createMessage("Failed to add reminder.").block();
    }

    scan.close();
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

  public static void displayAllReminders(MessageCreateEvent event) {
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

  public static void displayReminders(TextChannel channel) {
    if (events.size() == 0) {
      channel.createMessage("You have no reminders scheduled.").block();
    } else {
      channel.createMessage("Here is a list of your scheduled events for today and tomorrow:").block();
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

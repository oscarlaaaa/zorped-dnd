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

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Static Schedule class that handles all the functionality regarding reminders.
 * Handles the functionality for setting, deleting, displaying, populating, and holding
 * local events grabbed from the MySQL server. 
 * 
 * @author Oscar La
 * @version 2021
 */
public class Schedule {

  /**
   * Holds the loaded events for the current day + the next day as Reminder
   * objects.
   */
  private static List<Reminder> events;

  /**
   * MySQL connection derived from local .env file
   */
  private static Connection myConnection;

  static {
    // Instantiate the events arraylist
    events = new ArrayList<Reminder>();
    Dotenv dotenv = Dotenv.load();

    // Connecting to SQL Database
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      System.out.println(dotenv.get("MYSQL_CREDENTIAL"));
      myConnection = DriverManager.getConnection(dotenv.get("MYSQL_CREDENTIAL"));

      System.out.println("Connection with SQL database established!");

      // Create SQL table if does not exist
      Statement createTables = myConnection.createStatement();
      createTables.executeUpdate("CREATE TABLE IF NOT EXISTS reminder(" + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
          + "due_date DATE NOT NULL," + "due_time TIME NOT NULL," + "message TEXT" + ");");

    } catch (Exception e) {
      System.out.println("There was an error with connecting to the database.");
    }

  }

  public Schedule() {
    // Not to be used
  }

  /**
   * Getter for the local list of loaded events.
   * @return list of reminders defined in this file
   */
  public static List<Reminder> getEvents() {
    return events;
  }

  /**
   * Populates the event list with events due within the next 24 hours. Connects
   * to the MySQL server as connected in the above static definition to pull
   * reminder events in a sorted order.
   */
  public static void populateEvents() {
    // Clears event list of previous events
    events.clear();

    // Queries events in SQL where due dates are within 24 hours in the future;
    // order by due date and due time
    try {
      String reminderSql = "SELECT * FROM reminder WHERE due_date >= CURDATE() AND due_date < CURDATE() + INTERVAL 1 DAY ORDER BY due_date DESC, due_time DESC;";
      Statement grabNearbyEvents = myConnection.createStatement();
      ResultSet queryResults = grabNearbyEvents.executeQuery(reminderSql);

      // Adds all the queried results into the event list
      while (queryResults.next()) {
        int index = queryResults.getInt("id");
        LocalDate date = queryResults.getDate("due_date").toLocalDate();
        LocalTime time = queryResults.getTime("due_time").toLocalTime();
        String message = queryResults.getString("message");

        LocalDateTime dateTime = LocalDateTime.of(date, time);

        Reminder reminder = new Reminder(index, dateTime, message);
        events.add(reminder);
      }

      System.out.println("Events added to local list!");

    } catch (Exception e) {
      System.out.println("Error with grabbing events.");
      System.out.println(e);
    }
  }

  /**
   * Sends a message to the channel about the various commands and how to use the
   * bot. Takes in a message event and sends a message to the channel about how to
   * use the bot including the various commands, the formatting of the commands,
   * and the keywords to use.
   * 
   * @param event - the MessageCreateEvent from the channel calling this function
   */
  public static void reminderHelp(MessageCreateEvent event) {
    event.getMessage().getChannel().block()
        .createMessage(
            "```\n<!setreminder - sets a reminder with a date, time, and message to be reminded of on the day of.>\n"
                + "\tTo use setreminder: !setreminder YYYY-MM-DD(numerical) HH:MM(24-hour clock) message-goes-here\n\n"
                + "<!deletereminder - deletes a reminder with the first word of the message being the input.>\n"
                + "\tTo use deletereminder: !deletereminder keyword\n\n"
                + "<!displayallreminders - displays a list of all stored reminders.>\n"
                + "\tTo use displayallreminders: !displayallreminders\n```")
        .block();
  }

  /**
   * Inputs the requested reminder into the MySQL server and re-calls the
   * populateEvents method. Checks for valid inputs before querying the MySQL
   * server to insert a reminder entry for that reminder (to be finished).
   * 
   * @param event - the MessageCreateEvent from the channel calling this function
   */
  public static void setReminder(MessageCreateEvent event) {
    String content = event.getMessage().getContent();
    Scanner scan = new Scanner(content);

    // Get rid of initial "!schedule" command
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

    // Query the MySQL server to insert the reminder
    try {
      PreparedStatement input = myConnection
          .prepareStatement("INSERT INTO reminder (due_date, due_time, message) values (?, ?, ?);");
      input.setDate(1, sqlDate);
      input.setTime(2, sqlTime);
      input.setString(3, message);
      input.executeUpdate();
      input.close();
      populateEvents();
      event.getMessage().getChannel().block().createMessage("Reminder added!").block();
    } catch (SQLException e) {
      event.getMessage().getChannel().block().createMessage("Failed to add reminder.").block();
    }
    scan.close();
  }

  /**
   * Deletes the reminder from the list of reminders (TO BE REWORKED FOR MYSQL).
   * 
   * @param event - the MessageCreateEvent from the channel calling this function
   */
  public static void deleteReminder(MessageCreateEvent event) {
    String content = event.getMessage().getContent();
    Scanner scan = new Scanner(content);

    // Get rid of initial "deleteReminder" command
    scan.next();

    // Default value if no event found; otherwise, updated to index in local list to
    // delete
    Integer eventToDelete = null;
    Integer sqlEventToDelete = null;

    // Scan through the events list for the index of the reminder to delete
    if (scan.hasNext()) {
      for (int i = 0; i < events.size(); i++) {
        if (events.get(i).getMessage().startsWith(scan.next())) {
          eventToDelete = i;
          sqlEventToDelete = events.get(i).getIndex();
          break;
        }
      }
    }

    // Checks that both eventToDelete and sqlEventToDelete are not their default
    // values (means reminder not found).
    if (eventToDelete != null && sqlEventToDelete != null) {

      // Local removal
      events.remove(eventToDelete.intValue());

      // MySQL removal
      try {
        PreparedStatement deleteEntry = myConnection.prepareStatement("DELETE FROM reminder WHERE id = ?;");
        deleteEntry.setInt(1, sqlEventToDelete.intValue());
        deleteEntry.executeUpdate();

        event.getMessage().getChannel().block()
            .createMessage("Reminder " + events.get(eventToDelete).toString() + " deleted!").block();
      } catch (SQLException e) {
        // Sends error message into the channel when failed.
        event.getMessage().getChannel().block().createMessage(e.getMessage()).block();
        event.getMessage().getChannel().block().createMessage("Reminder deletion has failed.").block();
      }
    }
    scan.close();
  }

  /**
   * The periodic rmeinder call that displays all reminders scheduled for the 24h
   * time period.
   * 
   * @param channel - the TextChannel to send the regularly scheduled messages
   *                into
   */
  public static void deleteAllReminders(MessageCreateEvent event) {
    // Clear all local events
    events.clear();

    // Delete all entries in the reminders table in MySQL
    try {
      String delete = "DELETE FROM reminder;";
      Statement deleteAll = myConnection.createStatement();
      deleteAll.executeUpdate(delete);
      event.getMessage().getChannel().block().createMessage("All reminders deleted.").block();
    } catch (SQLException e) {
      event.getMessage().getChannel().block().createMessage("Error in deleting all reminders.").block();
      event.getMessage().getChannel().block().createMessage(e.getMessage()).block();
    }
  }

  /**
   * Displays all reminders currently stored for the 24-hour period (TO BE
   * REWORKED INTO CALLING ALL REMINDERS FROM THE MYSQL DATABASE).
   * 
   * @param event - the MessageCreateEvent from the channel calling this function
   */
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

  /**
   * The periodic rmeinder call that displays all reminders scheduled for the 24h
   * time period.
   * 
   * @param channel - the TextChannel to send the regularly scheduled messages
   *                into
   */
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

  /**
   * Returns the current time for the Vancouver/BC time zone.
   * 
   * @return a ZonedDateTime instance for comparison against reminder due
   *         dates/times.
   */
  public static ZonedDateTime currentTime() {
    return ZonedDateTime.now(ZoneId.of("GMT-7"));
  }

}

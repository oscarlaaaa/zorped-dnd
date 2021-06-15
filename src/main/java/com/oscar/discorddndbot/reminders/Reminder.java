package com.oscar.discorddndbot.reminders;

import java.time.LocalDateTime;

/**
 * A reminder object to be stored in schedule. 
 * Originally used to store reminders in an ArrayList to keep track, but now only reminders from 
 * the next day and the current day will be loaded from the MySQL database.
 * 
 * @version 2021
 * @author Oscar La
 */
public class Reminder {

  /** Date and Time set for the reminder to be parsed as LocalTime in Schedule */
  private LocalDateTime dateTime;

  /** Message set to be displayed when the time arrives */
  private String message;

  /** 
   * Constructor for a Reminder object; takes in ints for year-month-day, Strings for time and message.
   * Used to easily store queried reminders without having to access the MySQL server every time
   * 
   * @param datetime - what time the reminder's event is set to occur in the format of HH:MM (24-hour format)
   * @param message - the reminder message to be stored
   */
  public Reminder(LocalDateTime dateTime, String message) throws IllegalArgumentException {
    this.dateTime = dateTime;
    this.message = message;
  }

  /**
   * Getter for a reminder's date-time.
   * @return - LocalDateTime object from the reminder.
   */
  public LocalDateTime getDateTime() {
    return dateTime;
  }

  /**
   * Getter for a reminder's message.
   * @return - the reminder's message in String object form.
   */
  public String getMessage() {
    return message;
  }

  /**
   * New toString method formatted in my preferred way to display them.
   * @return - the reminder formatted into a String object.
   */
  @Override
  public String toString() {
    return dateTime.toString() + ": " + message;
  }
}

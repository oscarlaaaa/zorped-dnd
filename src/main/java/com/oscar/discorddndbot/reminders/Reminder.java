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
   * 
   * @param datetime - what time the reminder's event is set to occur in the format of HH:MM (24-hour format)
   * @param message - the reminder message to be stored
   * 
   */
  public Reminder(LocalDateTime dateTime, String message) throws IllegalArgumentException {
    // REMINDER: Need to add a check for time as well
    this.dateTime = dateTime;
    this.message = message;
  }

  public LocalDateTime getDateTime() {
    return dateTime;
  }

  public String getMessage() {
    return message;
  }

  // /** 
  //  * Compares the reminder with a ZonedDateTime to see if it is ready to be reminded. 
  //  * 
  //  * @param zdt - a ZonedDateTime of the current date/time
  //  * @return boolean - true if zdt day matches current date
  //  */
  // public boolean readyToRemind(ZonedDateTime zdt) {
  //   Reminder comparethis = new Reminder(zdt.getYear(), zdt.getMonthValue(), zdt.getDayOfMonth(), "", "");
  //   return (this.compareTo(comparethis) == 0);
  // }

  // @Override
  // public int compareTo(Reminder r) {
  //   if (this.year < r.getYear() || (this.year == r.getYear() && this.month < r.getMonth())
  //   || this.year == r.getYear() && this.month == r.getMonth() && this.day < r.getDay()) {
  //     return -1;
  //   } else if (this.year == r.getYear() && this.month == r.getMonth() && this.day == r.getDay()){
  //     return 0;
  //   } else {
  //     return 1;
  //   }
  // }

  @Override
  public String toString() {
    return dateTime.toString() + ": " + message;
  }
}

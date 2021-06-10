package com.oscar.discorddndbot;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class Reminder implements Comparable<Reminder> {

  /** Year set for the reminder */
  private int year;

  /** Month set for the reminder */
  private int month;

  /** Day set for the reminder */
  private int day;

  /** Time set for the reminder to be parsed as LocalTime in Schedule */
  private String time;

  /** Message set to be displayed when the time arrives */
  private String message;

  /** Constructor for a Reminder object; takes in ints for year-month-day, Strings for time and message */
  public Reminder(int year, int month, int day, String time, String message) throws IllegalArgumentException {
    LocalDate today = LocalDate.now();
    if (year < today.getYear() || month < today.getMonthValue() || month > 12 || month < 1) {
      throw new IllegalArgumentException("The entered day is not valid.");
    }

    // REMINDER: Need to add a check for time as well

    this.year = year;
    this.month = month;
    this.day = day;
    this.time = time;
    this.message = message;

  }

  public int getYear() {
    return year;
  }

  public int getMonth() {
    return month;
  }

  public int getDay() {
    return day;
  }

  public String getTime() {
    return time;
  }

  public String getMessage() {
    return message;
  }

  /** Compares the reminder with a ZonedDateTime to see if it is ready to be reminded */
  public boolean readyToRemind(ZonedDateTime zdt) {
    Reminder comparethis = new Reminder(zdt.getYear(), zdt.getMonthValue(), zdt.getDayOfMonth(), "", "");
    return (this.compareTo(comparethis) == 0);
  }

  @Override
  public int compareTo(Reminder r) {
    if (this.year < r.getYear() || (this.year == r.getYear() && this.month < r.getMonth())
    || this.year == r.getYear() && this.month == r.getMonth() && this.day < r.getDay()) {
      return -1;
    } else if (this.year == r.getYear() && this.month == r.getMonth() && this.day == r.getDay()){
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  public String toString() {
    return year + "-" + month + "-" + day + " @ " + time + ": " + message;
  }
}

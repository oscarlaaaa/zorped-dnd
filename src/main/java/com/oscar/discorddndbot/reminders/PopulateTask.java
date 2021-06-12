package com.oscar.discorddndbot.reminders;

import java.util.TimerTask;

public class PopulateTask extends TimerTask {
  
  public void run() {
    Schedule.populateEvents();
  }
}

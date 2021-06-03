package com.oscar.discorddndbot;

public class DnD {

  public static int roll(int d, int m) {
    return (int) Math.floor(Math.random() * d) + m;
  }

}

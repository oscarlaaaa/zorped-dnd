package com.oscar.discorddndbot;

public class Player {

    private String name = "Un-named Player";

    private int Initiative;

    public Player(String n) {

        name = n;

    }

    public void setName(String n) {
        name = n;
    }
}
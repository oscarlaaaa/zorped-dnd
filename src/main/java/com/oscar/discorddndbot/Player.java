package com.oscar.discorddndbot;

import java.util.*;

public class Player {

    private String name = "Un-named Player";
    private String owner;
    private HashMap<String, Integer> stats = new HashMap<String, Integer>();

    private int initiative;

    public Player(String n, String o) {
        name = n;
        owner = o;

    }

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public void setStat(String stat, int value) {
        stats.put(stat, value);
    }

    public int getStat(String stat) {
        if (stats.containsKey(stat)) {
            return stats.get(stat);
        } else
            return 0;
    }

    public void setInitiative(int i) {
        initiative = i;
    }

    public int getInitiative() {
        return initiative;
    }

    public String getOwner() {
        return owner;
    }
}
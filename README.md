# Zorped Discord Bot

## Table of Contents
- [Mission](#mission)
- [Technology](#technology)
- [Configuration](#configuration)
- [Commands](#commands)
- [References](#references)

## Mission
This project serves as a discord bot for the purpose of managing reminders as well as performing various Dungeons
and Dragons gameplay functions. The goal was to produce an integrated service that provides various quality-of-life
functionalities to groups looking to plan DnD events together.

## Technology
- Heroku
- Java
- Discord4j
- MySQL (clearDB)

## Configuration
This project is currently gets hosted locally run through the command line, with the required tokens being stored in a local .env file in the root folder. You can use this bot either locally or remotely, depending on preference.
### Locally
1. Clone the repository
2. Edit the placeholder value in Bot.java for the discord bot token to your own (learn how to create one at https://www.writebots.com/discord-bot-token/)
3. Edit the placeholder value in Schedule.java for the MySQL server credentials to your own (learn to set one up at https://devcenter.heroku.com/articles/cleardb)
4. Add your bot to your discord channel
5. Edit the value in ReminderTimer.java (line 21) to your desired output channel's id (learn how at https://docs.statbot.net/docs/faq/general/how-find-id/#:~:text=To%20get%20a%20Channel%20ID,the%20number%20is%20the%20ID.)
6. Run the program locally either through cmd or powershell

### Remotely (to be finished)

## Commands
### Reminder Commands:
- Setting a reminder:
  - syntax: !setreminder YYYY-MM-DD HH:MM message
  - function: Sets a reminder and uploads it into the MySQL server with the date, time, and message.
- Deleting a reminder:
  - syntax: !deletereminder start-of-message
  - function: Deletes the first reminder that contains the given keyword.
- Delete all reminders:
  - syntax: !deleteallreminders
  - function: Deletes all reminders in the database. Not really a smart command but was fun to add.
- Display all reminders:
  - syntax: !displayallreminders
  - function: Displays all reminders currently present in the database.
- Reminder commands:
  - syntax: !reminderhelp
  - function: Displays instructions on how to use the reminder commands as well as the necessary syntax.

### DnD Commands:
- add Player:
  syntax: !addPlayer <Player-Name> without spaces
  function: adds a player to the game.
- remove Player:
  syntax !removePlayer <Player-Name> without spaces
  function: removes the player from the game.
- list Players:
  **work in progress**
- roll:
  syntax: !roll <string of rolls>, use #d# dice rolls, use # for constant modifier, use '+' to string rolls together.
  function: rolls the list of dices and returns a single rolled value.
- skill
  **work in progress**
  function: roll a single d20 for a corresponding stat, looks up player in the game list for modifiers

## References:
- https://github.com/Discord4J/Discord4J
- https://discord4j.readthedocs.io/en/latest/

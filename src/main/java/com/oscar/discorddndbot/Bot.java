package com.oscar.discorddndbot;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Mono;

import java.util.*;

import com.oscar.discorddndbot.dnd.DnD;
import com.oscar.discorddndbot.music.LavaPlayerAudioProvider;
import com.oscar.discorddndbot.music.TrackScheduler;
import com.oscar.discorddndbot.reminders.PopulateTask;
import com.oscar.discorddndbot.reminders.ReminderTask;
import com.oscar.discorddndbot.reminders.Schedule;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public class Bot {

  private static final Map<String, Command> commands = new HashMap<>();

  public static void main(String[] args) {

    // Sets up the audio player
    final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
    AudioSourceManagers.registerRemoteSources(playerManager);
    final AudioPlayer player = playerManager.createPlayer();
    AudioProvider provider = new LavaPlayerAudioProvider(player);

    commands.put("hello", event -> event.getMessage().getChannel().block().createMessage("hello").block());

    commands.put("join", event -> {
      final Member member = event.getMember().orElse(null);
      if (member != null) {
        final VoiceState voiceState = member.getVoiceState().block();
        if (voiceState != null) {
          final VoiceChannel channel = voiceState.getChannel().block();
          if (channel != null) {
            channel.join(spec -> spec.setProvider(provider)).block();
          }
        }
      }
    });
    final TrackScheduler scheduler = new TrackScheduler(player);



    // All dnd-related commands
    commands.put("dndmusic", event -> {
      String output = DnD.music(event, provider);
      if (output.equals("stop")) {
        scheduler.stopPlaying();
      } else {
        playerManager.loadItem(output, scheduler);
      }
    });
    commands.put("roll", event -> DnD.roll(event));
    commands.put("skill", event -> DnD.skill(event));
    commands.put("addPlayer", event -> DnD.addPlayer(event));
    commands.put("removePlayer", event -> DnD.removePlayer(event));

    // All reminder-related commands
    commands.put("setreminder", event -> Schedule.setReminder(event));
    commands.put("deletereminder", event -> Schedule.deleteReminder(event));
    commands.put("displayallreminders", event -> Schedule.displayAllReminders(event));
    commands.put("reminderhelp", event -> Schedule.reminderHelp(event));

    
    // builds the client and logs in
    GatewayDiscordClient client = DiscordClientBuilder.create("xdxd").build().login().block();

    client.getEventDispatcher().on(ReadyEvent.class).subscribe((event) -> {
      final User self = event.getSelf();
      System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));

      // Grabs text channel and submits to remindertask
      Snowflake x = Snowflake.of("826195539824607262");
      Mono<Channel> botChannelRaw = client.getChannelById(x);
      TextChannel botChannelText = botChannelRaw.cast(TextChannel.class).block();

      // This timer is for repopulating the reminder list every day
      Timer populateReminderListTimer = new Timer();
      int day = 86400;
      PopulateTask populateList = new PopulateTask();
      populateReminderListTimer.schedule(populateList, 0, day * 1000);

      // This timer is for reminding people of their events multiple times a few hours before they're due
      Timer eventReminderTimer = new Timer();
      int sec = 3600;
      ReminderTask checkReminders = new ReminderTask(botChannelText);
      eventReminderTimer.schedule(checkReminders, 0, sec * 1000);
    });

    client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
      final String content = event.getMessage().getContent();

      for (final Map.Entry<String, Command> entry : commands.entrySet()) {
        if (content.startsWith("!" + entry.getKey())) {
          entry.getValue().execute(event);
          break;
        }
      }
    });

    client.onDisconnect().block();
  }
}

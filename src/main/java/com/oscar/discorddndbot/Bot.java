package com.oscar.discorddndbot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;

import java.util.*;

import com.oscar.discorddndbot.dnd.DnD;
import com.oscar.discorddndbot.music.LavaPlayerAudioProvider;
import com.oscar.discorddndbot.music.TrackScheduler;
import com.oscar.discorddndbot.reminders.ReminderTimer;
import com.oscar.discorddndbot.reminders.Schedule;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import io.github.cdimascio.dotenv.Dotenv;

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
    commands.put("deleteallreminders", event -> Schedule.deleteAllReminders(event));
    commands.put("displayallreminders", event -> Schedule.displayAllReminders(event));
    commands.put("reminderhelp", event -> Schedule.reminderHelp(event));

    // builds the client and logs in
    Dotenv dotenv = Dotenv.load();
    GatewayDiscordClient client = DiscordClientBuilder.create(dotenv.get("DISCORD_BOT_TOKEN")).build().login().block();

    client.getEventDispatcher().on(ReadyEvent.class).subscribe((event) -> {
      final User self = event.getSelf();
      System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));

      try {
        ReminderTimer reminderTimer = new ReminderTimer(client);
        reminderTimer.startEventTimers();
        reminderTimer.startPopulateList();
      } catch (Exception e) {
        System.out.println("Error with starting the reminder timer.");
      }
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

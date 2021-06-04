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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

public class Bot {

   private static final Map<String, Command> commands = new HashMap<>();

   public static void main(String[] args) {

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
      commands.put("music", event -> {
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

      commands.put("reminder", event -> Schedule.setReminder(event));

      // builds the client and logs in
      GatewayDiscordClient client = DiscordClientBuilder
            .create("ODQ5NzQ2MzgwMTgzNDM3MzYy.YLfp_A.dLo82O_4KJZiVV1-wWlYSyEpRvw").build().login().block();

      client.getEventDispatcher().on(ReadyEvent.class).subscribe((event) -> {
         final User self = event.getSelf();
         System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
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

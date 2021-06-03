package com.oscar.discorddndbot;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Random;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;

public class DnD {

  public static String music(MessageCreateEvent event, AudioProvider provider) {

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

    final String content = event.getMessage().getContent();
    final List<String> command = Arrays.asList(content.split(" "));

    switch (command.get(1)) {
      case "fight":

        event.getMessage().getChannel().block().createMessage("The fight begins!").block();
        return "https://www.youtube.com/watch?v=w0sUw735gRw";

      case "quest":

        event.getMessage().getChannel().block().createMessage("Quest received!").block();
        return "https://www.youtube.com/watch?v=A8qMyBWZNw0";

      case "victory":

        event.getMessage().getChannel().block().createMessage("Battle won!").block();
        return "https://www.youtube.com/watch?v=uz_sxVDuCV8";

      case "stop":

        event.getMessage().getChannel().block().createMessage("Stopping music.").block();
        return "stop";

      default:
        return "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
    }
  }

  public static void roll(MessageCreateEvent event) {

    try {
      final String input = event.getMessage().getContent();
      final List<String> tempList = Arrays.asList(input.split(" "));
      if (tempList.size() == 1) {
        event.getMessage().getChannel().block().createMessage("Invalid Roll command").block();
      } else {

        String rollStream = tempList.get(1);

        rollStream = rollStream.replaceAll("[+]", " ");

        Scanner scan = new Scanner(rollStream);

        String temp;

        int index, sides, times, rollTemp;

        int total = 0;

        String result = "";

        while (scan.hasNext()) {
          temp = scan.next().toLowerCase();

          index = temp.indexOf('d');
          if (index == 0) {
            event.getMessage().getChannel().block().createMessage("Invalid Roll command").block();

          } else if (index > 0) {
            times = Integer.parseInt(temp.substring(0, index));
            sides = Integer.parseInt(temp.substring(index + 1));
            rollTemp = diceRoll(sides, times);
            total += rollTemp;
            result = result + rollTemp + "(" + temp + ")" + " + ";

          } else if (index == -1) {
            total += Integer.parseInt(temp);
            result = result + Integer.parseInt(temp) + "(static) + ";
          }

        }

        result = result + " total: " + total;

        event.getMessage().getChannel().block().createMessage(result).block();

        scan.close();
      }
    } catch (Exception e) {
      event.getMessage().getChannel().block().createMessage("Invalid Roll command").block();
    }

  }

  private static int diceRoll(int side, int number) {
    Random rand = new Random();
    int total = 0;
    for (int index = 0; index < number; index++) {
      total = total + rand.nextInt(side) + 1;
    }

    return total;

  }

}

package fr.alkanife.alkabot.commands.music;

import fr.alkanife.alkabot.Alkabot;
import fr.alkanife.alkabot.commands.utils.Command;
import fr.alkanife.alkabot.music.AlkabotTrack;
import fr.alkanife.alkabot.music.Music;
import fr.alkanife.alkabot.music.MusicLoader;
import fr.alkanife.alkabot.playlists.Playlist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PlayerCommands {

    @Command(name = "play")
    public void play(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Music.connect(event.getMember());

        String url = event.getOption("input").getAsString();

        if (url.startsWith("https://open.spotify.com/playlist")) {
            MusicLoader.loadSpotifyPlaylist(event, url, false);
        } else {
            if (!Alkabot.isURL(url)) {
                Playlist playlist = Alkabot.getPlaylist(url);

                if (playlist == null)
                    url = "ytsearch: " + url;
                else
                    url = playlist.getUrl();
            }

            MusicLoader.load(event, url, false);
        }
    }

    @Command(name = "forceplay")
    public void forceplay(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Music.connect(event.getMember());

        String url = event.getOption("input").getAsString();

        if (url.startsWith("https://open.spotify.com/playlist")) {
            MusicLoader.loadSpotifyPlaylist(event, url, true);
        } else {
            if (!Alkabot.isURL(url)) {
                Playlist playlist = Alkabot.getPlaylist(url);

                if (playlist == null)
                    url = "ytsearch: " + url;
                else
                    url = playlist.getUrl();
            }

            MusicLoader.load(event, url, true);
        }
    }

    @Command(name = "remove")
    public void remove(SlashCommandInteractionEvent event) {

        if (Alkabot.getAudioPlayer().getPlayingTrack() == null) {
            event.reply(Alkabot.t("jukebox-command-no-current")).queue();
            return;
        }
        OptionMapping removeOption = event.getOption("input");
        long remove = 1;
        if (removeOption != null) {
            remove = removeOption.getAsLong();

            if (remove >= Alkabot.getTrackScheduler().getQueue().size()) {
                event.reply(Alkabot.t("jukebox-command-notenough")).queue();
                return;
            }
        }
        List<AlkabotTrack> aTracks = new ArrayList<>(Alkabot.getTrackScheduler().getQueue());
        try {
            AlkabotTrack t = aTracks.get(((int) remove) - 1);

            aTracks.remove(t);

            BlockingQueue<AlkabotTrack> newBlockingQueue = new LinkedBlockingQueue<>();

            for (AlkabotTrack audioTrack : aTracks)
                newBlockingQueue.offer(audioTrack);

            Alkabot.getTrackScheduler().setQueue(newBlockingQueue);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(Alkabot.t("jukebox-command-remove-title"));
            embedBuilder.setDescription("[" + t.getTitle() + "](" + t.getUrl() + ")"
                    + " " + Alkabot.t("jukebox-by") + " [" + t.getArtists() + "](" + t.getUrl() + ")");
            embedBuilder.setThumbnail(t.getThumbUrl());

            event.replyEmbeds(embedBuilder.build()).queue();
        } catch (Exception e) {
            event.reply(Alkabot.t("jukebox-command-remove-error")).queue();
        }
    }

    @Command(name = "skip")
    public void skip(SlashCommandInteractionEvent event) {
        if (Alkabot.getAudioPlayer().getPlayingTrack() == null) {
            event.reply(Alkabot.t("jukebox-command-no-current")).queue();
            return;
        }
        OptionMapping skipSize = event.getOption("input");
        int skip = 0;
        if (skipSize != null) {
            long skipLong = skipSize.getAsLong();

            if (skipLong >= Alkabot.getTrackScheduler().getQueue().size()) {
                event.reply(Alkabot.t("jukebox-command-notenough")).queue();
                return;
            }

            for (skip = 0; skip < skipLong; skip++)
                Alkabot.getTrackScheduler().getQueue().remove();
        }

        MusicLoader.play(Alkabot.getTrackScheduler().getQueue().poll());

        if (skipSize == null)
            event.reply(Alkabot.t("jukebox-command-skip-one")).queue();
        else
            event.reply(Alkabot.t("jukebox-command-skip-mult", String.valueOf(skip))).queue();
    }

    @Command(name = "stop")
    public void stop(SlashCommandInteractionEvent event) {
        event.reply(Alkabot.t("jukebox-command-stop")).queue();
        Alkabot.getGuild().getAudioManager().closeAudioConnection();
        //Music.reset();
    }

    @Command(name = "shuffle")
    public void shuffle(SlashCommandInteractionEvent event) {
        List<AlkabotTrack> audioTracks = new ArrayList<>(Alkabot.getTrackScheduler().getQueue());
        Collections.shuffle(audioTracks);
        BlockingQueue<AlkabotTrack> blockingQueue = new LinkedBlockingQueue<>();
        for (AlkabotTrack audioTrack : audioTracks)
            blockingQueue.offer(audioTrack);
        Alkabot.getTrackScheduler().setQueue(blockingQueue);
        event.reply(Alkabot.t("jukebox-command-shuffle")).queue();
    }

    @Command(name = "clear")
    public void clear(SlashCommandInteractionEvent event) {
        Alkabot.getTrackScheduler().setQueue(new LinkedBlockingQueue<>());
        event.reply(Alkabot.t("jukebox-command-clear")).queue();
    }

}
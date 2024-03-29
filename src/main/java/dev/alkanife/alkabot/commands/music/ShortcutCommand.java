package dev.alkanife.alkabot.commands.music;

import dev.alkanife.alkabot.command.AbstractCommand;
import dev.alkanife.alkabot.command.CommandManager;
import dev.alkanife.alkabot.lang.Lang;
import dev.alkanife.alkabot.music.data.Shortcut;
import dev.alkanife.alkabot.util.PagedList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ShortcutCommand extends AbstractCommand {

    public ShortcutCommand(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public String getName() {
        return "shortcut";
    }

    @Override
    public String getDescription() {
        return Lang.get("command.music.shortcut.description");
    }

    @Override
    public boolean isEnabled() {
        return alkabot.getConfig().getCommandConfig().getMusicCommandConfig().getShortcutCommandConfig().isBind()
                || alkabot.getConfig().getCommandConfig().getMusicCommandConfig().getShortcutCommandConfig().isUnbind()
                || alkabot.getConfig().getCommandConfig().getMusicCommandConfig().getShortcutCommandConfig().isInfo()
                || alkabot.getConfig().getCommandConfig().getMusicCommandConfig().getShortcutCommandConfig().isList();
    }

    @Override
    public SlashCommandData getCommandData() {
        SlashCommandData commandData = Commands.slash(getName(), getDescription());

        List<SubcommandData> subs = new ArrayList<>();

        if (alkabot.getConfig().getCommandConfig().getMusicCommandConfig().getShortcutCommandConfig().isBind())
            subs.add(new SubcommandData("bind", Lang.get("command.music.shortcut.bind.description"))
                    .addOption(OptionType.STRING, "name", Lang.get("command.music.shortcut.bind.input.name"), true)
                    .addOption(OptionType.STRING, "query", Lang.get("command.music.shortcut.bind.input.query"), true));

        if (alkabot.getConfig().getCommandConfig().getMusicCommandConfig().getShortcutCommandConfig().isUnbind())
            subs.add(new SubcommandData("unbind", Lang.get("command.music.shortcut.unbind.description"))
                    .addOption(OptionType.STRING, "name", Lang.get("command.music.shortcut.unbind.input.name"), true));

        if (alkabot.getConfig().getCommandConfig().getMusicCommandConfig().getShortcutCommandConfig().isInfo())
            subs.add(new SubcommandData("info", Lang.get("command.music.shortcut.info.description"))
                    .addOption(OptionType.STRING, "name", Lang.get("command.music.shortcut.info.input.name"), true));

        if (alkabot.getConfig().getCommandConfig().getMusicCommandConfig().getShortcutCommandConfig().isList())
            subs.add(new SubcommandData("list", Lang.get("command.music.shortcut.list.description"))
                    .addOption(OptionType.INTEGER, "page", Lang.get("command.music.shortcut.list.input.page"), false));

        if (!subs.isEmpty())
            commandData.addSubcommands(subs);

        return commandData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subCommand = event.getSubcommandName();

        event.deferReply().queue();

        switch (Objects.requireNonNull(subCommand)) {
            case "bind" -> {
                String name = Objects.requireNonNull(event.getOption("name")).getAsString();
                String query = Objects.requireNonNull(event.getOption("query")).getAsString();

                Shortcut shortcut = alkabot.getShortcut(name);

                if (shortcut != null) {
                    event.getHook().sendMessage(Lang.get("command.music.shortcut.bind.error.already_existing")).queue();
                    return;
                }

                shortcut = new Shortcut(name, query, event.getUser().getId(), new Date());

                try {
                    alkabot.getMusicData().getShortcutList().add(shortcut);
                    alkabot.updateMusicData();

                    event.getHook().sendMessage(
                            Lang.t("command.music.shortcut.bind.success")
                                    .parseShortcut(shortcut)
                                    .getValue()
                    ).queue();
                } catch (Exception e) {
                    event.getHook().sendMessage(Lang.get("command.music.shortcut.bind.error.failed")).queue();
                    alkabot.getLogger().error("Failed to bind a shortcut " + name + " to " + query + ":", e);
                }
            }

            case "unbind" -> {
                String name = Objects.requireNonNull(event.getOption("name")).getAsString();

                Shortcut shortcut = alkabot.getShortcut(name);

                if (shortcut == null) {
                    event.getHook().sendMessage(Lang.get("command.music.shortcut.unbind.error.not_existing")).queue();
                    return;
                }

                try {
                    alkabot.getMusicData().getShortcutList().remove(shortcut);
                    alkabot.updateMusicData();

                    event.getHook().sendMessage(
                            Lang.t("command.music.shortcut.unbind.success")
                                    .parseShortcut(shortcut)
                                    .getValue()
                    ).queue();
                } catch (Exception e) {
                    event.getHook().sendMessage(Lang.get("command.music.shortcut.unbind.error.failed")).queue();
                    alkabot.getLogger().error("Failed to remove a shortcut " + name + ":", e);
                }
            }

            case "list" -> {
                if (alkabot.getMusicData().getShortcutList().isEmpty()) {
                    event.getHook().sendMessage(Lang.get("command.music.shortcut.list.error.no_entries")).queue();
                    return;
                }

                int shortcutsSize = alkabot.getMusicData().getShortcutList().size();

                PagedList pagedList = new PagedList();

                if (!pagedList.parsePage(event, shortcutsSize, Lang.get("command.music.shortcut.list.error.out_of_range")))
                    return;

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(
                        Lang.t("command.music.shortcut.list.success.title")
                                .parseShortcutCount(alkabot.getMusicData())
                                .parseGuildName(alkabot.getGuild())
                                .getValue()
                );
                embed.setColor(Lang.getColor("command.music.shortcut.list.success.color"));
                embed.setThumbnail(
                        Lang.t("command.music.shortcut.list.success.icon")
                                .parseBotAvatars(alkabot)
                                .parseMemberAvatars(event.getMember())
                                .parseGuildAvatar(event.getGuild())
                                .getImage()
                );

                String shortcuts = pagedList.toStringList(index -> {
                    Shortcut shortcut = alkabot.getMusicData().getShortcutList().get(index);

                    return Lang.t("command.music.shortcut.list.success.shortcuts")
                            .parseShortcut(shortcut)
                            .parse("index", String.valueOf(index+1))
                            .getValue();
                });

                embed.setDescription(
                        Lang.t("command.music.shortcut.list.success.description")
                                .parse("shortcuts", shortcuts)
                                .parseShortcutCount(alkabot.getMusicData())
                                .parse("page", String.valueOf(pagedList.getPage()+1))
                                .parse("page_count", String.valueOf(pagedList.getPages()))
                                .getValue()
                );

                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }

            case "info" -> {
                String name = Objects.requireNonNull(event.getOption("name")).getAsString();

                Shortcut shortcut = alkabot.getShortcut(name);

                if (shortcut == null) {
                    event.getHook().sendMessage(Lang.get("command.music.shortcut.info.error.not_existing")).queue();
                    return;
                }

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(
                        Lang.t("command.music.shortcut.info.success.title")
                                .parseShortcut(shortcut)
                                .parseGuildName(alkabot.getGuild())
                                .getValue()
                );
                embed.setColor(Lang.getColor("command.music.shortcut.info.success.color"));
                embed.setThumbnail(
                        Lang.t("command.music.shortcut.info.success.icon")
                                .parseBotAvatars(alkabot)
                                .parseMemberAvatars(event.getMember())
                                .parseGuildAvatar(event.getGuild())
                                .getImage()
                );
                embed.setDescription(
                        Lang.t("command.music.shortcut.info.success.description")
                                .parseShortcut(shortcut)
                                .getValue()
                );

                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
}

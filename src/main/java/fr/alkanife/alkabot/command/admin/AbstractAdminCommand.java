package fr.alkanife.alkabot.command.admin;

import fr.alkanife.alkabot.Alkabot;
import fr.alkanife.alkabot.command.CommandManager;

public abstract class AbstractAdminCommand {

    public CommandManager commandManager;
    public Alkabot alkabot;

    public AbstractAdminCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
        this.alkabot = commandManager.getAlkabot();
    }

    public abstract String getName();
    public abstract String getUsage();
    public abstract String getDescription();
    public abstract boolean isDiscordOnly();

    public abstract void execute(AdminCommandExecution execution);
}
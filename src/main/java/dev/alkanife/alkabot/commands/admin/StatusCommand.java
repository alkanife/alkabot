package dev.alkanife.alkabot.commands.admin;

import dev.alkanife.alkabot.command.admin.AbstractAdminCommand;
import dev.alkanife.alkabot.command.admin.AdminCommandExecution;
import dev.alkanife.alkabot.command.CommandManager;
import dev.alkanife.alkabot.util.MemoryUtils;
import net.dv8tion.jda.api.entities.SelfUser;

import java.lang.management.ManagementFactory;
import java.time.Duration;

public class StatusCommand extends AbstractAdminCommand {

    public StatusCommand(CommandManager commandManager) {
        super(commandManager);
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getUsage() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Uptime & RAM usage";
    }

    @Override
    public boolean isDiscordOnly() {
        return false;
    }

    @Override
    public void execute(AdminCommandExecution execution) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[STATUS]\n\n");

        stringBuilder.append("Using Alkabot v").append(alkabot.getFullVersion()).append("\n");

        SelfUser selfUser = alkabot.getJda().getSelfUser();
        stringBuilder.append("Client: ").append(selfUser.getName()).append(" [").append(selfUser.getId()).append("]\n");

        Duration duration = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        String formattedElapsedTime = String.format("%d days, %02d hours, %02d minutes, %02d seconds",
                duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        stringBuilder.append("Uptime: ").append(formattedElapsedTime).append("\n\n");
        stringBuilder.append("Gateway ping: ").append(alkabot.getJda().getGatewayPing()).append(" ms\n");

        stringBuilder.append("Memory usage:\n")
                .append(" - Max: ").append(MemoryUtils.humanReadableByteCountBin(MemoryUtils.getMaxMemory())).append("\n")
                .append(" - Used: ").append(MemoryUtils.humanReadableByteCountBin(MemoryUtils.getUsedMemory())).append("\n")
                .append(" - Total: ").append(MemoryUtils.humanReadableByteCountBin(MemoryUtils.getTotalMemory())).append("\n")
                .append(" - Free: ").append(MemoryUtils.humanReadableByteCountBin(MemoryUtils.getFreeMemory()));

        execution.reply(stringBuilder.toString());
    }
}

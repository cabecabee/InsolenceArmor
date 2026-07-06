package me.cabeca.insolencearmor.command;

import me.cabeca.insolencearmor.HiddenArmor;
import me.cabeca.insolencearmor.command.util.AbstractCommand;
import me.cabeca.insolencearmor.command.util.CommandStatus;
import me.cabeca.insolencearmor.manager.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TogglePumpkinCommand extends AbstractCommand {
    private final HiddenArmor plugin;

    public TogglePumpkinCommand(HiddenArmor plugin, String command) {
        super(plugin, command);
        this.plugin = plugin;
    }

    @Override
    public CommandStatus execute(CommandSender sender, Command command, String[] arguments) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return CommandStatus.SUCCESS;
        }

        Player player = (Player) sender;
        PlayerManager playerManager = plugin.getPlayerManager();

        if (playerManager.isPumpkinHelmetEnabled(player)) {
            playerManager.disablePumpkinHelmet(player, true);
            return CommandStatus.SUCCESS;
        }

        if (arguments.length < 1) {
            sender.sendMessage("Uso: /togglepumpkin <nome>");
            return CommandStatus.SUCCESS;
        }

        String pumpkinName = String.join(" ", arguments).trim();
        if (pumpkinName.isEmpty()) {
            sender.sendMessage("Uso: /togglepumpkin <nome>");
            return CommandStatus.SUCCESS;
        }

        playerManager.enablePumpkinHelmet(player, pumpkinName, true);
        return CommandStatus.SUCCESS;
    }
}
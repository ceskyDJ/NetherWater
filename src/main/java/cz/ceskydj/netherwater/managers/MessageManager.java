package cz.ceskydj.netherwater.managers;

import cz.ceskydj.netherwater.NetherWater;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

public class MessageManager {
    private final NetherWater plugin;
    private final ConfigManager configManager;

    public MessageManager(NetherWater plugin) {
        this.plugin = plugin;

        this.configManager = plugin.getConfigManager();
    }

    public void consoleMessage(String message, ChatColor color) {
        String fullMessage = this.configManager.getMessagePrefix().trim() + " " + ChatColor.RESET + color + message;

        this.plugin.getServer().getConsoleSender().sendMessage(fullMessage);
    }

    public void consoleMessage(String message) {
        this.consoleMessage(message, ChatColor.WHITE);
    }

    public void playerMessage(Player player, String message) {
        String fullMessage = this.configManager.getMessagePrefix().trim() + " " + ChatColor.RESET + message;

        player.sendMessage(fullMessage);
    }

    public void dump(String message) {
        if (this.configManager.isDebugOn()) {
            this.consoleMessage(message, ChatColor.YELLOW);
        }
    }

    public void sendMessage(Player player, String messageName) {
        this.sendMessage(player, messageName, null);
    }

    public void sendMessage(Player player, String messageName, Map<String, String> variables) {
        this.playerMessage(player, this.prepareMessage(messageName, variables));
    }

    public void sendMessage(ConsoleCommandSender console, String messageName) {
        this.sendMessage(console, messageName, null);
    }

    public void sendMessage(ConsoleCommandSender console, String messageName, Map<String, String> variables) {
        this.consoleMessage(this.prepareMessage(messageName, variables));
    }

    private String prepareMessage(String messageName, Map<String, String> variables) {
        String message = this.configManager.getMessage(messageName).trim();

        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", Objects.requireNonNull(entry.getValue()));
            }
        }

        return message;
    }
}

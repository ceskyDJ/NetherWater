package cz.ceskydj.netherwater.commands;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class NWReloadCommand implements CommandExecutor {
	private final NetherWater plugin;
	private final ConfigManager configManager;

	public NWReloadCommand(NetherWater plugin) {
		this.plugin = plugin;
		this.configManager = this.plugin.getConfigManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("netherwater.reload")) {
			sender.sendMessage(ChatColor.RED + this.configManager.getMessage("permissions"));
		} else {
			this.configManager.loadConfig();
			sender.sendMessage(ChatColor.GREEN + this.configManager.getMessage("config-reload"));
		}

		return true;
	}
}

package com.leetzilantonis.netherwater;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class NWReloadCommand implements CommandExecutor {
	Main plugin;
	
	public NWReloadCommand(Main plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("netherwater.reload")) {

			sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
			return true;

		} else {

			plugin.reloadConfig();
			sender.sendMessage(ChatColor.GREEN + "Nether Water configuration reloaded!");
			return true;

		}
	}
}

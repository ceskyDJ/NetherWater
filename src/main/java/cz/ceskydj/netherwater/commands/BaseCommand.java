package cz.ceskydj.netherwater.commands;

import cz.ceskydj.netherwater.NetherWater;
import cz.ceskydj.netherwater.exceptions.MissingPermissionException;
import cz.ceskydj.netherwater.managers.MessageManager;
import cz.ceskydj.netherwater.updater.UpdateChecker;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class BaseCommand implements CommandExecutor, TabCompleter {
    private final NetherWater plugin;
    private final MessageManager messageManager;

    public BaseCommand(NetherWater plugin) {
        this.plugin = plugin;

        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length < 1) {
                this.help(sender, label);

                return true;
            }

            switch (args[0]) {
                case "version":
                    this.version(sender);
                    break;
                case "check":
                    this.check(sender);
                    break;
                default:
                    this.help(sender, label);
            }

            return true;
        } catch (MissingPermissionException e) {
            if (sender instanceof Player) {
                this.messageManager.sendMessage((Player) sender, "command-permissions");
            } else if (sender instanceof ConsoleCommandSender) {
                this.messageManager.sendMessage((ConsoleCommandSender) sender, "command-permissions");
            }

            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String[] commands;
        if (args.length == 0) {
            commands = new String[]{"nw", "netherwater"};
        } else if (args.length == 1) {
            commands = new String[]{"help", "version", "check"};
        } else {
            return null;
        }

        List<String> searchedCommands = StringUtil.copyPartialMatches(args[0], Arrays.asList(commands), new ArrayList<>());

        return searchedCommands.stream().filter(cmd -> this.hasPermission(sender, cmd)).collect(Collectors.toList());
    }

    private boolean hasPermission(CommandSender player, String commandName) {
        if (player.hasPermission("netherwater.command.*")) {
            return true;
        }

        return player.hasPermission("netherwater.command." + commandName);
    }

    private void help(CommandSender sender, String command) throws MissingPermissionException {
        if (!this.hasPermission(sender, "help")) {
            throw new MissingPermissionException("Missing permission for command");
        }

        Map<String, String> variables = new TreeMap<>();
        variables.put("command", sender instanceof Player ? "/" + command : command);

        String[] rows = {"help.heading", "help.underline", "help.help", "help.version", "help.check"};

        if (sender instanceof Player) {
            Arrays.stream(rows).forEach(row -> this.messageManager.sendMessage((Player) sender, row, variables));
        } else if (sender instanceof ConsoleCommandSender) {
            Arrays.stream(rows).forEach(row -> this.messageManager.sendMessage((ConsoleCommandSender) sender, row, variables));
        }
    }

    private void version(CommandSender sender) throws MissingPermissionException {
        if (!this.hasPermission(sender, "version")) {
            throw new MissingPermissionException("Missing permission for command");
        }

        Map<String, String> variables = new TreeMap<>();
        variables.put("version", this.plugin.getDescription().getVersion());

        if (sender instanceof Player) {
            this.messageManager.sendMessage((Player) sender, "version", variables);
        } else if (sender instanceof ConsoleCommandSender) {
            this.messageManager.sendMessage((ConsoleCommandSender) sender, "version", variables);
        }
    }

    private void check(CommandSender sender) throws MissingPermissionException {
        if (!this.hasPermission(sender, "check")) {
            throw new MissingPermissionException("Missing permission for command");
        }

        UpdateChecker
                .of(this.plugin)
                .handleResponse((versionResponse, version) -> {
                    String message;

                    switch (versionResponse) {
                        case FOUND_NEW:
                            message = "check.old";
                            break;
                        case LATEST:
                            message = "check.latest";
                            break;
                        case UNAVAILABLE:
                        default:
                            message = "check.error";
                    }

                    Map<String, String> variables = new TreeMap<>();
                    variables.put("version", version);

                    if (sender instanceof Player) {
                        this.messageManager.sendMessage((Player) sender, message, variables);
                    } else if (sender instanceof ConsoleCommandSender) {
                        this.messageManager.sendMessage((ConsoleCommandSender) sender, message, variables);
                    }
                }).check();
    }
}

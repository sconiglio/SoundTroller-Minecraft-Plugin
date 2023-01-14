package me.soundtroller.soundtroller;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Sound;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public final class SoundTroller extends JavaPlugin implements Listener {

    // A list of sound aliases
    private List<String> aliases;

    // A list of the sounds for each sound alias
    private List<List<String>> sounds;

    private String permissionMessage;

    public SoundTroller() {
        aliases = new ArrayList<>();
        sounds = new ArrayList<>();
        permissionMessage = "";
    }

    @Override
    public void onEnable() {
        // Load the categories and rules from the config file
        loadConfig();

        // Save the default config file if it doesn't exist
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(this, this);

        permissionMessage = ChatColor.translateAlternateColorCodes('&', "&0[&2SoundTroller&0] &cYou do not have permission to use this command.");

        // this.getCommand("soundtroller").setPermissionMessage(ChatColor.translateAlternateColorCodes('&', permissionMessage));

        getServer().getConsoleSender().sendMessage(getPluginNamePrefix() + ChatColor.GREEN + "Plugin is enabled!");

    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(getPluginNamePrefix() + ChatColor.RED + "Plugin is disabled!");
    }

    public String getPluginNamePrefix() {
        return (ChatColor.BLACK + "[" + ChatColor.DARK_GREEN + "SoundTroller" + ChatColor.BLACK + "] " + ChatColor.RESET);
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getPlayer().getItemInHand();

        // Check if the event is a right click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Check if the item is a voucher
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return;
        }

        String displayName = meta.getDisplayName();
        if (!displayName.startsWith(ChatColor.GREEN +"SoundTroller Voucher - ")) {
            System.out.println("line 92");
            System.out.println("Actual display name: " + displayName);
            System.out.println("What it should start with:" + ChatColor.GREEN + "SoundTroller Voucher - ");
            return;
        }

        // Execute the command bound to the voucher
        String command = displayName.substring(ChatColor.GREEN.toString().length() + "SoundTroller Voucher - ".length());
        String[] commandArgs = command.split(" ");
        command = "soundtroller " + command;
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
        player.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "The command '" + command + "' has been executed.");
        item.setAmount(item.getAmount() - 1);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try{
            if(args.length == 0 && cmd.getName().equalsIgnoreCase("soundtroller") && sender.hasPermission("soundtroller.voucher") && sender.hasPermission("soundtroller.troll")){
                sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Usage: /soundtroller player <player> <alias>, /soundtroller random/all <alias>, /soundtroller voucher player <receiver> <player> <alias>, /soundtroller voucher random/all <receiver> <alias> | Available Sound Aliases (Categories): " + String.join(", ", aliases));
                return true;
            } else if (args.length == 0 && cmd.getName().equalsIgnoreCase("soundtroller") && sender.hasPermission("soundtroller.troll")) {
                sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Usage: /soundtroller player <player> <alias>, /soundtroller random/all <alias> | Available Sound Aliases (Categories): " + String.join(", ", aliases));
                return true;
            } else if (args.length == 0 && cmd.getName().equalsIgnoreCase("soundtroller") && sender.hasPermission("soundtroller.voucher")) {
                sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Usage: /soundtroller voucher player <receiver> <player> <alias>, /soundtroller voucher random/all <receiver> <alias> | Available Sound Aliases (Categories): " + String.join(", ", aliases));
                return true;
            }
            if (cmd.getName().equalsIgnoreCase("soundtroller")) {
                if ("voucher".equals(args[0])) {
                    if (!sender.hasPermission("soundtroller.voucher")) {
                        return true;
                    } else if (sender.hasPermission("soundtroller.voucher")) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "This command can only be used by players.");
                            return true;
                        }

                        // Check if the player provided a valid command to execute
                        if (args.length == 0 || args.length == 1) {
                            sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Usage: /soundtroller voucher player <receiver> <player> <alias>, /soundtroller voucher random/all <receiver> <alias>");
                            return true;
                        }

                        Player player = (Player) Bukkit.getPlayer(args[2]);

                        if ("player".equals(args[1])) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(args[1]);
                            sb.append(" ");
                            sb.append(args[3]);
                            sb.append(" ");
                            sb.append(args[4]);
                            sb.append(" ");
                            String aliasAndPlayer = sb.toString().trim();

//                            List<Player> onlinePlayers = (List<Player>) Bukkit.getServer().getOnlinePlayers();
//                            System.out.println("Hello");
//                            for(Player player2 : onlinePlayers){
//                                System.out.println(player2.getName());
//                            }
//                            if (!(onlinePlayers.contains(args[2]))){
//                                sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "The player that is to receive this voucher is not online. Please wait for the player to log onto the server, or select a different player. Or, you made a typo in your command.");
//                                return true;
//                            }

                            // Create the voucher item and add it to the player's inventory
                            ItemStack voucher = new ItemStack(Material.PAPER);
                            ItemMeta meta = voucher.getItemMeta();
                            meta.setDisplayName(ChatColor.GREEN + "SoundTroller Voucher - " + aliasAndPlayer);
                            voucher.setItemMeta(meta);
                            player.getInventory().addItem(voucher);

                            player.sendMessage(getPluginNamePrefix() + ChatColor.GREEN + "You have been given a voucher that can be used to execute a soundtroller command for the following alias and player: " + args[3] + " " + args[4]);

                            return true;
                        } else if ("random".equals(args[1]) || "all".equals(args[1])) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(args[1]);
                            sb.append(" ");
                            sb.append(args[3]);
                            sb.append(" ");
                            String aliasAndRandomOrAll = sb.toString().trim();

                            // Create the voucher item and add it to the player's inventory
                            ItemStack voucher = new ItemStack(Material.PAPER, 1);
                            ItemMeta meta = voucher.getItemMeta();
                            meta.setDisplayName(ChatColor.GREEN + "SoundTroller Voucher - " + aliasAndRandomOrAll);
                            voucher.setItemMeta(meta);
                            player.getInventory().addItem(voucher);

                            player.sendMessage(getPluginNamePrefix() + ChatColor.GREEN + "You have been given a voucher that can be used to execute a soundtroller command for the following alias and player condition: " + aliasAndRandomOrAll);

                            return true;
                        }
                        return true;
                    }
                }
                if ("player".equals(args[0]) || "random".equals(args[0]) || "all".equals(args[0])) {
                    if (!sender.hasPermission("soundtroller.troll")) {
                        sender.sendMessage(permissionMessage);
                        return true;
                    } else if (sender.hasPermission("soundtroller.troll")) {

                        // Check if the player provided a category name
                        if (args.length == 0 || args.length == 1) {
                            sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Usage: /soundtroller player <player> <alias>, /soundtroller random/all <alias> | Available Sound Aliases (Categories): " + String.join(", ", aliases));
                            return true;
                        }

                        if (args[0].toLowerCase().equals("player")) {
                            String playerName = args[1].toLowerCase();
                            Player player = Bukkit.getPlayer(playerName);

                            String alias = args[2].toLowerCase();
                            int index = aliases.indexOf(alias);
                            List<String> aliasSounds = sounds.get(index);

                            for (String sound : aliasSounds) {
                                Sound soundEnumValue = Sound.valueOf(sound);
                                Location location = player.getLocation();
                                player.playSound(location, soundEnumValue, 1.0f, 1.0f);
                            }
                            sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + player.getName() + " with " + String.join(", and ", aliasSounds));
                            return true;
                        } else if (args[0].toLowerCase().equals("all")) {

                            String alias = args[1].toLowerCase();
                            int index = aliases.indexOf(alias);
                            List<String> aliasSounds = sounds.get(index);

                            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                            System.out.println("hello");
                            System.out.println(onlinePlayers);
                            for (Player player : onlinePlayers) {
                                for (String sound : aliasSounds) {
                                    Sound soundEnumValue = Sound.valueOf(sound);
                                    Location location = player.getLocation();
                                    player.playSound(location, soundEnumValue, 1.0f, 1.0f);
                                }
                                sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + player.getName() + " with " + String.join(", and ", aliasSounds));
                            }
                            return true;
                        } else if (args[0].toLowerCase().equals("random")) {

                            String alias = args[1].toLowerCase();
                            int index = aliases.indexOf(alias);
                            List<String> aliasSounds = sounds.get(index);

                            Random random = new Random();
                            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                            Player player = onlinePlayers.get(random.nextInt(onlinePlayers.size()));

                            for (String sound : aliasSounds) {
                                Sound soundEnumValue = Sound.valueOf(sound);
                                Location location = player.getLocation();
                                player.playSound(location, soundEnumValue, 1.0f, 1.0f);
                            }
                            sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + player.getName() + " with " + String.join(", and ", aliasSounds));
                            return true;
                        } else {
                            sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "Invalid command. Please do /soundtroll for details on how to use a correct command.");
                        }
                        return true;
                    }
                }
            }
            if(args.length == 0 && cmd.getName().equalsIgnoreCase("soundtroller") && sender.hasPermission("soundtroller.voucher") && sender.hasPermission("soundtroller.troll")){
                sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Usage: /soundtroller player <player> <alias>, /soundtroller random/all <alias>, /soundtroller voucher player <receiver> <player> <alias>, /soundtroller voucher random/all <receiver> <alias> | Available Sound Aliases (Categories): " + String.join(", ", aliases));
                return true;
            } else if (args.length == 0 && cmd.getName().equalsIgnoreCase("soundtroller") && sender.hasPermission("soundtroller.troll")) {
                sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Usage: /soundtroller player <player> <alias>, /soundtroller random/all <alias> | Available Sound Aliases (Categories): " + String.join(", ", aliases));
                return true;
            } else if (args.length == 0 && cmd.getName().equalsIgnoreCase("soundtroller") && sender.hasPermission("soundtroller.voucher")) {
                sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Usage: /soundtroller voucher player <receiver> <player> <alias>, /soundtroller voucher random/all <receiver> <alias> | Available Sound Aliases (Categories): " + String.join(", ", aliases));
                return true;
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "A Java language IllegalArgument Exception occurred. This may be because you tried to use an alias or a sounds (aliases) name that do not exist within the config.yml file for the SoundTroller plugin. Please check the console for the full stack trace.");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            illegalArgumentException.printStackTrace(pw);
            Bukkit.getServer().getConsoleSender().sendMessage(sw.toString());
            return true;
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "A Java language IndexOutOfBoundsException occurred. This may be because the minecraft sounds for one or more of the sounds (aliases) in the config.yml contains a sound that does not exist in the version bukkit that this server's main jar file is using or is based on. A full list of supported sounds for the version of spigot that this plugin was made for (.19.2-R0.1-SNAPSHOT) can be found as a comment in the config.yml file, and that list of available sounds might hold true for all instances of 1.8 or at least 1.8.8 minecraft. Please check the console for the full stack trace.");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            indexOutOfBoundsException.printStackTrace(pw);
            Bukkit.getServer().getConsoleSender().sendMessage(sw.toString());
            return true;
        }
    }

    // A method to load the categories and rules from the config file
    private void loadConfig() {
        // Get the plugin's config file
        FileConfiguration config = getConfig();

        // Clear the current aliases and sounds
        aliases.clear();
        sounds.clear();

        // Load the categories from the config file
        List<String> configAliases = config.getStringList("aliases");
        for (String alias : configAliases) {
            addAlias(alias);
        }

        // Load the sounds from the config file
        for (String alias : configAliases) {
            List<String> aliasSounds = config.getStringList("sounds." + alias);
            for (String sound : aliasSounds) {
                addSounds(alias, sound);
            }
        }
    }

    public void addAlias(String name) {
        aliases.add(name);
        sounds.add(new ArrayList<>());
    }

    public void addSounds(String alias, String sound) {
        int index = aliases.indexOf(alias);
        if (index != -1) {
            sounds.get(index).add(sound);
        }
    }
}

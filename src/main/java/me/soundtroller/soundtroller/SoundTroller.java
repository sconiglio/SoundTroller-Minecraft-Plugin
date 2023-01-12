package me.soundtroller.soundtroller;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Sound;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public final class SoundTroller extends JavaPlugin {

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
        for (Sound sound : Sound.values()) {
            System.out.println(sound.name());
        }

        // Load the categories and rules from the config file
        loadConfig();

        // Save the default config file if it doesn't exist
        saveDefaultConfig();

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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try{
            if (cmd.getName().equalsIgnoreCase("soundtroll")) {
                if (!sender.hasPermission("soundtroll.use")) {
                    System.out.println("Test.");
                    sender.sendMessage(permissionMessage);
                    return true;
                }

                // Check if the player provided a category name
                if (args.length == 0) {
                    sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Usage: /soundtroll player <player> <alias>, /soundtroll random/all <alias> | Available Sound Aliases (Categories): " + String.join(", ", aliases));
                    return true;
                }

                if (args[0].toLowerCase().equals("player")) {
                    String playerName = args[1].toLowerCase();
                    Player player = Bukkit.getPlayer(playerName);
                    System.out.println(player.getName());

                    String alias = args[2].toLowerCase();
                    int index = aliases.indexOf(alias);
                    List<String> aliasSounds = sounds.get(index);
                    System.out.println(alias);
                    System.out.println(index);
                    System.out.println(aliasSounds);

                    for (String sound : aliasSounds) {
                        Sound soundEnumValue = Sound.valueOf(sound);
                        Location location = player.getLocation();
                        player.playSound(location, soundEnumValue, 1.0f, 1.0f);
                    }
                    sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + player.getName() + " with " + String.join(" with ", aliasSounds));
                    return true;
                } else if (args[0].toLowerCase().equals("all")) {

                    String alias = args[1].toLowerCase();
                    int index = aliases.indexOf(alias);
                    List<String> aliasSounds = sounds.get(index);

                    List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                    for (Player player : onlinePlayers) {
                        for (String sound : aliasSounds) {
                            Sound soundEnumValue = Sound.valueOf(sound);
                            Location location = player.getLocation();
                            player.playSound(location, soundEnumValue, 1.0f, 1.0f);
                        }
                        sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + player.getName() + " with " + String.join(", ", aliasSounds));
                    }
                    return true;
                } else if (args[0].toLowerCase().equals("random")) {

                    for (Sound sound : Sound.values()) {
                        System.out.println(sound.name());
                    }

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
                    sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + player.getName() + " with " + String.join(", ", aliasSounds));
                    return true;
                } else {
                    sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "Invalid command. Please do /soundtroll for details on how to use a correct command.");
                }
                return true;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "A Java language IllegalArgument Exception occurred. This may be because you tried to use an alias or a sounds (collection) name that do not exist within the config.yml file for the SoundTroller plugin. Please check the console for the full stack trace.");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            illegalArgumentException.printStackTrace(pw);
            Bukkit.getServer().getConsoleSender().sendMessage(sw.toString());
            return true;
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "A Java language IndexOutOfBoundsException occurred. This may be because the minecraft sounds for one or more of the sound (collections) in the config.yml contains a sound that does not exist in the version bukkit that this server's main jar file is using or is based on. A full list of supported sounds for the version of spigot that this plugin was made for (.19.2-R0.1-SNAPSHOT) can be found as a comment in the config.yml file, and that list of available sounds might hold true for all instances of 1.8 or at least 1.8.8 minecraft. Please check the console for the full stack trace.");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            indexOutOfBoundsException.printStackTrace(pw);
            Bukkit.getServer().getConsoleSender().sendMessage(sw.toString());
            return true;
        }
        return false;
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

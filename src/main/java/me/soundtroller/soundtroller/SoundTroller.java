package me.soundtroller.soundtroller;

//import net.luckperms.api.LuckPerms;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Sound;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.InputStreamReader;
import java.util.*;

public final class SoundTroller extends JavaPlugin implements Listener {

    // A list of sound aliases
    private List<String> aliases;

    // A list of the sounds for each sound alias
    private List<List<String>> sounds;

    private List<List<ArrayList<String>>> namesAndLores;

    private String permissionMessage;

    private List<String> lastTrolledPlayersWithRegularTroll;

    private HashMap<String, String> lastChatMessageOfPlayers;

    public SoundTroller() {
        aliases = new ArrayList<>();
        sounds = new ArrayList<>();
        namesAndLores = new ArrayList<>();
        permissionMessage = "";
        lastTrolledPlayersWithRegularTroll = new ArrayList<>();
        lastChatMessageOfPlayers = new HashMap<>();
    }

    @Override
    public void onEnable() {

//        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
//        if (provider != null) {
//            LuckPerms api = provider.getProvider();
//
//        }

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
//        if (!displayName.startsWith(ChatColor.GREEN +"soundtroller_voucher_")) {
//            return;
//        }

        // Execute the command bound to the voucher
        PersistentDataContainer container = meta.getPersistentDataContainer();
        for (String alias : aliases) {
            NamespacedKey key = new NamespacedKey(this, "soundtroller_" + alias);
            for (NamespacedKey container_key : container.getKeys()) {
                if (container_key.getNamespace().equals("soundtroller")) {
                    String keyString = "";
                    for (NamespacedKey containerKeyForLoop : container.getKeys()) {
                        keyString = containerKeyForLoop.toString();
                    }

                    String command;

                    int index = keyString.indexOf("soundtroller:");
                    if (index != -1) {
                        command = keyString.substring(index + "soundtroller:".length());
                        command = command.replace('_', ' ');
                    } else {
                        command = "";
                    }

                    if (command.contains("unknown")){
                        lastChatMessageOfPlayers.put(player.getName(), "0987654678908765");
                        player.sendMessage(getPluginNamePrefix() + ChatColor.BLUE + "Please enter the name of the player that you would like to troll as a chat message.");
                        while (lastChatMessageOfPlayers.get(player.getName()).equals("0987654678908765")){
                            continue;
                        }

                        Player onlinePlayer = Bukkit.getPlayer(lastChatMessageOfPlayers.get(player.getName()));
                        if(onlinePlayer != null){
                            command = command.replace("unknown", lastChatMessageOfPlayers.get(player.getName()));

                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);

                            index = aliases.indexOf(alias);
                            List<String> aliasSounds = sounds.get(index);

                            for(String playerThatGotTrolled : lastTrolledPlayersWithRegularTroll) {
                                player.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + playerThatGotTrolled + " with " + String.join(", and ", aliasSounds));
                            }
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.sendMessage(getPluginNamePrefix() + ChatColor.RED + "Error. This player is not online. Please try again.");
                        }
                    } else {
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);

                        index = aliases.indexOf(alias);
                        List<String> aliasSounds = sounds.get(index);

                        for(String playerThatGotTrolled : lastTrolledPlayersWithRegularTroll) {
                            player.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + playerThatGotTrolled + " with " + String.join(", and ", aliasSounds));
                        }
                        item.setAmount(item.getAmount() - 1);
                    }



//                    for (NamespacedKey container_key_loop : container.getKeys()) {
//                        System.out.println(container_key_loop.getNamespace());
//                        System.out.println(container_key_loop.toString());
//                    }
                    //                String uuidString = container.get(key, PersistentDataType.STRING);
                    //                UUID uuid = UUID.fromString(uuidString);
                    //
                    //                int index = aliases.indexOf(alias);
                    //                String command = displayName.substring(ChatColor.GREEN.toString().length() + "soundtroller_".length());
                    //                String[] commandArgs = command.split(" ");
                    //                command = "soundtroller " + command;
                    //                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
                    //                player.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "The command '" + command + "' has been executed.");
                    //                item.setAmount(item.getAmount() - 1);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        try {
            Player player = event.getPlayer();
            if (lastChatMessageOfPlayers.get(player.getName()).equals("0987654678908765")) {
                event.setCancelled(true);
                String message = event.getMessage();
                lastChatMessageOfPlayers.put(player.getName(), message);
            } else {
                event.setCancelled(false);
                String message = event.getMessage();
                lastChatMessageOfPlayers.put(player.getName(), message);
            }
        } catch (NullPointerException exception) {
            //Do nothing, this is here because of a strange bug where the line Player player = event.getPlayer(); was causing a NullPointerExcepton.
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try{
            if (cmd.getName().equalsIgnoreCase("soundtroller")) {
                if((args.length == 0 && (findSoundTrollerBaseStringAtStartOfStringInSet(sender) == true)) || (args.length == 0 && (sender instanceof ConsoleCommandSender)) || (args.length == 0 && (sender.isOp()))){
                    sender.sendMessage(dynamicSoundTrollerCommandNoParamMessage(sender));
                    return true;
                } else if (args.length == 0 && (findSoundTrollerBaseStringAtStartOfStringInSet(sender) == false)) {
                    sender.sendMessage(noPermissionsMessage());
                    return true;
                }

                if ("voucher".equals(args[0])) {
                    String alias = args[args.length - 2];
                    String aliasBaseVoucherAliasPermission = "soundtroller.voucher." + alias;

                    Player player = (Player) Bukkit.getPlayer(args[2]);

                    if ("player".equals(args[1])) {
                        alias = args[4];
                        String aliasPlayerVoucherAliasPermission = "soundtroller.voucher.player." + alias;
                        if ((sender.hasPermission("soundtroller.voucher")) || (sender.hasPermission(aliasBaseVoucherAliasPermission)) || (sender.hasPermission("soundtroller.voucher.player")) || (sender.hasPermission(aliasPlayerVoucherAliasPermission)) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(args[1]);
                            sb.append(" ");
                            sb.append(args[3]);
                            sb.append(" ");
                            sb.append(args[4]);
                            sb.append(" ");

                            String customVoucherName = "";
                            String customVoucherLore = "";

                            int i = 0;
                            int startIndex = 0;
                            int endIndex = 0;
                            if (args.length >= 6) {
                                if (args[5].startsWith("'") && args[5].endsWith("'")){
                                    startIndex = args[5].indexOf("'") + 1;
                                    endIndex = args[5].lastIndexOf("'");
                                    customVoucherName = args[5].substring(startIndex, endIndex);

                                    i = 5;
                                } else {
                                        for (i = 5; !(args[i].endsWith("'")); i++) {
                                            if (i == 5) {
                                                customVoucherName += args[5].substring(1);
                                                customVoucherName += " ";
                                            } else {
                                                customVoucherName += args[i];
                                                customVoucherName += " ";
                                            }
                                        }
                                    endIndex = args[i].lastIndexOf("'");
                                    customVoucherName += args[i].substring(0, endIndex);
                                    }

                                if (args.length > (i + 1)) {
                                    i++;
                                    int initialI = i;
//                                    if (args[6].startsWith("'") && args[6].endsWith("'")){
//                                        startIndex = args[6].indexOf("'") + 1;
//                                        endIndex = args[6].lastIndexOf("'");
//                                        System.out.println("args[6]:" + args[6]);
//                                        customVoucherLore = args[6].substring(startIndex, endIndex);
//                                    }
//                                    else {
                                        for (i = i; !(args[i].endsWith("'")); i++) {
                                            if (i == initialI) {
                                                customVoucherLore += args[i].substring(1);
                                                customVoucherLore += " ";
                                            } else {
                                                customVoucherLore += args[i];
                                                customVoucherLore += " ";
                                            }
                                        }
                                        endIndex = args[i].lastIndexOf("'");
                                        customVoucherLore += args[i].substring(0, endIndex);
//                                    }

                                }
                            }
//                            if (args.length > 6) {
////                                customVoucherLore = args[6];
//                                if ((args.length == 6) && (args[5].startsWith("'")) && (args[5].endsWith("'"))) {
//
//                                } else {
//                                    for (int i = 6; i < args.length; i++) {
//                                        customVoucherLore += args[i];
//                                        System.out.println(args[i]);
//                                    }
//                                }
//                            }
                            String aliasAndPlayer = sb.toString().trim().replace(" ", "_");
                            // Create the voucher item and add it to the player's inventory
                            ItemStack voucher = new ItemStack(Material.PAPER);
                            ItemMeta meta = voucher.getItemMeta();
                            int index = aliases.indexOf(alias);
                            if(customVoucherName.equals("")) {
                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', namesAndLores.get(index).get(0).get(0)));
                            } else {
                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customVoucherName));
                            }
                            ArrayList<String> loreArrayList = new ArrayList<>();
                            for(String string : namesAndLores.get(index).get(0).get(1).split(" ")){
                                loreArrayList.add(ChatColor.translateAlternateColorCodes('&', string));
                            }
                            if(customVoucherLore.equals("")) {
                                meta.setLore(loreArrayList);
                            } else {
                                loreArrayList.clear();
                                loreArrayList.add(ChatColor.translateAlternateColorCodes('&', customVoucherLore));
                                meta.setLore(loreArrayList);
                            }
                            PersistentDataContainer container = meta.getPersistentDataContainer();

                            NamespacedKey key = new NamespacedKey(this, "soundtroller_" + "player_" + args[3] + "_" + alias);
                            UUID uuid = UUID.randomUUID();
                            container.set(key, PersistentDataType.STRING, uuid.toString());
                            voucher.setItemMeta(meta);
                            player.getInventory().addItem(voucher);

                            player.sendMessage(getPluginNamePrefix() + ChatColor.GREEN + "You have been given a voucher that can be used to execute a soundtroller command for the following alias and player: " + args[3] + " " + args[4]);

                            return true;
                        } else if ((!(sender.hasPermission("soundtroller.voucher"))) && (!(sender.hasPermission(aliasBaseVoucherAliasPermission))) && (!(sender.hasPermission("soundtroller.voucher.player"))) && (!(sender.hasPermission(aliasPlayerVoucherAliasPermission)))) {
                            sender.sendMessage(invalidPermissionsMessage());
                            return true;
                        }
                    } else if ("random".equals(args[1])) {
                        alias = args[3];
                        String aliasRandomVoucherAliasPermission = "soundtroller.voucher.random." + alias;
                        if ((sender.hasPermission("soundtroller.voucher")) || (sender.hasPermission(aliasBaseVoucherAliasPermission)) || (sender.hasPermission("soundtroller.voucher.random")) || (sender.hasPermission(aliasRandomVoucherAliasPermission)) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(args[1]);
                            sb.append(" ");
                            sb.append(args[3]);
                            sb.append(" ");
                            String aliasAndRandomOrAll = sb.toString().trim();

                            String customVoucherName = "";
                            String customVoucherLore = "";

                            int i = 0;
                            int startIndex = 0;
                            int endIndex = 0;
                            if (args.length >= 5) {
                                if (args[4].startsWith("|") && args[4].endsWith("|")){
                                    startIndex = args[4].indexOf("|") + 1;
                                    endIndex = args[4].lastIndexOf("|");
                                    customVoucherName = args[4].substring(startIndex, endIndex);

                                    i = 4;
                                } else {
                                    for (i = 4; !(args[i].endsWith("|")); i++) {
                                        if (i == 4) {
                                            customVoucherName += args[4].substring(1);
                                            customVoucherName += " ";
                                        } else {
                                            customVoucherName += args[i];
                                            customVoucherName += " ";
                                        }
                                    }
                                    endIndex = args[i].lastIndexOf("|");
                                    customVoucherName += args[i].substring(0, endIndex);
                                }

                                if (args.length > (i + 1)) {
                                    i++;
                                    int initialI = i;
//                                    if (args[5].startsWith("'") && args[5].endsWith("'")){
//                                        startIndex = args[5].indexOf("'") + 1;
//                                        endIndex = args[5].lastIndexOf("'");
//                                        customVoucherName = args[5].substring(startIndex, endIndex);
//                                    }
//                                    else {
                                        for (i = i; !(args[i].endsWith("|")); i++) {
                                            if (i == initialI) {
                                                customVoucherLore += args[i].substring(1);
                                                customVoucherLore += " ";
                                            } else {
                                                customVoucherLore += args[i];
                                                customVoucherLore += " ";
                                            }
                                        }
                                        endIndex = args[i].lastIndexOf("|");
                                        customVoucherLore += args[i].substring(0, endIndex);
//                                    }

                                }
                            }

                            // Create the voucher item and add it to the player's inventory
                            ItemStack voucher = new ItemStack(Material.PAPER, 1);
                            ItemMeta meta = voucher.getItemMeta();
                            int index = aliases.indexOf(alias);
//                            System.out.println("aliases: " + aliases);
//                            System.out.println("namesAndLores: " + namesAndLores);
                            if(customVoucherName.equals("")) {
                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', namesAndLores.get(index).get(0).get(0)));
                            } else {
                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customVoucherName));
                            }
                            ArrayList<String> loreArrayList = new ArrayList<>();
                            for(String string : namesAndLores.get(index).get(0).get(1).split(" ")){
                                loreArrayList.add(ChatColor.translateAlternateColorCodes('&', string));
                            }
                            if(customVoucherLore.equals("")) {
                                meta.setLore(loreArrayList);
                            } else {
                                loreArrayList.clear();
                                loreArrayList.add(ChatColor.translateAlternateColorCodes('&', customVoucherLore));
                                meta.setLore(loreArrayList);
                            }
                            PersistentDataContainer container = meta.getPersistentDataContainer();

                            NamespacedKey key = new NamespacedKey(this, "soundtroller_" + "random_" + alias);
                            UUID uuid = UUID.randomUUID();
                            container.set(key, PersistentDataType.STRING, uuid.toString());
                            voucher.setItemMeta(meta);
                            player.getInventory().addItem(voucher);

                            player.sendMessage(getPluginNamePrefix() + ChatColor.GREEN + "You have been given a voucher that can be used to execute a soundtroller command for the following alias and player condition: " + aliasAndRandomOrAll);

                            return true;
                        } else if ((!(sender.hasPermission("soundtroller.voucher"))) && (!(sender.hasPermission(aliasBaseVoucherAliasPermission))) && (!(sender.hasPermission("soundtroller.voucher.random"))) && (!(sender.hasPermission(aliasRandomVoucherAliasPermission)))){
                            sender.sendMessage(invalidPermissionsMessage());
                            return true;
                        }
                    } else if ("all".equals(args[1])) {
                        alias = args[3];
                        String aliasAllVoucherAliasPermission = "soundtroller.voucher.all." + alias;
                        if ((sender.hasPermission("soundtroller.voucher")) || (sender.hasPermission(aliasBaseVoucherAliasPermission)) || (sender.hasPermission("soundtroller.voucher.all")) || (sender.hasPermission(aliasAllVoucherAliasPermission)) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(args[1]);
                            sb.append(" ");
                            sb.append(args[3]);
                            sb.append(" ");
                            String aliasAndRandomOrAll = sb.toString().trim();

                            String customVoucherName = "";
                            String customVoucherLore = "";

                            int i = 0;
                            int startIndex = 0;
                            int endIndex = 0;
                            if (args.length >= 5) {
                                if (args[4].startsWith("|") && args[4].endsWith("|")){
                                    startIndex = args[4].indexOf("|") + 1;
                                    endIndex = args[4].lastIndexOf("|");
                                    customVoucherName = args[4].substring(startIndex, endIndex);

                                    i = 4;
                                } else {
                                    for (i = 4; !(args[i].endsWith("|")); i++) {
                                        if (i == 4) {
                                            customVoucherName += args[4].substring(1);
                                            customVoucherName += " ";
                                        } else {
                                            customVoucherName += args[i];
                                            customVoucherName += " ";
                                        }
                                    }
                                    endIndex = args[i].lastIndexOf("|");
                                    customVoucherName += args[i].substring(0, endIndex);
                                }

                                if (args.length > (i + 1)) {
                                    i++;
                                    int initialI = i;
//                                    if (args[5].startsWith("'") && args[5].endsWith("'")){
//                                        startIndex = args[5].indexOf("'") + 1;
//                                        endIndex = args[5].lastIndexOf("'");
//                                        customVoucherName = args[5].substring(startIndex, endIndex);
//                                    }
//                                  else {
                                        for (i = i; !(args[i].endsWith("|")); i++) {
                                            if (i == initialI) {
                                                customVoucherLore += args[i].substring(1);
                                                customVoucherLore += " ";
                                            } else {
                                                customVoucherLore += args[i];
                                                customVoucherLore += " ";
                                            }
                                        }
                                        endIndex = args[i].lastIndexOf("|");
                                        customVoucherLore += args[i].substring(0, endIndex);
//                                    }

                                }
                            }

                            // Create the voucher item and add it to the player's inventory
                            ItemStack voucher = new ItemStack(Material.PAPER, 1);
                            ItemMeta meta = voucher.getItemMeta();
                            int index = aliases.indexOf(alias);
                            if(customVoucherName.equals("")) {
                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', namesAndLores.get(index).get(0).get(0)));
                            } else {
                                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customVoucherName));
                            }
                            ArrayList<String> loreArrayList = new ArrayList<>();
                            for(String string : namesAndLores.get(index).get(0).get(1).split(" ")){
                                loreArrayList.add(ChatColor.translateAlternateColorCodes('&', string));
                            }
                            if(customVoucherLore.equals("")) {
                                meta.setLore(loreArrayList);
                            } else {
                                loreArrayList.clear();
                                loreArrayList.add(ChatColor.translateAlternateColorCodes('&', customVoucherLore));
                                meta.setLore(loreArrayList);
                            }
                            PersistentDataContainer container = meta.getPersistentDataContainer();

                            NamespacedKey key = new NamespacedKey(this, "soundtroller_" + "all_" + alias);
                            UUID uuid = UUID.randomUUID();
                            container.set(key, PersistentDataType.STRING, uuid.toString());
                            voucher.setItemMeta(meta);
                            player.getInventory().addItem(voucher);

                            player.sendMessage(getPluginNamePrefix() + ChatColor.GREEN + "You have been given a voucher that can be used to execute a soundtroller command for the following alias and player condition: " + aliasAndRandomOrAll);

                            return true;
                        } else if ((!(sender.hasPermission("soundtroller.voucher"))) && (!(sender.hasPermission(aliasBaseVoucherAliasPermission))) && (!(sender.hasPermission("soundtroller.voucher.random"))) && (!(sender.hasPermission(aliasAllVoucherAliasPermission)))){
                            sender.sendMessage(invalidPermissionsMessage());
                            return true;
                        }
                    } else {
                        sender.sendMessage(invalidCommandMessage());
                        return true;
                    }
                    return true;
                } else if ("player".equals(args[0]) || "random".equals(args[0]) || "all".equals(args[0])) {
                    String alias = args[args.length - 1];

                    String aliasBaseTrollAliasPermission = "soundtroller.troll." + alias;
                    if (args[0].toLowerCase().equals("player")) {
                        String aliasPlayerTrollAliasPermission = "soundtroller.troll.player." + alias;
                        if((sender.hasPermission("soundtroller.troll")) || (sender.hasPermission(aliasBaseTrollAliasPermission)) || (sender.hasPermission("soundtroller.troll.player")) || (sender.hasPermission(aliasPlayerTrollAliasPermission)) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
                            String playerName = args[1].toLowerCase();
                            Player player = Bukkit.getPlayer(playerName);

                            int index = aliases.indexOf(alias);

                            List<String> aliasSounds = sounds.get(index);

                            for (String sound : aliasSounds) {
                                Sound soundEnumValue = Sound.valueOf(sound);
                                Location location = player.getLocation();
                                player.playSound(location, soundEnumValue, 1.0f, 1.0f);
                            }
                            sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + player.getName() + " with " + String.join(", and ", aliasSounds));
                            lastTrolledPlayersWithRegularTroll.clear();
                            lastTrolledPlayersWithRegularTroll.add(player.getName());
                            return true;
                        } else if ((!(sender.hasPermission("soundtroller.troll"))) && (!(sender.hasPermission(aliasBaseTrollAliasPermission))) && (!(sender.hasPermission("soundtroller.troll.player"))) && (!(sender.hasPermission(aliasPlayerTrollAliasPermission)))) {
                            sender.sendMessage(invalidPermissionsMessage());
                            return true;
                        }
                    } else if (args[0].toLowerCase().equals("all")) {
                        String aliasAllTrollAliasPermission = "soundtroller.troll.all." + alias;
                        if ((sender.hasPermission("soundtroller.troll")) || (sender.hasPermission(aliasBaseTrollAliasPermission)) || (sender.hasPermission("soundtroller.troll.all")) || (sender.hasPermission(aliasAllTrollAliasPermission)) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
                            alias = args[1].toLowerCase();
                            int index = aliases.indexOf(alias);
                            List<String> aliasSounds = sounds.get(index);

                            lastTrolledPlayersWithRegularTroll.clear();
                            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                            for (Player player : onlinePlayers) {
                                for (String sound : aliasSounds) {
                                    Sound soundEnumValue = Sound.valueOf(sound);
                                    Location location = player.getLocation();
                                    player.playSound(location, soundEnumValue, 1.0f, 1.0f);
                                }
                                sender.sendMessage(getPluginNamePrefix() + ChatColor.AQUA + "Trolled " + player.getName() + " with " + String.join(", and ", aliasSounds));
                                lastTrolledPlayersWithRegularTroll.add(player.getName());
                            }
                            return true;
                        } else if ((!(sender.hasPermission("soundtroller.troll"))) && (!(sender.hasPermission(aliasBaseTrollAliasPermission))) && (!(sender.hasPermission("soundtroller.troll.player"))) && (!(sender.hasPermission(aliasAllTrollAliasPermission)))) {
                            sender.sendMessage(invalidPermissionsMessage());
                            return true;
                        }
                    } else if (args[0].toLowerCase().equals("random")) {
                        String aliasRandomTrollAliasPermission = "soundtroller.troll.random." + alias;
                        if ((sender.hasPermission("soundtroller.troll")) || (sender.hasPermission(aliasBaseTrollAliasPermission)) || (sender.hasPermission("soundtroller.troll.random")) || (sender.hasPermission(aliasRandomTrollAliasPermission)) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
                            alias = args[1].toLowerCase();
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
                            lastTrolledPlayersWithRegularTroll.clear();
                            lastTrolledPlayersWithRegularTroll.add(player.getName());
                            return true;
                        } else if ((!(sender.hasPermission("soundtroller.troll"))) && (!(sender.hasPermission(aliasBaseTrollAliasPermission))) && (!(sender.hasPermission("soundtroller.troll.player"))) && (!(sender.hasPermission(aliasRandomTrollAliasPermission)))) {
                            sender.sendMessage(invalidPermissionsMessage());
                            return true;
                        }
                    } else {
                        sender.sendMessage(invalidCommandMessage());
                        return true;
                    }
                    return true;
                } else {
                    sender.sendMessage(invalidCommandMessage());
                }
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
//            sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "A Java language IllegalArgument Exception occurred. This may be because you tried to use an alias or a sounds (aliases) name that do not exist within the config.yml file for the SoundTroller plugin. Please check the console for the full stack trace.");
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            illegalArgumentException.printStackTrace(pw);
//            Bukkit.getServer().getLogger().severe(sw.toString());
            sender.sendMessage(invalidCommandMessage() + " This may be because of the alias that you're using.");
            return true;
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
//            sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "A Java language IndexOutOfBoundsException occurred. This may be because the minecraft sounds for one or more of the sounds (aliases) in the config.yml contains a sound that does not exist in the version bukkit that this server's main jar file is using or is based on. A full list of supported sounds for the version of spigot that this plugin was made for (1.19.2-R0.1-SNAPSHOT) can be found as a comment in the config.yml file, and that list of available sounds might hold true for all instances of 1.19 or at least 1.19.2 minecraft. Please check the console for the full stack trace.");
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            indexOutOfBoundsException.printStackTrace(pw);
//            Bukkit.getServer().getLogger().severe(sw.toString());
            sender.sendMessage(invalidCommandMessage() + " There may be an error in the server files, please contact the server's admins.");
            return true;
        } catch (Exception exception) {
//            sender.sendMessage(getPluginNamePrefix() + ChatColor.RED + "Something went wrong, and idk what tbh.");
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            exception.printStackTrace(pw);
//            Bukkit.getServer().getLogger().severe(sw.toString());
            sender.sendMessage(invalidCommandMessage() + " You managed to somehow break the system by giving it an error that we aren't even familiar with. Good job.");
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
        namesAndLores.clear();

        // Load the categories from the config file
        List<String> configAliases = config.getStringList("aliases");
        for (String alias : configAliases) {
            addAlias(alias);
            namesAndLores.add(new ArrayList<ArrayList<String>>());
        }

        // Load the sounds from the config file
        for (String alias : configAliases) {
            List<String> aliasSounds = config.getStringList("sounds." + alias);
            for (String sound : aliasSounds) {
                addSounds(alias, sound);
            }
        }

        for (String alias : configAliases) {
            List<String> aliasNamesAndLores = config.getStringList("voucherNames." + alias);
            for (String sound : aliasNamesAndLores) {
                addNamesAndLores(alias, sound);
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

    public void addNamesAndLores(String alias, String NameOrLore) {
        int index = aliases.indexOf(alias);
        if (index != -1) {
            String[] array = NameOrLore.split("\\|");
            ArrayList<String> list = new ArrayList<>();
            for(String string : array){
                list.add(string);
            }
            namesAndLores.get(index).add(list);
        }
    }

    public String getPluginNamePrefix() {
        return (ChatColor.BLACK + "[" + ChatColor.DARK_GREEN + "SoundTroller" + ChatColor.BLACK + "] " + ChatColor.RESET);
    }

    public String invalidCommandMessage() {
        return (getPluginNamePrefix() + ChatColor.RED + "Invalid command. Please do /soundtroll for details on how to use a correct command.");
    }

    public String invalidPermissionsMessage() {
        return (getPluginNamePrefix() + ChatColor.RED + "Invalid permissions. Please do /soundtroll for details on how to use a command that you have access to.");
    }

    public String noPermissionsMessage() {
        return (getPluginNamePrefix() + ChatColor.RED + "You do not have the permissions for any SoundTroller commands.");
    }

    public String dynamicSoundTrollerCommandNoParamMessage(CommandSender sender){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "-----------------" + ((getPluginNamePrefix()).replaceAll(" ", "")) + ChatColor.DARK_PURPLE + "------------------" + "\n");
        stringBuilder.append(ChatColor.DARK_PURPLE + "Usage: " + "\n");

        if(findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerTrollPlayerStringAtStartOfStringInSet(sender) || findSoundTrollerTrollAnyAliasStringAtEndOfStringInSet(sender) || findSoundTrollerTrollPlayerAnyAliasStringAtEndOfStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
            stringBuilder.append(ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n");
            stringBuilder.append(ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "player <player> <alias> \n");
            stringBuilder.append(ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", collectSoundTrollerAnyAvailableAliasesForTrollPlayer(sender)) + "\n");
        }

        if (findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerTrollAnyAliasStringAtEndOfStringInSet(sender) || findSoundTrollerTrollRandomStringAtStartOfStringInSet(sender) || findSoundTrollerTrollRandomAnyAliasStringAtEndOfStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
            stringBuilder.append(ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n");
            stringBuilder.append(ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "random <alias> \n");
            stringBuilder.append(ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", collectSoundTrollerAnyAvailableAliasesForTrollRandom(sender)) + "\n");
        }

        if (findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerTrollAnyAliasStringAtEndOfStringInSet(sender) || findSoundTrollerTrollAllStringAtStartOfStringInSet(sender) || findSoundTrollerTrollAllAnyAliasStringAtEndOfStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
            stringBuilder.append(ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n");
            stringBuilder.append(ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "all <alias> \n");
            stringBuilder.append(ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", collectSoundTrollerAnyAvailableAliasesForTrollAll(sender)) + "\n");
        }

        if(findSoundTrollerVoucherBaseStringInSet(sender) || findSoundTrollerVoucherPlayerStringAtStartOfStringInSet(sender) || findSoundTrollerVoucherAnyAliasStringAtEndOfStringInSet(sender) || findSoundTrollerVoucherPlayerAliasStringAtEndOfStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
            stringBuilder.append(ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n");
            stringBuilder.append(ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "voucher player <receiver> <player/unknown> <alias> [|voucher name|] [|voucher lore|] \n");
            stringBuilder.append(ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", collectSoundTrollerAnyAvailableAliasesForVoucherPlayer(sender)) + "\n");
//            stringBuilder.append(ChatColor.GREEN + "Note: For best results, please surround" + ChatColor.AQUA + " 'voucher name'" + ChatColor.GREEN + " and " + ChatColor.AQUA + " 'voucher lore' "+ ChatColor.GREEN + " with single quotes." + "\n");
        }

        if (findSoundTrollerVoucherBaseStringInSet(sender) || findSoundTrollerVoucherAnyAliasStringAtEndOfStringInSet(sender) || findSoundTrollerVoucherBaseStringInSet(sender) || findSoundTrollerVoucherRandomAliasStringAtEndOfStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
            stringBuilder.append(ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n");
            stringBuilder.append(ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "voucher random <receiver> <alias> [|voucher name|] [|voucher lore|] \n");
            stringBuilder.append(ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", collectSoundTrollerAnyAvailableAliasesForVoucherRandom(sender)) + "\n");
//            stringBuilder.append(ChatColor.GREEN + "Note: For best results, please surround" + ChatColor.AQUA + " 'voucher name'" + ChatColor.GREEN + " and " + ChatColor.AQUA + " 'voucher lore' "+ ChatColor.GREEN + " with single quotes." + "\n");
        }

        if (findSoundTrollerVoucherBaseStringInSet(sender) || findSoundTrollerVoucherAnyAliasStringAtEndOfStringInSet(sender) || findSoundTrollerVoucherBaseStringInSet(sender) || findSoundTrollerVoucherAllAliasStringAtEndOfStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())) {
            stringBuilder.append(ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n");
            stringBuilder.append(ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "voucher all <receiver> <alias> [|voucher name|] [|voucher lore|] \n");
            stringBuilder.append(ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", collectSoundTrollerAnyAvailableAliasesForVoucherAll(sender)) + "\n");
//            stringBuilder.append(ChatColor.GREEN + "Note: For best results, please surround" + ChatColor.AQUA + " 'voucher name'" + ChatColor.GREEN + " and " + ChatColor.AQUA + " 'voucher lore' "+ ChatColor.GREEN + " with single quotes." + "\n");
        }

//        stringBuilder.append(ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n");
//
//        stringBuilder.append(ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", collectSoundTrollerAnyAliasStringAtEndOfStringInSet(sender)) + "\n");
//        System.out.println(collectSoundTrollerAnyAliasStringAtEndOfStringInSet(sender));

        stringBuilder.append(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "-------------------------------------------");

        return stringBuilder.toString();
    }

    public String baseSoundTrollerCommandNoParamMessage(){
        return  (ChatColor.DARK_PURPLE + "-------------------" + ((getPluginNamePrefix()).replaceAll(" ", "")) + ChatColor.DARK_PURPLE + "-------------------" + "\n" +
//                ChatColor.LIGHT_PURPLE + "----------" + ChatColor.GREEN + "[Help for command \"" + ChatColor.DARK_GREEN + "soundtroller" + ChatColor.GREEN +  " \"]" + ChatColor.LIGHT_PURPLE + "----------" + ChatColor.AQUA + "\n" +
                ChatColor.DARK_PURPLE + "Usage: " + "\n" +
                ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "player <player> <alias>, \n" +
                ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "random/all <alias>, \n" +
                ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "voucher player <receiver> <player> <alias>, \n" +
                ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "voucher random/all <receiver> <alias> \n" +
                ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n" +
                ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", aliases) + "\n" +
                ChatColor.DARK_PURPLE + "--------------------------------------------------");
    }
    public String trollSoundTrollerCommandNoParamMessage(){
        return  (ChatColor.DARK_PURPLE + "-------------------" + ((getPluginNamePrefix()).replaceAll(" ", "")) + ChatColor.DARK_PURPLE + "-------------------" + "\n" +
//                ChatColor.LIGHT_PURPLE + "----------" + ChatColor.GREEN + "[Help for command \"" + ChatColor.DARK_GREEN + "soundtroller" + ChatColor.GREEN +  " \"]" + ChatColor.LIGHT_PURPLE + "----------" + ChatColor.AQUA + "\n" +
                ChatColor.DARK_PURPLE + "Usage: " + "\n" +
                ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "player <player> <alias>, \n" +
                ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "random/all <alias>, \n" +
                ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n" +
                ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", aliases) + "\n" +
                ChatColor.DARK_PURPLE + "--------------------------------------------------");
    }

    public String voucherSoundTrollerCommandNoParamMessage(){
        return  (ChatColor.DARK_PURPLE + "-------------------" + ((getPluginNamePrefix()).replaceAll(" ", "")) + ChatColor.DARK_PURPLE + "-------------------" + "\n" +
//                ChatColor.LIGHT_PURPLE + "----------" + ChatColor.GREEN + "[Help for command \"" + ChatColor.DARK_GREEN + "soundtroller" + ChatColor.GREEN +  " \"]" + ChatColor.LIGHT_PURPLE + "----------" + ChatColor.AQUA + "\n" +
                ChatColor.DARK_PURPLE + "Usage: " + "\n" +
                ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "voucher player <receiver> <player> <alias>, \n" +
                ChatColor.AQUA + "/soundtroller " + ChatColor.GREEN + "voucher random/all <receiver> <alias> \n" +
                ChatColor.DARK_PURPLE + "--------------------------------------------------" + "\n" +
                ChatColor.AQUA + "Available Sound Aliases (Categories): " + ChatColor.GREEN + String.join(", ", aliases) + "\n" +
                ChatColor.DARK_PURPLE + "--------------------------------------------------");
    }

    public static boolean findSoundTrollerBaseStringAtStartOfStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().startsWith("soundtroller")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerTrollBaseStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().equals("soundtroller.troll")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerTrollPlayerStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().equals("soundtroller.troll.player")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerTrollRandomStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().equals("soundtroller.troll.random")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerTrollAllStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().equals("soundtroller.troll.all")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerTrollBaseStringAtStartOfStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().startsWith("soundtroller.troll")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerTrollPlayerStringAtStartOfStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().startsWith("soundtroller.troll.player")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerTrollRandomStringAtStartOfStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().startsWith("soundtroller.troll.random")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerTrollAllStringAtStartOfStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().startsWith("soundtroller.troll.all")) {
                return true;
            }
        }
        return false;
    }

    public boolean findSoundTrollerTrollAnyAliasStringAtEndOfStringInSet(CommandSender sender) {
        for(String alias : this.aliases) {
            for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().endsWith("soundtroller.troll." + alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean findSoundTrollerTrollPlayerAnyAliasStringAtEndOfStringInSet(CommandSender sender) {
        for(String alias : this.aliases) {
            for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().endsWith("soundtroller.troll.player." + alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean findSoundTrollerTrollRandomAnyAliasStringAtEndOfStringInSet(CommandSender sender) {
        for(String alias : this.aliases) {
            for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().endsWith("soundtroller.troll.random." + alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean findSoundTrollerTrollAllAnyAliasStringAtEndOfStringInSet(CommandSender sender) {
        for(String alias : this.aliases) {
            for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().endsWith("soundtroller.troll.all." + alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean findSoundTrollerVoucherBaseStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().equals("soundtroller.voucher")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerVoucherPlayerStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().equals("soundtroller.voucher.player")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerVoucherRandomStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().equals("soundtroller.voucher.random")) {
                return true;
            }
        }
        return false;
    }
    public static boolean findSoundTrollerVoucherAllStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().equals("soundtroller.voucher.all")) {
                return true;
            }
        }
        return false;
    }


    public static boolean findSoundTrollerVoucherBaseStringAtStartOfStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().startsWith("soundtroller.voucher")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerVoucherPlayerStringAtStartOfStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().startsWith("soundtroller.voucher.player")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerVoucherRandomStringAtStartOfStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().startsWith("soundtroller.voucher.random")) {
                return true;
            }
        }
        return false;
    }

    public static boolean findSoundTrollerVoucherAllStringAtStartOfStringInSet(CommandSender sender) {
        for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
            if (permissionAttachmentInfo.getPermission().startsWith("soundtroller.voucher.all")) {
                return true;
            }
        }
        return false;
    }

    public boolean findSoundTrollerVoucherAnyAliasStringAtEndOfStringInSet(CommandSender sender) {
        for (String alias: this.aliases) {
            for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().endsWith("soundtroller.voucher." + alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean findSoundTrollerVoucherPlayerAliasStringAtEndOfStringInSet(CommandSender sender) {
        for (String alias: this.aliases) {
            for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().endsWith("soundtroller.voucher.player." + alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean findSoundTrollerVoucherRandomAliasStringAtEndOfStringInSet(CommandSender sender) {
        for (String alias: this.aliases) {
            for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().endsWith("soundtroller.voucher.random." + alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean findSoundTrollerVoucherAllAliasStringAtEndOfStringInSet(CommandSender sender) {
        for (String alias: this.aliases) {
            for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().endsWith("soundtroller.voucher.all." + alias)) {
                    return true;
                }
            }
        }
        return false;
    }

//    public List<String> collectSoundTrollerAnyAliasStringAtEndOfStringInSet(CommandSender sender) {
//        List<String> playerAliasesList = new ArrayList<>();
//        if (findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerVoucherBaseStringInSet(sender) /* Still needs work*/) {
//            return this.aliases;
//        } else{
//            for (String alias : this.aliases) {
//                for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
//                    if (permissionAttachmentInfo.getPermission().endsWith(alias)) {
//                        if (!(playerAliasesList.contains(alias))) {
//                            playerAliasesList.add(alias);
//                        }
//                    }
//                }
//            }
//
//            return playerAliasesList;
//        }
//    }

    public List<String> collectSoundTrollerAnyAvailableAliasesForTrollPlayer(CommandSender sender) {
        List<String> playerAliasesList = new ArrayList<>();
        if(findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerTrollPlayerStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())){
            playerAliasesList = this.aliases;
        } else {
            for (String alias : this.aliases) {
                for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                    if (permissionAttachmentInfo.getPermission().endsWith("troll." + alias) || permissionAttachmentInfo.getPermission().endsWith("troll.player" + alias)) {
                        if (!(playerAliasesList.contains(alias))) {
                            playerAliasesList.add(alias);
                        }
                    }
                }
            }
        }
        return playerAliasesList;
    }

    public List<String> collectSoundTrollerAnyAvailableAliasesForTrollRandom(CommandSender sender) {
        List<String> playerAliasesList = new ArrayList<>();
        if(findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerTrollRandomStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())){
            playerAliasesList = this.aliases;
        } else {
            for (String alias : this.aliases) {
                for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                    if (permissionAttachmentInfo.getPermission().endsWith("troll." + alias) || permissionAttachmentInfo.getPermission().endsWith("troll.random" + alias)) {
                        if (!(playerAliasesList.contains(alias))) {
                            playerAliasesList.add(alias);
                        }
                    }
                }
            }
        }
        return playerAliasesList;
    }

    public List<String> collectSoundTrollerAnyAvailableAliasesForTrollAll(CommandSender sender) {
        List<String> playerAliasesList = new ArrayList<>();
        if(findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerTrollAllStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())){
            playerAliasesList = this.aliases;
        } else {
            for (String alias : this.aliases) {
                for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                    if (permissionAttachmentInfo.getPermission().endsWith("troll." + alias) || permissionAttachmentInfo.getPermission().endsWith("troll.all" + alias)) {
                        if (!(playerAliasesList.contains(alias))) {
                            playerAliasesList.add(alias);
                        }
                    }
                }
            }
        }
        return playerAliasesList;
    }

    public List<String> collectSoundTrollerAnyAvailableAliasesForVoucherPlayer(CommandSender sender) {
        List<String> playerAliasesList = new ArrayList<>();
        if(findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerVoucherPlayerStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())){
            playerAliasesList = this.aliases;
        } else {
            for (String alias : this.aliases) {
                for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                    if (permissionAttachmentInfo.getPermission().endsWith("voucher." + alias) || permissionAttachmentInfo.getPermission().endsWith("voucher.player" + alias)) {
                        if (!(playerAliasesList.contains(alias))) {
                            playerAliasesList.add(alias);
                        }
                    }
                }
            }
        }
        return playerAliasesList;
    }

    public List<String> collectSoundTrollerAnyAvailableAliasesForVoucherRandom(CommandSender sender) {
        List<String> playerAliasesList = new ArrayList<>();
        if(findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerVoucherPlayerStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())){
            playerAliasesList = this.aliases;
        } else {
            for (String alias : this.aliases) {
                for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                    if (permissionAttachmentInfo.getPermission().endsWith("voucher." + alias) || permissionAttachmentInfo.getPermission().endsWith("voucher.random" + alias)) {
                        if (!(playerAliasesList.contains(alias))) {
                            playerAliasesList.add(alias);
                        }
                    }
                }
            }
        }
        return playerAliasesList;
    }

    public List<String> collectSoundTrollerAnyAvailableAliasesForVoucherAll(CommandSender sender) {
        List<String> playerAliasesList = new ArrayList<>();
        if(findSoundTrollerTrollBaseStringInSet(sender) || findSoundTrollerVoucherPlayerStringInSet(sender) || (sender instanceof ConsoleCommandSender) || (sender.isOp())){
            playerAliasesList = this.aliases;
        } else {
            for (String alias : this.aliases) {
                for (PermissionAttachmentInfo permissionAttachmentInfo : sender.getEffectivePermissions()) {
                    if (permissionAttachmentInfo.getPermission().endsWith("voucher." + alias) || permissionAttachmentInfo.getPermission().endsWith("voucher.all" + alias)) {
                        if (!(playerAliasesList.contains(alias))) {
                            playerAliasesList.add(alias);
                        }
                    }
                }
            }
        }
        return playerAliasesList;
    }
}

package com.sulphurouscerebrum.plugins;

import org.apache.commons.math3.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {

    BlockShuffleParams params;

    public void onEnable(){
        Objects.requireNonNull(getCommand("blockshuffle")).setExecutor(new BlockShuffleCommands(this));
        Objects.requireNonNull(getCommand("blockshuffle")).setTabCompleter(new BlockShuffleTabCompleter(this));

        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        saveDefaultConfig();
        params = new BlockShuffleParams();

        loadConfigFile();
    }

    public void onDisable(){

    }

    private void loadConfigFile() {
        List<String> configBlocks;
        List<Pair<Material, Double>> validBlocks = new ArrayList<>();

        configBlocks = getConfig().getStringList("block-list");

        for(String block : configBlocks) {
            ArrayList<String> items = new ArrayList<>(Arrays.asList(block.split("\\s+")));
            if (items.size() == 1) {
                items.add(0, "1.0");
            }
            if(checkMaterialValidity(items.get(1))) {
                validBlocks.add(new Pair<>(Material.getMaterial(items.get(1), false), Double.parseDouble(items.get(0))));
                Bukkit.getLogger().info("Loaded " + block);
            } else {
                Bukkit.getLogger().info("Material " + items.get(1) + " is not valid. Skipping");
            }

        }

        if(validBlocks.isEmpty())
            Bukkit.getLogger().info("No blocks were added from the config.yml file. Game cannot start");

        this.params.setAvailableBlocks(validBlocks);
        Bukkit.getLogger().info("Total of " + validBlocks.size() + " blocks were added");

        int rounds = getConfig().getInt("parameters.rounds");
        int roundTime = getConfig().getInt("parameters.roundTime");

        if(rounds < 1) {
            Bukkit.getLogger().info("Number of rounds cannot be less than 1. Defaulting to 1");
            rounds = 1;
        }

        if(roundTime < 30 ) {
            Bukkit.getLogger().info("Round time cannot be less than 10 seconds. Defaulting to 1 minute");
            roundTime = 1200;
        }

        this.params.setNoOfRounds(rounds);
        this.params.setRoundTime(roundTime);
        Bukkit.getLogger().info("Number of rounds : " + rounds);
        Bukkit.getLogger().info("Round time : " + roundTime);

        int foodToBeGiven = getConfig().getInt("parameters.giveFood");
        if(foodToBeGiven < 0) {
            Bukkit.getLogger().info("Invalid food amount. Defaulting to 16");
            foodToBeGiven = 16;
        }

        if(foodToBeGiven > 2304) foodToBeGiven = 2304;
        this.params.setInitialFoodAmount(foodToBeGiven);
        Bukkit.getLogger().info("Amount of food to be given : " + foodToBeGiven);
    }

    private boolean checkMaterialValidity(String material) {
        Material m = Material.getMaterial(material, false);
        return m != null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player joinedPlayer = e.getPlayer();
        params.getAvailablePlayers().forEach((player) -> {
            if (player.player.getName().equals(joinedPlayer.getName())) {
                player.setPlayer(joinedPlayer);
            }
        });
    }
}

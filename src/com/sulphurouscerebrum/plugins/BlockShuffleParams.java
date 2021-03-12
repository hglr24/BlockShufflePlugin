package com.sulphurouscerebrum.plugins;

import org.apache.commons.math3.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

class BlockShuffleParams {
    @SuppressWarnings("FieldCanBeLocal")
    private BukkitTask task;
    private int noOfRounds;
    private int roundTime;
    private int initialFoodAmount;
    private boolean isGameRunning;
    private List<Pair<Material, Double>> availableBlocks;
    @SuppressWarnings("FieldMayBeFinal")
    private List<BlockShufflePlayer> availablePlayers;

    BlockShuffleParams(){
        noOfRounds = 5;
        roundTime = 6000;
        initialFoodAmount = 16;
        isGameRunning = false;
        availablePlayers = new ArrayList<>();
    }

    int getNoOfRounds() {
        return this.noOfRounds;
    }

    void setNoOfRounds(int noOfRounds){
        this.noOfRounds = noOfRounds;
    }

    int getRoundTime(){
        return this.roundTime;
    }

    void setRoundTime(int roundTime){
        this.roundTime = roundTime;
    }

    int getInitialFoodAmount(){
        return this.initialFoodAmount;
    }

    void setInitialFoodAmount(int initialFoodAmount){
        this.initialFoodAmount = initialFoodAmount;
    }

    List<Pair<Material, Double>> getAvailableBlocks(){
        return this.availableBlocks;
    }

    void setAvailableBlocks(List<Pair<Material, Double>> availableBlocks){
        this.availableBlocks = availableBlocks;
    }

    void setTask(BukkitTask task){
        this.task = task;
    }

    BukkitTask getTask(){
        return this.task;
    }

    boolean getIsGameRunning(){
        return this.isGameRunning;
    }

    void setGameRunning(boolean isGameRunning){
        this.isGameRunning = isGameRunning;
    }

    List<BlockShufflePlayer> getAvailablePlayers(){
        return this.availablePlayers;
    }

    boolean addAvailablePlayer(String playerString) {
        for(BlockShufflePlayer player : availablePlayers){
            if(player.getName().equalsIgnoreCase(playerString)) {
                return false;
            }
        }
        BlockShufflePlayer player = new BlockShufflePlayer(Bukkit.getPlayer(playerString));
        availablePlayers.add(player);
        return true;
    }

    boolean removeAvailablePlayer(String playerString){
        int indexToBeRemoved = -1;
        for(BlockShufflePlayer player : availablePlayers) {
            if(player.getName().equalsIgnoreCase(playerString)) {
                indexToBeRemoved = availablePlayers.indexOf(player);
                break;
            }
        }

        if(indexToBeRemoved >= 0) {
            availablePlayers.remove(indexToBeRemoved);
            return true;
        }
        else return false;
    }
}


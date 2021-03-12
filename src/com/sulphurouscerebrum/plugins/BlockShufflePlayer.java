package com.sulphurouscerebrum.plugins;

import org.bukkit.Material;
import org.bukkit.entity.Player;

class BlockShufflePlayer {
    Player player;
    private int score;
    private Material blockToBeFound;
    private boolean hasFoundBlock;

    BlockShufflePlayer(Player player){
        this.player = player;
        score = 0;
        blockToBeFound = null;
        hasFoundBlock = false;
    }

    String getName(){
        return this.player.getName();
    }

    int getScore(){
        return this.score;
    }

    void setScore(int score){
        this.score = score;
    }

    void setPlayer(Player player) {
        this.player = player;
    }

    boolean getHasFoundBlock(){
        return this.hasFoundBlock;
    }

    void setHasFoundBlock(boolean hasFoundBlock){
        this.hasFoundBlock = hasFoundBlock;
    }

    Material getBlockToBeFound(){
        return blockToBeFound;
    }

    void setBlockToBeFound(Material blockToBeFound) {
        this.blockToBeFound = blockToBeFound;
    }
}

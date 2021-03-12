package com.sulphurouscerebrum.plugins;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.logging.Logger;

class BlockShuffleTask extends BukkitRunnable {
    private Logger logger;
    private boolean hasRoundEnded;
    private Main plugin;
    private BlockShuffleTaskHelper helper;
    private int currentRoundTime;
    private int currentRound;
    private int successfulPlayers;
    private int counter;
    private SendTitle titleSender;

    BlockShuffleTask(Main plugin){
        this.logger = Bukkit.getLogger();
        this.plugin = plugin;
        this.hasRoundEnded = true;
        this.currentRoundTime = 0;
        this.currentRound = 0;
        this.successfulPlayers = 0;
        this.counter = 100;
        this.titleSender = new SendTitle();
        this.helper = new BlockShuffleTaskHelper(this.plugin, this.currentRound);
    }

    @Override
    public void run() {

        if(counter > 0) {
            if(counter % 20 == 0) {
                for (BlockShufflePlayer player : this.plugin.params.getAvailablePlayers())
                    if (Bukkit.getPlayerExact(player.getName()) != null) {
                        titleSender.sendTitle(player.player, 5, 10, 5, ChatColor.BLUE + "Game Starting", ChatColor.RED + "" + (counter / 20));
                    }
            }

            counter -= 10;
        }

        else {
            if (hasRoundEnded) {
                this.currentRound += 1;
                Bukkit.broadcastMessage("Starting Round : " + ChatColor.BOLD + "" + this.currentRound);
                this.currentRoundTime = 0;
                this.hasRoundEnded = false;
                this.successfulPlayers = 0;
                helper.startRound(this.currentRound);
            } else {
                for (BlockShufflePlayer player : this.plugin.params.getAvailablePlayers()) {
                    if (Bukkit.getPlayerExact(player.getName()) != null) {
                        if (!player.getHasFoundBlock()) {
                            boolean hasFound = helper.checkPlayer(player);
                            if (hasFound) {
                                this.successfulPlayers++;
                                Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + " found their block!");
                            }
                        }
                    }
                }

                int timeRemaining = this.plugin.params.getRoundTime() - this.currentRoundTime;

                if (this.successfulPlayers == this.plugin.params.getAvailablePlayers().size()) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "Everyone found their block!");
                    this.hasRoundEnded = true;
                } else if (timeRemaining <= 0) {
                    Bukkit.broadcastMessage("\nTime Up!");
                    for (BlockShufflePlayer player : this.plugin.params.getAvailablePlayers()) {
                        if (Bukkit.getPlayerExact(player.getName()) != null && !player.getHasFoundBlock()) {
                            Bukkit.broadcastMessage(ChatColor.BLUE + player.getName() + " did not find their block!");
                        }
                    }
                    this.hasRoundEnded = true;
                } else if (timeRemaining <= 200) {
                    if(timeRemaining % 20 == 0)
                        Bukkit.broadcastMessage(ChatColor.RED + "Time Remaining : " + ChatColor.BOLD + (timeRemaining / 20) + " seconds");
                    this.currentRoundTime += 10;
                } else if (timeRemaining <= 3600) {
                    if (timeRemaining % 1200 == 0) {
                        Bukkit.broadcastMessage(ChatColor.RED + "Time Remaining : " + ChatColor.BOLD + (timeRemaining / 1200) + " minutes");
                    }
                    this.currentRoundTime += 10;
                } else {
                    this.currentRoundTime += 10;
                }

                if (this.hasRoundEnded && this.currentRound == this.plugin.params.getNoOfRounds()) {
                    this.cancel();
                }
            }
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        helper.endGame();
    }
}

class BlockShuffleTaskHelper {

    private Main plugin;
    private int currentRound;
    BlockShuffleTaskHelper(Main plugin, int currentRound){
        this.plugin = plugin;
        this.currentRound = currentRound;
    }

    void startRound(int currentRound){
        this.currentRound = currentRound;
        for(BlockShufflePlayer player : this.plugin.params.getAvailablePlayers()){
            if (Bukkit.getPlayerExact(player.getName()) != null) {
                player.setHasFoundBlock(false);
                player.setBlockToBeFound(getRandomWeightedBlock());
                new SendTitle().sendTitle(player.player, 5, 80, 5, ChatColor.YELLOW +
                        "Find " + player.getBlockToBeFound().toString().replace("_", " "),
                        ChatColor.BLUE + "Good luck!");
                Bukkit.broadcastMessage(ChatColor.BLUE + "Assigned " + player.getName() + " with " +
                        player.getBlockToBeFound().toString().replace("_", " "));
                createBoard(player);
                player.player.getInventory().remove(Material.EGG);
                player.player.getInventory().addItem(new ItemStack(Material.EGG, 3));
            }
        }
    }

    private Material getRandomWeightedBlock(){
        return new EnumeratedDistribution<>(this.plugin.params.getAvailableBlocks()).sample();
    }

    void createBoard(BlockShufflePlayer player) {
        Bukkit.getLogger().info("Creating Scoreboard for " + player.getName());
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("BSObjective", "dummy", "Psi U Block Shuffle");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<BlockShufflePlayer> playersCopy = new ArrayList<>(this.plugin.params.getAvailablePlayers());
        playersCopy.sort(Comparator.comparingInt(BlockShufflePlayer::getScore).reversed());

        Score s1 = obj.getScore("ROUND : " + this.currentRound + "/" + this.plugin.params.getNoOfRounds());
        s1.setScore(10);
        Score s2 = obj.getScore("");
        s2.setScore(9);
        Score s3 = obj.getScore("YOUR SCORE : " + player.getScore());
        s3.setScore(8);
        Score s4 = obj.getScore("");
        s4.setScore(7);
        Score s5 = obj.getScore("YOUR BLOCK : " + player.getBlockToBeFound().toString()
                .replace("LEGACY_", "").replace("_", " "));
        s5.setScore(6);
        Score s6 = obj.getScore("");
        s6.setScore(5);
        Score s7 = obj.getScore("LEADERBOARD");
        s7.setScore(4);
        Score s8 = obj.getScore("1st: " + playersCopy.get(0).getName() + " - " + playersCopy.get(0).getScore());
        s8.setScore(3);
        if (playersCopy.size() > 1) {
            Score s9 = obj.getScore("2nd: " + playersCopy.get(1).getName() + " - " + playersCopy.get(1).getScore());
            s9.setScore(2);
        }
        if (playersCopy.size() > 2) {
            Score s10 = obj.getScore("3rd: " + playersCopy.get(2).getName() + " - " + playersCopy.get(2).getScore());
            s10.setScore(1);
        }
        player.player.setScoreboard(scoreboard);
    }

    boolean checkPlayer(BlockShufflePlayer player) {
        Material standingOn = Objects.requireNonNull(Bukkit.getPlayer(player.getName())).getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        Material standingIn = Objects.requireNonNull(Bukkit.getPlayer(player.getName())).getLocation().add(0, 1, 0).getBlock().getRelative(BlockFace.DOWN).getType();
        if(standingOn.equals(player.getBlockToBeFound()) || standingIn.equals(player.getBlockToBeFound())) {
            player.setHasFoundBlock(true);
            player.setScore(player.getScore() + 100);
            broadcastSound(Sound.BLOCK_END_PORTAL_SPAWN);
            return true;
        }
        return false;
    }

    private void broadcastSound(Sound sound) {
        for(BlockShufflePlayer player : this.plugin.params.getAvailablePlayers()){
            if (Bukkit.getPlayerExact(player.getName()) != null) {
                player.player.playSound(player.player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }

    void endGame(){
        SendTitle titleSender = new SendTitle();
        Bukkit.broadcastMessage("\nScores : \n");
        broadcastSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        TreeMap<Integer, String> scores = new TreeMap<>(Collections.reverseOrder());

        for(BlockShufflePlayer player : this.plugin.params.getAvailablePlayers()) {
            if (Bukkit.getPlayerExact(player.getName()) != null) {
                Player ply = Bukkit.getPlayer(player.getName());
                scores.put(player.getScore(), player.getName());
                ply.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                titleSender.sendTitle(ply, 10, 30, 10, ChatColor.RED + "" + "Game Over", "");
            }
        }

        Iterator iterator = scores.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry mapEntry = (Map.Entry) iterator.next();
            String message = mapEntry.getValue() + " : " + mapEntry.getKey();
            Bukkit.broadcastMessage(message);
        }
        this.plugin.params.setGameRunning(false);
    }
}

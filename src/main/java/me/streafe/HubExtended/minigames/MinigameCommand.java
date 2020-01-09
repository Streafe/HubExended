package me.streafe.HubExtended.minigames;

import me.streafe.HubExtended.HubExtended;
import me.streafe.HubExtended.player_utils.HubPlayer;
import me.streafe.HubExtended.utils.PacketUtils;
import me.streafe.HubExtended.utils.Utils;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class MinigameCommand implements CommandExecutor {
    Utils utils = new Utils();

    private Integer taskid;
    private MinigameType minigameType;
    private int taskIdStart;
    private int id1;



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String args[]) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            HubPlayer hubPlayer = HubExtended.getInstance().getHubPlayer(player.getUniqueId());

            Minigame minigame;

            if (cmd.getName().equalsIgnoreCase("minigames")) {
                if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
                    if(hubPlayer.isOwnerOfGame()){
                        hubPlayer.sendMessage(utils.getPluginPrefix() + utils.translate("&cYou are already a owner of a game!"));
                        return true;
                    }
                    try{
                        this.minigameType = MinigameType.valueOf(args[1].toUpperCase());
                    }catch (Exception e){
                        player.sendMessage(utils.translate("&c(!) &7That is not an gamemode"));
                        return true;
                    }
                    minigame = new Minigame(Integer.parseInt(args[2]), this.minigameType);
                    hubPlayer.sendMessage(utils.getPluginPrefix() + "Game ID: " + utils.translate("&d&l"+minigame.getGameID()) + utils.translate("&7")+ " (share with friends)");
                    minigame.addNewPlayer(player);
                    hubPlayer.inGame = true;

                    hubPlayer.setGameID(minigame.getGameID());
                    HubExtended.getInstance().minigameHashMap.put(minigame.getGameID(), minigame);
                    HubExtended.getInstance().minigameHashMap.get(minigame.getGameID()).setOwner(player);

                    hubPlayer.sendMessage(utils.translate("&7Max players: ") + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getMaxPlayers());
                    hubPlayer.sendMessage(utils.translate("&7Is started: ") + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).isStarted());

                }
                /*
                else if (args.length == 1 && args[0].equalsIgnoreCase("delete")) {
                    hubPlayer.sendMessage(utils.translate("&cDont use this command: use /minigames leave"));
                    return true;
                    if (HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner() == player) {
                        HubExtended.getInstance().minigameHashMap.remove(hubPlayer.getGameID());
                        hubPlayer.sendMessage(utils.getPluginPrefix() + "you deleted your game");
                        hubPlayer.setGameID("");
                        hubPlayer.inGame = false;
                    } else {
                        hubPlayer.sendMessage(utils.getPluginPrefix() + "You are not a current owner of a game");
                    }


                }
                */


                else if(args.length == 2 && args[0].equalsIgnoreCase("settype")){
                    try{
                        this.minigameType = MinigameType.valueOf(args[1]);
                        HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).setType(this.minigameType);
                        player.sendMessage(utils.translate("&aGame type set to " + this.minigameType.getName()));
                    }catch (Exception e){
                        player.sendMessage(utils.translate("&c(!) &7That is not an gamemode"));
                    }
                }

                else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
                    if(HubExtended.getInstance().getMinigameByID(args[1]).isStarted()){
                        player.sendMessage(utils.translate("&cThe minigame has already started"));
                        return true;
                    }
                    if(HubExtended.getInstance().getMinigameByID(args[1]).playerList.contains(hubPlayer)){
                        hubPlayer.sendMessage(utils.getPluginPrefix() + "&cYou are already in this game");
                        return true;
                    }
                    else if(HubExtended.getInstance().getHubPlayer(player.getUniqueId()).inGame){
                        hubPlayer.sendMessage(utils.getPluginPrefix() + utils.translate("&cYou are already in a game"));
                        return true;
                    }
                    HubExtended.getInstance().getMinigameByID(args[1]).addNewPlayer(player);
                    player.sendMessage(utils.getPluginPrefix() + "you joined the game with ID: " + HubExtended.getInstance().getMinigameByID(args[1]).getGameID());
                    hubPlayer.setGameID(args[1]);
                    hubPlayer.sendMessage("Your game id is: " + hubPlayer.getGameID());
                    hubPlayer.inGame = true;

                    for(HubPlayer partyPlayers : HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).playerList){
                        if(!(partyPlayers.getName().equalsIgnoreCase(hubPlayer.getName()))){
                            partyPlayers.sendMessage(utils.translate("&7" + hubPlayer.getName() + " &ajoined the party"));
                        }
                    }

                } else if(args.length == 1 && args[0].equalsIgnoreCase("list")){
                    hubPlayer.sendMessage(utils.getPluginPrefix() + "Your game id is: " + hubPlayer.getGameID());
                    hubPlayer.sendMessage("Gametype: " + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getType().toString());
                    hubPlayer.sendMessage("started: " + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).isStarted());
                    hubPlayer.sendMessage(utils.translate("&7List:"));
                    hubPlayer.sendMessage(utils.translate("&7") + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getPlayerListString());
                }
                else if(args.length == 1 && args[0].equalsIgnoreCase("leave")){

                    if(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner() == player){
                        for(HubPlayer partyPlayers : HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).playerList){
                            partyPlayers.sendMessage(utils.translate("&7" + hubPlayer.getName() + " &cleft the party"));
                            HubExtended.getInstance().minigameHashMap.remove(partyPlayers.getGameID());

                            partyPlayers.inGame = false;
                            partyPlayers.setGameID("");

                            partyPlayers.sendMessage(utils.getPluginPrefix() + "You left the gameparty");
                            partyPlayers.getPlayer().teleport(HubExtended.getInstance().getHubLocation());

                            //TODO implement different gamemodes and add inventory to chose one

                        }
                        hubPlayer.inGame = false;
                        hubPlayer.setGameID("");

                        hubPlayer.sendMessage(utils.getPluginPrefix() + "You left the gameparty and deleted it");

                    }

                    else{
                        for(HubPlayer partyPlayers : HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getPlayerList()){
                            if(!(partyPlayers.getName().equalsIgnoreCase(hubPlayer.getName()))){
                                partyPlayers.sendMessage(utils.translate("&7" + hubPlayer.getName() + " &cleft the party"));
                            }
                        }
                        HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).removePlayer(player);
                        HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).playerAmount--;
                        hubPlayer.inGame = false;
                        hubPlayer.setGameID("");

                        hubPlayer.sendMessage(utils.getPluginPrefix() + "You left the gameparty");
                    }
                    hubPlayer.getPlayer().teleport(HubExtended.getInstance().getHubLocation());

                }

                else if(args.length == 1 && args[0].equalsIgnoreCase("start")){
                    if(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner() == player){
                        if(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).playerAmount < 2){
                            player.sendMessage(utils.translate("&cYou are less than 2 players in the party!"));
                            player.sendMessage(utils.translate("&cWant to start it anyway? Do /minigames startanyway"));
                            return true;
                        }
                        HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).setStarted(true);
                        for(HubPlayer players : HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getPlayerList()){

                            players.getPlayer().teleport(HubExtended.getInstance().getLobbyLocation());

                            players.sendMessage(utils.translate("&aGame starting in: "));

                            players.sendMessage("");

                            MinigameCountdownTask startTask = new MinigameCountdownTask(0L, 20L) {
                                int iStart = 10;
                                @Override
                                public void run() {
                                    if(iStart == 0){
                                        HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).startGame();
                                        cancelTask();
                                    }else if(iStart>0){
                                        if(iStart == 10){
                                            PacketUtils.sendTitle(players.getPlayer(),iStart + "",ChatColor.GREEN);
                                        }
                                        else if(iStart == 5){
                                            PacketUtils.sendTitle(players.getPlayer(),iStart + "",ChatColor.YELLOW);
                                        }else if(iStart == 4){
                                            PacketUtils.sendTitle(players.getPlayer(),iStart + "",ChatColor.RED);
                                        }else if(iStart == 3){
                                            PacketUtils.sendTitle(players.getPlayer(),iStart + "",ChatColor.RED);
                                        }else if(iStart == 2){
                                            PacketUtils.sendTitle(players.getPlayer(),iStart + "",ChatColor.RED);
                                        }else if(iStart == 1){
                                            PacketUtils.sendTitle(players.getPlayer(),iStart + "",ChatColor.RED);
                                        }

                                        iStart--;
                                    }
                                    else if(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).playerAmount<2){
                                        /*
                                        if(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()) == null){
                                            return;
                                        }

                                         */

                                        hubPlayer.sendMessage(utils.translate("&cSomeone left your lobby"));
                                        cancelTask();
                                        if(!players.getName().equalsIgnoreCase(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner().getName())){
                                            players.getPlayer().teleport(HubExtended.getInstance().getHubLocation());

                                            if(isRunning()){
                                                cancelTask();
                                            }

                                        }

                                    }
                                }

                            };

                        }
                    }
                }

                else if(args.length == 1 && args[0].equalsIgnoreCase("setlobbyspawn")){
                    if(player.isOp()){
                        Location loc = player.getLocation();

                        HubExtended.getInstance().getConfig().set("minigames.lobbySpawn.X",loc.getX());
                        HubExtended.getInstance().getConfig().set("minigames.lobbySpawn.Y",loc.getY());
                        HubExtended.getInstance().getConfig().set("minigames.lobbySpawn.Z",loc.getZ());
                        HubExtended.getInstance().getConfig().set("minigames.lobbySpawn.Pitch",loc.getPitch());
                        HubExtended.getInstance().getConfig().set("minigames.lobbySpawn.Yaw",loc.getYaw());

                        HubExtended.getInstance().getConfig().options().copyDefaults(true);
                        HubExtended.getInstance().saveConfig();

                        player.sendMessage(utils.translate("&aMinigames lobby spawn set!"));

                    }
                }
/*
                else if(args.length == 1 && args[0].equalsIgnoreCase("startanyway")){
                    if(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner() == player){
                        HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).setStarted(true);
                        for(HubPlayer players : HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getPlayerList()){
                            players.sendMessage(utils.translate("&aGame starting in: "));
                            player.sendMessage("");

                            this.taskid = HubExtended.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(HubExtended.getInstance(), new Runnable() {
                                int i = 10;
                                @Override
                                public void run() {
                                    if(i <= 0){
                                        HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).startGame();
                                        Bukkit.getScheduler().cancelTask(taskid);
                                        return;
                                    }
                                    if(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).playerAmount<2){
                                        hubPlayer.sendMessage(utils.translate("&cSomeone left your lobby"));
                                        Bukkit.getScheduler().cancelTask(taskid);
                                    }
                                    players.sendMessage(utils.translate("&a") + i);
                                    i--;

                                }
                            },0L, 20L);
                        }
                    }
                }

 */
                else {
                    hubPlayer.sendMessage("§c/minigames help");
                }
            }
        }
        return true;
    }
}

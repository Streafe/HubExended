package me.streafe.HubExtended.utils;

import me.streafe.HubExtended.HubExtended;
import me.streafe.HubExtended.bungee.BungeeMessageListener;
import me.streafe.HubExtended.minigames.MinigameCountdownTask;
import me.streafe.HubExtended.minigames.MinigameType;
import me.streafe.HubExtended.player_utils.HBConfigSetup;
import me.streafe.HubExtended.player_utils.HubPlayer;
import me.streafe.HubExtended.player_utils.RankEnum;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.scoreboard.Scoreboard;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AnimatedScoreboard {

    private Player player;
    public HubPlayer hubPlayer;
    public int tokens;
    private String tokenString;
    private int taskId;
    private int animateTask;
    public String animatedText;
    int time;
    public ScoreboardScore owner;
    private String serverName;
    private int online;

    Utils utils = new Utils();

    public AnimatedScoreboard(Player player){
        this.player = player;
        this.hubPlayer = HubExtended.getInstance().getHubPlayer(player.getUniqueId());
    }


    public void animateText(){
        this.time = 2;
        taskId = HubExtended.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(HubExtended.getInstance(), () -> {
            if(time == 2){
                this.animatedText = "&a&lStreafe's Server";
                time--;
            }
            else if(time == 1){
                this.animatedText = "&e&lStreafe's Server";
                time--;
            }
            else if(time == 0){
                this.animatedText = "&f&lStreafe's Server";
                time = 2;
            }

        },0L,20L);
    }



    public void view(){

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("Tokens","dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(utils.translate("&a&lStreafe's Server"));

        Score tokens = objective.getScore("Tokens:" + hubPlayer.tokens);
        tokens.setScore(1);

        player.setScoreboard(scoreboard);
        updateScoreBoard();
    }

    public void setMinigameScoreboard(){

    }

    public void updateScoreBoard() {
        MinigameCountdownTask updateScoreTask = new MinigameCountdownTask(0L, 20L) {
            @Override
            public void run() {
                if (hubPlayer.isInGame() && hubPlayer.getGame().minigameType == MinigameType.OITC) {
                    HBConfigSetup hbConfigSetup = new HBConfigSetup(player);
                    tokens = hbConfigSetup.getInt("player.tokens");

                    net.minecraft.server.v1_8_R3.Scoreboard board = new net.minecraft.server.v1_8_R3.Scoreboard();
                    ScoreboardObjective obj = board.registerObjective("§eLobby Mode", IScoreboardCriteria.b);
                    board.setDisplaySlot(1, obj);

                    PacketPlayOutScoreboardObjective removepacket = new PacketPlayOutScoreboardObjective(obj, 1);
                    PacketPlayOutScoreboardObjective createpacket = new PacketPlayOutScoreboardObjective(obj, 0);
                    PacketPlayOutScoreboardDisplayObjective display = new PacketPlayOutScoreboardDisplayObjective(1, obj);

                    ScoreboardScore header = new ScoreboardScore(board, obj, utils.translate("&7Game ID: &c&o") + hubPlayer.getGameID());
                    /*
                    if(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner() == null){
                        owner = new ScoreboardScore(board, obj, utils.translate("&7Party leader: &a" + "&c&o@null &cleave!" ));
                    }else{
                        owner = new ScoreboardScore(board, obj, utils.translate("&7Party leader: &a" + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner().getName()));
                    }

                     */

                    owner = new ScoreboardScore(board, obj, utils.translate("&7Party leader: &a" + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner().getName()));
                    ScoreboardScore list = new ScoreboardScore(board, obj, utils.translate("&7Player amount: &a") + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).playerAmount);
                    ScoreboardScore leave1 = new ScoreboardScore(board,obj,utils.translate("    &7in case you want to leave"));
                    ScoreboardScore leave2 = new ScoreboardScore(board,obj,utils.translate("    &7use &c&o/minigames leave"));
                    ScoreboardScore gameState = new ScoreboardScore(board,obj,utils.translate("    &a"+HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).gameState.getName()) + utils.translate("  &7(&a" +
                            HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).minigameType.getName() + "&7)"));
                    ScoreboardScore points = new ScoreboardScore(board,obj,utils.translate(" &7POINTS: &a"+hubPlayer.gamePoints));

                    header.setScore(8);
                    owner.setScore(7);
                    list.setScore(6);
                    leave1.setScore(5);
                    leave2.setScore(4);
                    gameState.setScore(3);
                    points.setScore(2);

                    PacketPlayOutScoreboardScore ps1 = new PacketPlayOutScoreboardScore(header);
                    PacketPlayOutScoreboardScore ps2 = new PacketPlayOutScoreboardScore(owner);
                    PacketPlayOutScoreboardScore ps3 = new PacketPlayOutScoreboardScore(list);
                    PacketPlayOutScoreboardScore ps4 = new PacketPlayOutScoreboardScore(leave1);
                    PacketPlayOutScoreboardScore ps5 = new PacketPlayOutScoreboardScore(leave2);
                    PacketPlayOutScoreboardScore ps6 = new PacketPlayOutScoreboardScore(gameState);
                    PacketPlayOutScoreboardScore ps7 = new PacketPlayOutScoreboardScore(points);

                    PacketUtils.sendPacket(player, removepacket);
                    PacketUtils.sendPacket(player, createpacket);
                    PacketUtils.sendPacket(player, display);

                    PacketUtils.sendPacket(player, ps1);
                    PacketUtils.sendPacket(player, ps2);
                    PacketUtils.sendPacket(player, ps3);
                    PacketUtils.sendPacket(player, ps4);
                    PacketUtils.sendPacket(player, ps5);
                    PacketUtils.sendPacket(player, ps6);
                    PacketUtils.sendPacket(player, ps7);

                    return;
                }else if (hubPlayer.isInGame() && hubPlayer.getGame().minigameType == MinigameType.SKYWARS) {
                    HBConfigSetup hbConfigSetup = new HBConfigSetup(player);
                    tokens = hbConfigSetup.getInt("player.tokens");

                    net.minecraft.server.v1_8_R3.Scoreboard board = new net.minecraft.server.v1_8_R3.Scoreboard();
                    ScoreboardObjective obj = board.registerObjective("§eLobby Mode", IScoreboardCriteria.b);
                    board.setDisplaySlot(1, obj);

                    PacketPlayOutScoreboardObjective removepacket = new PacketPlayOutScoreboardObjective(obj, 1);
                    PacketPlayOutScoreboardObjective createpacket = new PacketPlayOutScoreboardObjective(obj, 0);
                    PacketPlayOutScoreboardDisplayObjective display = new PacketPlayOutScoreboardDisplayObjective(1, obj);

                    ScoreboardScore header = new ScoreboardScore(board, obj, utils.translate("&7Game ID: &c&o") + hubPlayer.getGameID());
                    /*
                    if(HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner() == null){
                        owner = new ScoreboardScore(board, obj, utils.translate("&7Party leader: &a" + "&c&o@null &cleave!" ));
                    }else{
                        owner = new ScoreboardScore(board, obj, utils.translate("&7Party leader: &a" + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner().getName()));
                    }

                     */

                    owner = new ScoreboardScore(board, obj, utils.translate("&7Party leader: &a" + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).getOwner().getName()));
                    ScoreboardScore list = new ScoreboardScore(board, obj, utils.translate("&7Player amount: &a") + HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).playerAmount);
                    ScoreboardScore leave1 = new ScoreboardScore(board,obj,utils.translate("    &7in case you want to leave"));
                    ScoreboardScore leave2 = new ScoreboardScore(board,obj,utils.translate("    &7use &c&o/minigames leave"));
                    ScoreboardScore gameState = new ScoreboardScore(board,obj,utils.translate("    &a"+HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).gameState.getName()) + utils.translate("  &7(&a" +
                            HubExtended.getInstance().getMinigameByID(hubPlayer.getGameID()).minigameType.getName() + "&7)"));
                    ScoreboardScore points = new ScoreboardScore(board,obj,utils.translate(" &7Kills: &a"+hubPlayer.gamePoints));
                    ScoreboardScore alive = new ScoreboardScore(board,obj,utils.translate(" &7Alive: &a"+hubPlayer.getGame().skywarsPlayersAlive));

                    header.setScore(8);
                    owner.setScore(7);
                    list.setScore(6);
                    leave1.setScore(5);
                    leave2.setScore(4);
                    gameState.setScore(3);
                    points.setScore(2);
                    alive.setScore(1);

                    PacketPlayOutScoreboardScore ps1 = new PacketPlayOutScoreboardScore(header);
                    PacketPlayOutScoreboardScore ps2 = new PacketPlayOutScoreboardScore(owner);
                    PacketPlayOutScoreboardScore ps3 = new PacketPlayOutScoreboardScore(list);
                    PacketPlayOutScoreboardScore ps4 = new PacketPlayOutScoreboardScore(leave1);
                    PacketPlayOutScoreboardScore ps5 = new PacketPlayOutScoreboardScore(leave2);
                    PacketPlayOutScoreboardScore ps6 = new PacketPlayOutScoreboardScore(gameState);
                    PacketPlayOutScoreboardScore ps7 = new PacketPlayOutScoreboardScore(points);
                    PacketPlayOutScoreboardScore ps8 = new PacketPlayOutScoreboardScore(alive);

                    PacketUtils.sendPacket(player, removepacket);
                    PacketUtils.sendPacket(player, createpacket);
                    PacketUtils.sendPacket(player, display);

                    PacketUtils.sendPacket(player, ps1);
                    PacketUtils.sendPacket(player, ps2);
                    PacketUtils.sendPacket(player, ps3);
                    PacketUtils.sendPacket(player, ps4);
                    PacketUtils.sendPacket(player, ps5);
                    PacketUtils.sendPacket(player, ps6);
                    PacketUtils.sendPacket(player, ps7);
                    PacketUtils.sendPacket(player, ps8);

                    return;
                } else if (!hubPlayer.isInGame()) {
                    HBConfigSetup hbConfigSetup = new HBConfigSetup(player);

                    tokens = hbConfigSetup.getInt("player.tokens");

                    net.minecraft.server.v1_8_R3.Scoreboard board = new net.minecraft.server.v1_8_R3.Scoreboard();
                    ScoreboardObjective obj = board.registerObjective("§eStreafeNetwork", IScoreboardCriteria.b);
                    board.setDisplaySlot(1, obj);


                    PacketPlayOutScoreboardObjective removepacket = new PacketPlayOutScoreboardObjective(obj, 1);
                    PacketPlayOutScoreboardObjective createpacket = new PacketPlayOutScoreboardObjective(obj, 0);
                    PacketPlayOutScoreboardDisplayObjective display = new PacketPlayOutScoreboardDisplayObjective(1, obj);


                    String prefix = "";

                    if (hbConfigSetup.get("player.rank").equalsIgnoreCase("MEMBER")) {
                        prefix = "&7";
                    }else if (hbConfigSetup.get("player.rank").equalsIgnoreCase("VIP")) {
                        prefix = "&e";
                    }else if (hbConfigSetup.get("player.rank").equalsIgnoreCase("ALIEN")){
                        prefix = "&5";
                    }else if(hbConfigSetup.get("player.rank").equalsIgnoreCase("ALIENPLUSS")){
                        prefix = "&5";
                    } else if (hbConfigSetup.get("player.rank").equalsIgnoreCase("MODERATOR")) {
                        prefix = "&3";
                    } else if (hbConfigSetup.get("player.rank").equalsIgnoreCase("ADMIN")) {
                        prefix = "&c";
                    } else if (hbConfigSetup.get("player.rank").equalsIgnoreCase("CO_OWNER")) {
                        prefix = "&d";
                    } else if (hbConfigSetup.get("player.rank").equalsIgnoreCase("OWNER")) {
                        prefix = "&4";
                    } else if (hbConfigSetup.get("player.rank").equalsIgnoreCase("DEVELOPER")) {
                        prefix = "&4";
                    }

                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    ScoreboardScore s1 = new ScoreboardScore(board, obj, utils.translate("&7" + formatter.format(date)));
                    ScoreboardScore s2 = new ScoreboardScore(board, obj, utils.translate(" "));
                    ScoreboardScore s3 = new ScoreboardScore(board, obj, utils.translate("&fRank: " + prefix + RankEnum.valueOf(hbConfigSetup.get("player.rank")).getPrefix()));
                    ScoreboardScore s4 = new ScoreboardScore(board, obj, utils.translate("&fTokens: &a" + tokens + "✯"));
                    BungeeMessageListener bungeeMessageListener = new BungeeMessageListener();
                    try{
                        HubExtended.getInstance().getBungeeChannelApi().getPlayerCount("ALL").whenComplete((result,error) -> {
                            online = result;
                        });
                    }catch (Exception ignored){
                    }
                    ScoreboardScore s5;
                    if(!(online == 0)){
                        s5 = new ScoreboardScore(board, obj, utils.translate("&fOnline: &a" + online));
                    }else{
                        s5 = new ScoreboardScore(board, obj, utils.translate("&fOnline: &a" + 0));
                    }

                    try{
                        HubExtended.getInstance().getBungeeChannelApi().getServer().whenComplete((result,error) -> {
                            serverName = result;
                        });
                    }catch (Exception ignored){

                    }

                    ScoreboardScore s6 = new ScoreboardScore(board, obj, utils.translate("&fServer: &a" + serverName));
                    ScoreboardScore s7 = new ScoreboardScore(board, obj, utils.translate("&fWins: &a" + hubPlayer.wins));
                    ScoreboardScore s8 = new ScoreboardScore(board, obj, utils.translate("&fLevel: &a" + hubPlayer.level));
                    ScoreboardScore s9 = new ScoreboardScore(board, obj, utils.translate(""));
                    ScoreboardScore s10 = new ScoreboardScore(board, obj, utils.translate("&estreafenetwork.gg"));

                    /*
                    if(hubPlayer.inGame){
                        ScoreboardScore s11 = new ScoreboardScore(board, obj, utils.translate("  &bIn game: &atrue"));
                        s11.setScore(-2);
                        PacketPlayOutScoreboardScore ps11 = new PacketPlayOutScoreboardScore(s11);
                        PacketUtils.sendPacket(player, ps11);
                    }else{
                        ScoreboardScore s11 = new ScoreboardScore(board, obj, utils.translate("  &bIn game: &afalse"));
                        s11.setScore(-2);
                        PacketPlayOutScoreboardScore ps11 = new PacketPlayOutScoreboardScore(s11);
                        PacketUtils.sendPacket(player, ps11);
                    }

                     */


                    s1.setScore(8);
                    s2.setScore(7);
                    s3.setScore(6);
                    s4.setScore(5);
                    s5.setScore(4);
                    s6.setScore(3);
                    s7.setScore(2);
                    s8.setScore(1);
                    s9.setScore(0);
                    s10.setScore(-1);


                    PacketPlayOutScoreboardScore ps1 = new PacketPlayOutScoreboardScore(s1);
                    PacketPlayOutScoreboardScore ps2 = new PacketPlayOutScoreboardScore(s2);
                    PacketPlayOutScoreboardScore ps3 = new PacketPlayOutScoreboardScore(s3);
                    PacketPlayOutScoreboardScore ps4 = new PacketPlayOutScoreboardScore(s4);
                    PacketPlayOutScoreboardScore ps5 = new PacketPlayOutScoreboardScore(s5);
                    PacketPlayOutScoreboardScore ps6 = new PacketPlayOutScoreboardScore(s6);
                    PacketPlayOutScoreboardScore ps7 = new PacketPlayOutScoreboardScore(s7);
                    PacketPlayOutScoreboardScore ps8 = new PacketPlayOutScoreboardScore(s8);
                    PacketPlayOutScoreboardScore ps9 = new PacketPlayOutScoreboardScore(s9);
                    PacketPlayOutScoreboardScore ps10 = new PacketPlayOutScoreboardScore(s10);


                    PacketUtils.sendPacket(player, removepacket);
                    PacketUtils.sendPacket(player, createpacket);
                    PacketUtils.sendPacket(player, display);

                    PacketUtils.sendPacket(player, ps1);
                    PacketUtils.sendPacket(player, ps2);
                    PacketUtils.sendPacket(player, ps3);
                    PacketUtils.sendPacket(player, ps4);
                    PacketUtils.sendPacket(player, ps5);
                    PacketUtils.sendPacket(player, ps6);
                    PacketUtils.sendPacket(player, ps7);
                    PacketUtils.sendPacket(player, ps8);
                    PacketUtils.sendPacket(player, ps9);
                    PacketUtils.sendPacket(player, ps10);

                }

            }

        };

    }
    public void updateScoreBoardOld(){
        taskId = HubExtended.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(HubExtended.getInstance(), new BukkitRunnable() {
            @Override
            public void run() {
                HBConfigSetup hbConfigSetup = new HBConfigSetup(player);

                tokens = hbConfigSetup.getInt("player.tokens");
                /*
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                Scoreboard scoreboard = manager.getNewScoreboard();


                 */
                Scoreboard scoreboard = HubExtended.getInstance().getServer().getScoreboardManager().getNewScoreboard();

                Objective objective = scoreboard.registerNewObjective("Tokens","dummy");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                objective.setDisplayName(utils.translate(animatedText + ""));

                Score space4 = objective.getScore(utils.translate(" "));
                space4.setScore(5);

                /*
                Score space = objective.getScore(utils.translate("&7--x---x--"));
                space.setScore(5);
                 */

                Score tokens = objective.getScore(utils.translate("&7Tokens: &e") + hubPlayer.tokens);
                tokens.setScore(2);

                Score space2 = objective.getScore(utils.translate("&7"));
                space2.setScore(3);

                String prefix = "";

                if(hbConfigSetup.get("player.rank").equalsIgnoreCase("MEMBER")){
                    prefix = "&7";
                } else if(hbConfigSetup.get("player.rank").equalsIgnoreCase("VIP")){
                    prefix = "&a";
                }else if(hbConfigSetup.get("player.rank").equalsIgnoreCase("ALIEN")){
                    prefix = "&5";
                }else if(hbConfigSetup.get("player.rank").equalsIgnoreCase("ALIENPLUSS")){
                    prefix = "&5";
                }else if(hbConfigSetup.get("player.rank").equalsIgnoreCase("MODERATOR")) {
                    prefix = "&3";
                }else if(hbConfigSetup.get("player.rank").equalsIgnoreCase("ADMIN")){
                    prefix = "&c";
                } else if(hbConfigSetup.get("player.rank").equalsIgnoreCase("CO_OWNER")){
                    prefix = "&d";
                } else if(hbConfigSetup.get("player.rank").equalsIgnoreCase("OWNER")){
                    prefix = "&4";
                }

                Score rank = objective.getScore(utils.translate("&7Rank: ") + utils.translate(prefix + hbConfigSetup.get("player.rank")));
                rank.setScore(4);


                Score space3 = objective.getScore(utils.translate("  "));
                space3.setScore(1);

                player.setScoreboard(scoreboard);
            }
        },0L,1L);
    }


}

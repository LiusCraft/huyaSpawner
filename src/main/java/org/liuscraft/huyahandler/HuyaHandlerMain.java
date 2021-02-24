package org.liuscraft.huyahandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.java_websocket.enums.ReadyState;
import org.liuscraft.huyahandler.listen.PlayerListen;
import org.liuscraft.huyahandler.utils.HuyaDinYue;
import org.liuscraft.huyahandler.utils.MessageUtils;
import org.liuscraft.huyahandler.utils.MobSpawnUtils;
import org.liuscraft.huyahandler.utils.ParamsUtil;

import java.io.File;
import java.net.URI;
import java.util.*;

public class HuyaHandlerMain extends JavaPlugin {
    public static HuyaHandlerMain instance;
    public static Runnable fanTask;
    public static boolean listen = true; //监听时间秒
    public static HashMap<Integer, GiftEntity> giftList = new HashMap<Integer, GiftEntity>();
    public static int money = 0;
    public static int baseMoney = 0;
    public static int totalMoney = 0;
    public static int totalMoneys = 0;
    public static boolean moneySp = false;
    public static boolean giftSp = false;
    public static int sharerCount = 0;
    public static HashMap<String, Integer> barrageCountList = new HashMap<String, Integer>();
    public static long runnableTime = 0;
    public static HuyaDinYue huyaDinYue;
    public static int barrageCount = 0;
    public static boolean actionBarAwait = false;
    public static int mb = 1;
    public static HashMap<String, Location> deathLocationList= new HashMap<String, Location>();

    @Override
    public void onEnable() {
        HuyaHandlerMain.instance = this;
        if (!new File(this.getDataFolder(), "config.yml").exists()) {
            this.saveDefaultConfig();
        }
        getConfig();
        HuyaHandlerMain.instance.getServer().getPluginManager().registerEvents(new PlayerListen(), this);
        new BukkitRunnable(){
            public void run() {
                MessageUtils.log("启动监听线程!");
                new Thread(HuyaHandlerMain.fanTask).start();
                new Thread(new Runnable() {
                    public void run() {
                        huyaDinYue = new HuyaDinYue();
                        long id = getConfig().getLong("huYaId");
                        if (id < 1){
                            MessageUtils.log("虎牙主播订阅模式未开启");
                            return;
                        }
                        if(huyaDinYue.initDinYue(id)){
                            MessageUtils.log("虎牙主播订阅模式已开启");
                            int radius = HuyaHandlerMain.instance.getConfig().contains("radius") ? HuyaHandlerMain.instance.getConfig().getInt("radius") : 30;
                            int maxTryTimes = HuyaHandlerMain.instance.getConfig().contains("maxTryTimes") ? HuyaHandlerMain.instance.getConfig().getInt("maxTryTimes") : 200;
                            while (true) {
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                int i = huyaDinYue.getNewDinYue();
                                if (i>0){
                                    final Collection<? extends Player> onlinePlayers = (Collection<? extends Player>) Bukkit.getOnlinePlayers();
                                    if (onlinePlayers.isEmpty()){
                                        continue;
                                    }
                                    final int r = new Random().nextInt(onlinePlayers.size());
                                    Player player = onlinePlayers.toArray(new Player[0])[r];
                                    for (int j = 0; j < (i/instance.getConfig().getInt("subscription", 1)); j++) {
                                        MobSpawnUtils.spawnMob(player, radius, maxTryTimes);
                                        if (HuyaHandlerMain.instance.getConfig().getBoolean("multiPlayer")){
                                            player = onlinePlayers.toArray(new Player[0])[new Random().nextInt(onlinePlayers.size())];
                                        }
                                    }
                                    MessageUtils.sendActionBar("&a主播增加了 §c§l"+i+"§6§l个订阅数 §6§l召唤了 §c§l"+i+" §6§l只怪！", 3, true);
                                }
                            }
                        }else {
                            MessageUtils.log("虎牙主播订阅模式未开启");
                        }
                    }
                }).start();
            }
        }.runTaskAsynchronously(HuyaHandlerMain.instance);
        new BukkitRunnable(){
            public void run() {
                runnableTime++;
                if (runnableTime % (20*60) == 0){
                    MessageUtils.send("● 作者(B站): LiusCraft_TOP");
                    MessageUtils.send( "· (B站): 六芒猫");
                }
                if (runnableTime% (2)==0){
                    String message = "";
                    if (mb==1){
                        if (getConfig().getBoolean("moneySpawner")){
                            if (getConfig().getInt("totalMoneySpawner")>0){
                                int deftotalS = getConfig().getInt("totalMoneySpawner");
                                int ci = HuyaHandlerMain.totalMoney/deftotalS;
                                message += " ● &e累积礼物&f(&a"+(HuyaHandlerMain.totalMoney-deftotalS*ci)+"&f/&e"+deftotalS+"&f)"+message;
                            }
                        }
                        if (getConfig().getBoolean("shareLiveNotice", false)){
                            if (getConfig().getInt("shareCount", 0)>0){
                                message += " ● &d累积分享&f(&a"+sharerCount+"&f/&e"+getConfig().getInt("shareCount", 0)+"&f)";
                            }
                        }
                        if (getConfig().getBoolean("barrageSpawner", false)){
                            if (getConfig().getInt("barrageCount", 0)>0){
                                int d = getConfig().getInt("barrageCount", 0);
                                message += " ● &c累积弹幕&f(&a"+(barrageCount - (barrageCount/d)*d)+"&f/&e"+d+"&f)";
                            }
                        }
                    }
                    if (mb==2){
                        int deftotal = getConfig().getInt("totalMoney", 0);
                        int cii = HuyaHandlerMain.totalMoney/deftotal;
                        message += " ● &c距离BOSS出现&f(&a"+(HuyaHandlerMain.totalMoney-deftotal*cii)+"&f/&e"+deftotal+"&f)";
                        if (huyaDinYue!=null&&huyaDinYue.getDinYueShu()>0){
                            message += " ● &d订阅&f(&a"+huyaDinYue.getDinYueShu()+"&f)";
                        }
                    }
                    MessageUtils.sendActionBar(message, 2);
                }
            }
        }.runTaskTimer(this, 21, 21);
        MessageUtils.log(String.valueOf(moneySp));
    }
    @Override
    public FileConfiguration getConfig() {
        FileConfiguration fileConfiguration = super.getConfig();
        List<String> gifts = fileConfiguration.getStringList("gift");
        giftList = new HashMap<Integer, GiftEntity>();
        for (String gift : gifts) {
            String[] s = gift.split(";");
            if (s.length > 1){
                GiftEntity giftEntity = new GiftEntity(Integer.parseInt(s[0]), "未知礼物["+s[0]+"]", Integer.parseInt(s[1]));
                if (s.length==3){
                    giftEntity.setName(s[2]);
                }
                giftList.put(Integer.parseInt(s[0]), giftEntity);
            }
        }
        moneySp = fileConfiguration.getBoolean("moneySpawner");
        giftSp = fileConfiguration.getBoolean("giftSpawner");
        baseMoney = fileConfiguration.getInt("money");
        return fileConfiguration;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length==1){
            if (!sender.isOp()){
                MessageUtils.send("权限不足",sender);
                return true;
            }
            if ("stop".equals(args[0])){
                HuyaHandlerMain.listen = false;
                MessageUtils.send("关闭了虎牙监听!", sender);
            }else if ("start".equals(args[0])){
                HuyaHandlerMain.listen = true;
                MessageUtils.send("开启了虎牙监听!", sender);
            }else if ("reload".equals(args[0])){
                reloadConfig();
                MessageUtils.send("重载成功！",sender);
            }else if ("1".equals(args[0])){
                mb = 1;
                MessageUtils.send("面板切换成功", sender);
            } else if ("2".equals(args[0])){
                mb = 2;
                MessageUtils.send("面板切换成功", sender);
            } else if("t".equalsIgnoreCase(args[0])){
                MobSpawnUtils.spawnEntity(EntityType.ZOMBIE, ((Player)sender).getLocation());
            } else {
                MessageUtils.send("错误的指令",sender);
            }
        } else if (args.length==2){
            if ("t".equalsIgnoreCase(args[0])){
                if (!(sender instanceof Player)){
                    MessageUtils.send("玩家才可以执行",sender);
                    return true;
                }
                Player player = (Player) sender;
                Location location = deathLocationList.get(args[1]);
                if (location!=null){
                    deathLocationList.remove(args[1]);
                    if(player.teleport(location)){
                        MessageUtils.send("您已回到死亡的位置！", player);
                    }else {
                        MessageUtils.send("您回到死亡的位置失败！", player);
                    }
                }else {
                    MessageUtils.send("您没有这个位置的死亡记录！", player);
                }
            }

        }else {
            MessageUtils.send("stop：关闭监听\nstart：开启监听",sender);
        }
        return true;
    }

    @Override
    public void onDisable() {
        HuyaHandlerMain.listen = false;

    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        getConfig();
    }

    static {
        HuyaHandlerMain.fanTask = new Runnable() {
            public void run() {
                while (true){
                    if (!HuyaHandlerMain.listen) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    MessageUtils.log("正在开始监听");
                    try {
                            String appId = "e0fa340f92189294";      //小程序开发者ID（成为开发者后，https://ext.huya.com可查）
                            String secret = "d4f123dd94af8ed2acc6d22adfc5e012";     //小程序开发者密钥（成为开发者后，https://ext.huya.com可查）
                            long roomId = HuyaHandlerMain.instance.getConfig().getInt("roomId");        //监听主播的房间号

                            Map<String, Object> map = new HashMap<String, Object>(16);
                            map = WebSocketClient.getWebSocketJwtParamsMap(appId,secret,roomId);

                            StringBuffer urlBuffer = new StringBuffer();
                            urlBuffer.append("ws://ws-apiext.huya.com/index.html").append(ParamsUtil.MapToUrlString(map));

                            WebSocketClient client = new WebSocketClient(URI.create(urlBuffer.toString()));
                            client.setConnectionLostTimeout(3600);
                            client.connect();
                            while (!client.getReadyState().equals(ReadyState.OPEN)) {
                            }
                            Long reqId = System.currentTimeMillis();
                            String sendMsg = "{\"command\":\"subscribeNotice\",\"data\":[\"getShareLiveNotice\",\"getSendItemNotice\",\"getMessageNotice\"],\"reqId\":\"" + reqId + "\"}";
                            client.send(sendMsg);
                            while (HuyaHandlerMain.listen) {
                                Thread.sleep(1000);
                                client.send("ping");
                            }
                            client.closeConnection(0,"bye");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }


}

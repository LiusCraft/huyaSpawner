package org.liuscraft.huyahandler;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.liuscraft.huyahandler.utils.MessageUtils;
import org.liuscraft.huyahandler.utils.MobSpawnUtils;
import org.liuscraft.huyahandler.utils.ParamsUtil;

import java.net.URI;
import java.util.*;


/**
 * 开放API websocket 接入实现样例
 *
 */
public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    
    public WebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake arg0) {
    	System.out.println("------ WebSocketClient onOpen ------");
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
    	System.out.println("------ WebSocketClient onClose ------");
    }

    @Override
    public void onError(Exception arg0) {
    	System.out.println("------ WebSocketClient onError ------");
    }

    @Override
    public void onMessage(String arg0) {
    	//System.out.println("-------- 接收到服务端数据： " + arg0 + "--------");
    	try {
        	JSONObject res = JSONObject.parseObject(arg0);
        	if("command".equals(res.getString("notice"))) {//监听成功回包
        		//System.out.println("-------- 监听事件： " + res.getJSONObject("data").getJSONArray("data") + " 成功--------");
        	}
            final Collection<? extends Player> onlinePlayers = (Collection<? extends Player>) Bukkit.getOnlinePlayers();
            if (onlinePlayers.isEmpty()){
                return;
            }
            final int r = new Random().nextInt(onlinePlayers.size());
            Player player = onlinePlayers.toArray(new Player[0])[r];
            int radius = HuyaHandlerMain.instance.getConfig().contains("radius") ? HuyaHandlerMain.instance.getConfig().getInt("radius") : 30;
            int maxTryTimes = HuyaHandlerMain.instance.getConfig().contains("maxTryTimes") ? HuyaHandlerMain.instance.getConfig().getInt("maxTryTimes") : 200;
        	if("getSendItemNotice".equals(res.getString("notice"))) {
                JSONObject data = JSONObject.parseObject(arg0).getJSONObject("data");
                //粉丝徽章名称
                String badgeName = data.getString("badgeName");
                //粉丝等级
                Integer fansLevel = data.getInteger("fansLevel");
                //礼物id
                Integer giftId = data.getInteger("itemId");
                //贵族等级
                Integer nobleLevel = data.getInteger("nobleLevel");
                //房间号
                Long roomId = data.getLong("roomId");
                //送礼连击数
                Long sendItemCount = data.getLong("sendItemCount");
                //送礼者昵称
                String sendNick = data.getString("sendNick");
                //用户等级
                Long senderLevel = data.getLong("senderLevel");
                //System.out.println(String.format("-------- 粉丝勋章：%s,粉丝等级:%s,礼物id:%s,贵族等级:%s,房间号:%s,送礼连击数:%s,送礼者昵称:%s,用户等级:%s --------"
                //		,badgeName,fansLevel,giftId,nobleLevel,roomId,sendItemCount,sendNick,senderLevel));

                GiftEntity giftEntity = HuyaHandlerMain.giftList.get(giftId);
                if (giftEntity==null){
                    //MessageUtils.send("未知的礼物"+giftId);
                    return;
                }
                int gwSize = 0;

                if (HuyaHandlerMain.moneySp){
                    HuyaHandlerMain.money += giftEntity.getMoney()*sendItemCount;
                    int i;
                    if (HuyaHandlerMain.baseMoney>0){
                        for (i = 0; i < HuyaHandlerMain.money / HuyaHandlerMain.baseMoney; i++) {
                            if (HuyaHandlerMain.instance.getConfig().getBoolean("multiPlayer")){
                                player = onlinePlayers.toArray(new Player[0])[new Random().nextInt(onlinePlayers.size())];
                            }
                            gwSize++;
                            MobSpawnUtils.spawnMob(player, radius, maxTryTimes);
                        }
                    }
                    //MessageUtils.send("用户:"+sendNick+"赠送了价值"+HuyaHandlerMain.money+"的礼物");
                    HuyaHandlerMain.totalMoney += HuyaHandlerMain.money;
                    HuyaHandlerMain.totalMoneys += HuyaHandlerMain.money;
                    HuyaHandlerMain.money = 0;
                    int totalMoneySpawner = HuyaHandlerMain.instance.getConfig().getInt("totalMoneySpawner");
                    int totalMoney = HuyaHandlerMain.instance.getConfig().getInt("totalMoney", 0);
                    if (totalMoney>0&&HuyaHandlerMain.totalMoney % totalMoney == 0){
                        Player player2 = player;
                        if (r<(onlinePlayers.size()-1)){
                            player2 = onlinePlayers.toArray(new Player[0])[r+1];
                        }else if (r>0){
                            player2 = onlinePlayers.toArray(new Player[0])[r-1];
                        }
                        if (MobSpawnUtils.spawnBossEntity(player2, radius)){
                            gwSize++;
                            MessageUtils.sendTitle("&c注意 &a"+player2.getDisplayName(), "BOSS将出现在"+player2.getDisplayName()+"的身边，请赶紧前往帮助TA吧！", 1, 5, 1);
                        }else {
                            MessageUtils.send("&c&l召唤BOSS发生了错误...");
                        }
                    }
                    if (totalMoneySpawner>0){
                        for (i = 0; i < HuyaHandlerMain.totalMoneys / totalMoneySpawner; i++) {
                            if (HuyaHandlerMain.instance.getConfig().getBoolean("multiPlayer")){
                                player = onlinePlayers.toArray(new Player[0])[new Random().nextInt(onlinePlayers.size())];
                            }
                            gwSize++;
                            MobSpawnUtils.spawnMob(player, radius, maxTryTimes);
                        }
                        HuyaHandlerMain.totalMoneys = 0;
                    }
                }
                if (HuyaHandlerMain.giftSp){
                    gwSize++;
                    MobSpawnUtils.spawnMob(player, radius, maxTryTimes);
                }
                if (gwSize==0){
                    return;
                }
                MessageUtils.sendActionBar("&f[&a&l"+sendNick+"&f] §6§l赠送了 §c§l"+giftEntity.getName()+" "+sendItemCount+"个 §6§l召唤了 §c§l"+gwSize+" §6§l只怪！", 3, true);
                //MessageUtils.sendTitle(player, "&c注意", "怪物在你身边！！！", 1, 3,1);
        	}
        	if ("getMessageNotice".equals(res.get("notice"))){
                JSONObject data = JSONObject.parseObject(arg0).getJSONObject("data");

                if (HuyaHandlerMain.instance.getConfig().getBoolean("barrageSpawner")){
                    HuyaHandlerMain.barrageCount++;
                    if (HuyaHandlerMain.barrageCount % HuyaHandlerMain.instance.getConfig().getInt("barrageCount") == 0){
                        // 刷怪
                        MessageUtils.sendActionBar("因为观众们在直播间说了"+HuyaHandlerMain.instance.getConfig().getInt("barrageCount")+"句话，所以生成了 1 只怪", 3,true);
                        MobSpawnUtils.spawnMob(player, radius, maxTryTimes);
                    }
                    List<String> barrageList = HuyaHandlerMain.instance.getConfig().getStringList("barrage");
                    if (barrageList!=null){
                        for (String s : barrageList) {
                            String[] sp = s.split(";");
                            if (sp.length==2){
                                if (!data.getString("content").contains(sp[0])){
                                    continue;
                                }
                                Integer count = HuyaHandlerMain.barrageCountList.get(sp[0]);
                                if (count == null){
                                    count = 1;
                                }else {
                                    count++;
                                }
                                if (count.intValue() >= Integer.parseInt(sp[1])){
                                    MessageUtils.sendActionBar("因大家都说 "+sp[0]+"，所以要来一只怪物了！", 3,true);
                                    MobSpawnUtils.spawnMob(player, radius, maxTryTimes);
                                    count = 0;
                                }
                                HuyaHandlerMain.barrageCountList.put(sp[0], count);
                                break;
                            }
                        }
                    }
                }
                if (!HuyaHandlerMain.instance.getConfig().getBoolean("showChat")){
                    return;
                }
                MessageUtils.send("["+data.getInteger("fansLevel")+"级]"+data.getString("sendNick")+": "+data.getString("content"));
            }
            if ("getShareLiveNotice".equals(res.get("notice"))&&HuyaHandlerMain.instance.getConfig().getBoolean("shareLiveNotice")){
                JSONObject data = JSONObject.parseObject(arg0).getJSONObject("data");
                HuyaHandlerMain.sharerCount++;
                if (HuyaHandlerMain.sharerCount>=HuyaHandlerMain.instance.getConfig().getInt("shareCount")){
                    MessageUtils.sendActionBar("因分享了"+HuyaHandlerMain.sharerCount+"次直播间，要来一只怪物了！", 3, true);
                    MobSpawnUtils.spawnMob(player, radius, maxTryTimes);
                    HuyaHandlerMain.sharerCount = 0;
                }

            }
		} catch (Exception e) {
			System.out.println("-------- 数据处理异常 --------");
			e.printStackTrace();
		}
    }
    
    /**
     * 生成开放API Websocket连接参数
     * @param appId  开发者ID（https://ext.huya.com成为开发者后自动生成）
     * @param secret 开发者密钥（https://ext.huya.com成为开发者后自动生成）
     * @param roomId 要监听主播的房间号
     * @return
     */
    public static Map<String, Object> getWebSocketJwtParamsMap(String appId, String secret, long roomId){
        //获取时间戳（毫秒）
        long currentTimeMillis = System.currentTimeMillis();
        long expireTimeMillis = System.currentTimeMillis() + 10 * 60 * 1000;  //超时时间:通常设置10分钟有效，即exp=iat+600，注意不少于当前时间且不超过当前时间60分钟
        Date iat = new Date(currentTimeMillis);
        Date exp = new Date(expireTimeMillis);

        try {
        	
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            
            //生成JWT凭证
            Algorithm algorithm = Algorithm.HMAC256(secret);   //开发者密钥
            String sToken = JWT.create()
                    .withHeader(header)                    //JWT声明
                    .withIssuedAt(iat)                     //jwt凭证生成时间
                    .withExpiresAt(exp)                    //jwt凭证超时时间
                    .withClaim("appId", appId)             //开发者ID
                    .sign(algorithm);


            Map<String, Object> authMap = new HashMap<String, Object>();
            authMap.put("iat", currentTimeMillis / 1000);    //jwt凭证生成时间戳（秒）
            authMap.put("exp", expireTimeMillis / 1000);     //jwt凭证超时时间戳（秒）
            authMap.put("sToken", sToken);                   //jwt签名串
            authMap.put("appId",appId);                      //开发者ID
            authMap.put("do", "comm");                       //接口默认参数
            authMap.put("roomId", roomId);                   //需要监听主播的房间号

            return authMap;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}


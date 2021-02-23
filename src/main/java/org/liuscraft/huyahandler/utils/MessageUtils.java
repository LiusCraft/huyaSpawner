package org.liuscraft.huyahandler.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.liuscraft.huyahandler.HuyaHandlerMain;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MessageUtils
{
    public static String getPrefix() {
        // HuyaHandlerMain.instance.getConfig().contains("prefix") ? (HuyaHandlerMain.instance.getConfig().getString("prefix") + ChatColor.RESET) : ("[" + ChatColor.AQUA + "夏天y团队" + ChatColor.RESET + "]");
        return "[" + ChatColor.AQUA + "夏天y团队" + ChatColor.RESET + "]";
    }

    public static void log(final String message) {
        send(message, (CommandSender) Bukkit.getConsoleSender());
    }

    public static void send(final String message, final CommandSender sender) {
        sender.sendMessage(handleColor(getPrefix() + " " + message));
    }

    public static void send(final String message) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            send(message, (CommandSender)player);
        }
    }

    /**
     *
     * @param player
     * @param maintitle 主标题
     * @param subtitle 副标题
     * @param fadein 渐入时间
     * @param stay 持续秒
     * @param fadeout 渐出时间
     */
    public static void sendTitle(final Player player, final String maintitle, final String subtitle, final int fadein, final int stay, final int fadeout) {
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', maintitle.replace("null", "")), ChatColor.translateAlternateColorCodes('&', subtitle.replace("null", "")), fadein * 20, stay * 20, fadeout * 20);
    }
    /**
     *
     * @param maintitle 主标题
     * @param subtitle 副标题
     * @param fadein 渐入时间
     * @param stay 持续秒
     * @param fadeout 渐出时间
     */
    public static void sendTitle(final String maintitle, final String subtitle, final int fadein, final int stay, final int fadeout) {
        for (final Player player : HuyaHandlerMain.instance.getServer().getOnlinePlayers()) {
            sendTitle(player, maintitle, subtitle, fadein, stay, fadeout);
        }
    }

    public static void sendActionBar(final String message, final int time, boolean await){
        if (await){
            new Thread(new Runnable() {
                public void run() {
                    while (HuyaHandlerMain.actionBarAwait==false) {
                        break;
                    }
                    HuyaHandlerMain.actionBarAwait = false;
                    sendActionBar(message, time);
                    HuyaHandlerMain.actionBarAwait = true;
                    try {
                        Thread.sleep(time*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    HuyaHandlerMain.actionBarAwait = false;
                }
            }).start();
        }else {
            HuyaHandlerMain.actionBarAwait=false;
            sendActionBar(message, time);
        }
    }

    public static void sendActionBar(final String message, int time){
        if (!HuyaHandlerMain.actionBarAwait){
            for (final Player player : Bukkit.getOnlinePlayers()) {
                sendActionBar(message, player, time);
            }
        }
    }
    public static void sendActionBar(final String m, final Player player, int time) {
        if (time < 3) {
            time = 3;
        }
        final int finalTime = time;
        new BukkitRunnable() {
            int i = 0;

            public void run() {
                try {
                    final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
                    final Field playerConnection = Class.forName("net.minecraft.server." + version + ".EntityPlayer").getField("playerConnection");
                    final Class<?> packetPlayOutChat = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
                    final Method send = Class.forName("net.minecraft.server." + version + ".PlayerConnection").getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));
                    final Method getHandle = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer").getMethod("getHandle", (Class<?>[])new Class[0]);
                    final Class<?> iChatBaseComponent = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
                    final Class<?> chatComponentText = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
                    final Class<?> chatMessageType = Class.forName("net.minecraft.server." + version + ".ChatMessageType");
                    Enum e = null;
                    for (final Enum inside_enum : (Enum[])chatMessageType.getEnumConstants()) {
                        if (inside_enum.name().equalsIgnoreCase("GAME_INFO")) {
                            e = inside_enum;
                            break;
                        }
                    }
                    final Object message = chatComponentText.getConstructor(String.class).newInstance(ChatColor.translateAlternateColorCodes('&', m.replace("null", "")));
                    Object packet;
                    try {
                        packet = packetPlayOutChat.getConstructor(iChatBaseComponent, chatMessageType).newInstance(message, e);
                    }
                    catch (Exception a) {
                        packet = packetPlayOutChat.getConstructor(iChatBaseComponent, chatMessageType, player.getUniqueId().getClass()).newInstance(message, e, player.getUniqueId());
                    }
                    send.invoke(playerConnection.get(getHandle.invoke(player, new Object[0])), packet);
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                    player.sendMessage("§c版本/服务端不兼容！");
                }
                ++this.i;
                if (this.i >= finalTime - 2) {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(HuyaHandlerMain.instance, 0L, 20L);
    }

    public static String handleColor(final String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}

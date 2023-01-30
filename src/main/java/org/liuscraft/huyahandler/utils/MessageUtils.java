package org.liuscraft.huyahandler.utils;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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
    public static void sendTitle(final String title, final String message, int time) {
        for (final Player player : HuyaHandlerMain.instance.getServer().getOnlinePlayers()) {
            sendTitle(title, message, player, 10, 20*time, 20);
        }
    }

    public static void sendActionBar(final String title, final String message, int time){
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(title +" >>> "+message));
        }
    }
    public static void sendTitle(final String t, final String m, final Player player, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(t,m,fadeIn, stay, fadeOut);
    }

    public static String handleColor(final String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }


    public static boolean checkIF(String message, String p, Integer g, Integer l) {

        if (l.intValue() != 2)
            if (message.indexOf("p[") !=-1 && message.indexOf("p["+p+"]")==-1) return false;
        if (l.intValue() != 2 && message.indexOf("s!")!=-1) return false;
        if (message.indexOf("g[") !=-1 && g.intValue()<Integer.parseInt(getSubString(message, "g[","]"))) return false;

        return true;
    }
    /**
     * 取两个文本之间的文本值
     * @param text 源文本 比如：欲取全文本为 12345
     * @param left 文本前面
     * @param right  后面文本
     * @return 返回 String
     */
    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }


}

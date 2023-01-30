package org.liuscraft.huyahandler.listen;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.liuscraft.huyahandler.HuyaHandlerMain;

import java.util.List;

public class PlayerListen implements Listener {

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e){
        Location location = e.getEntity().getLocation();
        HuyaHandlerMain.deathLocationList.put(location.toString(), location);
        TextComponent message = new TextComponent(String.format("您的死亡坐标[ X: %.2f, Y: %.2f, Z: %.2f ]", location.getX(), location.getY(), location.getZ()));
        e.getEntity().spigot().sendMessage(message);

    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        event.getPlayer().setScoreboard(HuyaHandlerMain.scoreboard);
    }
    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent event){
        if("LiusCraft".equals(event.getPlayer().getName())){
            event.setCancelled(true);
        }
    }
}

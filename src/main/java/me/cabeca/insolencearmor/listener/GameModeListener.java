package me.cabeca.insolencearmor.listener;

import me.cabeca.insolencearmor.HiddenArmor;
import me.cabeca.insolencearmor.manager.PlayerManager;
import me.cabeca.insolencearmor.util.EventUtil;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

public class GameModeListener implements Listener {
    HiddenArmor plugin;
    PlayerManager hiddenArmorManager;

    public GameModeListener(HiddenArmor plugin){
        EventUtil.register(this, plugin);

        this.plugin = plugin;
        this.hiddenArmorManager = plugin.getPlayerManager();
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event){
        if(!hiddenArmorManager.isEnabled(event.getPlayer())) return;
        if(event.getNewGameMode().equals(GameMode.CREATIVE)) {
            hiddenArmorManager.disablePlayer(event.getPlayer(), false);
            plugin.getArmorUpdater().updatePlayer(event.getPlayer());
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                if (event.getNewGameMode().equals(GameMode.CREATIVE)) {
                    hiddenArmorManager.enablePlayer(event.getPlayer(), false);
                } else {
                    plugin.getArmorUpdater().updatePlayer(event.getPlayer());
                }
            }
        }.runTaskLater(plugin, 1L);
    }


}

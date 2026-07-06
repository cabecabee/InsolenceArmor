package me.cabeca.insolencearmor.listener;

import me.cabeca.insolencearmor.HiddenArmor;
import me.cabeca.insolencearmor.handler.ArmorUpdateHandler;
import me.cabeca.insolencearmor.manager.PlayerManager;
import me.cabeca.insolencearmor.util.EventUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityToggleGlideListener implements Listener {
    HiddenArmor plugin;
    PlayerManager playerManager;
    ArmorUpdateHandler armorUpdater;

    public EntityToggleGlideListener(HiddenArmor plugin){
        EventUtil.register(this, plugin);

        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        this.armorUpdater = plugin.getArmorUpdater();
    }

    @EventHandler
    public void onPlayerToggleGlide(EntityToggleGlideEvent e){
        if(!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        if(playerManager.isArmorVisible(player)) return;

        new BukkitRunnable(){
            @Override
            public void run() {
                armorUpdater.updatePlayer(player);
            }
        }.runTaskLater(plugin, 1L);
    }
}

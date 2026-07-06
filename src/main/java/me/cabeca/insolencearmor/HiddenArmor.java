package me.cabeca.insolencearmor;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.cabeca.insolencearmor.command.HiddenArmorTabCompleter;
import me.cabeca.insolencearmor.command.HiddenArmorCommand;
import me.cabeca.insolencearmor.command.ToggleArmorCommand;
import me.cabeca.insolencearmor.handler.ArmorPlaceholderHandler;
import me.cabeca.insolencearmor.handler.ArmorUpdateHandler;
import me.cabeca.insolencearmor.handler.MessageHandler;
import me.cabeca.insolencearmor.listener.packet.WindowItemsPacketListener;
import me.cabeca.insolencearmor.util.ConfigHolder;
import me.cabeca.insolencearmor.util.protocol.PacketIndexMapper;
import me.cabeca.insolencearmor.util.Metrics;
import me.cabeca.insolencearmor.listener.EntityToggleGlideListener;
import me.cabeca.insolencearmor.listener.GameModeListener;
import me.cabeca.insolencearmor.listener.PotionEffectListener;
import me.cabeca.insolencearmor.listener.InventoryShiftClickListener;
import me.cabeca.insolencearmor.listener.packet.EntityEquipmentPacketListener;
import me.cabeca.insolencearmor.listener.packet.SetSlotPacketListener;
import me.cabeca.insolencearmor.manager.PlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class HiddenArmor extends JavaPlugin {
    private PlayerManager playerManager;
    private ArmorUpdateHandler armorUpdater;
    private ArmorPlaceholderHandler armorPlaceholderHandler;
    private MessageHandler messageHandler;

    private List<ConfigHolder> configHolders;

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        // Default config file
        this.saveDefaultConfig();
        checkConfig();

        PacketIndexMapper packetIndexMapper = new PacketIndexMapper(this);

        // Instantiate members
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.messageHandler = new MessageHandler(this, "&c[&fHiddenArmor&c] &f");
        this.armorUpdater = new ArmorUpdateHandler(this, packetIndexMapper);
        this.playerManager = new PlayerManager(this);
        this.armorPlaceholderHandler = new ArmorPlaceholderHandler(this);

        // Enable commands
        new ToggleArmorCommand(this, "togglearmor")
                .setPermission("hiddenarmor")
                .setPermissionRequired(false);
        new HiddenArmorCommand(this, "hiddenarmor")
                .setPermission("hiddenarmor")
                .setPermissionRequired(false)
                .setTabCompleter(new HiddenArmorTabCompleter(this));

        // Register ProtocolLib packet listeners
        protocolManager.addPacketListener(new SetSlotPacketListener(this, packetIndexMapper));
        protocolManager.addPacketListener(new WindowItemsPacketListener(this, packetIndexMapper));
        protocolManager.addPacketListener(new EntityEquipmentPacketListener(this, packetIndexMapper));

        // Register event listeners
        new InventoryShiftClickListener(this);
        new GameModeListener(this);
        new PotionEffectListener(this);
        new EntityToggleGlideListener(this);

        //getCommand("hiddenarmor").setTabCompleter(new HiddenArmorTabCompleter(this));
        reloadConfig();

        // Metrics
        new Metrics(this, 14419);
    }

    @Override
    public void onDisable() {
        playerManager.saveCurrentEnabledPlayers();
    }

    private void checkConfig() {
        reloadConfig();
        if(getConfig().getInt("config-version") >= getConfig().getDefaults().getInt("config-version"))
            return;
        getLogger().log(Level.WARNING, "Your HiddenArmor configuration file is outdated!");
        getLogger().log(Level.WARNING, "Please regenerate the 'config.yml' file when possible.");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (configHolders == null)
            configHolders = new ArrayList<>();
        configHolders.forEach(c -> c.loadConfig(getConfig()));
    }

    public void addConfigHolder(ConfigHolder configHolder) {
        configHolders.add(configHolder);
    }


    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ArmorUpdateHandler getArmorUpdater() {
        return armorUpdater;
    }

    public ArmorPlaceholderHandler getArmorPlaceholderHandler() {
        return armorPlaceholderHandler;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}

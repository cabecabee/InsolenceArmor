package me.cabeca.insolencearmor.listener.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import me.cabeca.insolencearmor.HiddenArmor;
import me.cabeca.insolencearmor.manager.PlayerManager;
import me.cabeca.insolencearmor.util.ConfigHolder;
import me.cabeca.insolencearmor.util.ItemUtil;
import me.cabeca.insolencearmor.util.protocol.PacketFields;
import me.cabeca.insolencearmor.util.protocol.PacketIndexMapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class EntityEquipmentPacketListener extends PacketAdapter implements ConfigHolder {
    private final PlayerManager hiddenArmorManager;
    private final ProtocolManager protocolManager;

    private boolean ignoreLeatherArmor;
    private boolean ignoreTurtleHelmet;
    private boolean ignoreElytra;

    private final int ENTITY_ID_INDEX;
    private final int SLOT_ITEM_PAIR_LIST_INDEX;

    public EntityEquipmentPacketListener(HiddenArmor plugin, PacketIndexMapper indexMapper) {
        super(plugin, PacketType.Play.Server.ENTITY_EQUIPMENT);
        plugin.addConfigHolder(this);

        this.hiddenArmorManager = plugin.getPlayerManager();
        this.protocolManager = plugin.getProtocolManager();

        this.ENTITY_ID_INDEX = indexMapper.get(PacketFields.ENTITY_EQUIPMENT_$ENTITY_ID);
        this.SLOT_ITEM_PAIR_LIST_INDEX = indexMapper.get(PacketFields.ENTITY_EQUIPMENT_$SLOT_ITEM_PAIR_LIST);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player viewer = event.getPlayer();

        LivingEntity livingEntity = (LivingEntity) protocolManager.getEntityFromID(
                viewer.getWorld(),
                packet.getIntegers().read(ENTITY_ID_INDEX)
        );
        if (!(livingEntity instanceof Player)) return;

        Player packetPlayer = (Player) livingEntity;

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList =
                packet.getSlotStackPairLists().read(SLOT_ITEM_PAIR_LIST_INDEX);

        boolean armorHidden = !hiddenArmorManager.isArmorVisible(packetPlayer);
        boolean pumpkinEnabled = hiddenArmorManager.isPumpkinHelmetEnabled(packetPlayer);

        if (!armorHidden && !pumpkinEnabled) return;

        for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : pairList) {
            EnumWrappers.ItemSlot slot = pair.getFirst();
            ItemStack item = pair.getSecond();

            if (pumpkinEnabled && slot == EnumWrappers.ItemSlot.HEAD) {
                pair.setSecond(createNamedPumpkin(hiddenArmorManager.getPumpkinHelmetName(packetPlayer)));
                continue;
            }

            if (armorHidden) {
                if (item.getType().equals(Material.ELYTRA)
                        && ((packetPlayer.isGliding() || ignoreElytra) && !packetPlayer.isInvisible())) {
                    pair.setSecond(new ItemStack(Material.ELYTRA));
                } else if (!shouldIgnore(item)) {
                    pair.setSecond(new ItemStack(Material.AIR));
                }
            }
        }

        packet.getSlotStackPairLists().write(SLOT_ITEM_PAIR_LIST_INDEX, pairList);
    }

    private ItemStack createNamedPumpkin(String name) {
        ItemStack pumpkin = new ItemStack(Material.CARVED_PUMPKIN);
        ItemMeta meta = pumpkin.getItemMeta();
        if (meta != null) {
            Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(name);
            meta.displayName(comp);
            pumpkin.setItemMeta(meta);
        }
        return pumpkin;
    }

    private boolean shouldIgnore(ItemStack itemStack) {
        Material material = itemStack.getType();

        return (ignoreLeatherArmor && material.toString().startsWith("LEATHER")) ||
                (ignoreTurtleHelmet && material.equals(Material.TURTLE_HELMET)) ||
                (!ItemUtil.isArmor(itemStack) && !itemStack.getType().equals(Material.ELYTRA)) ||
                (ignoreElytra && itemStack.getType().equals(Material.ELYTRA));
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.ignoreLeatherArmor = config.getBoolean("ignore.leather-armor");
        this.ignoreTurtleHelmet = config.getBoolean("ignore.turtle-helmet");
        this.ignoreElytra = config.getBoolean("ignore.elytra");
    }
}
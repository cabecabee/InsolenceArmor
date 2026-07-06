package me.cabeca.insolencearmor.listener.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.cabeca.insolencearmor.HiddenArmor;
import me.cabeca.insolencearmor.handler.ArmorPlaceholderHandler;
import me.cabeca.insolencearmor.manager.PlayerManager;
import me.cabeca.insolencearmor.util.protocol.PacketFields;
import me.cabeca.insolencearmor.util.protocol.PacketIndexMapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SetSlotPacketListener extends PacketAdapter {
    private final PlayerManager playerManager;
    private final ArmorPlaceholderHandler placeholderHandler;

    private final int WINDOW_ID_INDEX;
    private final int SLOT_NUMBER_INDEX;
    private final int ITEM_INDEX;

    public SetSlotPacketListener(HiddenArmor plugin, PacketIndexMapper indexMapper) {
        super(plugin, PacketType.Play.Server.SET_SLOT);
        this.playerManager = plugin.getPlayerManager();
        this.placeholderHandler = plugin.getArmorPlaceholderHandler();

        this.WINDOW_ID_INDEX = indexMapper.get(PacketFields.SET_SLOT_$WINDOW_ID);
        this.SLOT_NUMBER_INDEX = indexMapper.get(PacketFields.SET_SLOT_$SLOT_NUMBER);
        this.ITEM_INDEX = indexMapper.get(PacketFields.SET_SLOT_$ITEM);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player viewer = event.getPlayer();
        PacketContainer packet = event.getPacket();

        if (!packet.getIntegers().read(WINDOW_ID_INDEX).equals(0)) return;

        int slotNumber = packet.getIntegers().read(SLOT_NUMBER_INDEX);
        if (slotNumber < 5 || slotNumber > 8) return;

        ItemStack itemStack = packet.getItemModifier().read(ITEM_INDEX);
        if (itemStack == null) return;

        if (slotNumber == 5 && playerManager.isPumpkinHelmetEnabled(viewer)) {
            packet.getItemModifier().write(ITEM_INDEX, createNamedPumpkin(playerManager.getPumpkinHelmetName(viewer)));
            return;
        }

        if (!playerManager.isArmorVisible(viewer)) {
            ItemStack placeholder = placeholderHandler.buildItemPlaceholder(itemStack);
            packet.getItemModifier().write(ITEM_INDEX, placeholder);
        }
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
}
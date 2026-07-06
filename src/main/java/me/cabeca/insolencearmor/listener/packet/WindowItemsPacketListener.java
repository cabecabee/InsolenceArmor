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

import java.util.List;

public class WindowItemsPacketListener extends PacketAdapter {
    private final PlayerManager playerManager;
    private final ArmorPlaceholderHandler placeholderHandler;

    private final int WINDOW_ID_INDEX;
    private final int ITEM_LIST_INDEX;

    public WindowItemsPacketListener(HiddenArmor plugin, PacketIndexMapper indexMapper) {
        super(plugin, PacketType.Play.Server.WINDOW_ITEMS);
        this.playerManager = plugin.getPlayerManager();
        this.placeholderHandler = plugin.getArmorPlaceholderHandler();

        this.WINDOW_ID_INDEX = indexMapper.get(PacketFields.WINDOW_ITEMS_$WINDOW_ID);
        this.ITEM_LIST_INDEX = indexMapper.get(PacketFields.WINDOW_ITEMS_$ITEM_LIST);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player viewer = event.getPlayer();
        PacketContainer packet = event.getPacket();

        if (!packet.getIntegers().read(WINDOW_ID_INDEX).equals(0)) return;

        List<ItemStack> items = packet.getItemListModifier().read(ITEM_LIST_INDEX);
        if (items == null || items.size() < 9) return;

        for (int i = 5; i < 9; i++) {
            ItemStack itemStack = items.get(i);
            if (itemStack == null) continue;

            if (i == 5 && playerManager.isPumpkinHelmetEnabled(viewer)) {
                items.set(i, createNamedPumpkin(playerManager.getPumpkinHelmetName(viewer)));
                continue;
            }

            if (!playerManager.isArmorVisible(viewer)) {
                ItemStack placeholder = placeholderHandler.buildItemPlaceholder(itemStack);
                items.set(i, placeholder);
            }
        }

        packet.getItemListModifier().write(ITEM_LIST_INDEX, items);
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
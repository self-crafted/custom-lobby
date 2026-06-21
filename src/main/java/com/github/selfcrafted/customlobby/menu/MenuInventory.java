package com.github.selfcrafted.customlobby.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class MenuInventory extends Inventory {
    private static final ItemStack MENU_ITEM = ItemStack.of(Material.COMPASS, 1).withCustomName(
            Component.text("Server Menu", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
    private static final ItemStack WINDOW_FRAME =
            ItemStack.of(Material.GREEN_STAINED_GLASS_PANE, 1).withCustomName(Component.empty());
    private static final ItemStack WINDOW_BACKGROUND =
            ItemStack.of(Material.GRAY_STAINED_GLASS_PANE, 1).withCustomName(Component.empty());
    private final Map<ItemStack, String> servers = new HashMap<>();
    private final Set<ServerButton> buttonList = new HashSet<>();

    public MenuInventory() {
        super(InventoryType.CHEST_5_ROW, Component.text("Server Menu")
                .color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));

        /*
         * ==== click handling
         */
        eventNode().addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);
            var item = event.getClickedItem();
            var player = event.getPlayer();
            var server = servers.get(item);
            if (server != null)
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream msg = new DataOutputStream(out);
                    msg.writeUTF("Connect");
                    msg.writeUTF(server);
                    player.sendPluginMessage("bungeecord:main", out.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });

        /*
         * ==== main window decoration
         */
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 9; x++) {
                var index = y*9+x;
                if (y==0 || y==4 || x==0 || x==8) this.setItemStack(index, WINDOW_FRAME);
                else this.setItemStack(index, WINDOW_BACKGROUND);
            }
        }

        /*
         * dynamic server list TODO allow updating of the list
         */
        for (Map.Entry<ItemStack, String> server : servers.entrySet()) {
            var target = server.getValue();
            var button = server.getKey();
            var serverButton = new ServerButton(button, target);
            servers.put(button, target);
            buttonList.add(serverButton);
        }
    }

    public ItemStack getOpener() {
        return MENU_ITEM;
    }

    public Set<ServerButton> getServerSet() {
        return buttonList;
    }
}

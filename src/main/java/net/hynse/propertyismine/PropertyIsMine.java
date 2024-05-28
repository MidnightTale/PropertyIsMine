package net.hynse.propertyismine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class PropertyIsMine extends JavaPlugin implements Listener {

    private final NamespacedKey ownerKey = new NamespacedKey(this, "owner");
    private final NamespacedKey lockedKey = new NamespacedKey(this, "locked");

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        if (block.getState() instanceof Chest) {
            UUID ownerUUID = player.getUniqueId();
            setOwner(block, ownerUUID);
            player.sendMessage(ChatColor.GREEN + "Chest claimed successfully!");
        }
    }

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null && block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            UUID ownerUUID = getOwner(block);
            if (ownerUUID == null) {
                player.sendMessage(ChatColor.RED + "This chest is unclaimed.");
                return;
            }
            if (event.getPlayer().isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.STICK) {
                if (ownerUUID.equals(player.getUniqueId())) {
                    // Lock the chest
                    chest.getPersistentDataContainer().set(lockedKey, PersistentDataType.BYTE, (byte) 1);
                    chest.update();
                    player.sendMessage(ChatColor.GREEN + "Chest locked successfully!");
                } else {
                    player.sendMessage(ChatColor.RED + "You can only lock chests you own.");
                }
            } else {
                if (isChestLocked(chest)) {
                    if (ownerUUID.equals(player.getUniqueId()) || player.hasPermission("chest.unlock")) {
                        // Allow owner or players with permission to open the locked chest
                        player.sendMessage(ChatColor.GREEN + "This chest is locked. You can still access it.");
                    } else {
                        player.sendMessage(ChatColor.RED + "This chest is locked. Only the owner can open it.");
                        event.setCancelled(true);
                    }
                } else {
                    player.sendMessage(ChatColor.GREEN + "This chest belongs to " + Bukkit.getPlayer(ownerUUID).getName());
                }
            }
        }
    }

    private void setOwner(Block chestBlock, UUID ownerUUID) {
        if (chestBlock.getState() instanceof TileState) {
            TileState tileState = (TileState) chestBlock.getState();
            tileState.getPersistentDataContainer().set(
                    ownerKey,
                    PersistentDataType.STRING,
                    ownerUUID.toString()
            );
            tileState.update();
        } else {
            getLogger().warning("Failed to set owner: Block is not a TileState.");
        }
    }

    private UUID getOwner(Block chestBlock) {
        if (chestBlock.getState() instanceof TileState) {
            TileState tileState = (TileState) chestBlock.getState();
            String ownerUUIDString = tileState.getPersistentDataContainer().get(
                    ownerKey,
                    PersistentDataType.STRING
            );
            return ownerUUIDString != null ? UUID.fromString(ownerUUIDString) : null;
        } else {
            getLogger().warning("Failed to get owner: Block is not a TileState.");
            return null;
        }
    }

    private boolean isChestLocked(Chest chest) {
        return chest.getPersistentDataContainer().has(lockedKey, PersistentDataType.BYTE);
    }
}

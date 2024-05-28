package net.hynse.propertyismine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        Block block = event.getClickedBlock();
        if (block != null && block.getState() instanceof Chest) {
            UUID ownerUUID = getOwner(block);
            if (ownerUUID == null) {
                event.getPlayer().sendMessage(ChatColor.RED + "This chest is unclaimed.");
            } else {
                Player owner = Bukkit.getPlayer(ownerUUID);
                if (owner != null) {
                    event.getPlayer().sendMessage(ChatColor.GREEN + "This chest belongs to " + owner.getName());
                } else {
                    event.getPlayer().sendMessage(ChatColor.GREEN + "This chest belongs to an offline player.");
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

}

package one.lindegaard.MobHunting.compatibility;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.packetwrapper.WrapperPlayServerCollect;
import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;

import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.grinding.Area;

public class ProtocolLibHelper {

    private ProtocolManager protocolManager;

    public void enableProtocolLib() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(MobHunting.getInstance(),
                ListenerPriority.NORMAL, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    PacketContainer packet = event.getPacket().deepClone();
                    StructureModifier<ItemStack> sm = packet.getItemModifier();
                    for (int i = 0; i < sm.size(); i++) {
                        ItemStack is = sm.getValues().get(i);
                        removeIfLoreHidden(is);
                    }
                    event.setPacket(packet);
                } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
                    PacketContainer packet = event.getPacket().deepClone();
                    StructureModifier<List<ItemStack>> modifiers = packet.getItemListModifier();
                    for (int j = 0; j < modifiers.size(); j++) {
                        List<ItemStack> itemStackList = modifiers.getValues().get(j);
                        for (int i = 0; i < itemStackList.size(); i++) {
                            ItemStack is = itemStackList.get(i);
                            removeIfLoreHidden(is);
                        }
                        event.setPacket(packet);
                    }
                }
            }
        });
    }


    public void removeIfLoreHidden(ItemStack is) {
        if (is.hasItemMeta()) {
            ItemMeta itemMeta = is.getItemMeta();
            if (itemMeta.hasLore()) {
                List<String> lore = itemMeta.getLore();
                lore.removeIf(str -> str.startsWith("Hidden:"));
                itemMeta.setLore(lore);
                is.setItemMeta(itemMeta);
            }
        }
    }


    public ProtocolManager getProtocolmanager() {
        return protocolManager;
    }

    public void pickupMoney(Player player, Entity ent) {
        WrapperPlayServerCollect wpsc = new WrapperPlayServerCollect();
        wpsc.setCollectedEntityId(ent.getEntityId());
        wpsc.setCollectorEntityId(player.getEntityId());
        wpsc.sendPacket(player);
    }

    public void showGrindingArea(final Player player, final Area grindingArea, final Location killedLocation) {
        if (ProtocolLibCompat.isSupported()) {
            final WrapperPlayServerWorldParticles wpwp = new WrapperPlayServerWorldParticles();
            final long now = System.currentTimeMillis();
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Killed mob location
                    if (killedLocation != null) {
                        wpwp.setParticleType(Particle.CLOUD);
                        wpwp.setNumberOfParticles(1);
                        wpwp.setOffsetX(0);
                        wpwp.setOffsetY(0);
                        wpwp.setOffsetZ(0);
                        wpwp.setX((float) (killedLocation.getBlockX() + 0.5));
                        wpwp.setZ((float) (killedLocation.getBlockZ() + 0.5));
                        for (int n = 0; n < 10; n++) {
                            wpwp.setY((float) (killedLocation.getBlockY() + 0.2 + 0.2 * n));
                            wpwp.sendPacket(player);
                        }
                    }

                    // Grinding Area
                    if (grindingArea != null) {
                        // Show center of grinding area
                        wpwp.setParticleType(Particle.FLAME);
                        wpwp.setNumberOfParticles(3);
                        wpwp.setOffsetX(0);
                        wpwp.setOffsetY(0);
                        wpwp.setOffsetZ(0);
                        wpwp.setX((float) (grindingArea.getCenter().getBlockX() + 0.5));
                        wpwp.setZ((float) (grindingArea.getCenter().getBlockZ() + 0.5));
                        for (int n = 0; n < 10; n++) {
                            wpwp.setY((float) (grindingArea.getCenter().getBlockY() + 0.2 + 0.1 * n));
                            wpwp.sendPacket(player);
                        }

                        // Circle around the grinding area
                        wpwp.setParticleType(Particle.FLAME);
                        wpwp.setY((float) (grindingArea.getCenter().getBlockY() + 0.2));
                        wpwp.setOffsetY(0);
                        for (int n = 0; n < 360; n = n
                                + (int) (45 / MobHunting.getConfigManager().grindingDetectionRange)) {
                            wpwp.setX((float) (grindingArea.getCenter().getBlockX() + 0.5
                                    + Math.cos(n) * MobHunting.getConfigManager().grindingDetectionRange));
                            wpwp.setZ((float) (grindingArea.getCenter().getBlockZ() + 0.5
                                    + Math.sin(n) * MobHunting.getConfigManager().grindingDetectionRange));
                            wpwp.sendPacket(player);
                        }
                        if (System.currentTimeMillis() > now + (20000L))
                            this.cancel();
                    }
                }
            }.runTaskTimerAsynchronously(MobHunting.getInstance(), 0L, 20L);
        }
    }
}

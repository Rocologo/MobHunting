package one.lindegaard.MobHunting.achievements;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public abstract class AbstractSkullAchievement implements Achievement{

    @Override
    public ItemStack getSymbol() {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 4);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner("MHF_Creeper");
        skull.setItemMeta(skullMeta);
        return skull;
    }
}

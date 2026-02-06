package worldgeneratorextension.vanillagenerator.populator;

import cn.nukkit.block.Block;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.level.generator.object.tree.ObjectMangroveTree;

public class MangroveTreePopulator extends Populator {
    private int baseAmount = 1;

    public void setBaseAmount(int amount) { this.baseAmount = amount; }

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random) {
        int x = (chunkX << 4) + random.nextRange(0, 15);
        int z = (chunkZ << 4) + random.nextRange(0, 15);
        int y = level.getHighestBlockAt(x, z);

        if (level.getBlockIdAt(x, y - 1, z) == Block.MUD) {
            // Genera el árbol de tu archivo ObjectMangroveTree.java
            new ObjectMangroveTree().generate(level, random, new Vector3(x, y, z));

            // Genera Moss Carpet (Musgo) en lugar de flores/pasto
            for (int i = 0; i < 5; i++) {
                int mx = x + random.nextRange(-2, 2);
                int mz = z + random.nextRange(-2, 2);
                int my = level.getHighestBlockAt(mx, mz);
                if (level.getBlockIdAt(mx, my, mz) == Block.AIR) {
                    level.setBlockAt(mx, my, mz, Block.MOSS_CARPET);
                }
            }
        }
    }
}

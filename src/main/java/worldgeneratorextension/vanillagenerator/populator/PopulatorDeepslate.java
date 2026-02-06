package worldgeneratorextension.vanillagenerator.populator;

import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk; // CAMBIO: Usamos FullChunk
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;

public class PopulatorDeepslate extends Populator {
    
    // Si falla BlockID.DEEPSLATE usa el ID numérico correspondiente
    private static final int DEEPSLATE_ID = BlockID.DEEPSLATE; 
    private static final int STONE_ID = BlockID.STONE;

    private final int minHeight;

    public PopulatorDeepslate(int minHeight) {
        this.minHeight = minHeight;
    }

    @Override
    // CAMBIO: El último parámetro ahora es FullChunk
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 8; y >= this.minHeight; y--) {
                    int blockId = chunk.getBlockId(x, y, z);
                    
                    if (blockId == STONE_ID) {
                        if (y <= 0) {
                            chunk.setBlockId(x, y, z, DEEPSLATE_ID);
                        } else {
                            if (random.nextFloat() < (8 - y) / 9.0f) {
                                chunk.setBlockId(x, y, z, DEEPSLATE_ID);
                            }
                        }
                    }
                }
            }
        }
    }
}

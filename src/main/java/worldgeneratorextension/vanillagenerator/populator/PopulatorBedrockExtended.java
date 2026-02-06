package worldgeneratorextension.vanillagenerator.populator;

import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk; 
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;

public class PopulatorBedrockExtended extends Populator {
    
    private final int minHeight;

    public PopulatorBedrockExtended(int minHeight) {
        this.minHeight = minHeight;
    }

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Capa inferior sólida
                chunk.setBlockId(x, minHeight, z, BlockID.BEDROCK);

                // Capas superiores aleatorias
                for (int i = 1; i < 5; i++) {
                    if (random.nextBoundedInt(i + 1) == 0) {
                        chunk.setBlockId(x, minHeight + i, z, BlockID.BEDROCK);
                    }
                }
            }
        }
    }
}

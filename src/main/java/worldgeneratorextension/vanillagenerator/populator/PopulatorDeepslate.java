package worldgeneratorextension.vanillagenerator.populator;

import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;

public class PopulatorDeepslate extends Populator {
    
    // Intenta usar el ID estático, si tu Nukkit es muy viejo y falla aquí, cámbialo por el ID numérico (ej. -6)
    private static final int DEEPSLATE_ID = BlockID.DEEPSLATE; 
    private static final int STONE_ID = BlockID.STONE;

    private final int minHeight;

    public PopulatorDeepslate(int minHeight) {
        this.minHeight = minHeight;
    }

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, BaseFullChunk chunk) {
        // Barrido del chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Iteramos desde Y=8 hacia abajo hasta la altura mínima
                for (int y = 8; y >= this.minHeight; y--) {
                    int blockId = chunk.getBlockId(x, y, z);
                    
                    // Solo reemplazamos Piedra normal
                    if (blockId == STONE_ID) {
                        if (y <= 0) {
                            // Por debajo de 0, todo es Deepslate
                            chunk.setBlockId(x, y, z, DEEPSLATE_ID);
                        } else {
                            // Entre 0 y 8, hacemos una mezcla (dithering)
                            // Probabilidad de Deepslate aumenta cuanto más bajamos
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

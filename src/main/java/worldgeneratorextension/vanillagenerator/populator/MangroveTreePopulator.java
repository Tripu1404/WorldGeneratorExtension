package worldgeneratorextension.vanillagenerator.populator;

import cn.nukkit.block.Block;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk; // Importante
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.level.generator.object.tree.ObjectMangroveTree;

public class MangroveTreePopulator extends Populator {
    private int baseAmount = 1;

    public void setBaseAmount(int amount) { this.baseAmount = amount; }

    // CORRECCIÓN 1: La firma debe incluir FullChunk
    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        int amount = random.nextRange(0, 1) + baseAmount;
        
        for (int i = 0; i < amount; ++i) {
            int x = random.nextRange(0, 15);
            int z = random.nextRange(0, 15);
            
            // CORRECCIÓN 2: Usamos el chunk para obtener la altura, no el level
            // Y usamos coordenadas locales (0-15)
            int y = chunk.getHighestBlockAt(x, z);

            // Obtenemos coordenadas globales para la generación
            int globalX = (chunkX << 4) + x;
            int globalZ = (chunkZ << 4) + z;

            // Verificamos el bloque de suelo usando el ID 1610 (Mud)
            int floorId = chunk.getBlockId(x, y - 1, z);
            
            if (floorId == 1610 || floorId == Block.DIRT || floorId == Block.GRASS) {
                // CORRECCIÓN 3: Nos aseguramos de pasar los tipos correctos
                ObjectMangroveTree tree = new ObjectMangroveTree();
                tree.generate(level, random, new Vector3(globalX, y, globalZ));

                // Generar musgo (Moss Carpet = 1615)
                for (int j = 0; j < 6; j++) {
                    int randX = random.nextRange(-3, 3);
                    int randZ = random.nextRange(-3, 3);
                    
                    // Verificamos límites del chunk para evitar errores
                    if (x + randX >= 0 && x + randX < 16 && z + randZ >= 0 && z + randZ < 16) {
                         int currentY = chunk.getHighestBlockAt(x + randX, z + randZ);
                         if (chunk.getBlockId(x + randX, currentY, z + randZ) == 0) { // 0 es AIR
                             chunk.setBlock(x + randX, currentY, z + randZ, 1615); // 1615 es Moss Carpet
                         }
                    }
                }
            }
        }
    }
}

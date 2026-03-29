package worldgeneratorextension.vanillagenerator.ground;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.NukkitRandom;
import worldgeneratorextension.vanillagenerator.NormalGenerator;
import worldgeneratorextension.vanillagenerator.biome.BiomeClimate;

public class GroundGenerator implements BlockID {

    protected int topMaterial;
    protected int topData;
    protected int groundMaterial;
    protected int groundData;

    // ID de la Deepslate (Pizarra Profunda) para Nukkit
    private static final int DEEPSLATE_ID = -378; 

    public GroundGenerator() {
        setTopMaterial(GRASS);
        setGroundMaterial(DIRT);
    }

    /**
     * Genera la columna de terreno con soporte para capas negativas y Deepslate.
     */
    public void generateTerrainColumn(ChunkManager world, BaseFullChunk chunkData, NukkitRandom random, int chunkX, int chunkZ, int biome, double surfaceNoise) {
        int seaLevel = NormalGenerator.SEA_LEVEL; // Basado en la configuración global

        // Configuración de transición para Deepslate
        int deepslateMax = 8; 
        int deepslateMin = 0;

        int topMat = this.topMaterial;
        int groundMat = this.groundMaterial;

        int x = chunkX & 0xF;
        int z = chunkZ & 0xF;

        int surfaceHeight = Math.max((int) (surfaceNoise / 3.0D + 3.0D + random.nextDouble() * 0.25D), 1);
        int deep = -1;

        // Bucle corregido para procesar desde el cielo hasta el nuevo fondo en -64
        for (int y = 255; y >= -64; y--) {
            // Generación de Bedrock ajustada al nuevo fondo del mundo
            if (y <= -64 + random.nextBoundedInt(5)) {
                chunkData.setBlock(x, y, z, BEDROCK);
            } else {
                int mat = chunkData.getBlockId(x, y, z);

                // Lógica de Deepslate: Reemplaza la piedra en las nuevas capas profundas
                if (mat == STONE && y < deepslateMax) {
                    // Crea una mezcla orgánica; por debajo de deepslateMin todo es Deepslate
                    if (y <= deepslateMin || random.nextBoundedInt(Math.max(1, y - deepslateMin)) == 0) {
                        mat = DEEPSLATE_ID;
                        chunkData.setBlock(x, y, z, mat);
                    }
                }

                if (mat == AIR) {
                    deep = -1;
                } else if (mat == STONE || mat == DEEPSLATE_ID) {
                    if (deep == -1) {
                        if (y >= seaLevel - 5 && y <= seaLevel) {
                            topMat = this.topMaterial;
                            groundMat = this.groundMaterial;
                        }

                        deep = surfaceHeight;
                        if (y >= seaLevel - 2) {
                            chunkData.setBlock(x, y, z, topMat, this.topData);
                        } else if (y < seaLevel - 8 - surfaceHeight) {
                            topMat = AIR;
                            // Mantiene el material base (Piedra o Deepslate) para el relleno profundo
                            groundMat = mat; 
                            chunkData.setBlock(x, y, z, GRAVEL);
                        } else {
                            chunkData.setBlock(x, y, z, groundMat, this.groundData);
                        }
                    } else if (deep > 0) {
                        deep--;
                        chunkData.setBlock(x, y, z, groundMat, this.groundData);

                        // Soporte para biomas arenosos (arenisca)
                        if (deep == 0 && groundMat == SAND) {
                            deep = random.nextBoundedInt(4) + Math.max(0, y - seaLevel - 1);
                            groundMat = SANDSTONE;
                        }
                    }
                } else if (mat == Block.STILL_WATER && y == seaLevel - 2 && BiomeClimate.isCold(biome, chunkX, y, chunkZ)) {
                    // Capa de hielo en biomas fríos
                    chunkData.setBlock(x, y, z, ICE);
                }
            }
        }
    }

    protected final void setTopMaterial(int topMaterial) {
        this.setTopMaterial(topMaterial, 0);
    }

    protected final void setTopMaterial(int topMaterial, int topData) {
        this.topMaterial = topMaterial;
        this.topData = topData;
    }

    protected final void setGroundMaterial(int groundMaterial) {
        this.setGroundMaterial(groundMaterial, 0);
    }

    protected final void setGroundMaterial(int groundMaterial, int groundData) {
        this.groundMaterial = groundMaterial;
        this.groundData = groundData;
    }
}

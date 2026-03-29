package worldgeneratorextension.vanillagenerator.ground;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.NukkitRandom;
import worldgeneratorextension.vanillagenerator.biome.BiomeClimate;

public class GroundGenerator implements BlockID {

    protected int topMaterial;
    protected int topData;
    protected int groundMaterial;
    protected int groundData;

    // ID específica para Deepslate en tu versión
    private static final int DEEPSLATE_ID = 57;

    public GroundGenerator() {
        setTopMaterial(GRASS);
        setGroundMaterial(DIRT);
    }

    /**
     * Genera la columna de terreno con capas de Deepslate corregidas.
     */
    public void generateTerrainColumn(ChunkManager world, BaseFullChunk chunkData, NukkitRandom random, int chunkX, int chunkZ, int biome, double surfaceNoise) {
        int seaLevel = 64;

        // Configuración de altura para que la Deepslate sea visible
        int deepslateMax = 30; 
        int deepslateMin = 20;

        int topMat = this.topMaterial;
        int groundMat = this.groundMaterial;

        int x = chunkX & 0xF;
        int z = chunkZ & 0xF;

        int surfaceHeight = Math.max((int) (surfaceNoise / 3.0D + 3.0D + random.nextDouble() * 0.25D), 1);
        int deep = -1;

        for (int y = 255; y >= 0; y--) {
            // Capa de Bedrock aleatoria
            if (y <= random.nextBoundedInt(5)) {
                chunkData.setBlock(x, y, z, BEDROCK);
            } else {
                int mat = chunkData.getBlockId(x, y, z);

                // --- LÓGICA DE DEEPSLATE CORREGIDA ---
                // Si encontramos piedra en las capas bajas, la convertimos
                if (mat == STONE && y < deepslateMax) {
                    // Crea un efecto de mezcla orgánica entre piedra y pizarra
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
                            // Mantenemos el material base (Piedra o Deepslate)
                            groundMat = mat; 
                            chunkData.setBlock(x, y, z, GRAVEL);
                        } else {
                            chunkData.setBlock(x, y, z, groundMat, this.groundData);
                        }
                    } else if (deep > 0) {
                        deep--;
                        chunkData.setBlock(x, y, z, groundMat, this.groundData);

                        if (deep == 0 && groundMat == SAND) {
                            deep = random.nextBoundedInt(4) + Math.max(0, y - seaLevel - 1);
                            groundMat = SANDSTONE;
                        }
                    }
                } else if (mat == Block.STILL_WATER && y == seaLevel - 2 && BiomeClimate.isCold(biome, chunkX, y, chunkZ)) {
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

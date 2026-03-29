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
    
    // ID personalizada para Deepslate en tu versión
    private static final int DEEPSLATE_ID = -378;

    public GroundGenerator() {
        setTopMaterial(GRASS);
        setGroundMaterial(DIRT);
    }

    /**
     * Generates a terrain column with Deepslate layers.
     */
    public void generateTerrainColumn(ChunkManager world, BaseFullChunk chunkData, NukkitRandom random, int chunkX, int chunkZ, int biome, double surfaceNoise) {
        int seaLevel = 64;
        
        // Configuración de la capa profunda (Estilo 1.18)
        int deepslateMax = 8; // Altura máxima donde empieza a aparecer
        int deepslateMin = 0; // Altura donde ya todo es Deepslate

        int topMat = this.topMaterial;
        int groundMat = this.groundMaterial;

        int x = chunkX & 0xF;
        int z = chunkZ & 0xF;

        int surfaceHeight = Math.max((int) (surfaceNoise / 3.0D + 3.0D + random.nextDouble() * 0.25D), 1);
        int deep = -1;

        for (int y = 255; y >= 0; y--) {
            // Capa de Bedrock (Fondo del mundo)
            if (y <= random.nextBoundedInt(5)) {
                chunkData.setBlock(x, y, z, BEDROCK);
            } else {
                int mat = chunkData.getBlockId(x, y, z);

                // --- Lógica de Sustitución por Deepslate ---
                if (mat == STONE) {
                    if (y < deepslateMax) {
                        // Crea un efecto de degradado/mezcla entre piedra y deepslate
                        if (y <= deepslateMin || random.nextBoundedInt(Math.max(1, y - deepslateMin)) == 0) {
                            mat = DEEPSLATE_ID;
                            chunkData.setBlock(x, y, z, mat);
                        }
                    }
                }

                if (mat == AIR) {
                    deep = -1;
                } else if (mat == STONE || mat == DEEPSLATE_ID) {
                    if (deep == -1) {
                        // Definir materiales de superficie según el nivel del mar
                        if (y >= seaLevel - 5 && y <= seaLevel) {
                            topMat = this.topMaterial;
                            groundMat = this.groundMaterial;
                        }

                        deep = surfaceHeight;
                        if (y >= seaLevel - 2) {
                            // Capa de césped o material superior
                            chunkData.setBlock(x, y, z, topMat, this.topData);
                        } else if (y < seaLevel - 8 - surfaceHeight) {
                            // Capas muy profundas bajo el océano o cuevas
                            topMat = AIR;
                            groundMat = mat; // Mantiene el material base (Stone o Deepslate)
                            chunkData.setBlock(x, y, z, GRAVEL);
                        } else {
                            // Capa de tierra o material de subsuelo
                            chunkData.setBlock(x, y, z, groundMat, this.groundData);
                        }
                    } else if (deep > 0) {
                        deep--;
                        chunkData.setBlock(x, y, z, groundMat, this.groundData);

                        // Lógica para desiertos: Convertir arena profunda en arenisca
                        if (deep == 0 && groundMat == SAND) {
                            deep = random.nextBoundedInt(4) + Math.max(0, y - seaLevel - 1);
                            groundMat = SANDSTONE;
                        }
                    }
                } else if (mat == Block.STILL_WATER && y == seaLevel - 2 && BiomeClimate.isCold(biome, chunkX, y, chunkZ)) {
                    // Congelar agua en biomas fríos
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

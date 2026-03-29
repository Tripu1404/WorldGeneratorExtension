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

    private static final int DEEPSLATE_ID = 57;

    public GroundGenerator() {
        setTopMaterial(GRASS);
        setGroundMaterial(DIRT);
    }

    public void generateTerrainColumn(ChunkManager world, BaseFullChunk chunkData, NukkitRandom random, int chunkX, int chunkZ, int biome, double surfaceNoise) {
        int seaLevel = 64;
        int deepslateMax = 30; 
        int deepslateMin = 20;

        int topMat = this.topMaterial;
        int groundMat = this.groundMaterial;

        int x = chunkX & 0xF;
        int z = chunkZ & 0xF;

        int surfaceHeight = Math.max((int) (surfaceNoise / 3.0D + 3.0D + random.nextDouble() * 0.25D), 1);
        int deep = -1;

        for (int y = 255; y >= -64; y--) { // Rango extendido a -64
            // Capa de Bedrock corregida al fondo real
            if (y <= -64 + random.nextBoundedInt(5)) {
                chunkData.setBlock(x, y, z, BEDROCK);
            } else {
                int mat = chunkData.getBlockId(x, y, z);

                // Conversión de Piedra a Deepslate
                if (mat == STONE && y < deepslateMax) {
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
                            groundMat = mat; 
                            chunkData.setBlock(x, y, z, GRAVEL);
                        } else {
                            chunkData.setBlock(x, y, z, groundMat, this.groundData);
                        }
                    } else if (deep > 0) {
                        deep--;
                        chunkData.setBlock(x, y, z, groundMat, this.groundData);
                    }
                }
            }
        }
    }

    protected final void setTopMaterial(int topMaterial, int topData) { this.topMaterial = topMaterial; this.topData = topData; }
    protected final void setTopMaterial(int topMaterial) { this.setTopMaterial(topMaterial, 0); }
    protected final void setGroundMaterial(int groundMaterial, int groundData) { this.groundMaterial = groundMaterial; this.groundData = groundData; }
    protected final void setGroundMaterial(int groundMaterial) { this.setGroundMaterial(groundMaterial, 0); }
}

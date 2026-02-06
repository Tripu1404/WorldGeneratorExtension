package worldgeneratorextension.vanillagenerator;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockStone;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.FullChunk; // CAMBIO: Usamos FullChunk
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.populator.impl.PopulatorSpring;
import cn.nukkit.level.generator.populator.impl.WaterIcePopulator;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import worldgeneratorextension.vanillagenerator.biomegrid.MapLayer;
import worldgeneratorextension.vanillagenerator.ground.*;
import worldgeneratorextension.vanillagenerator.noise.PerlinOctaveGenerator;
import worldgeneratorextension.vanillagenerator.noise.SimplexOctaveGenerator;
import worldgeneratorextension.vanillagenerator.noise.bukkit.OctaveGenerator;
import worldgeneratorextension.vanillagenerator.object.OreType;
import worldgeneratorextension.vanillagenerator.populator.PopulatorOre;
import worldgeneratorextension.vanillagenerator.populator.overworld.PopulatorCaves;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import worldgeneratorextension.vanillagenerator.populator.overworld.PopulatorSnowLayers;

// Imports de nuestras clases corregidas
import worldgeneratorextension.vanillagenerator.populator.PopulatorDeepslate;
import worldgeneratorextension.vanillagenerator.populator.PopulatorBedrockExtended;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class NormalGenerator extends Generator {

    public static final int TYPE_LARGE_BIOMES = 5;
    public static final int TYPE_AMPLIFIED = 6;

    public static int SEA_LEVEL = 64; 

    private MapLayer[] biomeGrid;
    private static final double[][] ELEVATION_WEIGHT = new double[5][5];
    private static final Int2ObjectMap<GroundGenerator> GROUND_MAP = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<BiomeHeight> HEIGHT_MAP = new Int2ObjectOpenHashMap<>();

    private static final double coordinateScale = 684.412d;
    private static final double heightScale = 684.412d;
    private static final double heightNoiseScaleX = 200d; 
    private static final double heightNoiseScaleZ = 200d; 
    private static final double detailNoiseScaleX = 80d;  
    private static final double detailNoiseScaleY = 160d; 
    private static final double detailNoiseScaleZ = 80d;  
    private static final double surfaceScale = 0.0625d;
    private static final double baseSize = 8.5d;
    private static final double stretchY = 12d;
    private static final double biomeHeightOffset = 0d;    
    private static final double biomeHeightWeight = 1d;    
    private static final double biomeScaleOffset = 0d;
    private static final double biomeScaleWeight = 1d;

    static {
        setBiomeSpecificGround(new GroundGeneratorSandy(), EnumBiome.BEACH.id, EnumBiome.COLD_BEACH.id, EnumBiome.DESERT.id, EnumBiome.DESERT_HILLS.id, EnumBiome.DESERT_M.id);
        setBiomeSpecificGround(new GroundGeneratorRocky(),  EnumBiome.STONE_BEACH.id);
        setBiomeSpecificGround(new GroundGeneratorSnowy(),  EnumBiome.ICE_PLAINS_SPIKES.id);
        setBiomeSpecificGround(new GroundGeneratorMycel(),  EnumBiome.MUSHROOM_ISLAND.id,  EnumBiome.MUSHROOM_ISLAND_SHORE.id);
        setBiomeSpecificGround(new GroundGeneratorPatchStone(),  EnumBiome.EXTREME_HILLS.id);
        setBiomeSpecificGround(new GroundGeneratorPatchGravel(),  EnumBiome.EXTREME_HILLS_M.id,  EnumBiome.EXTREME_HILLS_PLUS_M.id);
        setBiomeSpecificGround(new GroundGeneratorPatchDirtAndStone(),  EnumBiome.SAVANNA_M.id,  EnumBiome.SAVANNA_PLATEAU_M.id);
        setBiomeSpecificGround(new GroundGeneratorPatchDirt(),  EnumBiome.MEGA_TAIGA.id,  EnumBiome.MEGA_TAIGA_HILLS.id,  EnumBiome.MEGA_SPRUCE_TAIGA.id,  EnumBiome.MEGA_SPRUCE_TAIGA_HILLS.id);
        setBiomeSpecificGround(new GroundGeneratorPatchPodzol(),  EnumBiome.BAMBOO_JUNGLE.id,  EnumBiome.BAMBOO_JUNGLE_HILLS.id);
        setBiomeSpecificGround(new GroundGeneratorMesa(),  EnumBiome.MESA.id,  EnumBiome.MESA_PLATEAU.id,  EnumBiome.MESA_PLATEAU_F.id);
        setBiomeSpecificGround(new GroundGeneratorMesa(GroundGeneratorMesa.MesaType.BRYCE),  EnumBiome.MESA_BRYCE.id);
        setBiomeSpecificGround(new GroundGeneratorMesa(GroundGeneratorMesa.MesaType.FOREST),  EnumBiome.MESA_PLATEAU_F.id,  EnumBiome.MESA_PLATEAU_F_M.id);
        setBiomeSpecificGround(new GroundGeneratorSandOcean(), EnumBiome.WARM_OCEAN.id, EnumBiome.LUKEWARM_OCEAN.id, EnumBiome.DEEP_WARM_OCEAN.id, EnumBiome.DEEP_LUKEWARM_OCEAN.id);

        setBiomeHeight(BiomeHeight.OCEAN, EnumBiome.OCEAN.id, EnumBiome.FROZEN_OCEAN.id, EnumBiome.WARM_OCEAN.id, EnumBiome.LUKEWARM_OCEAN.id);
        setBiomeHeight(BiomeHeight.DEEP_OCEAN, EnumBiome.DEEP_OCEAN.id, EnumBiome.DEEP_FROZEN_OCEAN.id, EnumBiome.DEEP_WARM_OCEAN.id, EnumBiome.DEEP_LUKEWARM_OCEAN.id);
        setBiomeHeight(BiomeHeight.RIVER, EnumBiome.RIVER.id, EnumBiome.FROZEN_RIVER.id);
        setBiomeHeight(BiomeHeight.FLAT_SHORE, EnumBiome.BEACH.id, EnumBiome.COLD_BEACH.id, EnumBiome.MUSHROOM_ISLAND_SHORE.id);
        setBiomeHeight(BiomeHeight.ROCKY_SHORE, EnumBiome.STONE_BEACH.id);
        setBiomeHeight(BiomeHeight.FLATLANDS, EnumBiome.DESERT.id, EnumBiome.ICE_PLAINS.id, EnumBiome.SAVANNA.id);
        setBiomeHeight(BiomeHeight.EXTREME_HILLS, EnumBiome.EXTREME_HILLS.id, EnumBiome.EXTREME_HILLS_PLUS.id, EnumBiome.EXTREME_HILLS_M.id, EnumBiome.EXTREME_HILLS_PLUS_M.id);
        setBiomeHeight(BiomeHeight.MID_PLAINS, EnumBiome.TAIGA.id, EnumBiome.COLD_TAIGA.id, EnumBiome.MEGA_TAIGA.id);
        setBiomeHeight(BiomeHeight.SWAMPLAND, EnumBiome.SWAMP.id);
        setBiomeHeight(BiomeHeight.LOW_HILLS, EnumBiome.MUSHROOM_ISLAND.id);
        setBiomeHeight(BiomeHeight.HILLS, EnumBiome.DESERT_HILLS.id, EnumBiome.FOREST_HILLS.id, EnumBiome.TAIGA_HILLS.id, EnumBiome.EXTREME_HILLS_EDGE.id, EnumBiome.JUNGLE_HILLS.id, EnumBiome.BIRCH_FOREST_HILLS.id, EnumBiome.COLD_TAIGA_HILLS.id, EnumBiome.MEGA_TAIGA_HILLS.id, EnumBiome.MESA_PLATEAU_F_M.id, EnumBiome.MESA_PLATEAU_M.id, EnumBiome.ICE_MOUNTAINS.id);
        setBiomeHeight(BiomeHeight.HIGH_PLATEAU, EnumBiome.SAVANNA_PLATEAU.id, EnumBiome.MESA_PLATEAU_F.id, EnumBiome.MESA_PLATEAU.id);
        setBiomeHeight(BiomeHeight.FLATLANDS_HILLS, EnumBiome.DESERT_M.id);
        setBiomeHeight(BiomeHeight.BIG_HILLS, EnumBiome.ICE_PLAINS_SPIKES.id);
        setBiomeHeight(BiomeHeight.BIG_HILLS2, EnumBiome.BIRCH_FOREST_HILLS_M.id);
        setBiomeHeight(BiomeHeight.SWAMPLAND_HILLS, EnumBiome.SWAMPLAND_M.id);
        setBiomeHeight(BiomeHeight.DEFAULT_HILLS, EnumBiome.JUNGLE_M.id, EnumBiome.JUNGLE_EDGE_M.id, EnumBiome.BIRCH_FOREST_M.id, EnumBiome.ROOFED_FOREST_M.id);
        setBiomeHeight(BiomeHeight.MID_HILLS, EnumBiome.TAIGA_M.id, EnumBiome.COLD_TAIGA_M.id, EnumBiome.MEGA_SPRUCE_TAIGA.id, EnumBiome.MEGA_SPRUCE_TAIGA_HILLS.id);
        setBiomeHeight(BiomeHeight.MID_HILLS2, EnumBiome.FLOWER_FOREST.id);
        setBiomeHeight(BiomeHeight.LOW_SPIKES, EnumBiome.SAVANNA_M.id);
        setBiomeHeight(BiomeHeight.HIGH_SPIKES, EnumBiome.SAVANNA_PLATEAU_M.id);

        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                int sqX = x - 2;
                sqX *= sqX;
                int sqZ = z - 2;
                sqZ *= sqZ;
                ELEVATION_WEIGHT[x][z] = 10d / Math.sqrt(sqX + sqZ + 0.2d);
            }
        }
    }

    private final Map<String, Map<String, OctaveGenerator>> octaveCache = Maps.newHashMap();
    private final double[][][] density = new double[5][5][33];
    private final GroundGenerator groundGen = new GroundGenerator();
    private final BiomeHeight defaultHeight = BiomeHeight.DEFAULT;

    private static void setBiomeSpecificGround(GroundGenerator gen, int... biomes) {
        for (int biome : biomes) {
            GROUND_MAP.put(biome, gen);
        }
    }

    private static void setBiomeHeight(BiomeHeight height, int... biomes) {
        for (int biome : biomes) {
            HEIGHT_MAP.put(biome, height);
        }
    }

    private List<Populator> generationPopulators = Lists.newArrayList();
    private List<Populator> populators = Lists.newArrayList();
    private ChunkManager level;
    private NukkitRandom nukkitRandom;
    private long localSeed1;
    private long localSeed2;

    public NormalGenerator() {
    }

    public NormalGenerator(Map<String, Object> options) {
    }

    @Override
    public int getId() {
        return TYPE_INFINITE;
    }

    @Override
    public ChunkManager getChunkManager() {
        return level;
    }

    @Override
    public String getName() {
        return "normal";
    }

    @Override
    public Map<String, Object> getSettings() {
        return Collections.emptyMap();
    }

    @Override
    public void init(ChunkManager level, NukkitRandom random) {
        this.level = level;
        this.nukkitRandom = random;
        this.nukkitRandom.setSeed(this.level.getSeed());
        this.localSeed1 = ThreadLocalRandom.current().nextLong();
        this.localSeed2 = ThreadLocalRandom.current().nextLong();
        this.nukkitRandom.setSeed(this.level.getSeed());

        // Generation Populators
        this.generationPopulators = ImmutableList.of(
                new PopulatorDeepslate(-64),
                new PopulatorCaves()
        );

        // Ore & Decoration Populators
        this.populators = ImmutableList.of(
                new PopulatorOre(STONE, new OreType[]{
                        new OreType(Block.get(BlockID.COAL_ORE), 20, 17, 0, 131),
                        new OreType(Block.get(BlockID.COPPER_ORE), 20, 9, 0, 192),
                        new OreType(Block.get(BlockID.IRON_ORE), 20, 9, 0, 63),
                        new OreType(Block.get(BlockID.REDSTONE_ORE), 8, 8, 0, 15),
                        new OreType(Block.get(BlockID.LAPIS_ORE), 1, 7, 0, 33),
                        new OreType(Block.get(BlockID.GOLD_ORE), 2, 9, 0, 33),
                        new OreType(Block.get(BlockID.DIAMOND_ORE), 1, 8, 0, 15),
                        new OreType(Block.get(DIRT), 10, 33, 0, 128),
                        new OreType(Block.get(GRAVEL), 8, 33, 0, 128),
                        new OreType(Block.get(STONE, BlockStone.GRANITE), 10, 33, 0, 80),
                        new OreType(Block.get(STONE, BlockStone.DIORITE), 10, 33, 0, 80),
                        new OreType(Block.get(STONE, BlockStone.ANDESITE), 10, 33, 0, 80)
                }),
                
                new cn.nukkit.level.generator.populator.impl.PopulatorOre(BlockID.DEEPSLATE, new cn.nukkit.level.generator.object.ore.OreType[]{
                        new cn.nukkit.level.generator.object.ore.OreType(Block.get(BlockID.DEEPSLATE_COAL_ORE), 1, 13, -4, 8, BlockID.DEEPSLATE),
                        new cn.nukkit.level.generator.object.ore.OreType(Block.get(BlockID.DEEPSLATE_COPPER_ORE), 5, 9, -64, 8, BlockID.DEEPSLATE),
                        new cn.nukkit.level.generator.object.ore.OreType(Block.get(BlockID.DEEPSLATE_IRON_ORE), 5, 9, -64, 8, BlockID.DEEPSLATE),
                        new cn.nukkit.level.generator.object.ore.OreType(Block.get(BlockID.DEEPSLATE_REDSTONE_ORE), 8, 8, -64, 8, BlockID.DEEPSLATE),
                        new cn.nukkit.level.generator.object.ore.OreType(Block.get(BlockID.DEEPSLATE_LAPIS_ORE), 6, 6, -64, 8, BlockID.DEEPSLATE),
                        new cn.nukkit.level.generator.object.ore.OreType(Block.get(BlockID.DEEPSLATE_GOLD_ORE), 2, 9, -64, 8, BlockID.DEEPSLATE),
                        new cn.nukkit.level.generator.object.ore.OreType(Block.get(BlockID.DEEPSLATE_DIAMOND_ORE), 4, 5, -64, 8, BlockID.DEEPSLATE)
                }),
                
                new PopulatorSnowLayers(),
                new WaterIcePopulator(),
                new PopulatorSpring(BlockID.WATER, BlockID.STONE, 15, 8, 255),
                new PopulatorSpring(BlockID.LAVA, BlockID.STONE, 10, 16, 255),
                
                // Bedrock
                new PopulatorBedrockExtended(-64)
        );
        this.biomeGrid = MapLayer.initialize(level.getSeed(), this.getDimension(), this.getId());
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        this.nukkitRandom.setSeed(chunkX * localSeed1 ^ chunkZ * localSeed2 ^ this.level.getSeed());

        // CAMBIO: Especificamos FullChunk aquí para evitar problemas con los populadores
        FullChunk chunkData = level.getChunk(chunkX, chunkZ);

        int x = chunkX << 2;
        int z = chunkZ << 2;

        int[] biomeGrid = this.biomeGrid[1].generateValues(x - 2, z - 2, 10, 10);

        Map<String, OctaveGenerator> octaves = getWorldOctaves();
        double[] heightNoise = ((PerlinOctaveGenerator) octaves.get("height")).getFractalBrownianMotion(x, z, 0.5d, 2d);
        double[] roughnessNoise = ((PerlinOctaveGenerator) octaves.get("roughness")).getFractalBrownianMotion(x, 0, z, 0.5d, 2d);
        double[] roughnessNoise2 = ((PerlinOctaveGenerator) octaves.get("roughness2")).getFractalBrownianMotion(x, 0, z, 0.5d, 2d);
        double[] detailNoise = ((PerlinOctaveGenerator) octaves.get("detail")).getFractalBrownianMotion(x, 0, z, 0.5d, 2d);

        int index = 0;
        int indexHeight = 0;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                double avgHeightScale = 0;
                double avgHeightBase = 0;
                double totalWeight = 0;
                int biome = Biome.getBiome(biomeGrid[i + 2 + (j + 2) * 10]).getId();
                BiomeHeight biomeHeight = HEIGHT_MAP.getOrDefault(biome, defaultHeight);
                for (int m = 0; m < 5; m++) {
                    for (int n = 0; n < 5; n++) {
                        int nearBiome = Biome.getBiome(biomeGrid[i + m + (j + n) * 10]).getId();
                        BiomeHeight nearBiomeHeight = HEIGHT_MAP.getOrDefault(nearBiome, defaultHeight);
                        double heightBase = biomeHeightOffset + nearBiomeHeight.getHeight() * biomeHeightWeight;
                        double heightScale = biomeScaleOffset + nearBiomeHeight.getScale() * biomeScaleWeight;
                        if (this.getId() == TYPE_AMPLIFIED && heightBase > 0) {
                            heightBase = 1d + heightBase * 2d;
                            heightScale = 1d + heightScale * 4d;
                        }
                        double weight = ELEVATION_WEIGHT[m][n] / (heightBase + 2d);
                        if (nearBiomeHeight.getHeight() > biomeHeight.getHeight()) {
                            weight *= 0.5d;
                        }
                        avgHeightScale += heightScale * weight;
                        avgHeightBase += heightBase * weight;
                        totalWeight += weight;
                    }
                }
                avgHeightScale /= totalWeight;
                avgHeightBase /= totalWeight;
                avgHeightScale = avgHeightScale * 0.9d + 0.1d;
                avgHeightBase = (avgHeightBase * 4d - 1d) / 8d;

                double noiseH = heightNoise[indexHeight++] / 8000d;
                if (noiseH < 0) {
                    noiseH = Math.abs(noiseH) * 0.3d;
                }
                noiseH = noiseH * 3d - 2d;
                if (noiseH < 0) {
                    noiseH = Math.max(noiseH * 0.5d, -1) / 1.4d * 0.5d;
                } else {
                    noiseH = Math.min(noiseH, 1) / 8d;
                }

                noiseH = (noiseH * 0.2d + avgHeightBase) * baseSize / 8d * 4d + baseSize;
                for (int k = 0; k < 33; k++) {
                    double nh = (k - noiseH) * stretchY * 128d / 256d / avgHeightScale;
                    if (nh < 0) {
                        nh *= 4d;
                    }
                    double noiseR = roughnessNoise[index] / 512d;
                    double noiseR2 = roughnessNoise2[index] / 512d;
                    double noiseD = (detailNoise[index] / 10d + 1d) / 2d;
                    double dens = noiseD < 0 ? noiseR
                            : noiseD > 1 ? noiseR2 : noiseR + (noiseR2 - noiseR) * noiseD;
                    dens -= nh;
                    index++;
                    if (k > 29) {
                        double lowering = (k - 29) / 3d;
                        dens = dens * (1d - lowering) + -10d * lowering;
                    }
                    this.density[i][j][k] = dens;
                }
            }
        }

        int fill = 0;
        int afill = 0;
        int seaFill = 0;
        double densityOffset = 0.0;

        for (int i = 0; i < 5 - 1; i++) {
            for (int j = 0; j < 5 - 1; j++) {
                for (int k = 0; k < 33 - 1; k++) {
                    double d1 = this.density[i][j][k];
                    double d2 = this.density[i + 1][j][k];
                    double d3 = this.density[i][j + 1][k];
                    double d4 = this.density[i + 1][j + 1][k];
                    double d5 = (this.density[i][j][k + 1] - d1) / 8;
                    double d6 = (this.density[i + 1][j][k + 1] - d2) / 8;
                    double d7 = (this.density[i][j + 1][k + 1] - d3) / 8;
                    double d8 = (this.density[i + 1][j + 1][k + 1] - d4) / 8;

                    for (int l = 0; l < 8; l++) {
                        double d9 = d1;
                        double d10 = d3;
                        for (int m = 0; m < 4; m++) {
                            double dens = d9;
                            for (int n = 0; n < 4; n++) {
                                if (afill == 1 || afill == 10 || afill == 13 || afill == 16) {
                                    chunkData.setBlock(m + (i << 2), l + (k << 3), n + (j << 2), STILL_WATER);
                                } else if (afill == 2 || afill == 9 || afill == 12 || afill == 15) {
                                    chunkData.setBlock(m + (i << 2), l + (k << 3), n + (j << 2), STONE);
                                }
                                if (dens > densityOffset && fill > -1 || dens <= densityOffset && fill < 0) {
                                    if (afill == 0 || afill == 3 || afill == 6 || afill == 9 || afill == 12) {
                                        chunkData.setBlock(m + (i << 2), l + (k << 3), n + (j << 2), STONE);
                                    } else if (afill == 2 || afill == 7 || afill == 10 || afill == 16) {
                                        chunkData.setBlock(m + (i << 2), l + (k << 3), n + (j << 2), STILL_WATER);
                                    }
                                } else if (l + (k << 3) < SEA_LEVEL - 1 && seaFill == 0 || l + (k << 3) >= SEA_LEVEL - 1 && seaFill == 1) {
                                    if (afill == 0 || afill == 3 || afill == 7 || afill == 10 || afill == 13) {
                                        chunkData.setBlock(m + (i << 2), l + (k << 3), n + (j << 2), STILL_WATER);
                                    } else if (afill == 1 || afill == 6 || afill == 9 || afill == 15) {
                                        chunkData.setBlock(m + (i << 2), l + (k << 3), n + (j << 2), STONE);
                                    }
                                }
                                dens += (d10 - d9) / 4;
                            }
                            d9 += (d2 - d1) / 4;
                            d10 += (d4 - d3) / 4;
                        }
                        d1 += d5;
                        d3 += d7;
                        d2 += d6;
                        d4 += d8;
                    }
                }
            }
        }

        int cx = chunkX << 4;
        int cz = chunkZ << 4;

        BiomeGrid biomes = new BiomeGrid();
        int[] biomeValues = this.biomeGrid[0].generateValues(cx, cz, 16, 16);
        for (int i = 0; i < biomeValues.length; i++) {
            biomes.biomes[i] = (byte) biomeValues[i];
        }

        SimplexOctaveGenerator octaveGenerator = ((SimplexOctaveGenerator) getWorldOctaves().get("surface"));
        int sizeX = octaveGenerator.getSizeX();
        int sizeZ = octaveGenerator.getSizeZ();

        double[] surfaceNoise = octaveGenerator.getFractalBrownianMotion(cx, cz, 0.5d, 0.5d);
        for (int sx = 0; sx < sizeX; sx++) {
            for (int sz = 0; sz < sizeZ; sz++) {
                GROUND_MAP.getOrDefault(biomes.getBiome(sx, sz), groundGen).generateTerrainColumn(level, chunkData, this.nukkitRandom, cx + sx, cz + sz, biomes.getBiome(sx, sz), surfaceNoise[sx | sz << 4]);
                chunkData.setBiomeId(sx, sz, biomes.getBiome(sx, sz));
            }
        }

        this.generationPopulators.forEach(populator -> populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, chunkData));
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        BaseFullChunk chunk = this.level.getChunk(chunkX, chunkZ);
        this.nukkitRandom.setSeed(0xdeadbeef ^ (chunkX << 8) ^ chunkZ ^ this.level.getSeed());

        Biome.getBiome(chunk.getBiomeId(7, 7)).populateChunk(this.level, chunkX, chunkZ, this.nukkitRandom);
        this.populators.forEach(populator -> populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, chunk));
    }

    @Override
    public Vector3 getSpawn() {
        return new Vector3(0.5, 256, 0.5);
    }

    private Map<String, OctaveGenerator> getWorldOctaves() {
        Map<String, OctaveGenerator> octaves = this.octaveCache.get(this.getName());
        if (octaves == null) {
            octaves = Maps.newHashMap();
            NukkitRandom seed = new NukkitRandom(this.level.getSeed());

            OctaveGenerator gen = new PerlinOctaveGenerator(seed, 16, 5, 5);
            gen.setXScale(heightNoiseScaleX);
            gen.setZScale(heightNoiseScaleZ);
            octaves.put("height", gen);

            gen = new PerlinOctaveGenerator(seed, 16, 5, 33, 5);
            gen.setXScale(coordinateScale);
            gen.setYScale(heightScale);
            gen.setZScale(coordinateScale);
            octaves.put("roughness", gen);

            gen = new PerlinOctaveGenerator(seed, 16, 5, 33, 5);
            gen.setXScale(coordinateScale);
            gen.setYScale(heightScale);
            gen.setZScale(coordinateScale);
            octaves.put("roughness2", gen);

            gen = new PerlinOctaveGenerator(seed, 8, 5, 33, 5);
            gen.setXScale(coordinateScale / detailNoiseScaleX);
            gen.setYScale(heightScale / detailNoiseScaleY);
            gen.setZScale(coordinateScale / detailNoiseScaleZ);
            octaves.put("detail", gen);

            gen = new SimplexOctaveGenerator(seed, 4, 16, 16);
            gen.setScale(surfaceScale);
            octaves.put("surface", gen);

            this.octaveCache.put(this.getName(), octaves);
        }
        return octaves;
    }

    private static class BiomeGrid {

        public final byte[] biomes = new byte[256];

        public int getBiome(int x, int z) {
            return Biome.biomes[biomes[x | z << 4] & 0xff].getId();
        }

        public void setBiome(int x, int z, int bio) {
            biomes[x | z << 4] = (byte) Biome.biomes[bio].getId();
        }
    }

    private static class BiomeHeight {

        public static final BiomeHeight DEFAULT = new BiomeHeight(0.1d,0.2d);
        public static final BiomeHeight FLAT_SHORE = new BiomeHeight(0d,0.025d);
        public static final BiomeHeight HIGH_PLATEAU = new BiomeHeight(1.5d,0.025d);
        public static final BiomeHeight FLATLANDS = new BiomeHeight(0.125d,0.05d);
        public static final BiomeHeight SWAMPLAND = new BiomeHeight(-0.2d,0.1d);
        public static final BiomeHeight MID_PLAINS = new BiomeHeight(0.2d,0.2d);
        public static final BiomeHeight FLATLANDS_HILLS = new BiomeHeight(0.275d,0.25d);
        public static final BiomeHeight SWAMPLAND_HILLS = new BiomeHeight(-0.1d,0.3d);
        public static final BiomeHeight LOW_HILLS = new BiomeHeight(0.2d,0.3d);
        public static final BiomeHeight HILLS = new BiomeHeight(0.45d,0.3d);
        public static final BiomeHeight MID_HILLS2 = new BiomeHeight(0.1d,0.4d);
        public static final BiomeHeight DEFAULT_HILLS = new BiomeHeight(0.2d,0.4d);
        public static final BiomeHeight MID_HILLS = new BiomeHeight(0.3d,0.4d);
        public static final BiomeHeight BIG_HILLS = new BiomeHeight(0.525d,0.55d);
        public static final BiomeHeight BIG_HILLS2 = new BiomeHeight(0.55d,0.5d);
        public static final BiomeHeight EXTREME_HILLS = new BiomeHeight(1d,0.5d);
        public static final BiomeHeight ROCKY_SHORE = new BiomeHeight(0.1d,0.8d);
        public static final BiomeHeight LOW_SPIKES = new BiomeHeight(0.4125d,1.325d);
        public static final BiomeHeight HIGH_SPIKES = new BiomeHeight(1.1d,1.3125d);
        public static final BiomeHeight RIVER = new BiomeHeight(-0.5d,0d);
        public static final BiomeHeight OCEAN = new BiomeHeight(-1d,0.1d);
        public static final BiomeHeight DEEP_OCEAN = new BiomeHeight(-1.8d,0.1d);

        private final double height;
        private final double scale;

        BiomeHeight(double height, double scale){
            this.height = height;
            this.scale = scale;
        }

        public double getHeight(){
            return this.height;
        }

        public double getScale(){
            return this.scale;
        }
    }
}

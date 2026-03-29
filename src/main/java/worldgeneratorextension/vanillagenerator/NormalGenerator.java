package worldgeneratorextension.vanillagenerator;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import worldgeneratorextension.vanillagenerator.biomegrid.MapLayer;
import worldgeneratorextension.vanillagenerator.ground.*;
import worldgeneratorextension.vanillagenerator.noise.PerlinOctaveGenerator;
import worldgeneratorextension.vanillagenerator.noise.SimplexOctaveGenerator;
import worldgeneratorextension.vanillagenerator.noise.bukkit.OctaveGenerator;
import worldgeneratorextension.vanillagenerator.populator.overworld.PopulatorCaves;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class NormalGenerator extends Generator {

    // --- CONSTANTES DE COMPATIBILIDAD ---
    public static final int TYPE_CHUNKY = 0;
    public static final int TYPE_LARGE_BIOMES = 5;
    public static final int TYPE_AMPLIFIED = 6;
    public static int SEA_LEVEL = 64;

    private static final double coordinateScale = 684.412d;
    private static final double heightScale = 684.412d;
    private static final double baseSize = 8.5d;
    private static final double stretchY = 12d;

    private MapLayer[] biomeGrid;
    private static final double[][] ELEVATION_WEIGHT = new double[5][5];
    private static final Int2ObjectMap<GroundGenerator> GROUND_MAP = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<BiomeHeight> HEIGHT_MAP = new Int2ObjectOpenHashMap<>();

    static {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                ELEVATION_WEIGHT[x][z] = 10d / Math.sqrt((x - 2) * (x - 2) + (z - 2) * (z - 2) + 0.2d);
            }
        }
        initializeBiomeHeights();
    }

    private final Map<String, Map<String, OctaveGenerator>> octaveCache = Maps.newHashMap();
    private final double[][][] density = new double[5][5][41]; 
    private final GroundGenerator groundGen = new GroundGenerator();
    private final Map<String, Object> options;

    // --- VARIABLES DE ESTADO DEL NIVEL ---
    private ChunkManager level;
    private NukkitRandom nukkitRandom;
    private long localSeed1;
    private long localSeed2;
    private List<Populator> generationPopulators = Lists.newArrayList();

    public NormalGenerator() { this(Collections.emptyMap()); }
    public NormalGenerator(Map<String, Object> options) { this.options = options; }

    @Override public String getName() { return "normal"; }
    @Override public int getId() { return TYPE_INFINITE; }

    // --- MÉTODOS REQUERIDOS POR LA CLASE BASE (RESTAURADOS) ---
    @Override
    public ChunkManager getChunkManager() {
        return this.level;
    }

    @Override
    public Map<String, Object> getSettings() {
        return options != null ? options : Collections.emptyMap();
    }

    @Override
    public void init(ChunkManager level, NukkitRandom random) {
        this.level = level;
        this.nukkitRandom = random;
        this.nukkitRandom.setSeed(this.level.getSeed());
        this.localSeed1 = ThreadLocalRandom.current().nextLong();
        this.localSeed2 = ThreadLocalRandom.current().nextLong();
        this.generationPopulators = ImmutableList.of(new PopulatorCaves());
        this.biomeGrid = MapLayer.initialize(level.getSeed(), this.getDimension(), this.getId());
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        this.nukkitRandom.setSeed(chunkX * localSeed1 ^ chunkZ * localSeed2 ^ this.level.getSeed());
        BaseFullChunk chunkData = level.getChunk(chunkX, chunkZ);

        int x = chunkX << 2;
        int z = chunkZ << 2;

        int[] biomesMap = this.biomeGrid[1].generateValues(x - 2, z - 2, 10, 10);
        Map<String, OctaveGenerator> octaves = getWorldOctaves();
        
        double[] heightNoise = ((PerlinOctaveGenerator) octaves.get("height")).getFractalBrownianMotion(x, z, 0.5d, 2d);
        double[] roughnessNoise = ((PerlinOctaveGenerator) octaves.get("roughness")).getFractalBrownianMotion(x, 0, z, 0.5d, 2d);
        double[] roughnessNoise2 = ((PerlinOctaveGenerator) octaves.get("roughness2")).getFractalBrownianMotion(x, 0, z, 0.5d, 2d);
        double[] detailNoise = ((PerlinOctaveGenerator) octaves.get("detail")).getFractalBrownianMotion(x, 0, z, 0.5d, 2d);

        int index = 0;
        int indexHeight = 0;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                double avgScale = 0, avgHeight = 0, weightSum = 0;
                BiomeHeight center = HEIGHT_MAP.getOrDefault(biomesMap[i + 2 + (j + 2) * 10], BiomeHeight.DEFAULT);

                for (int m = 0; m < 5; m++) {
                    for (int n = 0; n < 5; n++) {
                        BiomeHeight bh = HEIGHT_MAP.getOrDefault(biomesMap[i + m + (j + n) * 10], BiomeHeight.DEFAULT);
                        double weight = ELEVATION_WEIGHT[m][n] / (bh.height + 2d);
                        if (bh.height > center.height) weight *= 0.5d;
                        avgScale += bh.scale * weight;
                        avgHeight += bh.height * weight;
                        weightSum += weight;
                    }
                }
                
                avgScale /= weightSum; avgHeight /= weightSum;
                avgScale = avgScale * 0.9d + 0.1d;
                avgHeight = (avgHeight * 4d - 1d) / 8d;

                double noiseH = heightNoise[indexHeight++] / 8000d;
                if (noiseH < 0) noiseH = Math.abs(noiseH) * 0.3d;
                noiseH = noiseH * 3d - 2d;
                noiseH = noiseH < 0 ? Math.max(noiseH * 0.5d, -1) / 1.4d * 0.5d : Math.min(noiseH, 1) / 8d;
                noiseH = (noiseH * 0.2d + avgHeight) * baseSize / 8d * 4d + baseSize;
                
                for (int k = 0; k < 41; k++) {
                    double nh = (k - noiseH) * stretchY * 128.0d / 256.0d / avgScale;
                    if (nh < 0) nh *= 4d;

                    double dR = roughnessNoise[index] / 512d;
                    double dR2 = roughnessNoise2[index] / 512d;
                    double dD = (detailNoise[index] / 10d + 1d) / 2d;
                    double dens = dD < 0 ? dR : dD > 1 ? dR2 : dR + (dR2 - dR) * dD;
                    
                    dens -= nh;
                    if (k > 37) { 
                        double lowering = (k - 37) / 3d;
                        dens = dens * (1d - lowering) + -10d * lowering;
                    }
                    this.density[i][j][k] = dens;
                    index++;
                }
            }
        }

        // Generación de bloques de Piedra
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 40; k++) {
                    double d1 = density[i][j][k], d2 = density[i+1][j][k], d3 = density[i][j+1][k], d4 = density[i+1][j+1][k];
                    double d5 = (density[i][j][k+1] - d1) / 8, d6 = (density[i+1][j][k+1] - d2) / 8;
                    double d7 = (density[i][j+1][k+1] - d3) / 8, d8 = (density[i+1][j+1][k+1] - d4) / 8;

                    double dy = d1, dy2 = d3;
                    for (int l = 0; l < 8; l++) {
                        int blockY = (l + (k << 3)) - 64; 
                        double dx = dy, dx2 = dy2;
                        for (int m = 0; m < 4; m++) {
                            double dz = dx;
                            for (int n = 0; n < 4; n++) {
                                if (dz > 0.0) {
                                    chunkData.setBlock(m + (i << 2), blockY, n + (j << 2), STONE);
                                } else if (blockY < SEA_LEVEL) {
                                    chunkData.setBlock(m + (i << 2), blockY, n + (j << 2), STILL_WATER);
                                }
                                dz += (dx2 - dx) / 4;
                            }
                            dx += (d2 - d1) / 4; dx2 += (d4 - d3) / 4;
                        }
                        dy += d5; dy2 += d7;
                    }
                }
            }
        }

        // Aplicar Biomas y Capa de Tierra
        int cx = chunkX << 4, cz = chunkZ << 4;
        int[] finalBiomes = this.biomeGrid[0].generateValues(cx, cz, 16, 16);
        double[] sNoise = ((SimplexOctaveGenerator) getWorldOctaves().get("surface")).getFractalBrownianMotion(cx, cz, 0.5d, 0.5d);

        for (int sx = 0; sx < 16; sx++) {
            for (int sz = 0; sz < 16; sz++) {
                int bId = finalBiomes[sx | sz << 4] & 0xff;
                chunkData.setBiomeId(sx, sz, bId);
                GROUND_MAP.getOrDefault(bId, groundGen).generateTerrainColumn(level, chunkData, nukkitRandom, cx + sx, cz + sz, bId, sNoise[sx | sz << 4]);
            }
        }

        generationPopulators.forEach(p -> p.populate(level, chunkX, chunkZ, nukkitRandom, chunkData));
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        BaseFullChunk chunk = this.level.getChunk(chunkX, chunkZ);
        this.nukkitRandom.setSeed(0xdeadbeef ^ (chunkX << 8) ^ chunkZ ^ this.level.getSeed());
        Biome.getBiome(chunk.getBiomeId(7, 7)).populateChunk(this.level, chunkX, chunkZ, this.nukkitRandom);
    }

    private static void initializeBiomeHeights() {
        setBH(BiomeHeight.OCEAN, 0, 10, 24, 46); 
        setBH(BiomeHeight.FLATLANDS, 1, 2, 4, 5, 35);
        setBH(BiomeHeight.EXTREME_HILLS, 3, 13, 17, 18, 19, 34);
    }

    private static void setBH(BiomeHeight h, int... ids) { for(int id : ids) HEIGHT_MAP.put(id, h); }

    private Map<String, OctaveGenerator> getWorldOctaves() {
        Map<String, OctaveGenerator> octs = octaveCache.get(getName());
        if (octs == null) {
            octs = Maps.newHashMap();
            NukkitRandom seed = new NukkitRandom(level.getSeed());
            octs.put("height", new PerlinOctaveGenerator(seed, 16, 5, 5));
            octs.put("roughness", new PerlinOctaveGenerator(seed, 16, 5, 41, 5));
            octs.put("roughness2", new PerlinOctaveGenerator(seed, 16, 5, 41, 5));
            octs.put("detail", new PerlinOctaveGenerator(seed, 8, 5, 41, 5));
            octs.put("surface", new SimplexOctaveGenerator(seed, 4, 16, 16));
            octs.get("roughness").setXScale(coordinateScale); octs.get("roughness").setYScale(heightScale); octs.get("roughness").setZScale(coordinateScale);
            octs.get("roughness2").setXScale(coordinateScale); octs.get("roughness2").setYScale(heightScale); octs.get("roughness2").setZScale(coordinateScale);
            octaveCache.put(getName(), octs);
        }
        return octs;
    }

    private static class BiomeHeight {
        static final BiomeHeight DEFAULT = new BiomeHeight(0.1d, 0.2d);
        static final BiomeHeight OCEAN = new BiomeHeight(-1.0d, 0.1d);
        static final BiomeHeight FLATLANDS = new BiomeHeight(0.125d, 0.05d);
        static final BiomeHeight EXTREME_HILLS = new BiomeHeight(1.0d, 0.5d);
        double height, scale;
        BiomeHeight(double h, double s) { height = h; scale = s; }
    }

    @Override public Vector3 getSpawn() { return new Vector3(0.5, 90, 0.5); }
}

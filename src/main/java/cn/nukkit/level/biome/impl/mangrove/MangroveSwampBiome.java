package cn.nukkit.level.biome.impl.mangrove;

import cn.nukkit.level.biome.type.GrassyBiome;
import cn.nukkit.level.generator.populator.impl.PopulatorSugarcane;
import worldgeneratorextension.vanillagenerator.populator.MangroveTreePopulator;

public class MangroveSwampBiome extends GrassyBiome {

    public MangroveSwampBiome() {
        super();
        
        // Configuramos los populadores
        MangroveTreePopulator mangroveTrees = new MangroveTreePopulator();
        mangroveTrees.setBaseAmount(3); 
        this.addPopulator(mangroveTrees);

        PopulatorSugarcane sugarcane = new PopulatorSugarcane();
        sugarcane.setBaseAmount(8);
        this.addPopulator(sugarcane);

        this.setBaseHeight(-0.2f);
        this.setHeightVariation(0.1f);
    }

    @Override
    public String getName() {
        return "Mangrove Swamp";
    }

    // --- CORRECCIÓN: Quitamos @Override y añadimos ambas variantes ---

    // Opción A: Firma estándar (x, z)
    public int getCoverId(int x, int z) {
        return 1610; // Mud
    }

    public int getGroundId(int x, int z) {
        return 1610; // Mud
    }

    // Opción B: Firma alternativa (x, y, z) que usan algunas versiones
    public int getCoverId(int x, int y, int z) {
        return 1610;
    }

    public int getGroundId(int x, int y, int z) {
        return 1610;
    }
}

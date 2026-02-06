package cn.nukkit.level.biome.impl.mangrove;

import cn.nukkit.level.biome.type.GrassyBiome;
import cn.nukkit.level.generator.populator.impl.PopulatorSugarcane;
import worldgeneratorextension.vanillagenerator.populator.MangroveTreePopulator;

public class MangroveSwampBiome extends GrassyBiome {

    public MangroveSwampBiome() {
        super();
        
        // ELIMINADO: No usamos setters (setCoverId, setSurfaceBlock) porque dan error.
        // La configuración del suelo la hacemos abajo con los @Override.

        // Populador de árboles (asegúrate de que MangroveTreePopulator esté corregido también)
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

    // SOBRESCRIBIMOS: Esto define el bloque de superficie (lo que pisas)
    // 1610 es el ID de Mud (Lodo) en Bedrock.
    @Override
    public int getCoverId(int x, int z) {
        return 1610; 
    }

    // SOBRESCRIBIMOS: Esto define el bloque justo debajo de la superficie (tierra)
    @Override
    public int getGroundId(int x, int z) {
        return 1610;
    }
}

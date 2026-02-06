package cn.nukkit.level.biome.impl.mangrove;

import cn.nukkit.block.Block;
import cn.nukkit.level.biome.type.GrassyBiome; // Puedes mantenerlo o cambiar a Biome
import cn.nukkit.level.generator.populator.impl.PopulatorSugarcane;
import worldgeneratorextension.vanillagenerator.populator.MangroveTreePopulator;

public class MangroveSwampBiome extends GrassyBiome {

    public MangroveSwampBiome() {
        super();
        
        // CORRECCIÓN: En lugar de setSurfaceBlock, usamos setCoverId
        // 1610 es Mud (Lodo). Si tu versión de Nukkit es antigua, esto podría ser dirt.
        this.setCoverId(1610); 

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
    
    // Opcional: Para asegurar que el suelo debajo de la superficie también sea lodo
    // Sobreescribimos este método si existe en tu versión de GrassyBiome/Biome
    public int getGroundId(int x, int y, int z) {
        return 1610;
    }
}

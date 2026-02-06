package cn.nukkit.level.biome.impl.mangrove;

import cn.nukkit.block.Block;
import cn.nukkit.level.biome.type.GrassyBiome;
import cn.nukkit.level.generator.populator.impl.PopulatorSugarcane;
import worldgeneratorextension.vanillagenerator.populator.MangroveTreePopulator;

public class MangroveSwampBiome extends GrassyBiome {
    public MangroveSwampBiome() {
        super();
        // Configuramos suelo de lodo (Mud)
        this.setSurfaceBlock(Block.get(Block.MUD));
        this.setGroundBlock(Block.get(Block.MUD));
        this.setTopBlock(Block.MUD);

        // Añadimos el populador de tus árboles personalizados
        MangroveTreePopulator mangroveTrees = new MangroveTreePopulator();
        mangroveTrees.setBaseAmount(3); 
        this.addPopulator(mangroveTrees);

        // Caña de azúcar (común en zonas húmedas)
        PopulatorSugarcane sugarcane = new PopulatorSugarcane();
        sugarcane.setBaseAmount(8);
        this.addPopulator(sugarcane);

        this.setBaseHeight(-0.2f); // Altura baja típica de manglares
        this.setHeightVariation(0.1f);
    }

    @Override
    public String getName() {
        return "Mangrove Swamp";
    }
}

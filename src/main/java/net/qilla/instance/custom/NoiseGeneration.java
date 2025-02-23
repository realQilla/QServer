package net.qilla.instance.custom;

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

public final class NoiseGeneration implements Generator {

    private static NoiseGeneration INSTANCE;

    private final JNoise noise = JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator.newBuilder())
            .octavate(6, 0.50, 1.5, FractalFunction.RIDGED_MULTI, true)
            .scale(1 / 64.0)
            .addModifier(v -> (v + 1) / 2.0)
            .clamp(0, 1)
            .build();

    public static NoiseGeneration get() {

        if(INSTANCE == null) INSTANCE = new NoiseGeneration();
        return INSTANCE;
    }

    private NoiseGeneration() {
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point start = unit.absoluteStart();
        for(int x = 0; x < unit.size().x(); x++) {
            for(int z = 0; z < unit.size().z(); z++) {
                int worldX = start.blockX() + x;
                int worldZ = start.blockZ() + z;

                double noiseVal = Math.max(0, (this.noise.evaluateNoise(worldX, worldZ) - 0.75));
                Point bottom = start.add(x, 0, z);

                if(noiseVal <= 0.10) unit.modifier().setBlock(bottom, Block.BLACK_CONCRETE);
                else if(noiseVal <= 0.15) unit.modifier().setBlock(bottom, Block.COAL_BLOCK);
                else if(noiseVal <= 0.20) unit.modifier().setBlock(bottom, Block.BLACK_WOOL);
                else if(noiseVal <= 0.25) unit.modifier().setBlock(bottom, Block.BLACK_CONCRETE_POWDER);
                else if(noiseVal <= 0.30) unit.modifier().setBlock(bottom, Block.OBSIDIAN);
                else if(noiseVal <= 0.35) unit.modifier().setBlock(bottom, Block.BLACKSTONE);
                else if(noiseVal <= 0.40) unit.modifier().setBlock(bottom, Block.GRAY_CONCRETE);
                else if(noiseVal <= 0.45) unit.modifier().setBlock(bottom, Block.GRAY_WOOL);
                else if(noiseVal <= 0.50) unit.modifier().setBlock(bottom, Block.GRAY_CONCRETE_POWDER);
                else if(noiseVal <= 0.55) unit.modifier().setBlock(bottom, Block.CYAN_TERRACOTTA);
                else if(noiseVal <= 0.60) unit.modifier().setBlock(bottom, Block.COBBLESTONE);
                else if(noiseVal <= 0.65) unit.modifier().setBlock(bottom, Block.LIGHT_GRAY_CONCRETE);
                else if(noiseVal <= 0.70) unit.modifier().setBlock(bottom, Block.LIGHT_GRAY_CONCRETE_POWDER);
                else if(noiseVal <= 0.75) unit.modifier().setBlock(bottom, Block.STONE);
                else if(noiseVal <= 0.80) unit.modifier().setBlock(bottom, Block.ANDESITE);
                else if(noiseVal <= 0.85) unit.modifier().setBlock(bottom, Block.WHITE_CONCRETE);
                else if(noiseVal <= 0.90) unit.modifier().setBlock(bottom, Block.WHITE_CONCRETE_POWDER);
                else if(noiseVal <= 0.95) unit.modifier().setBlock(bottom, Block.CHISELED_STONE_BRICKS);
                else if(noiseVal <= 0.99) unit.modifier().setBlock(bottom, Block.QUARTZ_BLOCK);
                else unit.modifier().setBlock(bottom, Block.SNOW_BLOCK);
            }
        }
    }
}
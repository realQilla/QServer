package net.qilla.instance.custom;

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class MainGeneration implements Generator {

    private static MainGeneration INSTANCE;

    private final Random random = new Random();
    private final JNoise worldNoise;

    private final JNoise cheeseCave;
    private final JNoise bedrockNoise;

    public static MainGeneration get() {
        if(INSTANCE == null) INSTANCE = new MainGeneration();
        return INSTANCE;
    }

    private MainGeneration() {
        this.worldNoise = JNoise.newBuilder()
                .perlin(PerlinNoiseGenerator.newBuilder().setSeed(random.nextLong()))
                .octavate(4, 1.65, 1.25, FractalFunction.RIDGED_MULTI, true)
                .scale(1 / 128.0)
                .addModifier(v -> (v + 1) / 2.0)
                .clamp(0, 1)
                .build();

        this.cheeseCave = JNoise.newBuilder()
                .perlin(PerlinNoiseGenerator.newBuilder())
                .octavate(6, 0.50, 1.5, FractalFunction.RIDGED_MULTI, true)
                .scale(1 / 64.0)
                .addModifier(v -> (v + 1) / 2.0)
                .clamp(0, 1)
                .build();
        this.bedrockNoise = JNoise.newBuilder()
                .perlin(PerlinNoiseGenerator.newBuilder().setSeed(random.nextLong()))
                .scale(1 / 64.0)
                .addModifier(v -> (v + 1) / 2.0)
                .clamp(0, 1)
                .build();
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        int chunkX = unit.absoluteStart().blockX() >> 4;
        int chunkZ = unit.absoluteStart().blockZ() >> 4;

        if(Math.abs(chunkX) > 16 || Math.abs(chunkZ) > 16) return;

        Point start = unit.absoluteStart();
        for(int x = 0; x < unit.size().x(); x++) {
            for(int z = 0; z < unit.size().z(); z++) {
                int worldX = start.blockX() + x;
                int worldZ = start.blockZ() + z;

                double surfaceVal = (worldNoise.evaluateNoise(worldX, worldZ) * 24) + 128;
                Point bottom = start.add(x, 0, z);

                //Generic fill
                unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(surfaceVal), Block.STONE);

                //Layering Logic
                unit.modifier().setBlock(bottom.withY(surfaceVal), Block.GRASS_BLOCK);
                unit.modifier().fill(bottom.withY(surfaceVal - 8), bottom.add(1, 0, 1).withY(surfaceVal), Block.DIRT);
                unit.modifier().fill(bottom.withY(surfaceVal - 16), bottom.add(1, 0, 1).withY(surfaceVal - 8), Block.COARSE_DIRT);
                unit.modifier().fill(bottom.withY(surfaceVal - 24), bottom.add(1, 0, 1).withY(surfaceVal - 16), Block.ROOTED_DIRT);
                unit.modifier().fill(bottom.withY(surfaceVal - 28), bottom.add(1, 0, 1).withY(surfaceVal - 24), Block.STONE);

                //Cave Logic
                for(int y = start.blockY(); y < surfaceVal; y++) {
                    double cheeseVal = Math.max(0, (this.cheeseCave.evaluateNoise(worldX, y, worldZ) - 0.75));

                    if(y >= surfaceVal - 16) {
                        if(cheeseVal > 0.18) unit.modifier().setBlock(bottom.withY(y), Block.AIR);
                    }
                    else if(cheeseVal > 0.15f) unit.modifier().setBlock(bottom.withY(y), Block.AIR);
                }

                //Bedrock logic
                double bedrockVal = (bedrockNoise.evaluateNoise(x + start.x(), z + start.z()) * 12) + 2;
                double bedrockHeight = bottom.y() + bedrockVal;

                unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(bedrockHeight), Block.BEDROCK);
                unit.modifier().fill(bottom.withY(bedrockHeight), bottom.add(1, 0, 1).withY(bedrockHeight + 8), Block.STONE);
            }
        }
    }
}
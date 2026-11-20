/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 */
package com.rotacore.world.viability;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public class SettlementViabilityService {
    public static final int DEFAULT_EVALUATION_RADIUS = 96;
    public static final int DEFAULT_MAX_ALLOWED_DELTA_Y = 14;
    public static final int DEFAULT_SAMPLE_STEP = 16;

    public static ViabilityResult evaluateViability(int centerX, int centerZ, HeightSampler sampler) {
        return SettlementViabilityService.evaluateViability(centerX, centerZ, 96, 14, sampler);
    }

    public static ViabilityResult evaluateViability(int centerX, int centerZ, int radius, int maxAllowedDeltaY, HeightSampler sampler) {
        double avgSlope;
        Objects.requireNonNull(sampler, "HeightSampler nao pode ser nulo");
        int centerHeight = sampler.getHeight(centerX, centerZ);
        BlockPos originalCenter = new BlockPos(centerX, centerHeight, centerZ);
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        double totalSlope = 0.0;
        int sampleCount = 0;
        int step = Math.max(8, 16);
        for (int dx = -radius; dx <= radius; dx += step) {
            for (int dz = -radius; dz <= radius; dz += step) {
                double dist;
                if (dx * dx + dz * dz > radius * radius) continue;
                int sampleX = centerX + dx;
                int sampleZ = centerZ + dz;
                int y = sampler.getHeight(sampleX, sampleZ);
                if (y < minY) {
                    minY = y;
                }
                if (y > maxY) {
                    maxY = y;
                }
                if ((dist = Math.sqrt(dx * dx + dz * dz)) > 0.001) {
                    double slope = (double)Math.abs(y - centerHeight) / dist;
                    totalSlope += slope;
                }
                ++sampleCount;
            }
        }
        if (sampleCount == 0) {
            minY = centerHeight;
            maxY = centerHeight;
        }
        int deltaY = maxY - minY;
        double d = avgSlope = sampleCount > 0 ? totalSlope / (double)sampleCount : 0.0;
        if (deltaY <= maxAllowedDeltaY) {
            return new ViabilityResult(true, minY, maxY, deltaY, avgSlope, originalCenter, originalCenter, "Terreno plano e estavel aprovado para assentamento.");
        }
        BlockPos alternativePlateau = SettlementViabilityService.findBestPlateau(centerX, centerZ, radius * 2, maxAllowedDeltaY, sampler);
        if (alternativePlateau != null) {
            return new ViabilityResult(true, minY, maxY, deltaY, avgSlope, originalCenter, alternativePlateau, "Centroide original com relevo acidentado (DeltaY=" + deltaY + "). Deslocado com sucesso para plato estavel em " + alternativePlateau.getX() + ", " + alternativePlateau.getZ() + ".");
        }
        return new ViabilityResult(false, minY, maxY, deltaY, avgSlope, originalCenter, null, "Relevo montanhoso e escarpado (DeltaY=" + deltaY + " > maximo permitido " + maxAllowedDeltaY + "). Reprovado para centros urbanos.");
    }

    public static BlockPos findBestPlateau(int originX, int originZ, int searchRadius, int maxAllowedDeltaY, HeightSampler sampler) {
        int bestDeltaY = Integer.MAX_VALUE;
        BlockPos bestCandidate = null;
        int localRadius = 36;
        int searchStep = 32;
        for (int r = 32; r <= searchRadius; r += searchStep) {
            for (int dx = -r; dx <= r; dx += searchStep) {
                for (int dz = -r; dz <= r; dz += searchStep) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    int candidateX = originX + dx;
                    int candidateZ = originZ + dz;
                    int localMin = Integer.MAX_VALUE;
                    int localMax = Integer.MIN_VALUE;
                    for (int lx = -localRadius; lx <= localRadius; lx += 12) {
                        for (int lz = -localRadius; lz <= localRadius; lz += 12) {
                            if (lx * lx + lz * lz > localRadius * localRadius) continue;
                            int y = sampler.getHeight(candidateX + lx, candidateZ + lz);
                            if (y < localMin) {
                                localMin = y;
                            }
                            if (y <= localMax) continue;
                            localMax = y;
                        }
                    }
                    int localDelta = localMax - localMin;
                    if (localDelta > maxAllowedDeltaY || localDelta >= bestDeltaY) continue;
                    bestDeltaY = localDelta;
                    int candidateY = sampler.getHeight(candidateX, candidateZ);
                    bestCandidate = new BlockPos(candidateX, candidateY, candidateZ);
                }
            }
            if (bestCandidate == null || bestDeltaY > maxAllowedDeltaY / 2) continue;
            return bestCandidate;
        }
        return bestCandidate;
    }

    public static ViabilityResult evaluateViability(Level level, BlockPos center, int radius) {
        if (level == null || center == null) {
            return new ViabilityResult(false, 0, 0, 0, 0.0, center, center, "Level ou centroide nulo");
        }
        HeightSampler sampler = (x, z) -> level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        return SettlementViabilityService.evaluateViability(center.getX(), center.getZ(), radius, 14, sampler);
    }

    @FunctionalInterface
    public static interface HeightSampler {
        public int getHeight(int var1, int var2);
    }

    public static class ViabilityResult {
        private final boolean viable;
        private final int minHeight;
        private final int maxHeight;
        private final int deltaY;
        private final double averageSlope;
        private final BlockPos originalCenter;
        private final BlockPos recommendedCenter;
        private final String diagnosis;

        public ViabilityResult(boolean viable, int minHeight, int maxHeight, int deltaY, double averageSlope, BlockPos originalCenter, BlockPos recommendedCenter, String diagnosis) {
            this.viable = viable;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
            this.deltaY = deltaY;
            this.averageSlope = averageSlope;
            this.originalCenter = originalCenter;
            this.recommendedCenter = recommendedCenter;
            this.diagnosis = diagnosis;
        }

        public boolean isViable() {
            return this.viable;
        }

        public int getMinHeight() {
            return this.minHeight;
        }

        public int getMaxHeight() {
            return this.maxHeight;
        }

        public int getDeltaY() {
            return this.deltaY;
        }

        public double getAverageSlope() {
            return this.averageSlope;
        }

        public BlockPos getOriginalCenter() {
            return this.originalCenter;
        }

        public BlockPos getRecommendedCenter() {
            return this.recommendedCenter;
        }

        public String getDiagnosis() {
            return this.diagnosis;
        }

        public String toString() {
            return "ViabilityResult{viable=" + this.viable + ", deltaY=" + this.deltaY + ", recommendedCenter=" + String.valueOf(this.recommendedCenter) + ", diagnosis='" + this.diagnosis + "'}";
        }
    }
}


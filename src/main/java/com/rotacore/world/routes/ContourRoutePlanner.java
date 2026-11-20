/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package com.rotacore.world.routes;

import com.rotacore.world.routes.RoadCurvatureMode;
import com.rotacore.world.routes.SplineRoute;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.world.phys.Vec3;

public class ContourRoutePlanner {
    public static final double MAX_SLOPE_PER_BLOCK = 0.5;
    public static final double DEFAULT_SAMPLE_SPACING = 24.0;

    public static List<Vec3> planContourWaypoints(Vec3 start, Vec3 end, RoadCurvatureMode mode, HeightSampler heightSampler, int seaLevel, long seed) {
        Objects.requireNonNull(start, "Ponto de inicio nao pode ser nulo");
        Objects.requireNonNull(end, "Ponto de destino nao pode ser nulo");
        RoadCurvatureMode curvatureMode = mode != null ? mode : RoadCurvatureMode.BALANCED;
        HeightSampler sampler = heightSampler != null ? heightSampler : (x, z) -> 64;
        double dx = end.x - start.x;
        double dz = end.z - start.z;
        double totalDist = Math.hypot(dx, dz);
        if (totalDist < 2.0) {
            return List.of(start, end);
        }
        double dirX = dx / totalDist;
        double dirZ = dz / totalDist;
        double normalX = -dirZ;
        double normalZ = dirX;
        List<Vec3> rawWaypoints = ContourRoutePlanner.generateHorizontalPath(start, end, totalDist, dirX, dirZ, normalX, normalZ, curvatureMode, sampler, seed);
        List<Vec3> elevatedWaypoints = ContourRoutePlanner.adjustAltitudesToTerrain(rawWaypoints, sampler, start.y, end.y, curvatureMode);
        List<Vec3> bridgeAdjusted = ContourRoutePlanner.levelBridgeCrossings(elevatedWaypoints, seaLevel, sampler);
        return ContourRoutePlanner.applySlopeRulesAndGradualStepping(bridgeAdjusted, 0.5);
    }

    public static SplineRoute planRoute(String id, String name, SplineRoute.Tier tier, Vec3 start, Vec3 end, RoadCurvatureMode mode, HeightSampler heightSampler, int seaLevel, long seed) {
        List<Vec3> controlPoints = ContourRoutePlanner.planContourWaypoints(start, end, mode, heightSampler, seaLevel, seed);
        return new SplineRoute(id, name, tier, controlPoints);
    }

    private static List<Vec3> generateHorizontalPath(Vec3 start, Vec3 end, double totalDist, double dirX, double dirZ, double normalX, double normalZ, RoadCurvatureMode mode, HeightSampler sampler, long seed) {
        ArrayList<Vec3> points = new ArrayList<Vec3>();
        points.add(start);
        int numSegments = Math.max(3, (int)Math.round(totalDist / 24.0));
        double segmentLength = totalDist / (double)numSegments;
        Random rng = new Random(seed ^ 0x5DEECE66DL);
        switch (mode) {
            case DIRECT: {
                for (int i = 1; i < numSegments; ++i) {
                    double dist = (double)i * segmentLength;
                    double px = start.x + dirX * dist;
                    double pz = start.z + dirZ * dist;
                    points.add(new Vec3(px, start.y, pz));
                }
                break;
            }
            case SERPENTINE: {
                double maxWarp = Math.min(64.0, totalDist * 0.28);
                for (int i = 1; i < numSegments; ++i) {
                    double lateralOffset;
                    double progress = (double)i / (double)numSegments;
                    double dist = (double)i * segmentLength;
                    double baseX = start.x + dirX * dist;
                    double baseZ = start.z + dirZ * dist;
                    double sign = i % 2 == 1 ? 1.0 : -1.0;
                    double envelope = Math.sin(progress * Math.PI);
                    double bestOffset = lateralOffset = sign * (maxWarp * envelope);
                    double minDeltaY = Double.MAX_VALUE;
                    double currentRefY = ((Vec3)points.get((int)(points.size() - 1))).y;
                    for (double d = -16.0; d <= 16.0; d += 8.0) {
                        double candOffset = lateralOffset + d;
                        double cx = baseX + normalX * candOffset;
                        double cz = baseZ + normalZ * candOffset;
                        int sampledH = sampler.getHeight(cx, cz);
                        double delta = Math.abs((double)sampledH - currentRefY);
                        if (!(delta < minDeltaY)) continue;
                        minDeltaY = delta;
                        bestOffset = candOffset;
                    }
                    double finalX = baseX + normalX * bestOffset;
                    double finalZ = baseZ + normalZ * bestOffset;
                    points.add(new Vec3(finalX, start.y, finalZ));
                }
                break;
            }
            default: {
                double balWarp = Math.min(32.0, totalDist * 0.16);
                for (int i = 1; i < numSegments; ++i) {
                    double progress = (double)i / (double)numSegments;
                    double dist = (double)i * segmentLength;
                    double baseX = start.x + dirX * dist;
                    double baseZ = start.z + dirZ * dist;
                    double envelope = Math.sin(progress * Math.PI);
                    double noiseOffset = (rng.nextDouble() * 2.0 - 1.0) * (balWarp * envelope);
                    int directH = sampler.getHeight(baseX, baseZ);
                    double candX1 = baseX + normalX * noiseOffset;
                    double candZ1 = baseZ + normalZ * noiseOffset;
                    int h1 = sampler.getHeight(candX1, candZ1);
                    double chosenX = candX1;
                    double chosenZ = candZ1;
                    double expectedGrad = start.y + progress * (end.y - start.y);
                    if (Math.abs((double)directH - expectedGrad) < Math.abs((double)h1 - expectedGrad)) {
                        chosenX = baseX + normalX * (noiseOffset * 0.3);
                        chosenZ = baseZ + normalZ * (noiseOffset * 0.3);
                    }
                    points.add(new Vec3(chosenX, start.y, chosenZ));
                }
            }
        }
        points.add(end);
        return points;
    }

    private static List<Vec3> adjustAltitudesToTerrain(List<Vec3> points, HeightSampler sampler, double startY, double endY, RoadCurvatureMode mode) {
        int n = points.size();
        ArrayList<Vec3> adjusted = new ArrayList<Vec3>(n);
        adjusted.add(points.get(0));
        for (int i = 1; i < n - 1; ++i) {
            Vec3 pt = points.get(i);
            double progress = (double)i / (double)(n - 1);
            double targetGrad = startY + progress * (endY - startY);
            int terrainH = sampler.getHeight(pt.x, pt.z);
            double finalY = mode == RoadCurvatureMode.SERPENTINE ? (double)terrainH : (mode == RoadCurvatureMode.DIRECT ? targetGrad * 0.7 + (double)terrainH * 0.3 : targetGrad * 0.4 + (double)terrainH * 0.6);
            adjusted.add(new Vec3(pt.x, finalY, pt.z));
        }
        adjusted.add(points.get(n - 1));
        return adjusted;
    }

    public static List<Vec3> levelBridgeCrossings(List<Vec3> points, int seaLevel, HeightSampler sampler) {
        if (points == null || points.size() < 2) {
            return points != null ? points : List.of();
        }
        int bridgeDeckY = seaLevel + 2;
        int n = points.size();
        ArrayList<Vec3> result = new ArrayList<Vec3>(n);
        double startY = points.get((int)0).y;
        double endY = points.get((int)(n - 1)).y;
        double averageBankY = Math.max((double)bridgeDeckY, (startY + endY) / 2.0);
        for (int i = 0; i < n; ++i) {
            int sampledH;
            Vec3 pt = points.get(i);
            if (i == 0 || i == n - 1) {
                result.add(pt);
                continue;
            }
            int n2 = sampledH = sampler != null ? sampler.getHeight(pt.x, pt.z) : (int)Math.round(pt.y);
            if (sampledH <= seaLevel + 1 || pt.y <= (double)(seaLevel + 1)) {
                result.add(new Vec3(pt.x, averageBankY, pt.z));
                continue;
            }
            result.add(pt);
        }
        return result;
    }

    public static List<Vec3> applySlopeRulesAndGradualStepping(List<Vec3> points, double maxSlope) {
        if (points == null || points.size() < 2) {
            return points != null ? points : List.of();
        }
        ArrayList<Vec3> result = new ArrayList<Vec3>();
        result.add(points.get(0));
        for (int i = 1; i < points.size(); ++i) {
            Vec3 prev = (Vec3)result.get(result.size() - 1);
            Vec3 curr = points.get(i);
            double hDist = Math.hypot(curr.x - prev.x, curr.z - prev.z);
            if (hDist < 0.001) continue;
            double rawDeltaY = curr.y - prev.y;
            double maxAllowedDeltaY = hDist * maxSlope;
            double clampedDeltaY = Math.max(-maxAllowedDeltaY, Math.min(maxAllowedDeltaY, rawDeltaY));
            double targetY = prev.y + clampedDeltaY;
            double absDeltaY = Math.abs(clampedDeltaY);
            if (absDeltaY < 0.25 || hDist < 2.0) {
                result.add(new Vec3(curr.x, targetY, curr.z));
                continue;
            }
            double minDistancePerStep = 0.5 / maxSlope;
            int maxPossibleSteps = (int)Math.floor(hDist / minDistancePerStep);
            int desiredSteps = (int)Math.round(absDeltaY / 0.5);
            int numSteps = Math.max(1, Math.min(desiredSteps, maxPossibleSteps));
            double stepDeltaY = Math.signum(clampedDeltaY) * absDeltaY / (double)numSteps;
            double dirX = (curr.x - prev.x) / hDist;
            double dirZ = (curr.z - prev.z) / hDist;
            double stepDist = hDist / (double)numSteps;
            for (int s = 1; s <= numSteps; ++s) {
                double dist = (double)s * stepDist;
                double px = prev.x + dirX * dist;
                double pz = prev.z + dirZ * dist;
                double py = prev.y + (double)s * stepDeltaY;
                result.add(new Vec3(px, py, pz));
            }
        }
        return result;
    }

    @FunctionalInterface
    public static interface HeightSampler {
        public int getHeight(double var1, double var3);
    }
}


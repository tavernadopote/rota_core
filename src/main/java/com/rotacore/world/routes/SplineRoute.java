/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.phys.Vec3
 */
package com.rotacore.world.routes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class SplineRoute {
    private final String id;
    private final String name;
    private final Tier tier;
    private final List<Vec3> controlPoints;
    private final double approximateLength;

    public SplineRoute(String id, String name, Tier tier, List<Vec3> controlPoints) {
        this.id = Objects.requireNonNull(id, "ID da rota nao pode ser nulo");
        this.name = name != null ? name : id;
        Tier tier2 = this.tier = tier != null ? tier : Tier.PRIMARY;
        if (controlPoints == null || controlPoints.size() < 2) {
            throw new IllegalArgumentException("Uma SplineRoute exige no minimo 2 waypoints de controle.");
        }
        this.controlPoints = Collections.unmodifiableList(new ArrayList<Vec3>(controlPoints));
        this.approximateLength = this.computeLength();
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Tier getTier() {
        return this.tier;
    }

    public List<Vec3> getControlPoints() {
        return this.controlPoints;
    }

    public double getApproximateLength() {
        return this.approximateLength;
    }

    public BlockPos getStartPos() {
        Vec3 start = this.controlPoints.get(0);
        return new BlockPos((int)Math.round(start.x), (int)Math.round(start.y), (int)Math.round(start.z));
    }

    public BlockPos getEndPos() {
        Vec3 end = this.controlPoints.get(this.controlPoints.size() - 1);
        return new BlockPos((int)Math.round(end.x), (int)Math.round(end.y), (int)Math.round(end.z));
    }

    public Vec3 sample(double progress) {
        double clamped = Math.max(0.0, Math.min(1.0, progress));
        int n = this.controlPoints.size();
        if (n == 2) {
            Vec3 p0 = this.controlPoints.get(0);
            Vec3 p1 = this.controlPoints.get(1);
            return new Vec3(SplineRoute.lerp(clamped, p0.x, p1.x), SplineRoute.lerp(clamped, p0.y, p1.y), SplineRoute.lerp(clamped, p0.z, p1.z));
        }
        int numSegments = n - 1;
        double scaledProgress = clamped * (double)numSegments;
        int segmentIndex = (int)Math.floor(scaledProgress);
        if (segmentIndex >= numSegments) {
            segmentIndex = numSegments - 1;
        }
        double t = scaledProgress - (double)segmentIndex;
        Vec3 p1 = this.controlPoints.get(segmentIndex);
        Vec3 p2 = this.controlPoints.get(segmentIndex + 1);
        Vec3 p0 = segmentIndex > 0 ? this.controlPoints.get(segmentIndex - 1) : SplineRoute.extrapolateBefore(p1, p2);
        Vec3 p3 = segmentIndex + 2 < n ? this.controlPoints.get(segmentIndex + 2) : SplineRoute.extrapolateAfter(p1, p2);
        return SplineRoute.catmullRom(p0, p1, p2, p3, t);
    }

    public List<Vec3> discretize(double stepDistance) {
        double step = Math.max(0.5, stepDistance);
        int samples = (int)Math.ceil(this.approximateLength / step);
        if (samples < 2) {
            samples = 2;
        }
        ArrayList<Vec3> points = new ArrayList<Vec3>(samples + 1);
        for (int i = 0; i <= samples; ++i) {
            double u = (double)i / (double)samples;
            points.add(this.sample(u));
        }
        return points;
    }

    public boolean passesThroughChunk(int chunkX, int chunkZ, double bufferRadius) {
        int minX = (chunkX << 4) - (int)Math.ceil(bufferRadius);
        int minZ = (chunkZ << 4) - (int)Math.ceil(bufferRadius);
        int maxX = (chunkX << 4) + 15 + (int)Math.ceil(bufferRadius);
        int maxZ = (chunkZ << 4) + 15 + (int)Math.ceil(bufferRadius);
        int numChecks = Math.max(10, (int)Math.ceil(this.approximateLength / 8.0));
        for (int i = 0; i <= numChecks; ++i) {
            double u = (double)i / (double)numChecks;
            Vec3 pos = this.sample(u);
            if (!(pos.x >= (double)minX) || !(pos.x <= (double)maxX) || !(pos.z >= (double)minZ) || !(pos.z <= (double)maxZ)) continue;
            return true;
        }
        return false;
    }

    public double calculateMinRadiusOfCurvature() {
        int samples = Math.max(30, (int)Math.ceil(this.approximateLength / 4.0));
        double minRadius = Double.MAX_VALUE;
        double dt = 1.0 / (double)samples;
        for (int i = 1; i < samples - 1; ++i) {
            double radius;
            double uPrev = (double)(i - 1) * dt;
            double uCurr = (double)i * dt;
            double uNext = (double)(i + 1) * dt;
            Vec3 pPrev = this.sample(uPrev);
            Vec3 pCurr = this.sample(uCurr);
            Vec3 pNext = this.sample(uNext);
            double dx1 = (pCurr.x - pPrev.x) / dt;
            double dz1 = (pCurr.z - pPrev.z) / dt;
            double dx2 = (pNext.x - pCurr.x) / dt;
            double dz2 = (pNext.z - pCurr.z) / dt;
            double ddx = (dx2 - dx1) / dt;
            double ddz = (dz2 - dz1) / dt;
            double speedSq = dx1 * dx1 + dz1 * dz1;
            double numerator = Math.pow(speedSq, 1.5);
            double denominator = Math.abs(dx1 * ddz - dz1 * ddx);
            if (!(denominator > 1.0E-6) || !((radius = numerator / denominator) < minRadius)) continue;
            minRadius = radius;
        }
        return minRadius == Double.MAX_VALUE ? Double.POSITIVE_INFINITY : minRadius;
    }

    public static boolean isPointOverWater(Vec3 point, int seaLevel, HeightSampler heightSampler) {
        if (point == null) {
            return false;
        }
        if (point.y <= (double)(seaLevel + 1)) {
            return true;
        }
        if (heightSampler != null) {
            return heightSampler.getHeight(point.x, point.z) <= seaLevel + 1;
        }
        return false;
    }

    public static List<Vec3> levelControlPointsForBridges(List<Vec3> points, int seaLevel, HeightSampler heightSampler) {
        if (points == null || points.size() < 2) {
            return points != null ? points : List.of();
        }
        int n = points.size();
        ArrayList<Vec3> result = new ArrayList<Vec3>(n);
        int bridgeDeckY = seaLevel + 2;
        double startY = points.get((int)0).y;
        double endY = points.get((int)(n - 1)).y;
        double averageBankY = Math.max((double)bridgeDeckY, (startY + endY) / 2.0);
        for (int i = 0; i < n; ++i) {
            int sampledHeight;
            Vec3 pt = points.get(i);
            if (i == 0 || i == n - 1) {
                result.add(pt);
                continue;
            }
            int n2 = sampledHeight = heightSampler != null ? heightSampler.getHeight(pt.x, pt.z) : (int)Math.round(pt.y);
            if (sampledHeight <= seaLevel + 1 || pt.y <= (double)(seaLevel + 1)) {
                result.add(new Vec3(pt.x, averageBankY, pt.z));
                continue;
            }
            result.add(pt);
        }
        return Collections.unmodifiableList(result);
    }

    private double computeLength() {
        int samples = Math.max(20, (int)Math.ceil(this.controlPoints.size() * 15));
        double total = 0.0;
        Vec3 prev = this.sample(0.0);
        for (int i = 1; i <= samples; ++i) {
            double u = (double)i / (double)samples;
            Vec3 curr = this.sample(u);
            total += prev.distanceTo(curr);
            prev = curr;
        }
        return total;
    }

    private static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double x = 0.5 * (2.0 * p1.x + (-p0.x + p2.x) * t + (2.0 * p0.x - 5.0 * p1.x + 4.0 * p2.x - p3.x) * t2 + (-p0.x + 3.0 * p1.x - 3.0 * p2.x + p3.x) * t3);
        double y = 0.5 * (2.0 * p1.y + (-p0.y + p2.y) * t + (2.0 * p0.y - 5.0 * p1.y + 4.0 * p2.y - p3.y) * t2 + (-p0.y + 3.0 * p1.y - 3.0 * p2.y + p3.y) * t3);
        double z = 0.5 * (2.0 * p1.z + (-p0.z + p2.z) * t + (2.0 * p0.z - 5.0 * p1.z + 4.0 * p2.z - p3.z) * t2 + (-p0.z + 3.0 * p1.z - 3.0 * p2.z + p3.z) * t3);
        return new Vec3(x, y, z);
    }

    private static Vec3 extrapolateBefore(Vec3 p1, Vec3 p2) {
        return new Vec3(p1.x - (p2.x - p1.x), p1.y - (p2.y - p1.y), p1.z - (p2.z - p1.z));
    }

    private static Vec3 extrapolateAfter(Vec3 p1, Vec3 p2) {
        return new Vec3(p2.x + (p2.x - p1.x), p2.y + (p2.y - p1.y), p2.z + (p2.z - p1.z));
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SplineRoute that = (SplineRoute)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "SplineRoute{id='" + this.id + "', name='" + this.name + "', tier=" + String.valueOf((Object)this.tier) + ", waypoints=" + this.controlPoints.size() + ", length=" + String.format("%.1f", this.approximateLength) + "m}";
    }

    public static enum Tier {
        PRIMARY("Rodovia Real", 6.0),
        SECONDARY("Estrada Regional", 4.0),
        TRAIL("Trilha Local", 2.0);

        private final String displayName;
        private final double defaultWidth;

        private Tier(String displayName, double defaultWidth) {
            this.displayName = displayName;
            this.defaultWidth = defaultWidth;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public double getDefaultWidth() {
            return this.defaultWidth;
        }
    }

    @FunctionalInterface
    public static interface HeightSampler {
        public int getHeight(double var1, double var3);
    }
}


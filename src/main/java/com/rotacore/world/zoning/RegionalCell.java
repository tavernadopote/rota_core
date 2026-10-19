package com.rotacore.world.zoning;

/**
 * Representa uma célula de zoneamento macro-regional baseada na seed do mundo.
 * Cada célula possui coordenadas de grid (cellX, cellZ) e calcula deterministamente
 * os chunks centrais e de fronteira eleitos para estruturas civis e militares.
 */
public class RegionalCell {

    private final int cellX;
    private final int cellZ;
    private final int cellSizeChunks;

    private final int minChunkX;
    private final int maxChunkX;
    private final int minChunkZ;
    private final int maxChunkZ;

    private final int villageChunkX;
    private final int villageChunkZ;
    private final int outpostChunkX;
    private final int outpostChunkZ;

    public RegionalCell(long worldSeed, int cellX, int cellZ, int cellSizeChunks) {
        this.cellX = cellX;
        this.cellZ = cellZ;
        this.cellSizeChunks = Math.max(16, cellSizeChunks);

        this.minChunkX = cellX * this.cellSizeChunks;
        this.maxChunkX = minChunkX + this.cellSizeChunks - 1;
        this.minChunkZ = cellZ * this.cellSizeChunks;
        this.maxChunkZ = minChunkZ + this.cellSizeChunks - 1;

        // Hash pseudo-aleatório determinístico baseado na seed do mundo e índices da célula
        long h = (worldSeed ^ 0x5DEECE66DL) + (long) cellX * 341873128712L + (long) cellZ * 132897987541L;
        h = (h ^ (h >>> 16)) * 0x85ebca6bL;
        h = (h ^ (h >>> 13)) * 0xc2b2ae35L;
        long cellSeed = h ^ (h >>> 16);

        // 1. Chank eleito para a Vila (área central com margem segura das bordas)
        int margin = Math.max(4, this.cellSizeChunks / 6);
        int span = Math.max(1, this.cellSizeChunks - (margin * 2));

        int vOffsetX = margin + (int) (Math.abs(cellSeed % span));
        int vOffsetZ = margin + (int) (Math.abs((cellSeed / 31) % span));

        this.villageChunkX = minChunkX + vOffsetX;
        this.villageChunkZ = minChunkZ + vOffsetZ;

        // 2. Chank eleito para o Posto Militar (periferia / fronteira externa da célula)
        // Posicionado no quadrante oposto à vila para maximizar a separação territorial
        int borderMargin = 2;
        int oOffsetX = (vOffsetX < this.cellSizeChunks / 2)
                ? (this.cellSizeChunks - borderMargin - 1 - (int) (Math.abs((cellSeed / 17) % 4)))
                : (borderMargin + (int) (Math.abs((cellSeed / 17) % 4)));

        int oOffsetZ = (vOffsetZ < this.cellSizeChunks / 2)
                ? (this.cellSizeChunks - borderMargin - 1 - (int) (Math.abs((cellSeed / 23) % 4)))
                : (borderMargin + (int) (Math.abs((cellSeed / 23) % 4)));

        this.outpostChunkX = minChunkX + oOffsetX;
        this.outpostChunkZ = minChunkZ + oOffsetZ;
    }

    public int getCellX() {
        return cellX;
    }

    public int getCellZ() {
        return cellZ;
    }

    public int getCellSizeChunks() {
        return cellSizeChunks;
    }

    public int getMinChunkX() {
        return minChunkX;
    }

    public int getMaxChunkX() {
        return maxChunkX;
    }

    public int getMinChunkZ() {
        return minChunkZ;
    }

    public int getMaxChunkZ() {
        return maxChunkZ;
    }

    public int getMinBlockX() {
        return minChunkX << 4;
    }

    public int getMaxBlockX() {
        return (maxChunkX << 4) + 15;
    }

    public int getMinBlockZ() {
        return minChunkZ << 4;
    }

    public int getMaxBlockZ() {
        return (maxChunkZ << 4) + 15;
    }

    public int getVillageChunkX() {
        return villageChunkX;
    }

    public int getVillageChunkZ() {
        return villageChunkZ;
    }

    public int getVillageBlockX() {
        return (villageChunkX << 4) + 8;
    }

    public int getVillageBlockZ() {
        return (villageChunkZ << 4) + 8;
    }

    public int getOutpostChunkX() {
        return outpostChunkX;
    }

    public int getOutpostChunkZ() {
        return outpostChunkZ;
    }

    public int getOutpostBlockX() {
        return (outpostChunkX << 4) + 8;
    }

    public int getOutpostBlockZ() {
        return (outpostChunkZ << 4) + 8;
    }

    public double getDistanceBetweenVillageAndOutpost() {
        int dx = getVillageBlockX() - getOutpostBlockX();
        int dz = getVillageBlockZ() - getOutpostBlockZ();
        return Math.hypot(dx, dz);
    }
}

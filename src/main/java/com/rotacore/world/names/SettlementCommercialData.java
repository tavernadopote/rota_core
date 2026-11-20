/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.rotacore.world.names;

import com.rotacore.world.zoning.SettlementBounds;
import com.rotacore.world.zoning.SettlementZone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SettlementCommercialData {
    public static final Map<String, SettlementVocation> WORKSTATION_MAPPING = Map.ofEntries(Map.entry("minecraft:composter", SettlementVocation.AGRICULTURE), Map.entry("minecraft:smoker", SettlementVocation.AGRICULTURE), Map.entry("minecraft:blast_furnace", SettlementVocation.METALLURGY), Map.entry("minecraft:grindstone", SettlementVocation.METALLURGY), Map.entry("minecraft:smithing_table", SettlementVocation.METALLURGY), Map.entry("minecraft:barrel", SettlementVocation.FISHING), Map.entry("minecraft:cartography_table", SettlementVocation.FISHING), Map.entry("minecraft:lectern", SettlementVocation.SCHOLARSHIP), Map.entry("minecraft:brewing_stand", SettlementVocation.SCHOLARSHIP), Map.entry("minecraft:stonecutter", SettlementVocation.CRAFTING), Map.entry("minecraft:loom", SettlementVocation.CRAFTING), Map.entry("minecraft:cauldron", SettlementVocation.CRAFTING), Map.entry("minecraft:fletching_table", SettlementVocation.CRAFTING));
    private String primaryVocation;
    private String secondaryVocation;
    private List<String> highlightedTradeItems;
    private Map<String, Integer> workstationCounts;

    public SettlementCommercialData() {
        this("Com\u00e9rcio Geral", "", Collections.emptyList(), Collections.emptyMap());
    }

    public SettlementCommercialData(String primaryVocation, String secondaryVocation, List<String> highlightedTradeItems, Map<String, Integer> workstationCounts) {
        this.primaryVocation = primaryVocation != null ? primaryVocation : "Com\u00e9rcio Geral";
        this.secondaryVocation = secondaryVocation != null ? secondaryVocation : "";
        this.highlightedTradeItems = highlightedTradeItems != null ? new ArrayList<String>(highlightedTradeItems) : new ArrayList();
        this.workstationCounts = workstationCounts != null ? new HashMap<String, Integer>(workstationCounts) : new HashMap();
    }

    public String getPrimaryVocation() {
        return this.primaryVocation;
    }

    public void setPrimaryVocation(String primaryVocation) {
        this.primaryVocation = primaryVocation != null ? primaryVocation : "Com\u00e9rcio Geral";
    }

    public String getSecondaryVocation() {
        return this.secondaryVocation;
    }

    public void setSecondaryVocation(String secondaryVocation) {
        this.secondaryVocation = secondaryVocation != null ? secondaryVocation : "";
    }

    public List<String> getHighlightedTradeItems() {
        return Collections.unmodifiableList(this.highlightedTradeItems);
    }

    public void setHighlightedTradeItems(List<String> highlightedTradeItems) {
        this.highlightedTradeItems = highlightedTradeItems != null ? new ArrayList<String>(highlightedTradeItems) : new ArrayList();
    }

    public Map<String, Integer> getWorkstationCounts() {
        return Collections.unmodifiableMap(this.workstationCounts);
    }

    public void setWorkstationCounts(Map<String, Integer> workstationCounts) {
        this.workstationCounts = workstationCounts != null ? new HashMap<String, Integer>(workstationCounts) : new HashMap();
    }

    public String getFormattedDescription() {
        if (this.secondaryVocation != null && !this.secondaryVocation.trim().isEmpty()) {
            return this.primaryVocation + " / " + this.secondaryVocation;
        }
        return this.primaryVocation;
    }

    public String getTradeItemsSummary() {
        if (this.highlightedTradeItems.isEmpty()) {
            return "";
        }
        return String.join((CharSequence)", ", this.highlightedTradeItems);
    }

    public static List<String> getDefaultTradeItems(SettlementVocation vocation, String locale) {
        boolean isPt = locale != null && locale.toLowerCase().startsWith("pt");
        return switch (vocation.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (isPt) {
                    yield List.of("P\u00e3o", "Trigo", "Carne Bovina", "Cenouras");
                }
                yield List.of("Bread", "Wheat", "Beef", "Carrots");
            }
            case 1 -> {
                if (isPt) {
                    yield List.of("Armaduras de Ferro", "Espadas", "Ferramentas", "Lingotes de Ferro");
                }
                yield List.of("Iron Armor", "Swords", "Tools", "Iron Ingots");
            }
            case 2 -> {
                if (isPt) {
                    yield List.of("Peixe Fresco", "Barris", "Mapas do Tesouro", "B\u00fassolas");
                }
                yield List.of("Fresh Fish", "Barrels", "Treasure Maps", "Compasses");
            }
            case 3 -> {
                if (isPt) {
                    yield List.of("Livros Encantados", "Po\u00e7\u00f5es", "Papel", "Redstone");
                }
                yield List.of("Enchanted Books", "Potions", "Paper", "Redstone");
            }
            case 4 -> {
                if (isPt) {
                    yield List.of("Pedras Talhadas", "Tijolos", "L\u00e3 Colorida", "Couro", "Flechas");
                }
                yield List.of("Cut Stone", "Bricks", "Dyed Wool", "Leather", "Arrows");
            }
            case 5 -> isPt ? List.of("Suprimentos Variados", "Esmeraldas", "Cereais") : List.of("Assorted Supplies", "Emeralds", "Grains");
        };
    }

    /*
     * WARNING - void declaration
     */
    public static SettlementCommercialData estimateFromWorkstations(Map<String, Integer> counts, String locale) {
        List<String> secItems;
        void var4_8;
        EnumMap<SettlementVocation, Integer> score = new EnumMap<SettlementVocation, Integer>(SettlementVocation.class);
        for (SettlementVocation v : SettlementVocation.values()) {
            score.put(v, 0);
        }
        if (counts != null) {
            for (Map.Entry entry : counts.entrySet()) {
                String blockId = (String)entry.getKey();
                int c = (Integer)entry.getValue();
                SettlementVocation voc = WORKSTATION_MAPPING.get(blockId);
                if (voc == null) continue;
                score.put(voc, (Integer)score.get((Object)voc) + c);
            }
        }
        ArrayList sorted = new ArrayList(score.entrySet());
        sorted.sort((a, b) -> Integer.compare((Integer)b.getValue(), (Integer)a.getValue()));
        SettlementVocation settlementVocation = SettlementVocation.GENERAL_COMMERCE;
        SettlementVocation secondary = null;
        if (!sorted.isEmpty() && (Integer)((Map.Entry)sorted.get(0)).getValue() > 0) {
            SettlementVocation settlementVocation2 = (SettlementVocation)((Object)((Map.Entry)sorted.get(0)).getKey());
            if (sorted.size() > 1 && (Integer)((Map.Entry)sorted.get(1)).getValue() > 0 && ((Map.Entry)sorted.get(1)).getKey() != settlementVocation2) {
                secondary = (SettlementVocation)((Object)((Map.Entry)sorted.get(1)).getKey());
            }
        }
        String primaryName = var4_8.getDisplayName(locale);
        String secondaryName = secondary != null ? secondary.getDisplayName(locale) : "";
        ArrayList<String> items = new ArrayList<String>(SettlementCommercialData.getDefaultTradeItems((SettlementVocation)var4_8, locale));
        if (secondary != null && !(secItems = SettlementCommercialData.getDefaultTradeItems(secondary, locale)).isEmpty()) {
            items.add(secItems.get(0));
        }
        return new SettlementCommercialData(primaryName, secondaryName, items, counts != null ? counts : Collections.emptyMap());
    }

    public static SettlementCommercialData createDeterministic(long seed, int centerX, int centerZ, String locale) {
        List<String> secItems;
        SettlementVocation candidate;
        long combinedSeed = seed * 31L ^ ((long)centerX << 32 | (long)centerZ & 0xFFFFFFFFL);
        Random random = new Random(combinedSeed);
        SettlementVocation[] vocations = new SettlementVocation[]{SettlementVocation.AGRICULTURE, SettlementVocation.METALLURGY, SettlementVocation.FISHING, SettlementVocation.SCHOLARSHIP, SettlementVocation.CRAFTING};
        SettlementVocation primary = vocations[random.nextInt(vocations.length)];
        SettlementVocation secondary = null;
        if (random.nextInt(100) < 50 && (candidate = vocations[random.nextInt(vocations.length)]) != primary) {
            secondary = candidate;
        }
        String primaryName = primary.getDisplayName(locale);
        String secondaryName = secondary != null ? secondary.getDisplayName(locale) : "";
        ArrayList<String> items = new ArrayList<String>(SettlementCommercialData.getDefaultTradeItems(primary, locale));
        if (secondary != null && !(secItems = SettlementCommercialData.getDefaultTradeItems(secondary, locale)).isEmpty()) {
            items.add(secItems.get(0));
        }
        return new SettlementCommercialData(primaryName, secondaryName, items, Collections.emptyMap());
    }

    public static SettlementCommercialData estimateFromZone(LevelAccessor level, SettlementZone zone, String locale, long seed, int centerX, int centerZ) {
        if (level == null || zone == null || zone.getPieces().isEmpty()) {
            return SettlementCommercialData.createDeterministic(seed, centerX, centerZ, locale);
        }
        HashMap<String, Integer> workstationCounts = new HashMap<String, Integer>();
        try {
            for (SettlementBounds piece : zone.getPieces()) {
                int minX = piece.minX();
                int maxX = piece.maxX();
                int minY = piece.minY();
                int maxY = piece.maxY();
                int minZ = piece.minZ();
                int maxZ = piece.maxZ();
                int dx = maxX - minX;
                int dy = maxY - minY;
                int dz = maxZ - minZ;
                if (dx > 32 || dy > 32 || dz > 32) continue;
                for (int x = minX; x <= maxX; ++x) {
                    for (int y = minY; y <= maxY; ++y) {
                        for (int z = minZ; z <= maxZ; ++z) {
                            String id;
                            BlockPos p = new BlockPos(x, y, z);
                            BlockState state = level.getBlockState(p);
                            Block block = state.getBlock();
                            ResourceLocation key = BuiltInRegistries.BLOCK.getKey((Object)block);
                            if (key == null || !WORKSTATION_MAPPING.containsKey(id = key.toString())) continue;
                            workstationCounts.put(id, workstationCounts.getOrDefault(id, 0) + 1);
                        }
                    }
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (workstationCounts.isEmpty()) {
            return SettlementCommercialData.createDeterministic(seed, centerX, centerZ, locale);
        }
        return SettlementCommercialData.estimateFromWorkstations(workstationCounts, locale);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("primary_vocation", this.primaryVocation);
        if (this.secondaryVocation != null && !this.secondaryVocation.isEmpty()) {
            tag.putString("secondary_vocation", this.secondaryVocation);
        }
        ListTag itemsList = new ListTag();
        for (String item : this.highlightedTradeItems) {
            itemsList.add((Object)StringTag.valueOf((String)item));
        }
        tag.put("highlighted_trade_items", (Tag)itemsList);
        CompoundTag countsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : this.workstationCounts.entrySet()) {
            countsTag.putInt(entry.getKey(), entry.getValue().intValue());
        }
        tag.put("workstation_counts", (Tag)countsTag);
        return tag;
    }

    public static SettlementCommercialData load(CompoundTag tag) {
        if (tag == null) {
            return new SettlementCommercialData();
        }
        String primary = tag.getString("primary_vocation");
        String secondary = tag.getString("secondary_vocation");
        ArrayList<String> items = new ArrayList<String>();
        if (tag.contains("highlighted_trade_items", 9)) {
            ListTag list = tag.getList("highlighted_trade_items", 8);
            for (int i = 0; i < list.size(); ++i) {
                items.add(list.getString(i));
            }
        }
        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        if (tag.contains("workstation_counts", 10)) {
            CompoundTag cTag = tag.getCompound("workstation_counts");
            for (String key : cTag.getAllKeys()) {
                counts.put(key, cTag.getInt(key));
            }
        }
        return new SettlementCommercialData(primary, secondary, items, counts);
    }

    public static enum SettlementVocation {
        AGRICULTURE("Agropecu\u00e1ria", "Agriculture"),
        METALLURGY("Metalurgia e Armaria", "Metallurgy & Smithing"),
        FISHING("Pescaria e Navega\u00e7\u00e3o", "Fishing & Navigation"),
        SCHOLARSHIP("Alquimia e Conhecimento", "Alchemy & Scholarship"),
        CRAFTING("Manufatura e Alvenaria", "Crafting & Masonry"),
        GENERAL_COMMERCE("Com\u00e9rcio Geral", "General Goods");

        private final String ptBr;
        private final String enUs;

        private SettlementVocation(String ptBr, String enUs) {
            this.ptBr = ptBr;
            this.enUs = enUs;
        }

        public String getDisplayName(String locale) {
            if (locale != null && locale.toLowerCase().startsWith("pt")) {
                return this.ptBr;
            }
            return this.enUs;
        }

        public static SettlementVocation fromName(String name) {
            if (name == null || name.isEmpty()) {
                return GENERAL_COMMERCE;
            }
            for (SettlementVocation v : SettlementVocation.values()) {
                if (!v.name().equalsIgnoreCase(name) && !v.ptBr.equalsIgnoreCase(name) && !v.enUs.equalsIgnoreCase(name)) continue;
                return v;
            }
            return GENERAL_COMMERCE;
        }
    }
}


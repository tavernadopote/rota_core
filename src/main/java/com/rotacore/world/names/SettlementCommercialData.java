package com.rotacore.world.names;

import com.rotacore.world.zoning.SettlementBounds;
import com.rotacore.world.zoning.SettlementZone;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Modelo de vocação econômica e dados comerciais de assentamentos (vilas).
 * Analisa as estações de trabalho de profissões de aldeões ou infere deterministicamente
 * o perfil comercial e os itens de maior destaque para exibição em tooltips do mapa e interfaces.
 */
public class SettlementCommercialData {

    public enum SettlementVocation {
        AGRICULTURE("Agropecuária", "Agriculture"),
        METALLURGY("Metalurgia e Armaria", "Metallurgy & Smithing"),
        FISHING("Pescaria e Navegação", "Fishing & Navigation"),
        SCHOLARSHIP("Alquimia e Conhecimento", "Alchemy & Scholarship"),
        CRAFTING("Manufatura e Alvenaria", "Crafting & Masonry"),
        GENERAL_COMMERCE("Comércio Geral", "General Goods");

        private final String ptBr;
        private final String enUs;

        SettlementVocation(String ptBr, String enUs) {
            this.ptBr = ptBr;
            this.enUs = enUs;
        }

        public String getDisplayName(String locale) {
            if (locale != null && locale.toLowerCase().startsWith("pt")) {
                return ptBr;
            }
            return enUs;
        }

        public static SettlementVocation fromName(String name) {
            if (name == null || name.isEmpty()) return GENERAL_COMMERCE;
            for (SettlementVocation v : values()) {
                if (v.name().equalsIgnoreCase(name) || v.ptBr.equalsIgnoreCase(name) || v.enUs.equalsIgnoreCase(name)) {
                    return v;
                }
            }
            return GENERAL_COMMERCE;
        }
    }

    public static final Map<String, SettlementVocation> WORKSTATION_MAPPING = Map.ofEntries(
            Map.entry("minecraft:composter", SettlementVocation.AGRICULTURE),
            Map.entry("minecraft:smoker", SettlementVocation.AGRICULTURE),
            Map.entry("minecraft:blast_furnace", SettlementVocation.METALLURGY),
            Map.entry("minecraft:grindstone", SettlementVocation.METALLURGY),
            Map.entry("minecraft:smithing_table", SettlementVocation.METALLURGY),
            Map.entry("minecraft:barrel", SettlementVocation.FISHING),
            Map.entry("minecraft:cartography_table", SettlementVocation.FISHING),
            Map.entry("minecraft:lectern", SettlementVocation.SCHOLARSHIP),
            Map.entry("minecraft:brewing_stand", SettlementVocation.SCHOLARSHIP),
            Map.entry("minecraft:stonecutter", SettlementVocation.CRAFTING),
            Map.entry("minecraft:loom", SettlementVocation.CRAFTING),
            Map.entry("minecraft:cauldron", SettlementVocation.CRAFTING),
            Map.entry("minecraft:fletching_table", SettlementVocation.CRAFTING)
    );

    private String primaryVocation;
    private String secondaryVocation;
    private List<String> highlightedTradeItems;
    private Map<String, Integer> workstationCounts;

    public SettlementCommercialData() {
        this("Comércio Geral", "", Collections.emptyList(), Collections.emptyMap());
    }

    public SettlementCommercialData(
            String primaryVocation,
            String secondaryVocation,
            List<String> highlightedTradeItems,
            Map<String, Integer> workstationCounts
    ) {
        this.primaryVocation = primaryVocation != null ? primaryVocation : "Comércio Geral";
        this.secondaryVocation = secondaryVocation != null ? secondaryVocation : "";
        this.highlightedTradeItems = highlightedTradeItems != null ? new ArrayList<>(highlightedTradeItems) : new ArrayList<>();
        this.workstationCounts = workstationCounts != null ? new HashMap<>(workstationCounts) : new HashMap<>();
    }

    public String getPrimaryVocation() {
        return primaryVocation;
    }

    public void setPrimaryVocation(String primaryVocation) {
        this.primaryVocation = primaryVocation != null ? primaryVocation : "Comércio Geral";
    }

    public String getSecondaryVocation() {
        return secondaryVocation;
    }

    public void setSecondaryVocation(String secondaryVocation) {
        this.secondaryVocation = secondaryVocation != null ? secondaryVocation : "";
    }

    public List<String> getHighlightedTradeItems() {
        return Collections.unmodifiableList(highlightedTradeItems);
    }

    public void setHighlightedTradeItems(List<String> highlightedTradeItems) {
        this.highlightedTradeItems = highlightedTradeItems != null ? new ArrayList<>(highlightedTradeItems) : new ArrayList<>();
    }

    public Map<String, Integer> getWorkstationCounts() {
        return Collections.unmodifiableMap(workstationCounts);
    }

    public void setWorkstationCounts(Map<String, Integer> workstationCounts) {
        this.workstationCounts = workstationCounts != null ? new HashMap<>(workstationCounts) : new HashMap<>();
    }

    public String getFormattedDescription() {
        if (secondaryVocation != null && !secondaryVocation.trim().isEmpty()) {
            return primaryVocation + " / " + secondaryVocation;
        }
        return primaryVocation;
    }

    public String getTradeItemsSummary() {
        if (highlightedTradeItems.isEmpty()) {
            return "";
        }
        return String.join(", ", highlightedTradeItems);
    }

    public static List<String> getDefaultTradeItems(SettlementVocation vocation, String locale) {
        boolean isPt = locale != null && locale.toLowerCase().startsWith("pt");
        return switch (vocation) {
            case AGRICULTURE -> isPt
                    ? List.of("Pão", "Trigo", "Carne Bovina", "Cenouras")
                    : List.of("Bread", "Wheat", "Beef", "Carrots");
            case METALLURGY -> isPt
                    ? List.of("Armaduras de Ferro", "Espadas", "Ferramentas", "Lingotes de Ferro")
                    : List.of("Iron Armor", "Swords", "Tools", "Iron Ingots");
            case FISHING -> isPt
                    ? List.of("Peixe Fresco", "Barris", "Mapas do Tesouro", "Bússolas")
                    : List.of("Fresh Fish", "Barrels", "Treasure Maps", "Compasses");
            case SCHOLARSHIP -> isPt
                    ? List.of("Livros Encantados", "Poções", "Papel", "Redstone")
                    : List.of("Enchanted Books", "Potions", "Paper", "Redstone");
            case CRAFTING -> isPt
                    ? List.of("Pedras Talhadas", "Tijolos", "Lã Colorida", "Couro", "Flechas")
                    : List.of("Cut Stone", "Bricks", "Dyed Wool", "Leather", "Arrows");
            case GENERAL_COMMERCE -> isPt
                    ? List.of("Suprimentos Variados", "Esmeraldas", "Cereais")
                    : List.of("Assorted Supplies", "Emeralds", "Grains");
        };
    }

    public static SettlementCommercialData estimateFromWorkstations(Map<String, Integer> counts, String locale) {
        Map<SettlementVocation, Integer> score = new EnumMap<>(SettlementVocation.class);
        for (SettlementVocation v : SettlementVocation.values()) {
            score.put(v, 0);
        }

        if (counts != null) {
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                String blockId = entry.getKey();
                int c = entry.getValue();
                SettlementVocation voc = WORKSTATION_MAPPING.get(blockId);
                if (voc != null) {
                    score.put(voc, score.get(voc) + c);
                }
            }
        }

        List<Map.Entry<SettlementVocation, Integer>> sorted = new ArrayList<>(score.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        SettlementVocation primary = SettlementVocation.GENERAL_COMMERCE;
        SettlementVocation secondary = null;

        if (!sorted.isEmpty() && sorted.get(0).getValue() > 0) {
            primary = sorted.get(0).getKey();
            if (sorted.size() > 1 && sorted.get(1).getValue() > 0 && sorted.get(1).getKey() != primary) {
                secondary = sorted.get(1).getKey();
            }
        }

        String primaryName = primary.getDisplayName(locale);
        String secondaryName = secondary != null ? secondary.getDisplayName(locale) : "";

        List<String> items = new ArrayList<>(getDefaultTradeItems(primary, locale));
        if (secondary != null) {
            List<String> secItems = getDefaultTradeItems(secondary, locale);
            if (!secItems.isEmpty()) {
                items.add(secItems.get(0));
            }
        }

        return new SettlementCommercialData(primaryName, secondaryName, items, counts != null ? counts : Collections.emptyMap());
    }

    public static SettlementCommercialData createDeterministic(long seed, int centerX, int centerZ, String locale) {
        long combinedSeed = (seed * 31L) ^ ((((long) centerX) << 32) | (centerZ & 0xFFFFFFFFL));
        Random random = new Random(combinedSeed);

        SettlementVocation[] vocations = {
                SettlementVocation.AGRICULTURE,
                SettlementVocation.METALLURGY,
                SettlementVocation.FISHING,
                SettlementVocation.SCHOLARSHIP,
                SettlementVocation.CRAFTING
        };

        SettlementVocation primary = vocations[random.nextInt(vocations.length)];
        SettlementVocation secondary = null;
        if (random.nextInt(100) < 50) {
            SettlementVocation candidate = vocations[random.nextInt(vocations.length)];
            if (candidate != primary) {
                secondary = candidate;
            }
        }

        String primaryName = primary.getDisplayName(locale);
        String secondaryName = secondary != null ? secondary.getDisplayName(locale) : "";

        List<String> items = new ArrayList<>(getDefaultTradeItems(primary, locale));
        if (secondary != null) {
            List<String> secItems = getDefaultTradeItems(secondary, locale);
            if (!secItems.isEmpty()) {
                items.add(secItems.get(0));
            }
        }

        return new SettlementCommercialData(primaryName, secondaryName, items, Collections.emptyMap());
    }

    public static SettlementCommercialData estimateFromZone(
            LevelAccessor level,
            SettlementZone zone,
            String locale,
            long seed,
            int centerX,
            int centerZ
    ) {
        if (level == null || zone == null || zone.getPieces().isEmpty()) {
            return createDeterministic(seed, centerX, centerZ, locale);
        }

        Map<String, Integer> workstationCounts = new HashMap<>();
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

                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            BlockPos p = new BlockPos(x, y, z);
                            BlockState state = level.getBlockState(p);
                            Block block = state.getBlock();
                            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
                            if (key != null) {
                                String id = key.toString();
                                if (WORKSTATION_MAPPING.containsKey(id)) {
                                    workstationCounts.put(id, workstationCounts.getOrDefault(id, 0) + 1);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
            // Fallback seguro em caso de exceções durante verificação de blocos
        }

        if (workstationCounts.isEmpty()) {
            return createDeterministic(seed, centerX, centerZ, locale);
        }

        return estimateFromWorkstations(workstationCounts, locale);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("primary_vocation", primaryVocation);
        if (secondaryVocation != null && !secondaryVocation.isEmpty()) {
            tag.putString("secondary_vocation", secondaryVocation);
        }

        ListTag itemsList = new ListTag();
        for (String item : highlightedTradeItems) {
            itemsList.add(StringTag.valueOf(item));
        }
        tag.put("highlighted_trade_items", itemsList);

        CompoundTag countsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : workstationCounts.entrySet()) {
            countsTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("workstation_counts", countsTag);

        return tag;
    }

    public static SettlementCommercialData load(CompoundTag tag) {
        if (tag == null) {
            return new SettlementCommercialData();
        }

        String primary = tag.getString("primary_vocation");
        String secondary = tag.getString("secondary_vocation");

        List<String> items = new ArrayList<>();
        if (tag.contains("highlighted_trade_items", Tag.TAG_LIST)) {
            ListTag list = tag.getList("highlighted_trade_items", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                items.add(list.getString(i));
            }
        }

        Map<String, Integer> counts = new HashMap<>();
        if (tag.contains("workstation_counts", Tag.TAG_COMPOUND)) {
            CompoundTag cTag = tag.getCompound("workstation_counts");
            for (String key : cTag.getAllKeys()) {
                counts.put(key, cTag.getInt(key));
            }
        }

        return new SettlementCommercialData(primary, secondary, items, counts);
    }
}

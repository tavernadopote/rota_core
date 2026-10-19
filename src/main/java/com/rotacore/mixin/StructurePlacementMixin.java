package com.rotacore.mixin;

import com.rotacore.RotaCore;
import com.rotacore.config.RotaCoreConfig;
import com.rotacore.world.zoning.RegionalGrid;
import com.rotacore.world.zoning.StructureZoneRegistry;
import com.rotacore.world.zoning.ZoneType;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepta o cálculo determinístico de elegibilidade de chunks para estruturas.
 * Permite que vilas (vanilla ou mods como CTOV) e postos militares sejam orquestrados pelo
 * Zoneamento Regional do Rota Core, garantindo distribuição lógica, relevo ideal e sem sobreposições.
 */
@Mixin(StructurePlacement.class)
public class StructurePlacementMixin {

    @Inject(method = "isStructureChunk", at = @At("HEAD"), cancellable = true)
    private void rota$interceptStructureChunk(
            ChunkGeneratorStructureState structureState,
            int chunkX,
            int chunkZ,
            CallbackInfoReturnable<Boolean> cir
    ) {
        RotaCoreConfig config = RotaCore.getConfig();
        if (config == null || !config.enableControlledWorldgen) {
            return;
        }

        StructurePlacement placement = (StructurePlacement) (Object) this;
        ZoneType zoneType = StructureZoneRegistry.getZoneType(structureState, placement);

        if (zoneType == ZoneType.SETTLEMENT) {
            boolean isVillageChunk = RegionalGrid.isCandidateVillageChunk(
                    structureState.getLevelSeed(),
                    chunkX,
                    chunkZ,
                    config
            );
            cir.setReturnValue(isVillageChunk);
        } else if (zoneType == ZoneType.OUTPOST) {
            boolean isOutpostChunk = RegionalGrid.isCandidateOutpostChunk(
                    structureState.getLevelSeed(),
                    chunkX,
                    chunkZ,
                    config
            );
            cir.setReturnValue(isOutpostChunk);
        }
    }
}

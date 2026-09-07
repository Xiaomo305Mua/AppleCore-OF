package squeek.applecore.mixins.early.minecraft;

import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockMushroom;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import squeek.applecore.api.AppleCoreAPI;
import squeek.applecore.api.plants.PlantGrowthResult;

@Mixin(BlockMushroom.class)
public class BlockMushroomMixin extends BlockBush {

    @Unique
    private boolean appleCore$executedCondition;

    @ModifyExpressionValue(
            method = "updateTick",
            at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0))
    private int onUpdateTick(int original, World worldIn, int x, int y, int z, Random random) {
        PlantGrowthResult result = AppleCoreAPI.dispatcher.validatePlantGrowth(this, worldIn, x, y, z, random);
        if (result == PlantGrowthResult.ALLOW) {
            this.appleCore$executedCondition = true;
            return 0;
        } else if (result == PlantGrowthResult.DEFAULT) {
            this.appleCore$executedCondition = original == 0;
            return original;
        } else { // DENY
            this.appleCore$executedCondition = false;
            return -1;
        }
    }

    @Inject(method = "updateTick", at = @At("RETURN"))
    private void afterUpdateTick(World worldIn, int x, int y, int z, Random random, CallbackInfo ci) {
        if (this.appleCore$executedCondition) {
            AppleCoreAPI.dispatcher.announcePlantGrowthWithoutMetadataChange(this, worldIn, x, y, z);
        }
        this.appleCore$executedCondition = false;
    }
}

package com.bunubbv.gatekeeper.fabric.mixin;

import com.bunubbv.gatekeeper.fabric.PlayerState;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void preventDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "trySleep", at = @At("HEAD"), cancellable = true)
    private void preventSleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        if (!PlayerState.check(player)) {
            cir.setReturnValue(Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM));
        }
    }

    @Inject(method = "updateKilledAdvancementCriterion", at = @At("HEAD"), cancellable = true)
    private void preventKillCredit(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "openHandledScreen", at = @At("HEAD"), cancellable = true)
    private void preventOpenScreen(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            cir.setReturnValue(OptionalInt.empty());
        }
    }

    @Inject(method = "openHorseInventory", at = @At("HEAD"), cancellable = true)
    private void preventOpenHorseScreen(AbstractHorseEntity horse, Inventory inventory, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "openCommandBlockScreen", at = @At("HEAD"), cancellable = true)
    private void preventOpenCommandBlock(CommandBlockBlockEntity commandBlock, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "useBook", at = @At("HEAD"), cancellable = true)
    private void preventOpenBook(ItemStack book, Hand hand, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "openEditSignScreen", at = @At("HEAD"), cancellable = true)
    private void preventEditSign(SignBlockEntity sign, boolean front, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "sendTradeOffers", at = @At("HEAD"), cancellable = true)
    private void preventTrading(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void preventItemDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            int selected = player.getInventory().getSelectedSlot();
            ItemStack stack = player.getInventory().getStack(selected);

            player.currentScreenHandler.getSlotIndex(player.getInventory(), selected)
                    .ifPresent(index -> player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(
                            player.currentScreenHandler.syncId,
                            player.currentScreenHandler.nextRevision(),
                            index,
                            stack
                    )));

            cir.setReturnValue(false);
        }
    }

    @Inject(method = "consumeItem", at = @At("HEAD"), cancellable = true)
    private void preventItemUsage(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "tickItemStackUsage", at = @At("HEAD"), cancellable = true)
    private void preventItemUsageTick(ItemStack stack, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onRecipeCrafted", at = @At("HEAD"), cancellable = true)
    private void preventCraftingSync(RecipeEntry<?> recipe, List<ItemStack> ingredients, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;

        if (!PlayerState.check(player)) {
            ci.cancel();
        }
    }
}

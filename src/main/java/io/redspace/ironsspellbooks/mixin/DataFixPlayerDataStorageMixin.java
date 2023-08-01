package io.redspace.ironsspellbooks.mixin;

import com.mojang.datafixers.DataFixer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.datafix.IronsTagTraverser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

@Mixin(PlayerDataStorage.class)
public abstract class DataFixPlayerDataStorageMixin {
    @Shadow
    @Final
    private File playerDir;

    @Unique
    private static final Object iron_sSpells_nSpellbooks$sync = new Object();

    @Inject(method = "load", at = @At("HEAD"))
    private void load(Player pPlayer, CallbackInfoReturnable<CompoundTag> cir) {

        File file1 = new File(this.playerDir, pPlayer.getStringUUID() + ".dat");
        if (file1.exists() && file1.isFile()) {
            try {
                synchronized (iron_sSpells_nSpellbooks$sync) {
                    var compoundTag1 = NbtIo.readCompressed(file1);
                    var compoundTag2 = compoundTag1.getList("Inventory", Tag.TAG_COMPOUND);

                    var ironsTraverser = new IronsTagTraverser();
                    ironsTraverser.visit(compoundTag2);

                    if (ironsTraverser.changesMade()) {
                        NbtIo.writeCompressed(compoundTag1, file1);
                        IronsSpellbooks.LOGGER.debug("DataFixPlayerDataStorageMixin: Single player inventory updated: {} updates", ironsTraverser.totalChanges());
                    }
                }
            } catch (Exception exception) {
                IronsSpellbooks.LOGGER.debug("DataFixPlayerDataStorageMixin: Failed to load player data for {}", pPlayer.getName().getString());
            }
        }
    }
}

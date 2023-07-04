package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.SpellType;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class LobCreeperSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "lob_creeper");

    public LobCreeperSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchool(SchoolType.EVOCATION)
            .setMaxLevel(10)
            .setCooldownSeconds(2)
            .build();

    public LobCreeperSpell(int level) {
        super(SpellType.LOB_CREEPER_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.CREEPER_HURT);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, MagicData playerMagicData) {
        float speed = (6 + this.getLevel(entity)) * .1f;
        float damage = getDamage(entity);
        CreeperHeadProjectile head = new CreeperHeadProjectile(entity, level, speed, damage);
        Vec3 spawn = entity.getEyePosition().add(entity.getForward());
        head.moveTo(spawn.x, spawn.y - head.getBoundingBox().getYsize() / 2, spawn.z, entity.getYRot() + 180, entity.getXRot());
        level.addFreshEntity(head);
        super.onCast(level, entity, playerMagicData);
    }

    private float getDamage(LivingEntity entity) {
        return this.getSpellPower(entity) * .5f;
    }
}

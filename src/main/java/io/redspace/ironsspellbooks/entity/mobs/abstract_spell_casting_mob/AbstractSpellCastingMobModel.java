package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSpellCastingMobModel extends AnimatedGeoModel<AbstractSpellCastingMob> {
    protected TransformStack transformStack = new TransformStack();

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return AbstractSpellCastingMob.modelResource;
    }

    @Override
    public abstract ResourceLocation getTextureResource(AbstractSpellCastingMob mob);

    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return AbstractSpellCastingMob.animationInstantCast;
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
        if (Minecraft.getInstance().isPaused() || !entity.shouldBeExtraAnimated())
            return;

        float partialTick = animationEvent.getPartialTick();
        /*
                This overrides all other animation
         */
        IBone head = this.getAnimationProcessor().getBone(PartNames.HEAD);
        IBone body = this.getAnimationProcessor().getBone(PartNames.BODY);
        IBone torso = this.getAnimationProcessor().getBone("torso");
        IBone rightArm = this.getAnimationProcessor().getBone(PartNames.RIGHT_ARM);
        IBone leftArm = this.getAnimationProcessor().getBone(PartNames.LEFT_ARM);
        IBone rightLeg = this.getAnimationProcessor().getBone(PartNames.RIGHT_LEG);
        IBone leftLeg = this.getAnimationProcessor().getBone(PartNames.LEFT_LEG);

        /*
            Head Controls
         */
        //Make the head look forward, whatever forward is (influenced externally, such as a lootAt target)

        if (!entity.isAnimating() || entity.shouldAlwaysAnimateHead()) {
            transformStack.pushRotation(head,
                    Mth.lerp(partialTick, -entity.xRotO, -entity.getXRot()) * Mth.DEG_TO_RAD,
                    Mth.lerp(partialTick,
                            Mth.wrapDegrees(-entity.yHeadRotO + entity.yBodyRotO) * Mth.DEG_TO_RAD,
                            Mth.wrapDegrees(-entity.yHeadRot + entity.yBodyRot) * Mth.DEG_TO_RAD
                    ),
                    0);
        }
        /*
            Crazy Vanilla Magic Calculations (LivingEntityRenderer:116 & HumanoidModel#setupAnim
         */
        float pLimbSwingAmount = 0.0F;
        float pLimbSwing = 0.0F;
        if (entity.isAlive()) {
            pLimbSwingAmount = Mth.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed);
            pLimbSwing = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
            if (entity.isBaby()) {
                pLimbSwing *= 3.0F;
            }

            if (pLimbSwingAmount > 1.0F) {
                pLimbSwingAmount = 1.0F;
            }
        }
        float f = 1.0F;
        if (entity.getFallFlyingTicks() > 4) {
            f = (float) entity.getDeltaMovement().lengthSqr();
            f /= 0.2F;
            f *= f * f;
        }

        if (f < 1.0F) {
            f = 1.0F;
        }
        /*
            Leg Controls
         */
        if (entity.isPassenger() && entity.getVehicle().shouldRiderSit()) {
            //If we are riding something, pose ourselves sitting
            transformStack.pushRotation(rightLeg,
                    1.4137167F,
                    -(float) Math.PI / 10F,
                    -0.07853982F
            );
            transformStack.pushRotation(leftLeg,
                    1.4137167F,
                    (float) Math.PI / 10F,
                    0.07853982F
            );
        } else if (!entity.isAnimating() || entity.shouldAlwaysAnimateLegs()) {
            float strength = .75f;
            Vec3 facing = entity.getForward().multiply(1, 0, 1).normalize();
            Vec3 momentum = entity.getDeltaMovement().multiply(1, 0, 1).normalize();
            Vec3 facingOrth = new Vec3(-facing.z, 0, facing.x);
            float directionForward = (float) facing.dot(momentum);
            float directionSide = (float) facingOrth.dot(momentum) * .35f; //scale side to side movement so they dont rip off thier own legs
            float rightLateral = -Mth.sin(pLimbSwing * 0.6662F) * 4 * pLimbSwingAmount;
            float leftLateral = -Mth.sin(pLimbSwing * 0.6662F - Mth.PI) * 4 * pLimbSwingAmount;
            transformStack.pushPosition(rightLeg, rightLateral * directionSide, Mth.cos(pLimbSwing * 0.6662F) * 4 * strength * pLimbSwingAmount, rightLateral * directionForward);
            transformStack.pushRotation(rightLeg, Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount * strength, 0, 0);

            transformStack.pushPosition(leftLeg, leftLateral * directionSide, Mth.cos(pLimbSwing * 0.6662F - Mth.PI) * 4 * strength * pLimbSwingAmount, leftLateral * directionForward);
            transformStack.pushRotation(leftLeg, Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount * strength, 0, 0);

            if (entity.bobBodyWhileWalking()) {
                transformStack.pushPosition(body, 0, Mth.abs(Mth.cos((pLimbSwing * 1.2662F - Mth.PI * .5f) * .5f)) * 2 * strength * pLimbSwingAmount, 0);
            }
        }
        /*
            Arm Controls
         */
        if (!entity.isAnimating()) {
            transformStack.pushRotationWithBase(rightArm, Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f, 0, 0);
            transformStack.pushRotationWithBase(leftArm, Mth.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / f, 0, 0);
            bobBone(rightArm, entity.tickCount, 1);
            bobBone(leftArm, entity.tickCount, -1);
            if (entity.isDrinkingPotion()) {
                transformStack.pushRotation(entity.isLeftHanded() ? leftArm : rightArm,
                        35 * Mth.DEG_TO_RAD,
                        (entity.isLeftHanded() ? -25 : 25) * Mth.DEG_TO_RAD,
                        (entity.isLeftHanded() ? 15 : -15) * Mth.DEG_TO_RAD
                );
            }
        } else if (entity.shouldPointArmsWhileCasting() && entity.isCasting()) {
            transformStack.pushRotationWithBase(rightArm, -entity.getXRot() * Mth.DEG_TO_RAD, 0, 0);
            transformStack.pushRotationWithBase(leftArm, -entity.getXRot() * Mth.DEG_TO_RAD, 0, 0);
        }

        transformStack.popStack();
    }

    protected void bobBone(IBone bone, int offset, float multiplier) {
        float z = multiplier * (Mth.cos(offset * 0.09F) * 0.05F + 0.05F);
        float x = multiplier * Mth.sin(offset * 0.067F) * 0.05F;
        transformStack.pushRotation(bone, x, 0, z);
    }




}
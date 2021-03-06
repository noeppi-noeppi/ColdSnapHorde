package com.jedijoe.coldsnaphorde.Entities.Mobs;

import com.jedijoe.coldsnaphorde.ColdSnapHorde;
import com.jedijoe.coldsnaphorde.Register;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public class ColdSnapStabber extends MonsterEntity implements IAnimatable {
    private int animation;
    private AnimationFactory factory = new AnimationFactory(this);
    public ColdSnapStabber(EntityType<? extends MonsterEntity> type, World worldIn) { super(type, worldIn); animation = 0;}

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 0.5D));
        this.targetSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1D, false));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.5F));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAttack));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, VillagerEntity.class, 10, true, false, this::shouldAttack));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, SnowGolemEntity.class, 10, true, false, this::shouldAttack));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, 10, true, false, this::shouldAttack));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, BlazeEntity.class, 10, true, false, this::shouldAttack));

    }

    public static AttributeModifierMap.MutableAttribute customAttributes() {
        return MobEntity.func_233666_p_()
                .createMutableAttribute(Attributes.MAX_HEALTH, 20.0D)
                .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.5D)
                .createMutableAttribute(Attributes.ATTACK_DAMAGE, 2D);
    }

    public boolean shouldAttack(@Nullable LivingEntity entity){
        if (entity == null || entity.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem().equals(Register.TOPHAT.get().getItem())){
            return false;
        }else return true;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (entityIn instanceof LivingEntity && !this.world.isRemote()){
            int chance = rand.nextInt(100);
            if (chance <= 6){((LivingEntity) entityIn).addPotionEffect(new EffectInstance(Effects.NAUSEA, 10*20, 0));}
        }
        animation = 100;
        return super.attackEntityAsMob(entityIn);
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SNOW_GOLEM_AMBIENT;
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_SNOW_GOLEM_HURT;
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SNOW_GOLEM_DEATH;
    }

    public void livingTick() {
        super.livingTick();
        if (animation > 0) animation -= 1;
        if (!this.world.isRemote) {
            int i = MathHelper.floor(this.getPosX());
            int j = MathHelper.floor(this.getPosY());
            int k = MathHelper.floor(this.getPosZ());
            if (shouldOverHeat(this.world.getBiome(this.getPosition()).getTemperature(), ColdSnapHorde.cconfig.HEATPROT.get())) {
                this.attackEntityFrom(DamageSource.ON_FIRE, 1.0F);
            }

            if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
                return;
            }

            BlockState blockstate = Blocks.SNOW.getDefaultState();

            for(int l = 0; l < 4; ++l) {
                i = MathHelper.floor(this.getPosX() + (double)((float)(l % 2 * 2 - 1) * 0.25F));
                j = MathHelper.floor(this.getPosY());
                k = MathHelper.floor(this.getPosZ() + (double)((float)(l / 2 % 2 * 2 - 1) * 0.25F));
                BlockPos blockpos = new BlockPos(i, j, k);
                if (this.world.isAirBlock(blockpos) && !shouldOverHeat(this.world.getBiome(this.getPosition()).getTemperature(), ColdSnapHorde.cconfig.SNOWTRAIL.get()) && blockstate.isValidPosition(this.world, blockpos)) {
                    this.world.setBlockState(blockpos, blockstate);
                }
            }
        }
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event){
        if(this.isAggressive()){
            event.getController().setAnimation(new AnimationBuilder().addAnimation("stab", true));
            return PlayState.CONTINUE; }
        else return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 10, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }


    protected boolean shouldOverHeat(float currentTemp, int protectionlevel){
        switch(protectionlevel){
            case 0:
                return currentTemp > 0.3f;
            case 1:
                return currentTemp > 0.9f;
            case 2:
                return currentTemp > 1.5f;
            case 3:
                return false;
            default:
                return true;
        }
    }
}

package com.flansmod.common.projectiles;

import com.flansmod.client.render.FirstPersonManager;
import com.flansmod.common.FlansMod;
import com.flansmod.common.FlansModConfig;
import com.flansmod.common.actions.ActionGroupInstance;
import com.flansmod.common.actions.ActionInstance;
import com.flansmod.common.actions.contexts.*;
import com.flansmod.common.actions.nodes.EjectCasingAction;
import com.flansmod.common.network.FlansEntityDataSerializers;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.bullets.elements.ProjectileDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.guns.elements.ActionDefinition;
import com.flansmod.common.types.guns.elements.EActionType;
import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;
import com.flansmod.physics.common.util.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class CasingEntity extends Projectile {

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(CasingEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_SHOOTER_UUID = SynchedEntityData.defineId(CasingEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_GUN_ID = SynchedEntityData.defineId(CasingEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_ACTION_GROUP_PATH_HASH = SynchedEntityData.defineId(CasingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFETIME = SynchedEntityData.defineId(CasingEntity.class, EntityDataSerializers.INT);

    public int lifeTime = 0;
    public boolean initialized = false;
    public Vec3 firstPersonOffset = Vec3.ZERO;
    public String itemToSpawn = "";

    public void SetOwnerID(@Nonnull UUID ownerID) { entityData.set(DATA_OWNER_UUID, Optional.of(ownerID)); }
    public void SetShooterID(@Nonnull UUID shooterID) { entityData.set(DATA_SHOOTER_UUID, Optional.of(shooterID)); }
    public void SetGunID(@Nonnull UUID gunID) { entityData.set(DATA_GUN_ID, Optional.of(gunID)); }
    public void SetActionGroupPathHash(int hash) { entityData.set(DATA_ACTION_GROUP_PATH_HASH, hash); }

    @Nonnull
    public UUID GetOwnerID() { return entityData.get(DATA_OWNER_UUID).orElse(ShooterContext.InvalidID); }
    @Nonnull
    public UUID GetShooterID() { return entityData.get(DATA_SHOOTER_UUID).orElse(ShooterContext.InvalidID); }
    @Nonnull
    public UUID GetGunID() { return entityData.get(DATA_GUN_ID).orElse(GunContext.INVALID.GetUUID()); }
    public int GetActionGroupPathHash() { return entityData.get(DATA_ACTION_GROUP_PATH_HASH); }
    public void SetLifeTime(int index) { entityData.set(DATA_LIFETIME, index); }
    public int GetLifeTime() { return entityData.get(DATA_LIFETIME); }

    public GunDefinition GetGunDef(){
        return GetContext().Def;
    }

        public EjectCasingAction Action;
    @Nonnull
    public GunContext GetContext()
    {
        return ShooterContext.of(GetShooterID(), GetOwnerID()).CreateContext(GetGunID());
    }

    // TODO: These entity accessors need to check all entities
    @Nullable
    public Entity Owner() { return level().getPlayerByUUID(GetOwnerID()); }
    @Nullable
    public Entity GetShooter() { return level().getPlayerByUUID(GetOwnerID()); }


    public CasingEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_OWNER_UUID, Optional.empty());
        entityData.define(DATA_SHOOTER_UUID, Optional.empty());
        entityData.define(DATA_GUN_ID, Optional.empty());
        entityData.define(DATA_ACTION_GROUP_PATH_HASH, 0);
        entityData.define(DATA_LIFETIME, 0);
    }

    public void InitContext(@Nonnull ActionGroupContext context)
    {
        SetOwnerID(context.Gun.GetShooter().OwnerUUID());
        SetShooterID(context.Gun.GetShooter().EntityUUID());
        SetGunID(context.Gun.GetUUID());
        SetActionGroupPathHash(context.GroupPath.hashCode());
        lifeTime = 0;
        initialized = false;
    }


    //Some absolute bullshit we have to do to try and get this to render properly in first person.
    //Shoot me.
    //Also some absolute bullshit we have to do to get this to spawn in the right place because we cannot call any helper functions on the server thread
    public void InitOffset(){

        ShooterContext shooter = ShooterContext.of(GetShooterID(), GetOwnerID());
        GunContext gun = shooter.CreateContext(GetGunID());
        ActionGroupContext actionGroup = gun.GetActionGroupContextByHash(GetActionGroupPathHash());

        if(Action == null){
            for(ActionDefinition def : actionGroup.Def.actions){
                if(def.actionType == EActionType.EjectCasing)
                    Action = new EjectCasingAction(new ActionGroupInstance(actionGroup),def);
            }
        }

        if(Action== null) return;

        if(GetContext() instanceof GunContextPlayer playerGunContext) {
            ItemDisplayContext transformType;
            transformType = MinecraftHelpers.getFirstPersonTransformType(playerGunContext.GetHand());

            Transform origin = FirstPersonManager.GetWorldSpaceAPTransform(GetContext(), transformType, ActionGroupContext.CreateGroupPath(Action.AttachPoint()));
            TransformStack transformStack = TransformStack.empty();
            transformStack.add(origin);
            Vec3 position = transformStack.top().positionVec3();
            Vec3 position2 = this.position();
            firstPersonOffset = new Vec3(position.x()-position2.x(),position.y()-position2.y(),position.z()-position2.z());
        }

        if(true) return;

        if(GetContext() instanceof GunContextPlayer playerGunContext){
            GunContext gunContext = GetContext();

            ItemDisplayContext transformType;
            if(!Minecraft.getInstance().options.getCameraType().isFirstPerson() || !gunContext.GetShooter().IsLocalPlayerOwner())
            {
                transformType = MinecraftHelpers.getThirdPersonTransformType(gunContext.GetShooter().IsLocalPlayerOwner(), playerGunContext.GetHand());
            }
            else
            {
                transformType = MinecraftHelpers.getFirstPersonTransformType(playerGunContext.GetHand());
            }


            TransformStack transformStack = TransformStack.empty();
            Transform origin = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath(Action.AttachPoint()));
            Transform direction = FirstPersonManager.GetWorldSpaceAPTransform(gunContext, transformType, ActionGroupContext.CreateGroupPath(Action.EjectDirection()));

            transformStack = TransformStack.empty();
            transformStack.add(direction);
            Vec3 look = transformStack.top().forward();

            float speed = (float) Action.EjectSpeed();

            transformStack = TransformStack.empty();
            transformStack.add(origin);
            Vec3 position = transformStack.top().positionVec3();
            Vec3 velocity = look.scale(speed * (0.8f+(Math.random()*0.4f)));
            //Gunshot shot = getShot().GetResults(triggerIndex).Get(0);
            //if(shot == null) return;
            float speed2 = (float) (speed * (0.8f+(Math.random()*0.4f)));
            SetVelocity(velocity);
            setPos(position);
            RecalculateFacing(transformStack.top().forward());
            setOldPosAndRot();
            initialized = true;
        }

    }

    public void addAdditionalSaveData(CompoundTag tags)
    {
        //tags.putString("bullet", GetBulletDef().Location.toString());
        CompoundTag contextTags = new CompoundTag();
        GetContext().Save(contextTags);
        tags.put("context", contextTags);
        tags.putInt("lifeTime",lifeTime);
    }

    public void readAdditionalSaveData(CompoundTag tags)
    {
        if(tags.contains("bullet"))
        {
            //SetBulletDef(FlansMod.BULLETS.Get(new ResourceLocation(tags.getString("bullet"))));
        }
        if(tags.contains("context")) {
            ActionGroupContext context = ActionGroupContext.Load(tags.getCompound("context"), level().isClientSide);
            if (context != null)
                InitContext(context);
        }
        else{
            kill();
        }
        lifeTime = tags.getInt("lifeTime");

        CompoundTag tag = new CompoundTag();


    }

    public void RecalculateFacing(Vec3 direction)
    {
        double xz = Maths.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yawDeg = (float)Maths.atan2(direction.x, direction.z) * Maths.RadToDegF;
        float pitchDeg = (float)Maths.atan2(direction.y, xz) * Maths.RadToDegF;

        setXRot(pitchDeg);
        setYRot(yawDeg);

        setOldPosAndRot();
    }

    public void SetVelocity(Vec3 velocity)
    {
        setDeltaMovement(velocity);

        setOldPosAndRot();

        //RefreshLockOnTarget();
    }

    @Override
    public void tick(){
        super.tick();
        lifeTime = GetLifeTime();
        if(level().isClientSide &&lifeTime==0){
            InitOffset();
        }


        Vec3 motion = getDeltaMovement();
        motion = ApplyGravity(motion);
        Spin();
        setDeltaMovement(motion);
        move(MoverType.SELF, motion);
        if(!level().isClientSide())
            HitGround();
        lifeTime++;
        SetLifeTime(lifeTime);
    }

    protected Vec3 ApplyGravity(Vec3 motion)
    {
        //TODO, determine proper gravity value
        return new Vec3(
                motion.x,
                motion.y - 2f * 0.02d,
                motion.z);
    }

    public void Spin(){
        //TODO: Gradually alter pitch/yaw.
    }

    public void HitGround(){
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::CanHitEntity);
        if(hitResult.getType() == HitResult.Type.BLOCK){
            //kill entity, spawn bullet casing if specified
            Die();
        }
        if(onGround())
        {
            Die();
        }
    }

    public void Die(){
        if(itemToSpawn != null && !itemToSpawn.isEmpty())
        {
            Item i = BuiltInRegistries.ITEM.get(new ResourceLocation(itemToSpawn));
            if(i != null && i != Items.AIR && FlansModConfig.AllowBulletCasingDrops.get()) {
                ItemStack casing = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(itemToSpawn)), 1);
                ItemEntity created =
                        new ItemEntity(level(), getX(), getY(), getZ(), casing);

                created.setDefaultPickUpDelay();
                created.setDefaultPickUpDelay();
                //created.setDeltaMovement(VecHelper.offsetRandomly(new Vec3(0,0.5f,0), level().random, .05f));
                level().addFreshEntity(created);
            }
        }

        if(!level().isClientSide)
            kill();
    }

    private boolean CanHitEntity(Entity entity) {
        return true;
    }


}

package com.flansmod.common.network;

import com.flansmod.common.FlansMod;
import com.flansmod.common.actions.contexts.ActionGroupContext;
import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.GunshotContext;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.common.entity.vehicle.PerPartMap;
import com.flansmod.common.entity.vehicle.save.DamageSyncState;
import com.flansmod.common.entity.vehicle.save.GunSyncState;
import com.flansmod.common.entity.vehicle.save.ArticulationSyncState;
import com.flansmod.common.entity.vehicle.save.EngineSyncState;
import com.flansmod.common.entity.vehicle.save.SeatSyncState;
import com.flansmod.common.types.Definitions;
import com.flansmod.common.types.JsonDefinition;
import com.flansmod.common.types.abilities.CraftingTraitDefinition;
import com.flansmod.common.types.armour.ArmourDefinition;
import com.flansmod.common.types.attachments.AttachmentDefinition;
import com.flansmod.common.types.bullets.BulletDefinition;
import com.flansmod.common.types.crafting.MaterialDefinition;
import com.flansmod.common.types.crafting.WorkbenchDefinition;
import com.flansmod.common.types.grenades.GrenadeDefinition;
import com.flansmod.common.types.guns.GunDefinition;
import com.flansmod.common.types.magazines.MagazineDefinition;
import com.flansmod.common.types.npc.NpcDefinition;
import com.flansmod.common.types.parts.PartDefinition;
import com.flansmod.common.types.vehicles.ControlSchemeDefinition;
import com.flansmod.common.types.vehicles.VehicleDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FlansEntityDataSerializers
{
	public static class DefinitionSerializer<TDefType extends JsonDefinition> implements EntityDataSerializer.ForValueType<TDefType>
	{
		public final Definitions<TDefType> Library;
		public DefinitionSerializer(@Nonnull Definitions<TDefType> lib) { Library = lib; }

		@Override
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull TDefType def) { buf.writeInt(def.hashCode()); }
		@Override @Nonnull
		public TDefType read(@Nonnull FriendlyByteBuf buf) { return Library.ByHash(buf.readInt()); }
	}

	public static final DefinitionSerializer<GunDefinition> GUN_DEF = new DefinitionSerializer<>(FlansMod.GUNS);
	public static final DefinitionSerializer<BulletDefinition> BULLET_DEF = new DefinitionSerializer<>(FlansMod.BULLETS);
	public static final DefinitionSerializer<GrenadeDefinition> GRENADE_DEF = new DefinitionSerializer<>(FlansMod.GRENADES);
	public static final DefinitionSerializer<AttachmentDefinition> ATTACHMENT_DEF = new DefinitionSerializer<>(FlansMod.ATTACHMENTS);
	public static final DefinitionSerializer<PartDefinition> PART_DEF = new DefinitionSerializer<>(FlansMod.PARTS);
	public static final DefinitionSerializer<WorkbenchDefinition> WORKBENCH_DEF = new DefinitionSerializer<>(FlansMod.WORKBENCHES);
	public static final DefinitionSerializer<MaterialDefinition> MATERIAL_DEF = new DefinitionSerializer<>(FlansMod.MATERIALS);
	public static final DefinitionSerializer<MagazineDefinition> MAGAZINE_DEF = new DefinitionSerializer<>(FlansMod.MAGAZINES);
	public static final DefinitionSerializer<NpcDefinition> NPC_DEF = new DefinitionSerializer<>(FlansMod.NPCS);
	public static final DefinitionSerializer<CraftingTraitDefinition> TRAIT_DEF = new DefinitionSerializer<>(FlansMod.TRAITS);
	public static final DefinitionSerializer<ArmourDefinition> ARMOUR_DEF = new DefinitionSerializer<>(FlansMod.ARMOURS);
	public static final DefinitionSerializer<VehicleDefinition> VEHICLE_DEF = new DefinitionSerializer<>(FlansMod.VEHICLES);
	public static final DefinitionSerializer<ControlSchemeDefinition> CONTROL_SCHEME_DEF = new DefinitionSerializer<>(FlansMod.CONTROL_SCHEMES);

	public static class PerPartMapSerializer<TDataType> implements EntityDataSerializer.ForValueType<PerPartMap<TDataType>>
	{
		public final EntityDataSerializer<TDataType> ElementSerializer;
		public PerPartMapSerializer(@Nonnull EntityDataSerializer<TDataType> element) { ElementSerializer = element; }

		@Override
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull PerPartMap<TDataType> data)
		{
			buf.writeInt(data.Values().size());
			for(var kvp : data.EntrySet())
			{
				buf.writeInt(kvp.getKey());
				ElementSerializer.write(buf, kvp.getValue());
			}
		}
		@Override
		@Nonnull
		public PerPartMap<TDataType> read(@Nonnull FriendlyByteBuf buf)
		{
			int numEntries = buf.readInt();
			PerPartMap<TDataType> map = new PerPartMap<TDataType>();
			for(int i = 0; i < numEntries; i++)
			{
				int hash = buf.readInt();
				TDataType element = ElementSerializer.read(buf);
				map.Put(hash, element);
			}
			return map;
		}
	}


	public static final EntityDataSerializer<ArticulationSyncState> ARTICULATION =
		new EntityDataSerializer.ForValueType<>()
	{
		@Override
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull ArticulationSyncState data)
		{
			buf.writeFloat(data.Parameter);
			buf.writeFloat(data.Velocity);
		}
		@Override
		@Nonnull
		public ArticulationSyncState read(@Nonnull FriendlyByteBuf buf)
		{
			float param = buf.readFloat();
			float velocity = buf.readFloat();
			return new ArticulationSyncState(param, velocity);
		}
	};
	public static final EntityDataSerializer<EngineSyncState> ENGINE = new EntityDataSerializer.ForValueType<>()
	{
		@Override
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull EngineSyncState data)
		{
			buf.writeInt(data.BurnTimeDuration);
			buf.writeInt(data.BurnTimeRemaining);
		}
		@Override
		@Nonnull
		public EngineSyncState read(@Nonnull FriendlyByteBuf buf)
		{
			EngineSyncState state = new EngineSyncState();
			state.BurnTimeDuration = buf.readInt();
			state.BurnTimeRemaining = buf.readInt();
			return state;
		}
	};
	public static final EntityDataSerializer<SeatSyncState> SEAT = new EntityDataSerializer.ForValueType<>()
	{
		@Override
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull SeatSyncState data)
		{
			buf.writeUUID(data.PassengerID);
		}
		@Override
		@Nonnull
		public SeatSyncState read(@Nonnull FriendlyByteBuf buf)
		{
			SeatSyncState state = new SeatSyncState();
			state.PassengerID = buf.readUUID();
			return state;
		}
	};
	public static EntityDataSerializer<DamageSyncState> DAMAGE = new EntityDataSerializer.ForValueType<>()
	{
		@Override
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull DamageSyncState data)
		{
			EntityDataSerializers.FLOAT.write(buf, data.Health());
		}
		@Override
		@Nonnull
		public DamageSyncState read(@Nonnull FriendlyByteBuf buf)
		{
			return new DamageSyncState(EntityDataSerializers.FLOAT.read(buf));
		}
	};
	public static final EntityDataSerializer<GunSyncState> GUN = new EntityDataSerializer.ForValueType<>()
	{
		@Override
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull GunSyncState data)
		{
			buf.writeItemStack(data.GunStack, false);
		}
		@Override
		@Nonnull
		public GunSyncState read(@Nonnull FriendlyByteBuf buf)
		{
			GunSyncState state = new GunSyncState();
			state.GunStack = buf.readItem();
			return state;
		}
	};

	public static final EntityDataSerializer<PerPartMap<ArticulationSyncState>> ARTICULATION_MAP =
		new PerPartMapSerializer<>(ARTICULATION);
	public static final EntityDataSerializer<PerPartMap<EngineSyncState>> ENGINE_MAP =
		new PerPartMapSerializer<>(ENGINE);
	public static final EntityDataSerializer<PerPartMap<SeatSyncState>> SEAT_MAP =
		new PerPartMapSerializer<>(SEAT);
	public static final EntityDataSerializer<PerPartMap<DamageSyncState>> DAMAGE_MAP =
		new PerPartMapSerializer<>(DAMAGE);
	public static final EntityDataSerializer<PerPartMap<GunSyncState>> GUN_MAP =
		new PerPartMapSerializer<>(GUN);



	public static final EntityDataSerializer<ShooterContext> SHOOTER_CONTEXT = new EntityDataSerializer<>()
	{
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull ShooterContext context)
		{
			buf.writeUUID(context.OwnerUUID());
			buf.writeUUID(context.EntityUUID());
		}

		@Nonnull
		public ShooterContext read(@Nonnull FriendlyByteBuf buf)
		{
			UUID ownerID = buf.readUUID();
			UUID shooterID = buf.readUUID();
			return ShooterContext.of(shooterID, ownerID);
		}

		@Nonnull
		public ShooterContext copy(@Nonnull ShooterContext context)
		{
			return ShooterContext.of(context.OwnerUUID(), context.EntityUUID());
		}
	};
	public static final EntityDataSerializer<GunContext> GUN_CONTEXT_FULL = new EntityDataSerializer<>()
	{
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull GunContext context)
		{
			SHOOTER_CONTEXT.write(buf, context.GetShooter());
			buf.writeUUID(context.GetUUID());
		}

		@Nonnull
		public GunContext read(@Nonnull FriendlyByteBuf buf)
		{
			ShooterContext shooter = SHOOTER_CONTEXT.read(buf);
			UUID gunID = buf.readUUID();
			return GunContext.of(shooter, gunID);
		}

		@Nonnull
		public GunContext copy(@Nonnull GunContext context)
		{
			return GunContext.of(context.GetShooter(), context.GetUUID());
		}
	};
	public static final EntityDataSerializer<GunContext> GUN_CONTEXT_LIGHT = new EntityDataSerializer<>()
	{
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull GunContext context)
		{
			buf.writeUUID(context.GetUUID());
		}
		@Nonnull
		public GunContext read(@Nonnull FriendlyByteBuf buf)
		{
			UUID gunID = buf.readUUID();
			return GunContext.of(gunID);
		}
		@Nonnull
		public GunContext copy(@Nonnull GunContext context)
		{
			return GunContext.of(context.GetShooter(), context.GetUUID());
		}
	};
	public static final EntityDataSerializer<ActionGroupContext> ACTION_GROUP_CONTEXT_FULL = new EntityDataSerializer<>()
	{
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull ActionGroupContext context)
		{
			GUN_CONTEXT_FULL.write(buf, context.Gun);
			buf.writeInt(context.GroupPath.hashCode());
		}

		@Nonnull
		public ActionGroupContext read(@Nonnull FriendlyByteBuf buf)
		{
			GunContext gun = GUN_CONTEXT_FULL.read(buf);
			int pathHash = buf.readInt();
			return gun.GetActionGroupContextByHash(pathHash);
		}

		@Nonnull
		public ActionGroupContext copy(@Nonnull ActionGroupContext context)
		{
			return ActionGroupContext.CreateFrom(context.Gun, context.GroupPath);
		}
	};
	public static final EntityDataSerializer<GunshotContext> GUNSHOT_CONTEXT_FULL = new EntityDataSerializer<>()
	{
		public void write(@Nonnull FriendlyByteBuf buf, @Nonnull GunshotContext context)
		{
			ACTION_GROUP_CONTEXT_FULL.write(buf, context.ActionGroup);
			buf.writeInt(context.Bullet.hashCode());
			buf.writeShort(context.DefIndex);
			buf.writeShort(context.IsProjectile ? 1 : 0);
		}

		@Nonnull
		public GunshotContext read(@Nonnull FriendlyByteBuf buf)
		{
			ActionGroupContext actionGroup = ACTION_GROUP_CONTEXT_FULL.read(buf);
			BulletDefinition bullet = FlansMod.BULLETS.ByHash(buf.readInt());
			int defIndex = buf.readShort();
			boolean projectile = buf.readShort() != 0;
			return projectile ? GunshotContext.projectile(actionGroup, bullet, defIndex) : GunshotContext.hitscan(actionGroup, bullet, defIndex);
		}

		@Nonnull
		public GunshotContext copy(@Nonnull GunshotContext context)
		{
			return GunshotContext.of(context);
		}
	};

	public static void RegisterSerializers()
	{
		EntityDataSerializers.registerSerializer(SHOOTER_CONTEXT);
		EntityDataSerializers.registerSerializer(GUN_CONTEXT_FULL);
		EntityDataSerializers.registerSerializer(GUN_CONTEXT_LIGHT);
		EntityDataSerializers.registerSerializer(ACTION_GROUP_CONTEXT_FULL);
		EntityDataSerializers.registerSerializer(GUNSHOT_CONTEXT_FULL);

		EntityDataSerializers.registerSerializer(GUN_DEF);
		EntityDataSerializers.registerSerializer(BULLET_DEF);
		EntityDataSerializers.registerSerializer(GRENADE_DEF);
		EntityDataSerializers.registerSerializer(ATTACHMENT_DEF);
		EntityDataSerializers.registerSerializer(PART_DEF);
		EntityDataSerializers.registerSerializer(WORKBENCH_DEF);
		EntityDataSerializers.registerSerializer(MATERIAL_DEF);
		EntityDataSerializers.registerSerializer(MAGAZINE_DEF);
		EntityDataSerializers.registerSerializer(NPC_DEF);
		EntityDataSerializers.registerSerializer(TRAIT_DEF);
		EntityDataSerializers.registerSerializer(ARMOUR_DEF);
		EntityDataSerializers.registerSerializer(VEHICLE_DEF);
		EntityDataSerializers.registerSerializer(CONTROL_SCHEME_DEF);

		EntityDataSerializers.registerSerializer(ARTICULATION);
		EntityDataSerializers.registerSerializer(ENGINE);
		EntityDataSerializers.registerSerializer(SEAT);
		EntityDataSerializers.registerSerializer(DAMAGE);
		EntityDataSerializers.registerSerializer(GUN);

		EntityDataSerializers.registerSerializer(ARTICULATION_MAP);
		EntityDataSerializers.registerSerializer(ENGINE_MAP);
		EntityDataSerializers.registerSerializer(SEAT_MAP);
		EntityDataSerializers.registerSerializer(DAMAGE_MAP);
		EntityDataSerializers.registerSerializer(GUN_MAP);

	}
}

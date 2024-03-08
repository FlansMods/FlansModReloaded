package com.flansmod.common.actions.contexts;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.bullets.BulletDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ContextSerializers
{
	public static void RegisterSerializers()
	{
		EntityDataSerializers.registerSerializer(SHOOTER_CONTEXT);
		EntityDataSerializers.registerSerializer(GUN_CONTEXT_FULL);
		EntityDataSerializers.registerSerializer(GUN_CONTEXT_LIGHT);
		EntityDataSerializers.registerSerializer(ACTION_GROUP_CONTEXT_FULL);
		EntityDataSerializers.registerSerializer(GUNSHOT_CONTEXT_FULL);

	}

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
}

package com.flansmod.physics.network;

import com.flansmod.physics.common.units.AngularVelocity;
import com.flansmod.physics.common.units.LinearVelocity;
import com.flansmod.physics.common.util.Transform;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PhysicsSyncMessage extends PhysicsMessage
{
    public static final UUID INVALID_ID = new UUID(0L, 0L);

    public static class ToServer extends PhysicsSyncMessage
    {
    }
    public static class ToClient extends PhysicsSyncMessage
    {
    }

    public static class PhysicsStateChange
    {
        @Nonnull
        public UUID PhysicsComponentID = INVALID_ID;
        @Nullable
        public Transform Location;
        @Nullable
        public LinearVelocity LinearVelocityUpdate;
        @Nullable
        public AngularVelocity AngularVelocityUpdate;
    }

    public static final int LOCATION_FLAG = 0x1;
    public static final int ROLL_FLAG = 0x2;
    public static final int LINEAR_VELOCITY_FLAG = 0x4;
    public static final int ANGULAR_VELOCITY_FLAG = 0x8;

    public int EntityID;
    public long GameTick;
    public List<PhysicsStateChange> StateChanges = new ArrayList<PhysicsStateChange>();

    public void addStateChange(@Nonnull UUID id,
                               @Nullable Transform location,
                               @Nullable LinearVelocity linear,
                               @Nullable AngularVelocity angular)
    {
        PhysicsStateChange stateChange = new PhysicsStateChange();
        stateChange.PhysicsComponentID = id;
        stateChange.Location = location;
        stateChange.LinearVelocityUpdate = linear;
        stateChange.AngularVelocityUpdate = angular;
        StateChanges.add(stateChange);
    }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buf)
    {
        buf.writeInt(EntityID);
        buf.writeLong(GameTick);
        buf.writeInt(StateChanges.size());
        for(int i = 0; i < StateChanges.size(); i++)
        {
            PhysicsStateChange change = StateChanges.get(i);
            buf.writeUUID(change.PhysicsComponentID);
            int flags = 0;
            if(change.Location != null)
                flags = flags | LOCATION_FLAG;
            if(change.LinearVelocityUpdate != null)
                flags = flags | LINEAR_VELOCITY_FLAG;
            if(change.AngularVelocityUpdate != null)
                flags = flags | ANGULAR_VELOCITY_FLAG;
            buf.writeInt(flags);
            if(change.Location != null)
            {
                buf.writeDouble(change.Location.Position.x);
                buf.writeDouble(change.Location.Position.y);
                buf.writeDouble(change.Location.Position.z);
                buf.writeQuaternion(change.Location.Orientation);
            }
            if(change.LinearVelocityUpdate != null)
            {
                change.LinearVelocityUpdate.toBuf(buf);
            }
            if(change.AngularVelocityUpdate != null)
            {
                change.AngularVelocityUpdate.toBuf(buf);
            }
        }
    }

    @Override
    public void decode(@Nonnull FriendlyByteBuf buf)
    {
        EntityID = buf.readInt();
        GameTick = buf.readLong();
        int numStateChanges = buf.readInt();
        for(int i = 0; i < numStateChanges; i++)
        {
            PhysicsStateChange change = new PhysicsStateChange();
            change.PhysicsComponentID = buf.readUUID();
            int flags = buf.readInt();
            if((flags & LOCATION_FLAG) != 0)
            {
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                Quaternionf ori = buf.readQuaternion();
                change.Location = Transform.fromPosAndQuat(x, y, z, ori);
            }
            if((flags & LINEAR_VELOCITY_FLAG) != 0)
            {
                change.LinearVelocityUpdate = LinearVelocity.fromBuf(buf);
            }
            if((flags & ANGULAR_VELOCITY_FLAG) != 0)
            {
                change.AngularVelocityUpdate = AngularVelocity.fromBuf(buf);
            }
            StateChanges.add(change);
        }
    }
}

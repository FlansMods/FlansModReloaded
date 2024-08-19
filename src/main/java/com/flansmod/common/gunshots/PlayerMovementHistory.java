package com.flansmod.common.gunshots;

import com.flansmod.common.FlansMod;
import com.flansmod.common.gunshots.snapshots.CommonPlayerModel;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.Enumeration;

public class PlayerMovementHistory
{
    private CommonPlayerModel PlayerModel;
    private PlayerSnapshot[] snapshotRingBuffer;
    private int mostRecentSnapshot;
    private int snapshotCount;

    public PlayerMovementHistory(int numSnaps)
    {
        snapshotCount = numSnaps;
        snapshotRingBuffer = new PlayerSnapshot[numSnaps];
        mostRecentSnapshot = numSnaps - 1;
        for(int i = 0; i < snapshotCount; i++)
        {
            snapshotRingBuffer[i] = new PlayerSnapshot();
        }
        PlayerModel = new CommonPlayerModel();
    }

    private int GetNextIndex() { return (mostRecentSnapshot + 1) % snapshotCount; }
    private int GetCurrentIndex() { return mostRecentSnapshot; }
    private int GetIndexNTicksAgo(int n) { return Maths.Modulo(mostRecentSnapshot - n, snapshotCount); }

    public void TakeSnapshot(@Nonnull Player player)
    {
        PlayerSnapshot snap = snapshotRingBuffer[GetNextIndex()];
        PlayerModel.Snap(player, snap);
        mostRecentSnapshot = GetNextIndex();
    }

   //public PlayerSnapshot GetNextSnapshotForWriting()
   //{
   //    return snapshotRingBuffer[GetNextIndex()];
   //}

   //public void FinishedWriting()
   //{
   //    mostRecentSnapshot = GetNextIndex();
   //}

    public int GetNumSnapshots() { return snapshotCount; }

    @Nonnull
    public PlayerSnapshot GetSnapshotNTicksAgo(int nTicks)
    {
        if(nTicks < 0 || nTicks >= snapshotCount)
            return PlayerSnapshot.INVALID;

        PlayerSnapshot snap = snapshotRingBuffer[GetIndexNTicksAgo(nTicks)];
        return snap == null ? PlayerSnapshot.INVALID : snap;
    }

    // ---- Debug Render
    @OnlyIn(Dist.CLIENT)
    public void debugRender(boolean client)
    {
       // for(int i = (int)(MinecraftHelpers.GetTick() % 40L); i < GetNumSnapshots(); i += 40)
        {
            int i = 40;
            float decay = (float)i / (float)snapshotCount;
            // Add this if you want to compare client and server : (client ? new Vec3(1d, 0d, 0d) : new Vec3(-1d, 0d, 0d))
            GetSnapshotNTicksAgo(i).debugRender(new Vec3(0d, 0d, 0d), new Vector4f(decay, client ? 1.0f : 0.0f, client ? 0.0f : 1.0f, 1.0f - decay));
        }
    }
}

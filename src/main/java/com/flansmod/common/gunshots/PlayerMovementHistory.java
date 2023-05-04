package com.flansmod.common.gunshots;

import com.flansmod.util.Maths;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.Enumeration;

public class PlayerMovementHistory
{
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
    }

    private int GetNextIndex() { return (mostRecentSnapshot + 1) % snapshotCount; }
    private int GetCurrentIndex() { return mostRecentSnapshot; }
    private int GetIndexNTicksAgo(int n) { return Maths.Modulo(mostRecentSnapshot - n, snapshotCount); }

    public PlayerSnapshot GetNextSnapshotForWriting()
    {
        return snapshotRingBuffer[GetNextIndex()];
    }

    public void FinishedWriting()
    {
        mostRecentSnapshot = GetNextIndex();
    }

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
    public void debugRender()
    {
        //for(int i = 0; i < GetNumSnapshots(); i++)
        {
            snapshotRingBuffer[GetCurrentIndex()].debugRender(new Vector4f((float)GetCurrentIndex() / (float)snapshotCount, 1.0f, 0.0f, 0.5f));
        }
    }
}

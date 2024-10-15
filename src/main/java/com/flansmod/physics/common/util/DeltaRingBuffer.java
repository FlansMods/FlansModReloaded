package com.flansmod.physics.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeltaRingBuffer<TData extends Comparable<TData>>
{
    private final Object[] elements;
    private int headIndex;

    public DeltaRingBuffer(int size, TData initialState)
    {
        elements = new Object[size];
        elements[0] = initialState;
        headIndex = 0;
    }

    @Nonnull
    public TData getMostRecent()
    {
        return (TData)elements[headIndex];
    }
    @Nullable
    public TData getOldEntry(int depth)
    {
        if(depth < elements.length)
        {
            int oldIndex = Maths.Modulo(headIndex - depth, elements.length);
            if (elements[oldIndex] != null)
                return (TData) elements[oldIndex];
        }
        return null;
    }
    public void add(@Nonnull TData data)
    {
        headIndex++;
        if(headIndex >= elements.length)
            headIndex = 0;
        elements[headIndex] = data;
    }
    public boolean addIfChanged(@Nonnull TData data)
    {
        TData head = getMostRecent();
        int comparison = head.compareTo(data);
        if(comparison == 0)
            return false;
        add(data);
        return true;
    }
}

package com.flansmod.physics.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class DeltaRingBuffer<TData extends Comparable<TData>> implements Iterable<TData>
{
    private final Object[] elements;
    private int headIndex;
    private int numElements;

    public DeltaRingBuffer(int size, TData initialState)
    {
        elements = new Object[size];
        elements[0] = initialState;
        headIndex = 0;
        numElements = 1;
    }

    public void reset(TData initialState)
    {
        elements[0] = initialState;
        headIndex = 0;
        numElements = 1;
    }

    public int capacity() { return elements.length; }
    public int size() { return numElements; }

    @Nonnull
    public TData getMostRecent()
    {
        return (TData)elements[headIndex];
    }
    @Nullable
    public TData getOldEntry(int depth)
    {
        if(depth < numElements)
        {
            int oldIndex = Maths.Modulo(headIndex - depth, elements.length);
            if (elements[oldIndex] != null)
                return (TData) elements[oldIndex];
        }
        return null;
    }
    public void add(@Nonnull TData data)
    {
        numElements++;
        headIndex++;
        if(headIndex >= elements.length)
            headIndex = 0;
        elements[headIndex] = data;
    }
    @Nullable
    public TData removeLatest()
    {
        if(numElements > 1)
        {
            Object ret = elements[headIndex];
            elements[headIndex] = null;
            numElements--;
            headIndex--;
            if (headIndex < 0)
                headIndex += elements.length;
            return ret == null ? null : (TData)ret;
        }
        return null;
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
    @Nonnull
    public Iterator<TData> iterator()
    {
        return new Iterator<>()
        {
            private int idx = 0;
            @Override
            public boolean hasNext() { return idx < numElements; }
            @Override
            public TData next() { return getOldEntry(idx++); }
        };
    }
}

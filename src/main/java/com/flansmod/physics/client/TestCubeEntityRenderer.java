package com.flansmod.physics.client;

import com.flansmod.physics.common.FlansPhysicsMod;
import com.flansmod.physics.common.tests.TestCubeEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class TestCubeEntityRenderer extends EntityRenderer<TestCubeEntity>
{
    public TestCubeEntityRenderer(@Nonnull EntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    @Override @Nonnull
    public ResourceLocation getTextureLocation(@Nonnull TestCubeEntity testCubeEntity)
    {
        return new ResourceLocation(FlansPhysicsMod.MODID, "null");
    }
}

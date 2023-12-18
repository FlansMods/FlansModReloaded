package com.flansmod.client.render.debug;

import com.flansmod.common.actions.contexts.GunContext;
import com.flansmod.common.actions.contexts.ShooterContext;
import com.flansmod.util.Maths;
import com.flansmod.util.MinecraftHelpers;
import com.flansmod.util.Transform;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.swing.*;
import java.util.ArrayList;

public class DebugRenderer
{
    private abstract static class DebugRenderItem
    {
        public Transform transform;
        public int ticksLeft;
        public Vector4f colour;

        public DebugRenderItem(Transform t, int ticks, Vector4f col)
        {
            transform = t;
            ticksLeft = ticks;
            colour = col;
        }

        public abstract void Render(PoseStack poseStack, Tesselator tesselator);
    }

    private static class DebugRenderLine extends DebugRenderItem
    {
        public Vec3 direction;

        public DebugRenderLine(Transform t, int ticks, Vector4f col, Vec3 dir)
        {
            super(t, ticks, col);
            direction = dir;
        }

        protected void RenderLine(PoseStack poseStack, Tesselator tesselator, Vec3 start, Vec3 ray, Vector4f col)
        {
            BufferBuilder buf = tesselator.getBuilder();
            buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            Vec3 center = new Vec3(
                start.x + ray.x * 0.5f,
                start.y + ray.y * 0.5f,
                start.z + ray.z * 0.5f);
            Vec3 toCamera = Maths.Sub(center, MinecraftHelpers.GetCamera().position());
            Vec3 lateralAxis = Maths.Cross(toCamera, ray).normalize();
            lateralAxis = lateralAxis.scale(0.02d);

            buf.vertex(poseStack.last().pose(), (float)lateralAxis.x, (float)lateralAxis.y, (float)lateralAxis.z)
                .color(col.x, col.y, col.z, col.w)
                .endVertex();
            buf.vertex(poseStack.last().pose(), (float)-lateralAxis.x, (float)-lateralAxis.y, (float)-lateralAxis.z)
                .color(col.x, col.y, col.z, col.w)
                .endVertex();


            buf.vertex(poseStack.last().pose(),
                    (float)(ray.x + lateralAxis.x),
                    (float)(ray.y + lateralAxis.y),
                    (float)(ray.z + lateralAxis.z))
                .color(col.x, col.y, col.z, col.w)
                .endVertex();
            buf.vertex(poseStack.last().pose(),
                    (float)(ray.x - lateralAxis.x),
                    (float)(ray.y - lateralAxis.y),
                    (float)(ray.z - lateralAxis.z))
                .color(col.x, col.y, col.z, col.w)
                .endVertex();

            tesselator.end();
        }

        @Override
        public void Render(PoseStack poseStack, Tesselator tesselator)
        {
            if(MinecraftHelpers.GetCamera() == null)
                return;

            RenderLine(poseStack, tesselator, transform.PositionVec3(), direction, colour);
        }
    }

    private static class DebugRenderCube extends DebugRenderItem
    {
        public Vector3f halfExtents;
        public DebugRenderCube(Transform t, int ticks, Vector4f col, Vector3f h)
        {
            super(t, ticks, col);
            halfExtents = h;
        }

        // v = (z ? 4) + (y ? 2) + (x)
        private static int[] BoxStrips = new int[] { 5, 1, 7, 3, 2, 4, 1, 5, 4, 7, 6, 2, 4, 0 };
        @Override
        public void Render(PoseStack poseStack, Tesselator tesselator)
        {
            BufferBuilder buf = tesselator.getBuilder();
            buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
    
            Vector3f[] verts = new Vector3f[8];
            for(int x = 0; x < 2; x++)
                for(int y = 0; y < 2; y++)
                    for(int z = 0; z < 2; z++)
                    {
                        verts[z*4 + y*2 + x] = new Vector3f((x*2-1)*halfExtents.x, (y*2-1)*halfExtents.y, (z*2-1)*halfExtents.z);
                        transform.Orientation.transform(verts[z*4 + y*2 + x]);
                    }

            for(int i = 0; i < BoxStrips.length; i++)
            {
                buf.vertex(poseStack.last().pose(), verts[BoxStrips[i]].x, verts[BoxStrips[i]].y, verts[BoxStrips[i]].z)
                        .color(colour.x, colour.y, colour.z, colour.w)
                        .endVertex();
            }

            tesselator.end();
        }
    }

    private static class DebugRenderAxes extends DebugRenderLine
    {
        public DebugRenderAxes(Transform t, int ticks, Vector4f col)
        {
            super(t, ticks, col, t.ForwardVec3());
        }

        @Override
        public void Render(PoseStack poseStack, Tesselator tesselator)
        {
            if(MinecraftHelpers.GetCamera() == null)
                return;

            RenderLine(poseStack, tesselator, transform.PositionVec3(), transform.ForwardVec3(), new Vector4f(1f, 0f, 0f, 1f));
            RenderLine(poseStack, tesselator, transform.PositionVec3(), transform.UpVec3(), new Vector4f(0f, 1f, 0f, 1f));
            RenderLine(poseStack, tesselator, transform.PositionVec3(), transform.RightVec3(), new Vector4f(0f, 0f, 1f, 1f));
        }
    }

    public static ArrayList<DebugRenderItem> renderItems = new ArrayList<>();

    public static void RenderCube(Transform t, int ticks, Vector4f col, Vector3f h)
    {
        renderItems.add(new DebugRenderCube(t, ticks, col, h));
    }

    public static void RenderLine(Vec3 origin, int ticks, Vector4f col, Vec3 ray)
    {
        renderItems.add(new DebugRenderLine(new Transform("DebugLine", origin), ticks, col, ray));
    }

    public static void RenderAxes(Transform t, int ticks, Vector4f col)
    {
        renderItems.add(new DebugRenderAxes(t, ticks, col));
    }

    public DebugRenderer()
    {
        MinecraftForge.EVENT_BUS.addListener(this::renderTick);
        MinecraftForge.EVENT_BUS.addListener(this::clientTick);
    }

    public void renderTick(RenderLevelStageEvent event)
    {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
        {
            RenderSystem.enableBlend();
            //RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);//RenderSystem.disableTexture();
            RenderSystem.disableCull();
            
            Tesselator tesselator = Tesselator.getInstance();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            PoseStack poseStack = event.getPoseStack();
            
            Camera camera = event.getCamera();
            Vec3 pos = camera.getPosition();
            
            
            for (DebugRenderItem item : renderItems)
            {
                poseStack.pushPose();
                poseStack.translate(item.transform.Position.x - pos.x, item.transform.Position.y - pos.y, item.transform.Position.z - pos.z);
                item.Render(poseStack, tesselator);
                poseStack.popPose();
            }

            RenderSystem.enableCull();
            //RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    public void clientTick(TickEvent.ClientTickEvent event)
    {
        for(int i = renderItems.size() - 1; i >= 0; i--)
        {
            if(renderItems.get(i).ticksLeft-- <= 0)
                renderItems.remove(i);
        }

        // Add some useful rendering
        //Player player = Minecraft.getInstance().player;
        //if(player != null)
        //{
        //    ShooterContext playerShooterContext = ShooterContext.GetOrCreate(player);
        //    if(playerShooterContext.IsValid())
        //    {
        //        for(GunContext gunContext : playerShooterContext.GetAllGunContexts(true))
        //        {
        //            if(gunContext.IsValid())
        //            {
        //                Transform shootOrigin = gunContext.GetShootOrigin();
        //                if (shootOrigin != null)
        //                    RenderAxes(shootOrigin, 1, new Vector4f());
        //            }
        //        }
        //    }
        //}
    }
}

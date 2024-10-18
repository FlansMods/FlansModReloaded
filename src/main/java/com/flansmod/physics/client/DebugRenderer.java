package com.flansmod.physics.client;

import com.flansmod.physics.common.util.Maths;
import com.flansmod.physics.common.util.MinecraftHelpers;
import com.flansmod.physics.common.util.Transform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.joml.*;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

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

        public abstract void render(PoseStack poseStack, Tesselator tesselator);
    }

    private static class DebugRenderPoint extends DebugRenderItem
    {
        public DebugRenderPoint(Transform t, int ticks, Vector4f col)
        {
            super(t, ticks, col);
        }

        private static final int NUM_SEGMENTS = 16;
        private static final float RADIUS = 0.04f;
        private static final float RADS_PER_SEGMENT = Maths.TauF / NUM_SEGMENTS;

        @Override
        public void render(PoseStack poseStack, Tesselator tesselator)
        {
            if(MinecraftHelpers.getCamera() == null)
                return;

            Vec3 toCamera = MinecraftHelpers.getCamera().getForward().normalize();
            Vec3 verticalAxis = MinecraftHelpers.getCamera().getUpVector(0f).normalize();
            Vec3 lateralAxis = Maths.cross(toCamera, verticalAxis).normalize();


            BufferBuilder buf = tesselator.getBuilder();
            buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

            buf.vertex(poseStack.last().pose(), 0f, 0f, 0f).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            for(int i = 0; i <= NUM_SEGMENTS; i++)
            {
                Vec3 pos = lateralAxis.scale(RADIUS * Maths.sinF(i * RADS_PER_SEGMENT));
                pos = pos.add(verticalAxis.scale(RADIUS * Maths.cosF(i * RADS_PER_SEGMENT)));
                buf.vertex(poseStack.last().pose(), (float)pos.x, (float)pos.y, (float)pos.z)
                    .color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            }

            tesselator.end();
        }
    }

    private static class DebugRenderLine extends DebugRenderItem
    {
        public Vec3 direction;
        public boolean Arrow;

        public DebugRenderLine(Transform t, int ticks, Vector4f col, Vec3 dir, boolean arrow)
        {
            super(t, ticks, col);
            direction = dir;
            Arrow = arrow;
        }

        protected void renderLineSegment(PoseStack poseStack, Tesselator tesselator, Vec3 start, Vec3 ray, Vector4f col, boolean asArrow)
        {
            BufferBuilder buf = tesselator.getBuilder();
            buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

            Vec3 center = new Vec3(
                start.x + ray.x * 0.5f,
                start.y + ray.y * 0.5f,
                start.z + ray.z * 0.5f);

            Vector3f centerInModelSpace = poseStack.last().pose().transformPosition(center.toVector3f());
            Vector3f rayInModelSpace = poseStack.last().pose().transformDirection(ray.toVector3f());
            centerInModelSpace.normalize();
            rayInModelSpace.normalize();
            Vector3f lateralAxis = centerInModelSpace.cross(rayInModelSpace, new Vector3f());
            lateralAxis.normalize();
            lateralAxis.mul(0.02f);
            lateralAxis.mulTransposeDirection(poseStack.last().pose());

            final Vec3 v0 = start;
            final Vector3f vAxis = ray.toVector3f();
            final Vector3f uAxis = lateralAxis;

            BiConsumer<Float, Float> vertexFunc = (u, v) -> {
                buf.vertex(poseStack.last().pose(),
                    (float)(v0.x + uAxis.x * u + vAxis.x * v),
                    (float)(v0.y + uAxis.y * u + vAxis.y * v),
                    (float)(v0.z + uAxis.z * u + vAxis.z * v))
                    .color(col.x, col.y, col.z, col.w)
                    .endVertex();
            };

            if(asArrow)
            {
                vertexFunc.accept(1.0f, 0.0f);
                vertexFunc.accept(-1.0f, 0.0f);
                vertexFunc.accept(-1.0f, 0.9f);

                vertexFunc.accept(1.0f, 0.0f);
                vertexFunc.accept(-1.0f, 0.9f);
                vertexFunc.accept(1.0f, 0.9f);

                vertexFunc.accept(4.0f, 0.9f);
                vertexFunc.accept(-4.0f, 0.9f);
                vertexFunc.accept(0.0f, 1.0f);
            }
            else
            {
                vertexFunc.accept(1.0f, 0.0f);
                vertexFunc.accept(-1.0f, 0.0f);
                vertexFunc.accept(-1.0f, 1.0f);

                vertexFunc.accept(1.0f, 0.0f);
                vertexFunc.accept(-1.0f, 1.0f);
                vertexFunc.accept(1.0f, 1.0f);
            }

            tesselator.end();
        }

        @Override
        public void render(PoseStack poseStack, Tesselator tesselator)
        {
            if(MinecraftHelpers.getCamera() == null)
                return;

            renderLineSegment(poseStack, tesselator, Vec3.ZERO, direction, colour, Arrow);
        }
    }

    private static class DebugRenderRotation extends DebugRenderLine
    {
        private final double RADIUS = 0.5f;
        private final int NUM_SEGMENTS = 10;
        private final double RADS_PER_SEGMENT = Maths.Tau / NUM_SEGMENTS;
        public final double Magnitude;

        public DebugRenderRotation(Transform t, int ticks, Vector4f col, Vec3 dir, double mag, boolean arrow)
        {
            super(t, ticks, col, dir, arrow);
            Magnitude = mag;
        }

        @Override
        public void render(PoseStack poseStack, Tesselator tesselator)
        {
            if(MinecraftHelpers.getCamera() == null)
                return;

            Vec3 from = getPoint(0);
            Vec3 to = getPoint(1);
            for(int i = 1; i < NUM_SEGMENTS+1; i++)
            {
                renderLineSegment(poseStack, tesselator, from, to.subtract(from), colour, i == NUM_SEGMENTS);
                from = to;
                to = getPoint(i+1);
            }
        }

        private Vec3 getPoint(int index)
        {
            AxisAngle4d axisAngle = new AxisAngle4d(Magnitude * index * RADS_PER_SEGMENT, direction.x, direction.y, direction.z);
            Vector3d sweepVec = new Vector3d(direction.x, direction.y, direction.z);
            sweepVec.cross(new Vector3d(0d, 1d, 0d));
            sweepVec.normalize();
            sweepVec.normalize(RADIUS);
            axisAngle.transform(sweepVec);
            return new Vec3(sweepVec.x, sweepVec.y, sweepVec.z);
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
        private static int[] BoxTriangles = new int[] {
            0, 1, 3,    0, 3, 2, // Back -z
            4, 7, 5,    4, 6, 7, // Front +z

            0, 2, 6,    0, 6, 4, // Left -x
            1, 7, 3,    1, 5, 7, // Right +x

            0, 1, 5,    0, 5, 4, // Bottom -y
            2, 7, 3,    2, 6, 7, // Top +y
        };
        @Override
        public void render(PoseStack poseStack, Tesselator tesselator)
        {
            BufferBuilder buf = tesselator.getBuilder();
    
            Vector3f[] verts = new Vector3f[8];
            for(int x = 0; x < 2; x++)
                for(int y = 0; y < 2; y++)
                    for(int z = 0; z < 2; z++)
                    {
                        verts[z*4 + y*2 + x] = new Vector3f((x*2-1)*halfExtents.x, (y*2-1)*halfExtents.y, (z*2-1)*halfExtents.z);
                        //transform.Orientation.transform(verts[z*4 + y*2 + x]);
                    }

            //buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            //for(int i = 0; i < BoxTriangles.length; i++)
            //{
            //    buf.vertex(poseStack.last().pose(), verts[BoxTriangles[i]].x, verts[BoxTriangles[i]].y, verts[BoxTriangles[i]].z)
            //            .color(colour.x, colour.y, colour.z, colour.w)
            //            .endVertex();
            //}
            //tesselator.end();

            buf.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            buf.vertex(poseStack.last().pose(), verts[0].x, verts[0].y, verts[0].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[1].x, verts[1].y, verts[1].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[5].x, verts[5].y, verts[5].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[1].x, verts[1].y, verts[1].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[3].x, verts[3].y, verts[3].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[7].x, verts[7].y, verts[7].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[3].x, verts[3].y, verts[3].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[2].x, verts[2].y, verts[2].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[6].x, verts[6].y, verts[6].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[2].x, verts[2].y, verts[2].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[0].x, verts[0].y, verts[0].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[4].x, verts[4].y, verts[4].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[5].x, verts[5].y, verts[5].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[7].x, verts[7].y, verts[7].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[6].x, verts[6].y, verts[6].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();
            buf.vertex(poseStack.last().pose(), verts[4].x, verts[4].y, verts[4].z).color(colour.x, colour.y, colour.z, 1.0f).endVertex();

            tesselator.end();
        }
    }

    private static class DebugRenderAxes extends DebugRenderLine
    {
        public DebugRenderAxes(Transform t, int ticks, Vector4f col)
        {
            super(t, ticks, col, t.forward(), false);
        }

        @Override
        public void render(PoseStack poseStack, Tesselator tesselator)
        {
            if(MinecraftHelpers.getCamera() == null)
                return;

            renderLineSegment(poseStack, tesselator, Vec3.ZERO, transform.forward(), new Vector4f(1f, 0f, 0f, 1f), false);
            renderLineSegment(poseStack, tesselator, Vec3.ZERO, transform.up(), new Vector4f(0f, 1f, 0f, 1f), false);
            renderLineSegment(poseStack, tesselator, Vec3.ZERO, transform.right(), new Vector4f(0f, 0f, 1f, 1f), false);
        }
    }

    private static final Queue<DebugRenderItem> itemsToAdd = new ConcurrentLinkedQueue<>();
    private static final ArrayList<DebugRenderItem> renderItems = new ArrayList<>();

    public static void renderCube(Transform t, int ticks, Vector4f col, Vector3f h)
    {
        itemsToAdd.add(new DebugRenderCube(t, ticks, col, h));
    }

    public static void renderPoint(Transform t, int ticks, Vector4f col)
    {
        itemsToAdd.add(new DebugRenderPoint(t, ticks, col));
    }

    public static void renderLine(Transform t, int ticks, Vector4f col, Vec3 ray)
    {
        if(!Maths.approx(ray.lengthSqr(), 0d))
            itemsToAdd.add(new DebugRenderLine(t, ticks, col, ray, false));
    }
    public static void renderArrow(Transform t, int ticks, Vector4f col, Vec3 ray)
    {
        if(!Maths.approx(ray.lengthSqr(), 0d))
            itemsToAdd.add(new DebugRenderLine(t, ticks, col, ray, true));
    }
    public static void renderRotation(Transform t, int ticks, Vector4f col, Vec3 axis, double magnitude)
    {
        if(!Maths.approx(magnitude, 0d))
            itemsToAdd.add(new DebugRenderRotation(t, ticks, col, axis, magnitude, true));
    }
    public static void renderAxes(Transform t, int ticks, Vector4f col)
    {
        itemsToAdd.add(new DebugRenderAxes(t, ticks, col));
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

            while(!itemsToAdd.isEmpty())
            {
                DebugRenderItem item = itemsToAdd.poll();
                renderItems.add(item);
            }
            
            for(int i = 0; i < renderItems.size(); i++)
            {
                DebugRenderItem item = renderItems.get(i);

                poseStack.pushPose();
                poseStack.translate(item.transform.Position.x - pos.x, item.transform.Position.y - pos.y, item.transform.Position.z - pos.z);
                poseStack.mulPose(item.transform.Orientation);
                item.render(poseStack, tesselator);
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
            if(--renderItems.get(i).ticksLeft <= 0)
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

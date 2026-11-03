package ru.poufdlc.visual;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
public final class EspRenderer {
    private EspRenderer() {}
    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EspRenderer::onWorldRender);
    }
    private static void onWorldRender(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        MatrixStack matrices = ctx.matrixStack();
        Vec3d cam = ctx.camera().getPos();
        VertexConsumerProvider.Immediate consumers = mc.getBufferBuilders().getEntityVertexConsumers();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (mc.player.distanceTo(e) > VisualsConfig.espRange) continue;
            if (e instanceof LivingEntity liv) {
                if (VisualsConfig.boxEsp) drawBox(matrices, consumers, liv, cam, 0xFF3B82F6);
                if (VisualsConfig.tracers) drawTracer(matrices, consumers, liv, cam, 0x883B82F6);
            }
            if (VisualsConfig.itemEsp && e instanceof ItemEntity) drawBox(matrices, consumers, e, cam, 0xFF22C55E);
        }
        consumers.draw();
    }
    private static void drawBox(MatrixStack ms, VertexConsumerProvider vp, Entity e, Vec3d cam, int color) {
        Box box = e.getBoundingBox().offset(-cam.x, -cam.y, -cam.z);
        float a = ((color >> 24) & 255) / 255f, r = ((color >> 16) & 255) / 255f, g = ((color >> 8) & 255) / 255f, b = (color & 255) / 255f;
        VertexConsumer vc = vp.getBuffer(RenderLayer.getLines());
        Matrix4f m = ms.peek().getPositionMatrix();
        line(vc, m, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, r, g, b, a);
        line(vc, m, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, r, g, b, a);
        line(vc, m, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, r, g, b, a);
        line(vc, m, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, r, g, b, a);
        line(vc, m, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, a);
        line(vc, m, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        line(vc, m, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, r, g, b, a);
        line(vc, m, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, r, g, b, a);
        line(vc, m, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, r, g, b, a);
        line(vc, m, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, r, g, b, a);
        line(vc, m, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
        line(vc, m, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, r, g, b, a);
    }
    private static void drawTracer(MatrixStack ms, VertexConsumerProvider vp, Entity e, Vec3d cam, int color) {
        Vec3d to = e.getPos().add(0, e.getHeight() * 0.5, 0).subtract(cam);
        float a = ((color >> 24) & 255) / 255f * VisualsConfig.tracerAlpha;
        float r = ((color >> 16) & 255) / 255f, g = ((color >> 8) & 255) / 255f, b = (color & 255) / 255f;
        VertexConsumer vc = vp.getBuffer(RenderLayer.getLines());
        Matrix4f m = ms.peek().getPositionMatrix();
        line(vc, m, 0, 0, 0, (float) to.x, (float) to.y, (float) to.z, r, g, b, a);
    }
    private static void line(VertexConsumer vc, Matrix4f m, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        vc.vertex(m, (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(0, 1, 0);
        vc.vertex(m, (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(0, 1, 0);
    }
    public static void renderHudOverlay(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        int y = 30;
        if (VisualsConfig.fpsHud) { ctx.drawTextWithShadow(mc.textRenderer, "FPS: " + mc.getCurrentFps(), 6, y, 0xFF60A5FA); y += 12; }
        if (VisualsConfig.coordsHud) {
            var p = mc.player.getBlockPos();
            ctx.drawTextWithShadow(mc.textRenderer, String.format("XYZ: %d %d %d", p.getX(), p.getY(), p.getZ()), 6, y, 0xFFE2E8F0);
        }
    }
}

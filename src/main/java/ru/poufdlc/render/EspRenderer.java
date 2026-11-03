package ru.poufdlc.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.poufdlc.combat.Modules;

public final class EspRenderer {
    private EspRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EspRenderer::world);
    }

    private static void world(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        MatrixStack ms = ctx.matrixStack();
        Vec3d cam = ctx.camera().getPos();
        var consumers = mc.getBufferBuilders().getEntityVertexConsumers();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || mc.player.distanceTo(e) > Modules.espRange) continue;
            if (e instanceof LivingEntity liv) {
                if (Modules.boxEsp) box(ms, consumers, e, cam, liv instanceof PlayerEntity ? 0xFF3B82F6 : 0xFF22C55E);
                if (Modules.tracers) tracer(ms, consumers, e, cam, 0x883B82F6);
            }
            if (Modules.itemEsp && e instanceof ItemEntity) box(ms, consumers, e, cam, 0xFFEAB308);
        }
        consumers.draw();
    }

    private static void box(MatrixStack ms, VertexConsumerProvider vp, Entity e, Vec3d cam, int color) {
        Box b = e.getBoundingBox().offset(-cam.x, -cam.y, -cam.z);
        float a = ((color >> 24) & 255) / 255f, r = ((color >> 16) & 255) / 255f, g = ((color >> 8) & 255) / 255f, bl = (color & 255) / 255f;
        VertexConsumer vc = vp.getBuffer(RenderLayer.getLines());
        Matrix4f m = ms.peek().getPositionMatrix();
        edge(vc, m, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ, r, g, bl, a);
        edge(vc, m, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ, r, g, bl, a);
        edge(vc, m, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ, r, g, bl, a);
        edge(vc, m, b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ, r, g, bl, a);
        edge(vc, m, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, bl, a);
        edge(vc, m, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ, r, g, bl, a);
        edge(vc, m, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, bl, a);
        edge(vc, m, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ, r, g, bl, a);
        edge(vc, m, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ, r, g, bl, a);
        edge(vc, m, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, bl, a);
        edge(vc, m, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ, r, g, bl, a);
        edge(vc, m, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, bl, a);
    }

    private static void tracer(MatrixStack ms, VertexConsumerProvider vp, Entity e, Vec3d cam, int color) {
        Vec3d to = e.getPos().add(0, e.getHeight() * 0.5, 0).subtract(cam);
        float a = ((color >> 24) & 255) / 255f, r = ((color >> 16) & 255) / 255f, g = ((color >> 8) & 255) / 255f, b = (color & 255) / 255f;
        VertexConsumer vc = vp.getBuffer(RenderLayer.getLines());
        Matrix4f m = ms.peek().getPositionMatrix();
        edge(vc, m, 0, 0, 0, (float) to.x, (float) to.y, (float) to.z, r, g, b, a);
    }

    private static void edge(VertexConsumer vc, Matrix4f m, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        vc.vertex(m, (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(0, 1, 0);
        vc.vertex(m, (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(0, 1, 0);
    }

    public static void hud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        int y = 28;
        if (Modules.fpsHud) { ctx.drawTextWithShadow(mc.textRenderer, "FPS " + mc.getCurrentFps(), 6, y, 0xFF60A5FA); y += 11; }
        if (Modules.coordsHud) {
            var bp = mc.player.getBlockPos();
            ctx.drawTextWithShadow(mc.textRenderer, bp.getX() + " " + bp.getY() + " " + bp.getZ(), 6, y, 0xFFE2E8F0); y += 11;
        }
        if (Modules.targetHud && (Modules.triggerBot || Modules.autoMace)) {
            ctx.drawTextWithShadow(mc.textRenderer, "PoufDLC combat", 6, y, 0xFFF87171);
        }
    }
}

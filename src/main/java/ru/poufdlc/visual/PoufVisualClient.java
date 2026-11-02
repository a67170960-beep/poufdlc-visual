package ru.poufdlc.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class PoufVisualClient implements ClientModInitializer {
    public static float cornerRadius = 8.0f;
    public static boolean hudEnabled = true;
    public static final String HUD_TITLE = "PoufDLC Beta";

    public static boolean flyEnabled = false;
    public static boolean speedEnabled = false;
    public static boolean fullBrightEnabled = false;
    public static boolean noFallEnabled = false;
    public static boolean killAuraEnabled = false;

    private static final double FLY_SPEED = 0.8;
    private static final double SPRINT_MULTIPLIER = 2.5;
    private static final float KILL_AURA_RANGE = 4.5f;

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.poufdlc-visual.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.poufdlc-visual"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new VisualConfigScreen(client.currentScreen));
                }
            }
            if (client.player != null && client.world != null) {
                applyModules(client);
            }
        });

        HudRenderCallback.EVENT.register(PoufVisualClient::renderHud);
    }

    private static void applyModules(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        applyFly(player);
        applySpeed(player);
        applyFullBright(player);
        applyNoFall(player);
        applyKillAura(client);
    }

    private static void applyFly(ClientPlayerEntity player) {
        if (!flyEnabled) {
            player.getAbilities().flying = false;
            return;
        }
        player.getAbilities().flying = true;
        player.getAbilities().setFlySpeed((float) (FLY_SPEED / 10.0));

        Vec3d velocity = player.getVelocity();
        double motionY = 0.0;
        if (player.input.jumping) motionY = FLY_SPEED * 0.5;
        if (player.input.sneaking) motionY = -FLY_SPEED * 0.5;

        player.setVelocity(velocity.x, motionY, velocity.z);
        player.velocityModified = true;
    }

    private static void applySpeed(ClientPlayerEntity player) {
        if (!speedEnabled || !player.isOnGround()) return;
        if (player.forwardSpeed == 0 && player.sidewaysSpeed == 0) return;

        Vec3d velocity = player.getVelocity();
        double yaw = Math.toRadians(player.getYaw());
        double speed = SPRINT_MULTIPLIER * 0.2;

        double mx = -Math.sin(yaw) * speed;
        double mz = Math.cos(yaw) * speed;

        if (player.sidewaysSpeed != 0) {
            double sideYaw = yaw + (player.sidewaysSpeed > 0 ? -Math.PI / 2 : Math.PI / 2);
            mx += -Math.sin(sideYaw) * speed * 0.7;
            mz += Math.cos(sideYaw) * speed * 0.7;
        }

        player.setVelocity(mx, velocity.y, mz);
        player.velocityModified = true;
    }

    private static void applyFullBright(ClientPlayerEntity player) {
        if (fullBrightEnabled) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false));
        } else {
            player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    private static void applyNoFall(ClientPlayerEntity player) {
        if (noFallEnabled) {
            player.fallDistance = 0.0f;
        }
    }

    private static void applyKillAura(MinecraftClient client) {
        if (!killAuraEnabled || client.player == null || client.interactionManager == null) {
            Rotation.reset();
            return;
        }

        LivingEntity best = null;
        double bestDist = KILL_AURA_RANGE;

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == client.player || !living.isAlive()) continue;
            double dist = client.player.distanceTo(entity);
            if (dist > bestDist) continue;
            best = living;
            bestDist = dist;
        }

        if (best == null) {
            Rotation.reset();
            return;
        }

        Rotation.update(client);
        Rotation.lookAt(best);

        float yawDiff = Math.abs(MathHelper.wrapDegrees(Rotation.getServerYaw() - client.player.getYaw()));
        if (yawDiff < 35.0f || Rotation.isRotating()) {
            client.interactionManager.attackEntity(client.player, best);
            client.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (!hudEnabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null) return;

        int x = 6;
        int y = 6;
        int pad = 4;
        String text = HUD_TITLE;
        int tw = mc.textRenderer.getWidth(text);
        int th = mc.textRenderer.fontHeight;
        int w = tw + pad * 2;
        int h = th + pad * 2;

        int bg = 0xAA101018;
        int border = 0xFF7B5CFF;
        fillRounded(context, x, y, x + w, y + h, (int) Math.max(1, cornerRadius / 2), bg);
        context.drawBorder(x, y, w, h, border);
        context.drawTextWithShadow(mc.textRenderer, text, x + pad, y + pad, 0xFFE8E0FF);
    }

    static void fillRounded(DrawContext ctx, int x1, int y1, int x2, int y2, int r, int color) {
        r = Math.min(r, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
        ctx.fill(x1 + r, y1, x2 - r, y2, color);
        ctx.fill(x1, y1 + r, x2, y2 - r, color);
        ctx.fill(x1, y1, x1 + r, y1 + r, color);
        ctx.fill(x2 - r, y1, x2, y1 + r, color);
        ctx.fill(x1, y2 - r, x1 + r, y2, color);
        ctx.fill(x2 - r, y2 - r, x2, y2, color);
    }
}

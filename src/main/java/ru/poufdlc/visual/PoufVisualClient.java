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
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

public class PoufVisualClient implements ClientModInitializer {
    public static float cornerRadius = 8.0f;
    public static boolean hudEnabled = true;
    public static final String HUD_TITLE = "PoufDLC Beta";

    public static boolean flyEnabled = false;
    public static boolean speedEnabled = false;
    public static boolean fullBrightEnabled = false;
    public static boolean noFallEnabled = false;

    private static final double FLY_SPEED = 0.8;
    private static final double SPRINT_MULTIPLIER = 2.5;

    private static KeyBinding openGuiKey;
    private static long lastAttackMs = 0;

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
        EspRenderer.register();
    }

    private static void applyModules(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        applyFly(client, player);
        applySpeed(player);
        applyFullBright(player);
        applyNoFall(player);
        applyKillAura(client);
        AutoMace.tick(client);
        AutoAim.tick(client);
    }

    private static void applyFly(MinecraftClient client, ClientPlayerEntity player) {
        if (!flyEnabled) {
            player.getAbilities().flying = false;
            return;
        }
        player.getAbilities().flying = true;
        player.getAbilities().setFlySpeed((float) (FLY_SPEED / 10.0));
        Vec3d velocity = player.getVelocity();
        double motionY = 0.0;
        if (client.options.jumpKey.isPressed()) motionY = FLY_SPEED * 0.5;
        if (client.options.sneakKey.isPressed()) motionY = -FLY_SPEED * 0.5;
        player.setVelocity(velocity.x, motionY, velocity.z);
        player.velocityModified = true;
    }

    private static void applySpeed(ClientPlayerEntity player) {
        if (!speedEnabled || !player.isOnGround()) return;
        if (player.input.movementForward == 0 && player.input.movementSideways == 0) return;
        Vec3d velocity = player.getVelocity();
        double yaw = Math.toRadians(player.getYaw());
        double speed = SPRINT_MULTIPLIER * 0.2;
        double mx = -Math.sin(yaw) * speed;
        double mz = Math.cos(yaw) * speed;
        if (player.input.movementSideways != 0) {
            double sideYaw = yaw + (player.input.movementSideways > 0 ? -Math.PI / 2 : Math.PI / 2);
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
        if (noFallEnabled) player.fallDistance = 0.0f;
    }

    private static void applyKillAura(MinecraftClient client) {
        if (!KillAuraConfig.enabled || client.player == null || client.interactionManager == null) {
            Rotation.reset();
            return;
        }

        if (KillAuraConfig.adaptive) Rotation.tickAdapt();

        LivingEntity target = findTarget(client);
        if (target == null) {
            Rotation.reset();
            return;
        }

        Rotation.update(client);
        Rotation.lookAt(target, KillAuraConfig.rotationSpeed, KillAuraConfig.aimJitter);

        // Ждём пока ротация достаточно наведена
        if (!Rotation.isLookingAt(target, KillAuraConfig.aimAngle)) return;

        long now = System.currentTimeMillis();
        int cps = ThreadLocalRandom.current().nextInt(KillAuraConfig.minCps, KillAuraConfig.maxCps + 1);
        long delay = 1000L / Math.max(1, cps);
        if (now - lastAttackMs < delay) return;

        // Криты
        boolean canCrit = client.player.fallDistance > KillAuraConfig.critFallDistance
                && !client.player.isOnGround()
                && !client.player.isClimbing()
                && !client.player.isTouchingWater()
                && !client.player.hasVehicle();

        if (KillAuraConfig.onlyCrits && !canCrit) return;

        if (KillAuraConfig.crits && canCrit) {
            // уже в воздухе — обычный удар даст крит
        } else if (KillAuraConfig.crits && client.player.isOnGround() && !client.options.jumpKey.isPressed()) {
            // лёгкий подпрыг для крита (если не зажат прыжок игроком)
            client.player.jump();
        }

        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
        lastAttackMs = now;

        if (!KillAuraConfig.keepSprint) {
            client.player.setSprinting(false);
        }
    }

    private static LivingEntity findTarget(MinecraftClient client) {
        LivingEntity best = null;
        double bestDist = KillAuraConfig.range;

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == client.player || !living.isAlive()) continue;

            if (living instanceof PlayerEntity && !KillAuraConfig.attackPlayers) continue;
            if (living instanceof MobEntity && !KillAuraConfig.attackMobs) continue;
            if (!(living instanceof PlayerEntity) && !(living instanceof MobEntity)) continue;

            double dist = client.player.distanceTo(entity);
            if (dist > bestDist) continue;

            if (!KillAuraConfig.throughWalls && !client.player.canSee(entity)) continue;

            // FOV check по серверной ротации
            Vec3d eyes = client.player.getEyePos();
            Vec3d center = entity.getPos().add(0, entity.getHeight() * 0.5, 0);
            Vec3d dir = center.subtract(eyes).normalize();
            float yaw = (float) (MathHelper.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0f;
            float diff = Math.abs(MathHelper.wrapDegrees(yaw - Rotation.getServerYaw()));
            if (diff > KillAuraConfig.fov * 0.5f) continue;

            best = living;
            bestDist = dist;
        }
        return best;
    }

    private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (!hudEnabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null) return;

        int x = 6, y = 6, pad = 4;
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
        EspRenderer.renderHudOverlay(context);
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

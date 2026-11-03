package ru.poufdlc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import ru.poufdlc.combat.AutoMace;
import ru.poufdlc.combat.Modules;
import ru.poufdlc.combat.TriggerBot;
import ru.poufdlc.render.EspRenderer;
import ru.poufdlc.ui.ClickMenu;

public class PoufClient implements ClientModInitializer {
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.poufdlc.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.poufdlc"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.player != null) client.setScreen(new ClickMenu(client.currentScreen));
            }
            if (client.player == null || client.world == null) return;
            move(client);
            TriggerBot.tick(client);
            AutoMace.tick(client);
            if (Modules.fullBright) {
                client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 240, 0, false, false));
            }
        });

        HudRenderCallback.EVENT.register((ctx, tick) -> {
            if (Modules.hud) {
                var mc = MinecraftClient.getInstance();
                if (mc.player != null && !mc.options.hudHidden) {
                    ctx.drawTextWithShadow(mc.textRenderer, "PoufDLC", 6, 6, 0xFF93C5FD);
                    EspRenderer.hud(ctx);
                }
            }
        });
        EspRenderer.register();
    }

    private static void move(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (Modules.fly) {
            p.getAbilities().flying = true;
            p.getAbilities().setFlySpeed(0.08f);
            Vec3d v = p.getVelocity();
            double my = 0;
            if (client.options.jumpKey.isPressed()) my = 0.4;
            if (client.options.sneakKey.isPressed()) my = -0.4;
            p.setVelocity(v.x, my, v.z);
            p.velocityModified = true;
        } else {
            p.getAbilities().flying = false;
        }
        if (Modules.speed && p.isOnGround() && (p.input.movementForward != 0 || p.input.movementSideways != 0)) {
            double yaw = Math.toRadians(p.getYaw());
            double sp = 0.45;
            p.setVelocity(-Math.sin(yaw) * sp, p.getVelocity().y, Math.cos(yaw) * sp);
            p.velocityModified = true;
        }
        if (Modules.noFall) p.fallDistance = 0;
    }
}

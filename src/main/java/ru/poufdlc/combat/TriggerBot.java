package ru.poufdlc.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.poufdlc.util.CritUtil;
import ru.poufdlc.util.Rotation;

import java.util.concurrent.ThreadLocalRandom;

/**
 * SystemDLC-style TriggerBot + double mace crit on wind-charge fall.
 */
public final class TriggerBot {
    private static long lastHit = 0;
    private static int comboLeft = 0;   // remaining hits in double-crit burst
    private static long comboUntil = 0;

    private TriggerBot() {}

    public static void tick(MinecraftClient mc) {
        if (!Modules.triggerBot && !Modules.autoAim && !Modules.autoMace) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (Modules.adaptive) Rotation.adapt();

        ClientPlayerEntity p = mc.player;
        LivingEntity target = find(mc);
        if (target == null) {
            if (!Modules.autoMace) Rotation.reset();
            return;
        }

        // Auto aim always when enabled
        if (Modules.autoAim || Modules.triggerBot || Modules.autoMace) {
            Rotation.update(p);
            float spd = Modules.autoMace && !p.isOnGround() ? Modules.amRotSpeed : Modules.aimSpeed;
            Rotation.lookAt(target, spd, Modules.aimJitter);
        }

        if (!Modules.triggerBot && !Modules.autoMace) return;

        boolean aimed = Rotation.lookingAt(target, Modules.aimAngle) || mc.targetedEntity == target;
        if (!aimed) return;

        long now = System.currentTimeMillis();

        // Double-crit burst window (mace + falling after wind charge)
        boolean maceFall = CritUtil.holdingMace(p) && CritUtil.inCritWindow(p) && p.fallDistance >= Modules.minFall;
        if (Modules.doubleCrit && maceFall && comboLeft <= 0) {
            comboLeft = 2;
            comboUntil = now + 350;
        }

        if (comboLeft > 0 && now <= comboUntil) {
            if (Modules.maceSwap) swapMace(p);
            mc.interactionManager.attackEntity(p, target);
            p.swingHand(Hand.MAIN_HAND);
            comboLeft--;
            lastHit = now;
            return;
        }

        // Normal trigger pace
        int cps = ThreadLocalRandom.current().nextInt(Modules.minCps, Modules.maxCps + 1);
        if (now - lastHit < 1000L / Math.max(1, cps)) return;

        if (Modules.critOnly && !CritUtil.inCritWindow(p) && p.isOnGround()) {
            // try jump for crit if on ground
            if (p.isOnGround() && !mc.options.jumpKey.isPressed()) {
                p.jump();
            }
            return;
        }

        if (Modules.critOnly && !CritUtil.inCritWindow(p) && p.fallDistance < Modules.minFall) return;

        if (Modules.maceSwap && CritUtil.holdingMace(p) == false) {
            int slot = CritUtil.findMaceSlot(p);
            if (slot >= 0) p.getInventory().selectedSlot = slot;
        }

        mc.interactionManager.attackEntity(p, target);
        p.swingHand(Hand.MAIN_HAND);
        lastHit = now;
    }

    private static void swapMace(ClientPlayerEntity p) {
        int slot = CritUtil.findMaceSlot(p);
        if (slot >= 0) p.getInventory().selectedSlot = slot;
    }

    private static LivingEntity find(MinecraftClient mc) {
        LivingEntity best = null;
        double bestD = Modules.triggerBot ? Modules.tbRange : Modules.amRange;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity liv) || liv == mc.player || !liv.isAlive()) continue;
            if (liv instanceof PlayerEntity && !Modules.players) continue;
            if (liv instanceof MobEntity && !Modules.mobs) continue;
            if (!(liv instanceof PlayerEntity) && !(liv instanceof MobEntity)) continue;
            double d = mc.player.distanceTo(e);
            if (d > bestD) continue;
            if (!mc.player.canSee(e)) continue;
            Vec3d eyes = mc.player.getEyePos();
            Vec3d c = e.getPos().add(0, e.getHeight() * 0.5, 0);
            Vec3d dir = c.subtract(eyes).normalize();
            float yaw = (float) (MathHelper.atan2(dir.z, dir.x) * (180 / Math.PI)) - 90f;
            float diff = Math.abs(MathHelper.wrapDegrees(yaw - mc.player.getYaw()));
            if (diff > Modules.tbFov * 0.5f) continue;
            best = liv; bestD = d;
        }
        return best;
    }
}

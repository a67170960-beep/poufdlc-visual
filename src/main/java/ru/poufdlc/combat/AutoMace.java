package ru.poufdlc.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import ru.poufdlc.util.CritUtil;
import ru.poufdlc.util.Rotation;

/**
 * Wind charge down → multi-jump while airborne → double mace smash.
 */
public final class AutoMace {
    private enum St { IDLE, AIR, DONE }
    private static St st = St.IDLE;
    private static LivingEntity target;
    private static long lastLaunch, lastJump;
    private static int savedSlot = -1;

    private AutoMace() {}

    public static void tick(MinecraftClient mc) {
        if (!Modules.autoMace || mc.player == null || mc.world == null || mc.interactionManager == null) {
            st = St.IDLE; return;
        }
        ClientPlayerEntity p = mc.player;

        if (target == null || !target.isAlive() || p.distanceTo(target) > Modules.amRange * 1.4) {
            target = find(mc);
        }
        if (target == null) { st = St.IDLE; return; }

        boolean grounded = p.isOnGround();
        double vy = p.getVelocity().y;

        // Player already airborne (manual wind charge) → take over
        if (st == St.IDLE && !grounded) st = St.AIR;

        switch (st) {
            case IDLE -> {
                if (!grounded) return;
                if (p.distanceTo(target) < 2.5) return;
                if (System.currentTimeMillis() - lastLaunch < Modules.amCooldown) return;
                if (Modules.amAutoWind && throwWind(mc)) {
                    st = St.AIR;
                    lastLaunch = System.currentTimeMillis();
                }
            }
            case AIR -> {
                Rotation.update(p);
                Rotation.lookAt(target, Modules.amRotSpeed, Modules.aimJitter * 0.6f);

                // Multi-jump while going up / early fall — stacks fallDistance for mace
                if (Modules.amMultiJump && !grounded && System.currentTimeMillis() - lastJump > 120) {
                    if (vy > -0.2 || p.fallDistance < Modules.amMinFall + 1.5f) {
                        p.jump();
                        lastJump = System.currentTimeMillis();
                    }
                }

                // Hits handled by TriggerBot doubleCrit when falling with mace
                if (grounded && System.currentTimeMillis() - lastLaunch > 900) {
                    st = St.IDLE;
                    Rotation.reset();
                }
            }
            default -> { if (grounded) st = St.IDLE; }
        }
    }

    private static boolean throwWind(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        int slot = CritUtil.findWindSlot(p);
        if (slot < 0) return false;
        savedSlot = p.getInventory().selectedSlot;
        float yaw = p.getYaw(), pitch = p.getPitch();
        p.getInventory().selectedSlot = slot;
        // look straight down
        p.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                p.getX(), p.getY(), p.getZ(), yaw, 90f, p.isOnGround(), p.horizontalCollision));
        mc.interactionManager.interactItem(p, Hand.MAIN_HAND);
        p.swingHand(Hand.MAIN_HAND);
        p.getInventory().selectedSlot = savedSlot >= 0 ? savedSlot : p.getInventory().selectedSlot;
        p.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                p.getX(), p.getY(), p.getZ(), yaw, pitch, p.isOnGround(), p.horizontalCollision));
        return true;
    }

    private static LivingEntity find(MinecraftClient mc) {
        LivingEntity best = null;
        double bestD = Modules.amRange;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity liv) || liv == mc.player || !liv.isAlive()) continue;
            if (liv instanceof PlayerEntity && !Modules.players) continue;
            if (liv instanceof MobEntity && !Modules.mobs) continue;
            if (!(liv instanceof PlayerEntity) && !(liv instanceof MobEntity)) continue;
            double d = mc.player.distanceTo(e);
            if (d > bestD || !mc.player.canSee(e)) continue;
            best = liv; bestD = d;
        }
        return best;
    }
}

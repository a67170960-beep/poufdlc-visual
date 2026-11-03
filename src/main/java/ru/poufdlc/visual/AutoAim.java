package ru.poufdlc.visual;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.concurrent.ThreadLocalRandom;
public final class AutoAim {
    private static long lastClick = 0;
    private AutoAim() {}
    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) return;
        if (!AimConfig.autoAim && !AimConfig.triggerBot) return;
        if (AimConfig.adaptive) Rotation.tickAdapt();
        LivingEntity target = find(client);
        if (target == null) return;
        if (AimConfig.autoAim) {
            Rotation.update(client);
            Rotation.lookAt(target, AimConfig.speed, AimConfig.jitter);
        }
        if (AimConfig.triggerBot) {
            boolean aimed = Rotation.isLookingAt(target, AimConfig.aimAngle) || client.targetedEntity == target;
            if (!aimed) return;
            long now = System.currentTimeMillis();
            int cps = ThreadLocalRandom.current().nextInt(AimConfig.minCps, AimConfig.maxCps + 1);
            if (now - lastClick < 1000L / cps) return;
            client.interactionManager.attackEntity(client.player, target);
            client.player.swingHand(Hand.MAIN_HAND);
            lastClick = now;
        }
    }
    private static LivingEntity find(MinecraftClient client) {
        LivingEntity best = null;
        double bestD = AimConfig.range;
        for (Entity e : client.world.getEntities()) {
            if (!(e instanceof LivingEntity liv) || liv == client.player || !liv.isAlive()) continue;
            if (liv instanceof PlayerEntity && !AimConfig.players) continue;
            if (liv instanceof MobEntity && !AimConfig.mobs) continue;
            if (!(liv instanceof PlayerEntity) && !(liv instanceof MobEntity)) continue;
            double d = client.player.distanceTo(e);
            if (d > bestD) continue;
            if (AimConfig.onlyVisible && !client.player.canSee(e)) continue;
            if (!AimConfig.throughWalls && !client.player.canSee(e)) continue;
            Vec3d eyes = client.player.getEyePos();
            Vec3d c = e.getPos().add(0, e.getHeight() * 0.5, 0);
            Vec3d dir = c.subtract(eyes).normalize();
            float yaw = (float) (MathHelper.atan2(dir.z, dir.x) * (180 / Math.PI)) - 90f;
            float diff = Math.abs(MathHelper.wrapDegrees(yaw - client.player.getYaw()));
            if (diff > AimConfig.fov * 0.5f) continue;
            best = liv; bestD = d;
        }
        return best;
    }
}

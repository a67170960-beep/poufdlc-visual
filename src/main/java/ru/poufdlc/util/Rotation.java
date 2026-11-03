package ru.poufdlc.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.concurrent.ThreadLocalRandom;

/** Silent GCD rotation tuned for AI anticheats (Sloth). */
public final class Rotation {
    private static float sYaw, sPitch, lastYaw, lastPitch, lastDY, lastDP;
    private static boolean rotating;
    private static double sens = 0.5;
    private static double gcd = gcdOf(sens);

    private Rotation() {}

    public static void update(ClientPlayerEntity p) {
        if (!rotating) { sYaw = p.getYaw(); sPitch = p.getPitch(); }
        lastYaw = sYaw; lastPitch = sPitch;
    }

    public static void lookAt(Entity t, float maxSpeed, float jitter) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null || t == null) return;
        Vec3d eyes = p.getEyePos();
        double h = t.getHeight() * (0.35 + ThreadLocalRandom.current().nextDouble() * 0.3);
        Vec3d aim = t.getPos().add(
                (ThreadLocalRandom.current().nextDouble() - 0.5) * jitter,
                h,
                (ThreadLocalRandom.current().nextDouble() - 0.5) * jitter);
        double dx = aim.x - eyes.x, dy = aim.y - eyes.y, dz = aim.z - eyes.z;
        double xz = Math.sqrt(dx * dx + dz * dz);
        float ty = (float) (MathHelper.atan2(dz, dx) * (180 / Math.PI)) - 90f;
        float tp = (float) -(MathHelper.atan2(dy, xz) * (180 / Math.PI));
        ty = MathHelper.wrapDegrees(ty);
        tp = MathHelper.clamp(tp, -89f, 89f);
        float dyaw = MathHelper.wrapDegrees(ty - sYaw);
        float dpitch = tp - sPitch;
        float spd = maxSpeed * (0.4f + ThreadLocalRandom.current().nextFloat() * 0.4f);
        float sy = MathHelper.clamp(dyaw, -spd, spd);
        float sp = MathHelper.clamp(dpitch, -spd * 0.7f, spd * 0.7f);
        sy = snapGcd(sy); sp = snapGcd(sp);
        sy = sy * 0.72f + lastDY * 0.28f;
        sp = sp * 0.72f + lastDP * 0.28f;
        lastDY = sy; lastDP = sp;
        sYaw = MathHelper.wrapDegrees(sYaw + sy);
        sPitch = MathHelper.clamp(sPitch + sp, -89f, 89f);
        rotating = true;
        p.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                p.getX(), p.getY(), p.getZ(), sYaw, sPitch, p.isOnGround(), p.horizontalCollision));
    }

    public static boolean lookingAt(Entity t, float maxAng) {
        if (t == null) return false;
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return false;
        Vec3d dir = t.getPos().add(0, t.getHeight() * 0.5, 0).subtract(p.getEyePos()).normalize();
        float yaw = (float) (MathHelper.atan2(dir.z, dir.x) * (180 / Math.PI)) - 90f;
        return Math.abs(MathHelper.wrapDegrees(yaw - sYaw)) <= maxAng;
    }

    public static void reset() { rotating = false; lastDY = lastDP = 0; }
    public static float yaw() { return sYaw; }
    public static float pitch() { return sPitch; }
    public static boolean active() { return rotating; }

    public static void adapt() {
        if (ThreadLocalRandom.current().nextInt(180) == 0) {
            sens = 0.42 + ThreadLocalRandom.current().nextDouble() * 0.2;
            gcd = gcdOf(sens);
        }
    }

    private static float snapGcd(float v) {
        float g = (float) gcd;
        return v - (v % g);
    }
    private static double gcdOf(double s) {
        double f = s * 0.6 + 0.2;
        return f * f * f * 1.2;
    }
}

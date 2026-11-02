package ru.poufdlc.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

public final class Rotation {

    private static float serverYaw;
    private static float serverPitch;
    private static float lastServerYaw;
    private static float lastServerPitch;
    private static boolean rotating;
    private static long lastRotateMs;

    // Адаптивный профиль под AI-античиты (Sloth)
    private static double sensProfile = 0.48 + ThreadLocalRandom.current().nextDouble() * 0.12;
    private static double gcd = calcGcd(sensProfile);
    private static float lastDeltaYaw;
    private static float lastDeltaPitch;

    private Rotation() {}

    public static void update(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (!rotating) {
            serverYaw = player.getYaw();
            serverPitch = player.getPitch();
        }
        lastServerYaw = serverYaw;
        lastServerPitch = serverPitch;
    }

    public static void lookAt(Entity target, float maxSpeed, float jitter) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || target == null) return;

        Vec3d eyes = player.getEyePos();
        Vec3d aim = getAimPoint(target, jitter);

        double dx = aim.x - eyes.x;
        double dy = aim.y - eyes.y;
        double dz = aim.z - eyes.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float targetPitch = (float) -(MathHelper.atan2(dy, distXZ) * (180.0 / Math.PI));
        targetYaw = MathHelper.wrapDegrees(targetYaw);
        targetPitch = MathHelper.clamp(targetPitch, -89.0f, 89.0f);

        float dyaw = MathHelper.wrapDegrees(targetYaw - serverYaw);
        float dpitch = targetPitch - serverPitch;

        // Человеческая скорость + инерция (анти-snap)
        float accel = 0.35f + ThreadLocalRandom.current().nextFloat() * 0.35f;
        float speed = MathHelper.clamp(maxSpeed * accel, 8.0f, maxSpeed);

        // Ограничение по GCD + небольшой шум
        float stepYaw = clampStep(dyaw, speed);
        float stepPitch = clampStep(dpitch, speed * 0.7f);

        stepYaw = applyGcd(stepYaw);
        stepPitch = applyGcd(stepPitch);

        // Сглаживание с предыдущим кадром (убирает микро-рывки)
        stepYaw = stepYaw * 0.7f + lastDeltaYaw * 0.3f;
        stepPitch = stepPitch * 0.7f + lastDeltaPitch * 0.3f;
        lastDeltaYaw = stepYaw;
        lastDeltaPitch = stepPitch;

        serverYaw = MathHelper.wrapDegrees(serverYaw + stepYaw);
        serverPitch = MathHelper.clamp(serverPitch + stepPitch, -89.0f, 89.0f);
        rotating = true;
        lastRotateMs = System.currentTimeMillis();

        sendSilent(player, serverYaw, serverPitch);
    }

    public static void reset() {
        rotating = false;
        lastDeltaYaw = 0;
        lastDeltaPitch = 0;
    }

    public static float getServerYaw() { return serverYaw; }
    public static float getServerPitch() { return serverPitch; }
    public static boolean isRotating() { return rotating; }

    public static boolean isLookingAt(Entity target, float maxAngle) {
        if (target == null) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;
        Vec3d eyes = client.player.getEyePos();
        Vec3d center = target.getPos().add(0, target.getHeight() * 0.5, 0);
        Vec3d dir = center.subtract(eyes).normalize();
        float yaw = (float) (MathHelper.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0f;
        float diff = Math.abs(MathHelper.wrapDegrees(yaw - serverYaw));
        return diff <= maxAngle;
    }

    private static Vec3d getAimPoint(Entity target, float jitter) {
        // Центр тела + лёгкий случайный оффсет (не идеальный hitbox-центр)
        double h = target.getHeight() * (0.35 + ThreadLocalRandom.current().nextDouble() * 0.35);
        double jx = (ThreadLocalRandom.current().nextDouble() - 0.5) * jitter;
        double jz = (ThreadLocalRandom.current().nextDouble() - 0.5) * jitter;
        return target.getPos().add(jx, h, jz);
    }

    private static float clampStep(float delta, float max) {
        return MathHelper.clamp(delta, -max, max);
    }

    private static float applyGcd(float value) {
        float g = (float) gcd;
        return value - (value % g);
    }

    private static double calcGcd(double sensitivity) {
        double f = sensitivity * 0.6 + 0.2;
        return f * f * f * 1.2;
    }

    private static void sendSilent(ClientPlayerEntity player, float yaw, float pitch) {
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                player.getX(), player.getY(), player.getZ(),
                yaw, pitch,
                player.isOnGround(),
                player.horizontalCollision
        ));
    }

    // Периодически слегка меняем профиль чувствительности (адаптация)
    public static void tickAdapt() {
        if (ThreadLocalRandom.current().nextInt(200) == 0) {
            sensProfile = 0.45 + ThreadLocalRandom.current().nextDouble() * 0.18;
            gcd = calcGcd(sensProfile);
        }
    }
}

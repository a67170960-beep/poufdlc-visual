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

    private static final double GCD = getGcd(0.5);

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

    public static void lookAt(Entity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || target == null) return;

        Vec3d eyes = player.getEyePos();
        Vec3d aimPoint = getAimPoint(target);

        double dx = aimPoint.x - eyes.x;
        double dy = aimPoint.y - eyes.y;
        double dz = aimPoint.z - eyes.z;

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float targetPitch = (float) -(MathHelper.atan2(dy, distXZ) * (180.0 / Math.PI));

        targetYaw = MathHelper.wrapDegrees(targetYaw);
        targetPitch = MathHelper.clamp(targetPitch, -90.0f, 90.0f);

        float deltaYaw = MathHelper.wrapDegrees(targetYaw - serverYaw);
        float deltaPitch = targetPitch - serverPitch;

        float speed = 0.55f + ThreadLocalRandom.current().nextFloat() * 0.25f;
        float newYaw = serverYaw + deltaYaw * speed;
        float newPitch = serverPitch + deltaPitch * speed;

        newYaw = applyGcd(newYaw, lastServerYaw);
        newPitch = applyGcd(newPitch, lastServerPitch);
        newPitch = MathHelper.clamp(newPitch, -90.0f, 90.0f);

        serverYaw = newYaw;
        serverPitch = newPitch;
        rotating = true;

        sendSilent(player, serverYaw, serverPitch);
    }

    public static void reset() {
        rotating = false;
    }

    public static float getServerYaw() {
        return serverYaw;
    }

    public static boolean isRotating() {
        return rotating;
    }

    private static Vec3d getAimPoint(Entity target) {
        double height = target.getHeight() * (0.4 + ThreadLocalRandom.current().nextDouble() * 0.25);
        double offsetX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.15;
        double offsetZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.15;
        return target.getPos().add(offsetX, height, offsetZ);
    }

    private static void sendSilent(ClientPlayerEntity player, float yaw, float pitch) {
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                player.getX(), player.getY(), player.getZ(),
                yaw, pitch,
                player.isOnGround(),
                player.horizontalCollision
        ));
    }

    private static float applyGcd(float current, float previous) {
        float delta = current - previous;
        delta -= delta % GCD;
        return previous + delta;
    }

    private static double getGcd(double sensitivity) {
        double f = sensitivity * 0.6 + 0.2;
        return f * f * f * 1.2;
    }
}

package ru.poufdlc.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class AutoMace {

    private enum State { IDLE, WAIT_ASCEND, FALLING, DONE }

    private static State state = State.IDLE;
    private static LivingEntity target = null;
    private static long lastLaunchMs = 0;
    private static long lastAttackMs = 0;
    private static int prevSlot = -1;
    private static float savedYaw, savedPitch;

    private AutoMace() {}

    public static void tick(MinecraftClient client) {
        if (!AutoMaceConfig.enabled || client.player == null || client.world == null || client.interactionManager == null) {
            hardReset();
            return;
        }

        ClientPlayerEntity p = client.player;

        // Цель
        if (target == null || !target.isAlive() || p.distanceTo(target) > AutoMaceConfig.range * 1.5) {
            target = findTarget(client);
        }
        if (target == null) {
            state = State.IDLE;
            return;
        }

        double dist = p.distanceTo(target);
        boolean onGround = p.isOnGround();
        float fall = p.fallDistance;
        double velY = p.getVelocity().y;

        // Если игрок уже в воздухе (сам кинул заряд) — сразу работаем
        if (state == State.IDLE && !onGround && velY > 0.2) {
            state = State.WAIT_ASCEND;
        }
        if (state == State.IDLE && !onGround && velY < -0.1) {
            state = State.FALLING;
        }

        switch (state) {
            case IDLE -> {
                if (!onGround) return;
                if (dist < AutoMaceConfig.minTargetDistForLaunch) return;
                if (System.currentTimeMillis() - lastLaunchMs < AutoMaceConfig.launchCooldownMs) return;
                if (AutoMaceConfig.onlyWhenHoldingMace && !isMace(p) && !AutoMaceConfig.switchToMace) return;

                if (AutoMaceConfig.switchToMace) switchMace(client);

                if (AutoMaceConfig.autoWindCharge) {
                    if (throwWindDown(client)) {
                        state = State.WAIT_ASCEND;
                        lastLaunchMs = System.currentTimeMillis();
                    }
                }
            }

            case WAIT_ASCEND -> {
                // Жёсткая наводка сразу
                Rotation.update(client);
                Rotation.lookAt(target, AutoMaceConfig.rotationSpeed, AutoMaceConfig.aimJitter);

                if (velY < 0.05 && !onGround) {
                    state = State.FALLING;
                }
                if (onGround && System.currentTimeMillis() - lastLaunchMs > 1200) {
                    state = State.IDLE;
                }
            }

            case FALLING -> {
                // Максимально сильная наводка в падении
                Rotation.update(client);
                Rotation.lookAt(target, AutoMaceConfig.rotationSpeed * 1.15f, AutoMaceConfig.aimJitter * 0.5f);

                boolean fallOk = fall >= AutoMaceConfig.minFallDistance && fall <= AutoMaceConfig.maxFallDistance;
                boolean nearGround = closeToGround(client, AutoMaceConfig.hitHeightAboveGround);
                boolean inRange = dist <= AutoMaceConfig.hitRange;
                boolean aimed = Rotation.isLookingAt(target, AutoMaceConfig.aimAngle);

                if (fallOk && inRange && aimed && (nearGround || fall > 2.8f)) {
                    if (System.currentTimeMillis() - lastAttackMs >= AutoMaceConfig.attackCooldownMs) {
                        if (AutoMaceConfig.switchToMace) switchMace(client);
                        client.interactionManager.attackEntity(p, target);
                        p.swingHand(Hand.MAIN_HAND);
                        lastAttackMs = System.currentTimeMillis();
                        state = State.DONE;
                    }
                }

                if (onGround) {
                    state = State.IDLE;
                    Rotation.reset();
                }
            }

            case DONE -> {
                Rotation.update(client);
                Rotation.lookAt(target, AutoMaceConfig.rotationSpeed, AutoMaceConfig.aimJitter);
                if (onGround) {
                    state = State.IDLE;
                    Rotation.reset();
                }
            }
        }
    }

    /** Кидает Wind Charge строго под ноги — один тик, без задержек */
    private static boolean throwWindDown(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        int slot = AutoMaceConfig.windChargeSlot >= 0 ? AutoMaceConfig.windChargeSlot : findWind(p);
        if (slot < 0) return false;

        prevSlot = p.getInventory().selectedSlot;
        savedYaw = p.getYaw();
        savedPitch = p.getPitch();

        // Слот + взгляд строго вниз
        p.getInventory().selectedSlot = slot;
        p.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                p.getX(), p.getY(), p.getZ(),
                savedYaw, 90.0f,
                p.isOnGround(), p.horizontalCollision
        ));

        // Мгновенное использование
        client.interactionManager.interactItem(p, Hand.MAIN_HAND);
        p.swingHand(Hand.MAIN_HAND);

        // Возврат слота и взгляда
        p.getInventory().selectedSlot = prevSlot;
        p.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                p.getX(), p.getY(), p.getZ(),
                savedYaw, savedPitch,
                p.isOnGround(), p.horizontalCollision
        ));
        return true;
    }

    private static LivingEntity findTarget(MinecraftClient client) {
        LivingEntity best = null;
        double bestD = AutoMaceConfig.range;
        for (Entity e : client.world.getEntities()) {
            if (!(e instanceof LivingEntity liv) || liv == client.player || !liv.isAlive()) continue;
            if (liv instanceof PlayerEntity && !AutoMaceConfig.attackPlayers) continue;
            if (liv instanceof MobEntity && !AutoMaceConfig.attackMobs) continue;
            if (!(liv instanceof PlayerEntity) && !(liv instanceof MobEntity)) continue;
            double d = client.player.distanceTo(e);
            if (d > bestD) continue;
            if (!AutoMaceConfig.throughWalls && !client.player.canSee(e)) continue;
            best = liv;
            bestD = d;
        }
        return best;
    }

    private static int findWind(ClientPlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(Items.WIND_CHARGE)) return i;
        }
        return -1;
    }

    private static boolean isMace(ClientPlayerEntity p) {
        return p.getMainHandStack().isOf(Items.MACE);
    }

    private static void switchMace(MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isOf(Items.MACE)) {
                client.player.getInventory().selectedSlot = i;
                return;
            }
        }
    }

    private static boolean closeToGround(MinecraftClient client, float max) {
        ClientPlayerEntity p = client.player;
        Vec3d start = p.getPos();
        Vec3d end = start.add(0, -max - 0.6, 0);
        BlockHitResult hit = client.world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                p
        ));
        if (hit.getType() == HitResult.Type.MISS) return false;
        return (start.y - hit.getPos().y) <= max;
    }

    private static void hardReset() {
        state = State.IDLE;
        target = null;
        Rotation.reset();
    }
}

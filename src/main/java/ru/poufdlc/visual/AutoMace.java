package ru.poufdlc.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.ThreadLocalRandom;

public final class AutoMace {

    private enum State { IDLE, LAUNCHED, FALLING, ATTACKED }

    private static State state = State.IDLE;
    private static LivingEntity currentTarget = null;
    private static long lastLaunchMs = 0;
    private static long lastAttackMs = 0;
    private static int savedSlot = -1;

    private AutoMace() {}

    public static void tick(MinecraftClient client) {
        if (!AutoMaceConfig.enabled || client.player == null || client.world == null || client.interactionManager == null) {
            reset();
            return;
        }

        ClientPlayerEntity player = client.player;

        if (AutoMaceConfig.adaptive) {
            Rotation.tickAdapt();
        }

        // Поиск / удержание цели
        if (currentTarget == null || !currentTarget.isAlive() || player.distanceTo(currentTarget) > AutoMaceConfig.range * 1.4) {
            currentTarget = findTarget(client);
        }

        if (currentTarget == null) {
            state = State.IDLE;
            Rotation.reset();
            return;
        }

        double dist = player.distanceTo(currentTarget);
        float fall = player.fallDistance;
        boolean onGround = player.isOnGround();
        boolean holdingMace = isHoldingMace(player);

        switch (state) {
            case IDLE -> {
                if (!onGround) return;
                if (dist < AutoMaceConfig.minTargetDistForLaunch) return;
                if (System.currentTimeMillis() - lastLaunchMs < AutoMaceConfig.launchCooldownMs) return;

                if (AutoMaceConfig.onlyWhenHoldingMace && !holdingMace && !AutoMaceConfig.switchToMace) return;

                if (AutoMaceConfig.switchToMace) {
                    switchToMace(client);
                }

                if (AutoMaceConfig.autoWindCharge) {
                    if (tryThrowWindCharge(client)) {
                        state = State.LAUNCHED;
                        lastLaunchMs = System.currentTimeMillis();
                    }
                }
            }

            case LAUNCHED -> {
                // Сильная наводка пока летим вверх / начинаем падать
                Rotation.update(client);
                Rotation.lookAt(currentTarget, AutoMaceConfig.rotationSpeed, AutoMaceConfig.aimJitter);

                if (!onGround && player.getVelocity().y < 0.1) {
                    state = State.FALLING;
                }
                // если застряли на земле дольше 1.5с — сброс
                if (onGround && System.currentTimeMillis() - lastLaunchMs > 1500) {
                    state = State.IDLE;
                }
            }

            case FALLING -> {
                Rotation.update(client);
                Rotation.lookAt(currentTarget, AutoMaceConfig.rotationSpeed * 1.1f, AutoMaceConfig.aimJitter * 0.7f);

                boolean goodFall = fall >= AutoMaceConfig.minFallDistance && fall <= AutoMaceConfig.maxFallDistance;
                boolean closeToGround = isCloseToGround(client, AutoMaceConfig.hitHeightAboveGround);
                boolean inHitRange = dist <= AutoMaceConfig.hitRange;
                boolean looking = Rotation.isLookingAt(currentTarget, AutoMaceConfig.aimAngle);

                if (goodFall && (closeToGround || fall > 3.5) && inHitRange && looking) {
                    if (System.currentTimeMillis() - lastAttackMs >= AutoMaceConfig.attackCooldownMs) {
                        if (AutoMaceConfig.switchToMace) switchToMace(client);
                        client.interactionManager.attackEntity(player, currentTarget);
                        player.swingHand(Hand.MAIN_HAND);
                        lastAttackMs = System.currentTimeMillis();
                        state = State.ATTACKED;
                    }
                }

                if (onGround) {
                    state = State.IDLE;
                    Rotation.reset();
                }
            }

            case ATTACKED -> {
                // Добиваем ещё раз если ещё в воздухе и близко
                Rotation.update(client);
                Rotation.lookAt(currentTarget, AutoMaceConfig.rotationSpeed, AutoMaceConfig.aimJitter);

                if (onGround) {
                    state = State.IDLE;
                    Rotation.reset();
                }
            }
        }
    }

    private static LivingEntity findTarget(MinecraftClient client) {
        LivingEntity best = null;
        double bestDist = AutoMaceConfig.range;

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == client.player || !living.isAlive()) continue;
            if (living instanceof PlayerEntity && !AutoMaceConfig.attackPlayers) continue;
            if (living instanceof MobEntity && !AutoMaceConfig.attackMobs) continue;
            if (!(living instanceof PlayerEntity) && !(living instanceof MobEntity)) continue;

            double d = client.player.distanceTo(entity);
            if (d > bestDist) continue;
            if (!AutoMaceConfig.throughWalls && !client.player.canSee(entity)) continue;

            Vec3d eyes = client.player.getEyePos();
            Vec3d center = entity.getPos().add(0, entity.getHeight() * 0.5, 0);
            Vec3d dir = center.subtract(eyes).normalize();
            float yaw = (float) (MathHelper.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0f;
            float diff = Math.abs(MathHelper.wrapDegrees(yaw - client.player.getYaw()));
            if (diff > AutoMaceConfig.fov * 0.5f) continue;

            best = living;
            bestDist = d;
        }
        return best;
    }

    private static boolean tryThrowWindCharge(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        int slot = AutoMaceConfig.windChargeSlot >= 0 ? AutoMaceConfig.windChargeSlot : findWindChargeSlot(player);
        if (slot == -1) return false;

        savedSlot = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = slot;

        // Смотрим строго вниз и используем
        player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround(
                player.getYaw(), 90.0f, player.isOnGround(), player.horizontalCollision
        ));

        client.interactionManager.interactItem(player, Hand.MAIN_HAND);
        player.swingHand(Hand.MAIN_HAND);

        // возвращаем слот чуть позже логикой (сразу можно вернуть)
        if (savedSlot >= 0) {
            player.getInventory().selectedSlot = savedSlot;
        }
        return true;
    }

    private static int findWindChargeSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.WIND_CHARGE)) return i;
        }
        return -1;
    }

    private static boolean isHoldingMace(ClientPlayerEntity player) {
        return player.getMainHandStack().isOf(Items.MACE);
    }

    private static void switchToMace(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(Items.MACE)) {
                player.getInventory().selectedSlot = i;
                return;
            }
        }
    }

    private static boolean isCloseToGround(MinecraftClient client, float maxDist) {
        ClientPlayerEntity player = client.player;
        Vec3d start = player.getPos();
        Vec3d end = start.add(0, -maxDist - 0.5, 0);
        BlockHitResult result = client.world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
        ));
        if (result.getType() == HitResult.Type.MISS) return false;
        double dist = start.y - result.getPos().y;
        return dist <= maxDist;
    }

    private static void reset() {
        state = State.IDLE;
        currentTarget = null;
        Rotation.reset();
    }
}

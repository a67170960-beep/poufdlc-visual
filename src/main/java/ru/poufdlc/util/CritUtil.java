package ru.poufdlc.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.TridentItem;

public final class CritUtil {
    private CritUtil() {}

    public static boolean blockingState(ClientPlayerEntity p) {
        return p.isTouchingWater() || p.isInLava() || p.isClimbing()
                || p.hasVehicle() || p.isGliding() || p.isSwimming();
    }

    public static boolean inCritWindow(ClientPlayerEntity p) {
        if (p == null || p.isOnGround() || blockingState(p)) return false;
        return p.getVelocity().y < -0.01;
    }

    public static boolean goodFall(ClientPlayerEntity p, float min) {
        return inCritWindow(p) && p.fallDistance >= min;
    }

    public static boolean holdingMace(ClientPlayerEntity p) {
        return p.getMainHandStack().getItem() instanceof MaceItem
                || p.getMainHandStack().isOf(Items.MACE);
    }

    public static boolean heavyWeapon(ClientPlayerEntity p) {
        var it = p.getMainHandStack().getItem();
        return it instanceof MaceItem || it instanceof AxeItem || it instanceof TridentItem
                || p.getMainHandStack().isOf(Items.MACE);
    }

    public static int findMaceSlot(ClientPlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).isOf(Items.MACE)) return i;
        }
        return -1;
    }

    public static int findWindSlot(ClientPlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).isOf(Items.WIND_CHARGE)) return i;
        }
        return -1;
    }
}

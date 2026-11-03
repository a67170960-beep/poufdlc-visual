package ru.poufdlc.combat;

public final class Modules {
    // Combat
    public static boolean triggerBot = false;
    public static boolean autoAim = false;
    public static boolean autoMace = false;
    public static boolean maceSwap = true;
    public static boolean critOnly = true;
    public static boolean doubleCrit = true;      // 2 hits on wind-charge fall
    public static float tbRange = 3.6f;
    public static float tbFov = 80f;
    public static float aimSpeed = 52f;
    public static float aimJitter = 0.12f;
    public static float aimAngle = 24f;
    public static int minCps = 7;
    public static int maxCps = 11;
    public static float minFall = 0.15f;
    public static boolean players = true;
    public static boolean mobs = false;
    public static boolean adaptive = true;

    // AutoMace
    public static float amRange = 6.5f;
    public static float amHitRange = 4.0f;
    public static float amMinFall = 1.2f;
    public static float amHitHeight = 2.4f;
    public static float amRotSpeed = 90f;
    public static boolean amAutoWind = true;
    public static boolean amMultiJump = true;     // spam jump while airborne
    public static long amCooldown = 2200;

    // Movement
    public static boolean fly = false;
    public static boolean speed = false;
    public static boolean noFall = false;
    public static boolean noJumpDelay = true;

    // Visuals
    public static boolean hud = true;
    public static boolean boxEsp = true;
    public static boolean tracers = true;
    public static boolean nametags = true;
    public static boolean healthBar = true;
    public static boolean itemEsp = false;
    public static boolean fullBright = false;
    public static boolean fpsHud = true;
    public static boolean coordsHud = false;
    public static boolean targetHud = true;
    public static boolean armorHud = true;
    public static boolean chinaHat = false;
    public static boolean skeletonEsp = false;
    public static boolean holeEsp = false;
    public static boolean storageEsp = false;
    public static boolean viewModel = false;
    public static boolean crosshairCustom = false;
    public static boolean potionHud = true;
    public static boolean directionHud = false;
    public static boolean playerCount = false;
    public static boolean hitMarkers = true;
    public static float espRange = 64f;

    private Modules() {}
}

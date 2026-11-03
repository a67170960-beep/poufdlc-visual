package ru.poufdlc.visual;

public final class AutoMaceConfig {
    public static boolean enabled = false;

    // Цель
    public static float range = 6.0f;              // 3.0 – 10.0
    public static float fov = 120.0f;
    public static boolean attackPlayers = true;
    public static boolean attackMobs = false;
    public static boolean throughWalls = false;

    // Wind Charge
    public static boolean autoWindCharge = true;
    public static int windChargeSlot = -1;         // -1 = искать автоматически
    public static float minTargetDistForLaunch = 2.8f;
    public static long launchCooldownMs = 2500;

    // Удар булавой
    public static float minFallDistance = 1.5f;    // минимальная высота падения для удара
    public static float maxFallDistance = 20.0f;
    public static float hitRange = 4.0f;           // дистанция удара в воздухе
    public static float hitHeightAboveGround = 2.2f; // бить когда до земли примерно столько блоков
    public static boolean onlyWhenHoldingMace = true;
    public static boolean switchToMace = true;

    // Ротация (очень сильная наводка)
    public static float rotationSpeed = 85.0f;     // 40 – 120
    public static float aimJitter = 0.08f;
    public static float aimAngle = 18.0f;

    // Поведение
    public static boolean adaptive = true;
    public static long attackCooldownMs = 800;

    private AutoMaceConfig() {}
}

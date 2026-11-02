package ru.poufdlc.visual;

public final class KillAuraConfig {
    public static boolean enabled = false;

    // Основные
    public static float range = 3.6f;           // 2.5 – 4.5
    public static float fov = 90.0f;            // 30 – 180
    public static int minCps = 8;
    public static int maxCps = 12;

    // Ротация
    public static float rotationSpeed = 45.0f;  // 20 – 90
    public static float aimJitter = 0.18f;      // 0.05 – 0.40
    public static float aimAngle = 28.0f;       // допуск перед ударом

    // Криты
    public static boolean crits = true;
    public static boolean onlyCrits = false;    // бить только если крит возможен
    public static float critFallDistance = 0.12f;

    // Поведение
    public static boolean throughWalls = false;
    public static boolean attackPlayers = true;
    public static boolean attackMobs = false;
    public static boolean keepSprint = false;

    // Адаптив
    public static boolean adaptive = true;

    private KillAuraConfig() {}
}

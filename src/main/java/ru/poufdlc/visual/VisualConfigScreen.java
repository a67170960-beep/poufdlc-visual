package ru.poufdlc.visual;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
public class VisualConfigScreen extends Screen {
    private final Screen parent;
    private int cat = 0;
    private final String[] cats = {"Бой", "Движение", "Визуалы", "Игрок", "Прицел"};
    private final List<ClickableWidget> widgets = new ArrayList<>();
    public VisualConfigScreen(Screen parent) { super(Text.literal("PoufDLC")); this.parent = parent; }
    @Override protected void init() {
        clearChildren(); widgets.clear();
        int side = 100;
        for (int i = 0; i < cats.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal(cats[i]), b -> { cat = idx; init(); }).dimensions(10, 48 + i * 30, side - 16, 26).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> { if (client != null) client.setScreen(parent); }).dimensions(10, height - 32, side - 16, 22).build());
        int x = side + 24, y = 50, g = 24, w = Math.min(220, width - side - 40);
        if (cat == 0) {
            add(t(x, y, w, "Киллаура", () -> KillAuraConfig.enabled, v -> KillAuraConfig.enabled = v)); y += g;
            add(s(x, y, w, "Дистанция КА", KillAuraConfig.range, 2.5, 4.5, v -> KillAuraConfig.range = v.floatValue())); y += g;
            add(s(x, y, w, "КПС мин", KillAuraConfig.minCps, 4, 16, v -> KillAuraConfig.minCps = v.intValue())); y += g;
            add(s(x, y, w, "КПС макс", KillAuraConfig.maxCps, 6, 18, v -> KillAuraConfig.maxCps = v.intValue())); y += g;
            add(t(x, y, w, "Криты", () -> KillAuraConfig.crits, v -> KillAuraConfig.crits = v)); y += g;
            add(t(x, y, w, "Адаптив КА", () -> KillAuraConfig.adaptive, v -> KillAuraConfig.adaptive = v)); y += g + 4;
            add(t(x, y, w, "АвтоБулава", () -> AutoMaceConfig.enabled, v -> AutoMaceConfig.enabled = v)); y += g;
            add(s(x, y, w, "Дистанция АБ", AutoMaceConfig.range, 3, 10, v -> AutoMaceConfig.range = v.floatValue())); y += g;
            add(s(x, y, w, "Скорость наводки АБ", AutoMaceConfig.rotationSpeed, 50, 130, v -> AutoMaceConfig.rotationSpeed = v.floatValue())); y += g;
            add(t(x, y, w, "Авто Заряд Ветра", () -> AutoMaceConfig.autoWindCharge, v -> AutoMaceConfig.autoWindCharge = v)); y += g;
            add(t(x, y, w, "Переключать булаву", () -> AutoMaceConfig.switchToMace, v -> AutoMaceConfig.switchToMace = v));
        } else if (cat == 1) {
            add(t(x, y, w, "Полёт", () -> PoufVisualClient.flyEnabled, v -> PoufVisualClient.flyEnabled = v)); y += g;
            add(t(x, y, w, "Скорость", () -> PoufVisualClient.speedEnabled, v -> PoufVisualClient.speedEnabled = v)); y += g;
            add(t(x, y, w, "Нет падения", () -> PoufVisualClient.noFallEnabled, v -> PoufVisualClient.noFallEnabled = v));
        } else if (cat == 2) {
            add(t(x, y, w, "HUD", () -> PoufVisualClient.hudEnabled, v -> PoufVisualClient.hudEnabled = v)); y += g;
            add(t(x, y, w, "Бокс ESP", () -> VisualsConfig.boxEsp, v -> VisualsConfig.boxEsp = v)); y += g;
            add(t(x, y, w, "Трейсеры", () -> VisualsConfig.tracers, v -> VisualsConfig.tracers = v)); y += g;
            add(t(x, y, w, "Неймтеги", () -> VisualsConfig.nametags, v -> VisualsConfig.nametags = v)); y += g;
            add(t(x, y, w, "Полоска HP", () -> VisualsConfig.healthBar, v -> VisualsConfig.healthBar = v)); y += g;
            add(t(x, y, w, "ESP предметов", () -> VisualsConfig.itemEsp, v -> VisualsConfig.itemEsp = v)); y += g;
            add(t(x, y, w, "Target HUD", () -> VisualsConfig.targetHud, v -> VisualsConfig.targetHud = v)); y += g;
            add(t(x, y, w, "FPS", () -> VisualsConfig.fpsHud, v -> VisualsConfig.fpsHud = v)); y += g;
            add(t(x, y, w, "Координаты", () -> VisualsConfig.coordsHud, v -> VisualsConfig.coordsHud = v)); y += g;
            add(t(x, y, w, "Полная яркость", () -> PoufVisualClient.fullBrightEnabled, v -> PoufVisualClient.fullBrightEnabled = v)); y += g;
            add(s(x, y, w, "Дальность ESP", VisualsConfig.espRange, 16, 128, v -> VisualsConfig.espRange = v.floatValue()));
        } else if (cat == 3) {
            add(t(x, y, w, "КА Игроки", () -> KillAuraConfig.attackPlayers, v -> KillAuraConfig.attackPlayers = v)); y += g;
            add(t(x, y, w, "КА Мобы", () -> KillAuraConfig.attackMobs, v -> KillAuraConfig.attackMobs = v)); y += g;
            add(t(x, y, w, "АБ Игроки", () -> AutoMaceConfig.attackPlayers, v -> AutoMaceConfig.attackPlayers = v)); y += g;
            add(t(x, y, w, "АБ Мобы", () -> AutoMaceConfig.attackMobs, v -> AutoMaceConfig.attackMobs = v));
        } else {
            add(t(x, y, w, "АвтоПрицел", () -> AimConfig.autoAim, v -> AimConfig.autoAim = v)); y += g;
            add(t(x, y, w, "ТриггерБот", () -> AimConfig.triggerBot, v -> AimConfig.triggerBot = v)); y += g;
            add(s(x, y, w, "Дистанция", AimConfig.range, 2.5, 6, v -> AimConfig.range = v.floatValue())); y += g;
            add(s(x, y, w, "FOV", AimConfig.fov, 30, 180, v -> AimConfig.fov = v.floatValue())); y += g;
            add(s(x, y, w, "Скорость наводки", AimConfig.speed, 20, 100, v -> AimConfig.speed = v.floatValue())); y += g;
            add(s(x, y, w, "КПС мин", AimConfig.minCps, 4, 14, v -> AimConfig.minCps = v.intValue())); y += g;
            add(s(x, y, w, "КПС макс", AimConfig.maxCps, 6, 16, v -> AimConfig.maxCps = v.intValue())); y += g;
            add(t(x, y, w, "Игроки", () -> AimConfig.players, v -> AimConfig.players = v)); y += g;
            add(t(x, y, w, "Мобы", () -> AimConfig.mobs, v -> AimConfig.mobs = v)); y += g;
            add(t(x, y, w, "Адаптив", () -> AimConfig.adaptive, v -> AimConfig.adaptive = v));
        }
        for (ClickableWidget wdg : widgets) addDrawableChild(wdg);
    }
    private void add(ClickableWidget w) { widgets.add(w); }
    private ButtonWidget t(int x, int y, int w, String n, BooleanSupplier g, Consumer<Boolean> s) {
        return ButtonWidget.builder(Text.literal(n + ": " + (g.getAsBoolean() ? "§aВКЛ" : "§cВЫКЛ")), b -> {
            boolean v = !g.getAsBoolean(); s.accept(v);
            b.setMessage(Text.literal(n + ": " + (v ? "§aВКЛ" : "§cВЫКЛ")));
        }).dimensions(x, y, w, 20).build();
    }
    private SliderWidget s(int x, int y, int w, String n, double cur, double min, double max, Consumer<Double> set) {
        return new SliderWidget(x, y, w, 20, Text.literal(n + ": " + f(cur)), (cur - min) / (max - min)) {
            @Override protected void updateMessage() { setMessage(Text.literal(n + ": " + f(min + value * (max - min)))); }
            @Override protected void applyValue() { set.accept(min + value * (max - min)); }
        };
    }
    private String f(double v) { return v == (int) v ? "" + (int) v : String.format("%.1f", v); }
    @Override public void render(DrawContext ctx, int mx, int my, float d) {
        ctx.fill(0, 0, width, height, 0x66101827);
        ctx.fill(6 + 4, 6, 100 - 4, height - 6, 0xAA1E293B);
        ctx.fill(6, 6 + 4, 100, height - 6 - 4, 0xAA1E293B);
        ctx.fill(108 + 4, 6, width - 6 - 4, height - 6, 0x991E3A5F);
        ctx.fill(108, 6 + 4, width - 6, height - 6 - 4, 0x991E3A5F);
        ctx.fill(100, 8, 102, height - 8, 0xFF3B82F6);
        ctx.drawTextWithShadow(textRenderer, "PoufDLC", 18, 16, 0xFF93C5FD);
        ctx.drawTextWithShadow(textRenderer, cats[cat], 120, 16, 0xFFE0F2FE);
        ctx.drawTextWithShadow(textRenderer, "Настройки модуля", 120, 28, 0xFF94A3B8);
        super.render(ctx, mx, my, d);
    }
    @Override public boolean shouldPause() { return false; }
}

package ru.poufdlc.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import ru.poufdlc.combat.Modules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ClickMenu extends Screen {
    private final Screen parent;
    private int tab = 0;
    private final String[] tabs = {"Бой", "Движение", "Визуалы", "Прицел", "Прочее"};
    private final List<ClickableWidget> list = new ArrayList<>();

    public ClickMenu(Screen parent) {
        super(Text.literal("PoufDLC"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        list.clear();
        int side = 96;
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal(tabs[i]), b -> { tab = idx; init(); })
                    .dimensions(8, 44 + i * 28, side - 12, 24).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> {
            if (client != null) client.setScreen(parent);
        }).dimensions(8, height - 30, side - 12, 20).build());

        int x = side + 18, y = 48, g = 22, w = Math.min(210, width - side - 30);
        if (tab == 0) {
            add(t(x, y, w, "ТриггерБот", () -> Modules.triggerBot, v -> Modules.triggerBot = v)); y += g;
            add(t(x, y, w, "АвтоПрицел", () -> Modules.autoAim, v -> Modules.autoAim = v)); y += g;
            add(t(x, y, w, "АвтоБулава", () -> Modules.autoMace, v -> Modules.autoMace = v)); y += g;
            add(t(x, y, w, "Двойной крит", () -> Modules.doubleCrit, v -> Modules.doubleCrit = v)); y += g;
            add(t(x, y, w, "Только криты", () -> Modules.critOnly, v -> Modules.critOnly = v)); y += g;
            add(t(x, y, w, "MaceSwap", () -> Modules.maceSwap, v -> Modules.maceSwap = v)); y += g;
            add(t(x, y, w, "Мульти-прыжок АБ", () -> Modules.amMultiJump, v -> Modules.amMultiJump = v)); y += g;
            add(t(x, y, w, "Авто WindCharge", () -> Modules.amAutoWind, v -> Modules.amAutoWind = v)); y += g;
            add(s(x, y, w, "Дистанция ТБ", Modules.tbRange, 2.5, 4.5, v -> Modules.tbRange = v.floatValue())); y += g;
            add(s(x, y, w, "Дистанция АБ", Modules.amRange, 3, 10, v -> Modules.amRange = v.floatValue())); y += g;
            add(s(x, y, w, "КПС мин", Modules.minCps, 4, 14, v -> Modules.minCps = v.intValue())); y += g;
            add(s(x, y, w, "КПС макс", Modules.maxCps, 6, 16, v -> Modules.maxCps = v.intValue())); y += g;
            add(s(x, y, w, "Скорость наводки", Modules.aimSpeed, 20, 100, v -> Modules.aimSpeed = v.floatValue())); y += g;
            add(s(x, y, w, "Ротация АБ", Modules.amRotSpeed, 50, 130, v -> Modules.amRotSpeed = v.floatValue())); y += g;
            add(t(x, y, w, "Адаптив (Sloth)", () -> Modules.adaptive, v -> Modules.adaptive = v));
        } else if (tab == 1) {
            add(t(x, y, w, "Полёт", () -> Modules.fly, v -> Modules.fly = v)); y += g;
            add(t(x, y, w, "Скорость", () -> Modules.speed, v -> Modules.speed = v)); y += g;
            add(t(x, y, w, "Нет падения", () -> Modules.noFall, v -> Modules.noFall = v)); y += g;
            add(t(x, y, w, "NoJumpDelay", () -> Modules.noJumpDelay, v -> Modules.noJumpDelay = v));
        } else if (tab == 2) {
            add(t(x, y, w, "HUD", () -> Modules.hud, v -> Modules.hud = v)); y += g;
            add(t(x, y, w, "Бокс ESP", () -> Modules.boxEsp, v -> Modules.boxEsp = v)); y += g;
            add(t(x, y, w, "Трейсеры", () -> Modules.tracers, v -> Modules.tracers = v)); y += g;
            add(t(x, y, w, "Неймтеги", () -> Modules.nametags, v -> Modules.nametags = v)); y += g;
            add(t(x, y, w, "Полоска HP", () -> Modules.healthBar, v -> Modules.healthBar = v)); y += g;
            add(t(x, y, w, "ESP предметов", () -> Modules.itemEsp, v -> Modules.itemEsp = v)); y += g;
            add(t(x, y, w, "FullBright", () -> Modules.fullBright, v -> Modules.fullBright = v)); y += g;
            add(t(x, y, w, "FPS", () -> Modules.fpsHud, v -> Modules.fpsHud = v)); y += g;
            add(t(x, y, w, "Координаты", () -> Modules.coordsHud, v -> Modules.coordsHud = v)); y += g;
            add(t(x, y, w, "Target HUD", () -> Modules.targetHud, v -> Modules.targetHud = v)); y += g;
            add(t(x, y, w, "Броня HUD", () -> Modules.armorHud, v -> Modules.armorHud = v)); y += g;
            add(t(x, y, w, "Зелья HUD", () -> Modules.potionHud, v -> Modules.potionHud = v)); y += g;
            add(t(x, y, w, "China Hat", () -> Modules.chinaHat, v -> Modules.chinaHat = v)); y += g;
            add(t(x, y, w, "Skeleton ESP", () -> Modules.skeletonEsp, v -> Modules.skeletonEsp = v)); y += g;
            add(t(x, y, w, "Hit Markers", () -> Modules.hitMarkers, v -> Modules.hitMarkers = v)); y += g;
            add(s(x, y, w, "Дальность ESP", Modules.espRange, 16, 128, v -> Modules.espRange = v.floatValue()));
        } else if (tab == 3) {
            add(s(x, y, w, "FOV", Modules.tbFov, 30, 180, v -> Modules.tbFov = v.floatValue())); y += g;
            add(s(x, y, w, "Aim Angle", Modules.aimAngle, 8, 40, v -> Modules.aimAngle = v.floatValue())); y += g;
            add(s(x, y, w, "Jitter", Modules.aimJitter, 0.05, 0.4, v -> Modules.aimJitter = v.floatValue())); y += g;
            add(s(x, y, w, "Min Fall", Modules.minFall, 0.05, 1.0, v -> Modules.minFall = v.floatValue()));
        } else {
            add(t(x, y, w, "Игроки", () -> Modules.players, v -> Modules.players = v)); y += g;
            add(t(x, y, w, "Мобы", () -> Modules.mobs, v -> Modules.mobs = v));
        }
        for (ClickableWidget wdg : list) addDrawableChild(wdg);
    }

    private void add(ClickableWidget w) { list.add(w); }

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

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        ctx.fill(0, 0, width, height, 0x77101827);
        ctx.fill(4, 4, 96, height - 4, 0xBB0F172A);
        ctx.fill(100, 4, width - 4, height - 4, 0x991E3A5F);
        ctx.fill(96, 6, 98, height - 6, 0xFF3B82F6);
        ctx.drawTextWithShadow(textRenderer, "PoufDLC", 14, 14, 0xFF93C5FD);
        ctx.drawTextWithShadow(textRenderer, tabs[tab], 110, 14, 0xFFE0F2FE);
        ctx.drawTextWithShadow(textRenderer, "Sloth / SkyTime oriented", 110, 26, 0xFF94A3B8);
        super.render(ctx, mx, my, d);
    }

    @Override public boolean shouldPause() { return false; }
}

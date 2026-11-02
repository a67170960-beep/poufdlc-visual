package ru.poufdlc.visual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class VisualConfigScreen extends Screen {
    private final Screen parent;
    private int page = 0; // 0 = main, 1 = killaura

    public VisualConfigScreen(Screen parent) {
        super(Text.literal("PoufDLC — меню"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        int cx = this.width / 2;
        int y = 32;
        int gap = 22;

        if (page == 0) {
            addDrawableChild(toggle(cx, y, "HUD", () -> PoufVisualClient.hudEnabled, v -> PoufVisualClient.hudEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Fly", () -> PoufVisualClient.flyEnabled, v -> PoufVisualClient.flyEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Speed", () -> PoufVisualClient.speedEnabled, v -> PoufVisualClient.speedEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "FullBright", () -> PoufVisualClient.fullBrightEnabled, v -> PoufVisualClient.fullBrightEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "NoFall", () -> PoufVisualClient.noFallEnabled, v -> PoufVisualClient.noFallEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "KillAura", () -> KillAuraConfig.enabled, v -> KillAuraConfig.enabled = v)); y += gap;
            addDrawableChild(ButtonWidget.builder(Text.literal("KillAura Settings →"), b -> {
                page = 1;
                init();
            }).dimensions(cx - 100, y, 200, 20).build());
            y += gap + 6;
            addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
                if (client != null) client.setScreen(parent);
            }).dimensions(cx - 100, y, 200, 20).build());
        } else {
            // === KillAura page ===
            addDrawableChild(toggle(cx, y, "Enabled", () -> KillAuraConfig.enabled, v -> KillAuraConfig.enabled = v)); y += gap;
            addDrawableChild(slider(cx, y, "Range", KillAuraConfig.range, 2.5, 4.5, v -> KillAuraConfig.range = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "FOV", KillAuraConfig.fov, 30, 180, v -> KillAuraConfig.fov = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "Min CPS", KillAuraConfig.minCps, 4, 16, v -> KillAuraConfig.minCps = v.intValue())); y += gap;
            addDrawableChild(slider(cx, y, "Max CPS", KillAuraConfig.maxCps, 6, 18, v -> KillAuraConfig.maxCps = v.intValue())); y += gap;
            addDrawableChild(slider(cx, y, "Rot Speed", KillAuraConfig.rotationSpeed, 15, 90, v -> KillAuraConfig.rotationSpeed = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "Aim Jitter", KillAuraConfig.aimJitter, 0.05, 0.45, v -> KillAuraConfig.aimJitter = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "Aim Angle", KillAuraConfig.aimAngle, 10, 45, v -> KillAuraConfig.aimAngle = v.floatValue())); y += gap;
            addDrawableChild(toggle(cx, y, "Crits", () -> KillAuraConfig.crits, v -> KillAuraConfig.crits = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Only Crits", () -> KillAuraConfig.onlyCrits, v -> KillAuraConfig.onlyCrits = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Players", () -> KillAuraConfig.attackPlayers, v -> KillAuraConfig.attackPlayers = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Mobs", () -> KillAuraConfig.attackMobs, v -> KillAuraConfig.attackMobs = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Through Walls", () -> KillAuraConfig.throughWalls, v -> KillAuraConfig.throughWalls = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Keep Sprint", () -> KillAuraConfig.keepSprint, v -> KillAuraConfig.keepSprint = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Adaptive (Sloth)", () -> KillAuraConfig.adaptive, v -> KillAuraConfig.adaptive = v)); y += gap + 4;
            addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), b -> {
                page = 0;
                init();
            }).dimensions(cx - 100, y, 200, 20).build());
        }
    }

    private ButtonWidget toggle(int cx, int y, String name, java.util.function.BooleanSupplier get, java.util.function.Consumer<Boolean> set) {
        return ButtonWidget.builder(Text.literal(name + ": " + (get.getAsBoolean() ? "ON" : "OFF")), b -> {
            boolean n = !get.getAsBoolean();
            set.accept(n);
            b.setMessage(Text.literal(name + ": " + (n ? "ON" : "OFF")));
        }).dimensions(cx - 100, y, 200, 20).build();
    }

    private SliderWidget slider(int cx, int y, String name, double cur, double min, double max, java.util.function.Consumer<Double> set) {
        double norm = (cur - min) / (max - min);
        return new SliderWidget(cx - 100, y, 200, 20, Text.literal(name + ": " + format(cur)), norm) {
            @Override
            protected void updateMessage() {
                double val = min + this.value * (max - min);
                setMessage(Text.literal(name + ": " + format(val)));
            }
            @Override
            protected void applyValue() {
                double val = min + this.value * (max - min);
                set.accept(val);
            }
        };
    }

    private String format(double v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format("%.2f", v);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFE8E0FF);
        if (page == 1) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("KillAura — настройки"), width / 2, 20, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}

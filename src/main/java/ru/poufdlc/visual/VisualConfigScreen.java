package ru.poufdlc.visual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class VisualConfigScreen extends Screen {
    private final Screen parent;
    private int page = 0; // 0 main, 1 killaura, 2 automace

    public VisualConfigScreen(Screen parent) {
        super(Text.literal("PoufDLC — меню"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        int cx = width / 2;
        int y = 28;
        int gap = 20;

        if (page == 0) {
            addDrawableChild(toggle(cx, y, "HUD", () -> PoufVisualClient.hudEnabled, v -> PoufVisualClient.hudEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Fly", () -> PoufVisualClient.flyEnabled, v -> PoufVisualClient.flyEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Speed", () -> PoufVisualClient.speedEnabled, v -> PoufVisualClient.speedEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "FullBright", () -> PoufVisualClient.fullBrightEnabled, v -> PoufVisualClient.fullBrightEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "NoFall", () -> PoufVisualClient.noFallEnabled, v -> PoufVisualClient.noFallEnabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "KillAura", () -> KillAuraConfig.enabled, v -> KillAuraConfig.enabled = v)); y += gap;
            addDrawableChild(toggle(cx, y, "AutoMace", () -> AutoMaceConfig.enabled, v -> AutoMaceConfig.enabled = v)); y += gap;
            addDrawableChild(ButtonWidget.builder(Text.literal("KillAura Settings →"), b -> { page = 1; init(); }).dimensions(cx - 100, y, 200, 20).build()); y += gap;
            addDrawableChild(ButtonWidget.builder(Text.literal("AutoMace Settings →"), b -> { page = 2; init(); }).dimensions(cx - 100, y, 200, 20).build()); y += gap + 4;
            addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> { if (client != null) client.setScreen(parent); }).dimensions(cx - 100, y, 200, 20).build());
        } else if (page == 1) {
            // KillAura (короткая версия ключевых)
            addDrawableChild(toggle(cx, y, "Enabled", () -> KillAuraConfig.enabled, v -> KillAuraConfig.enabled = v)); y += gap;
            addDrawableChild(slider(cx, y, "Range", KillAuraConfig.range, 2.5, 4.5, v -> KillAuraConfig.range = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "CPS min", KillAuraConfig.minCps, 4, 16, v -> KillAuraConfig.minCps = v.intValue())); y += gap;
            addDrawableChild(slider(cx, y, "CPS max", KillAuraConfig.maxCps, 6, 18, v -> KillAuraConfig.maxCps = v.intValue())); y += gap;
            addDrawableChild(slider(cx, y, "Rot Speed", KillAuraConfig.rotationSpeed, 15, 90, v -> KillAuraConfig.rotationSpeed = v.floatValue())); y += gap;
            addDrawableChild(toggle(cx, y, "Crits", () -> KillAuraConfig.crits, v -> KillAuraConfig.crits = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Only Crits", () -> KillAuraConfig.onlyCrits, v -> KillAuraConfig.onlyCrits = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Adaptive", () -> KillAuraConfig.adaptive, v -> KillAuraConfig.adaptive = v)); y += gap + 4;
            addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), b -> { page = 0; init(); }).dimensions(cx - 100, y, 200, 20).build());
        } else {
            // AutoMace page
            addDrawableChild(toggle(cx, y, "Enabled", () -> AutoMaceConfig.enabled, v -> AutoMaceConfig.enabled = v)); y += gap;
            addDrawableChild(slider(cx, y, "Range", AutoMaceConfig.range, 3.0, 10.0, v -> AutoMaceConfig.range = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "Hit Range", AutoMaceConfig.hitRange, 2.5, 5.5, v -> AutoMaceConfig.hitRange = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "Min Fall", AutoMaceConfig.minFallDistance, 0.8, 5.0, v -> AutoMaceConfig.minFallDistance = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "Hit Height", AutoMaceConfig.hitHeightAboveGround, 1.0, 4.0, v -> AutoMaceConfig.hitHeightAboveGround = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "Rot Speed", AutoMaceConfig.rotationSpeed, 40, 120, v -> AutoMaceConfig.rotationSpeed = v.floatValue())); y += gap;
            addDrawableChild(slider(cx, y, "Aim Angle", AutoMaceConfig.aimAngle, 8, 35, v -> AutoMaceConfig.aimAngle = v.floatValue())); y += gap;
            addDrawableChild(toggle(cx, y, "Auto WindCharge", () -> AutoMaceConfig.autoWindCharge, v -> AutoMaceConfig.autoWindCharge = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Switch to Mace", () -> AutoMaceConfig.switchToMace, v -> AutoMaceConfig.switchToMace = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Only Holding Mace", () -> AutoMaceConfig.onlyWhenHoldingMace, v -> AutoMaceConfig.onlyWhenHoldingMace = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Players", () -> AutoMaceConfig.attackPlayers, v -> AutoMaceConfig.attackPlayers = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Mobs", () -> AutoMaceConfig.attackMobs, v -> AutoMaceConfig.attackMobs = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Through Walls", () -> AutoMaceConfig.throughWalls, v -> AutoMaceConfig.throughWalls = v)); y += gap;
            addDrawableChild(toggle(cx, y, "Adaptive", () -> AutoMaceConfig.adaptive, v -> AutoMaceConfig.adaptive = v)); y += gap + 4;
            addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), b -> { page = 0; init(); }).dimensions(cx - 100, y, 200, 20).build());
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
            @Override protected void updateMessage() {
                double val = min + value * (max - min);
                setMessage(Text.literal(name + ": " + format(val)));
            }
            @Override protected void applyValue() {
                set.accept(min + value * (max - min));
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
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFE8E0FF);
        if (page == 2) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("AutoMace — WindCharge + Mace"), width / 2, 18, 0xFFAAAAAA);
        }
    }

    @Override public boolean shouldPause() { return false; }
}

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
    private int category = 0; // 0 Combat, 1 Movement, 2 Visual, 3 Player
    private final String[] cats = {"Combat", "Movement", "Visual", "Player"};
    private final List<ClickableWidget> content = new ArrayList<>();

    public VisualConfigScreen(Screen parent) {
        super(Text.literal("PoufDLC"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        content.clear();

        int sideW = 90;
        int startY = 40;
        for (int i = 0; i < cats.length; i++) {
            final int idx = i;
            int y = startY + i * 28;
            addDrawableChild(ButtonWidget.builder(Text.literal(cats[i]), b -> {
                category = idx;
                init();
            }).dimensions(8, y, sideW - 10, 24).build());
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> {
            if (client != null) client.setScreen(parent);
        }).dimensions(8, height - 28, sideW - 10, 20).build());

        int cx = sideW + 20;
        int y = 42;
        int gap = 22;
        int w = 200;

        if (category == 0) { // Combat
            addContent(toggle(cx, y, w, "KillAura", () -> KillAuraConfig.enabled, v -> KillAuraConfig.enabled = v)); y += gap;
            addContent(slider(cx, y, w, "KA Range", KillAuraConfig.range, 2.5, 4.5, v -> KillAuraConfig.range = v.floatValue())); y += gap;
            addContent(slider(cx, y, w, "KA CPS min", KillAuraConfig.minCps, 4, 16, v -> KillAuraConfig.minCps = v.intValue())); y += gap;
            addContent(slider(cx, y, w, "KA CPS max", KillAuraConfig.maxCps, 6, 18, v -> KillAuraConfig.maxCps = v.intValue())); y += gap;
            addContent(toggle(cx, y, w, "KA Crits", () -> KillAuraConfig.crits, v -> KillAuraConfig.crits = v)); y += gap;
            addContent(toggle(cx, y, w, "KA Adaptive", () -> KillAuraConfig.adaptive, v -> KillAuraConfig.adaptive = v)); y += gap + 6;

            addContent(toggle(cx, y, w, "AutoMace", () -> AutoMaceConfig.enabled, v -> AutoMaceConfig.enabled = v)); y += gap;
            addContent(slider(cx, y, w, "AM Range", AutoMaceConfig.range, 3, 10, v -> AutoMaceConfig.range = v.floatValue())); y += gap;
            addContent(slider(cx, y, w, "AM Hit Range", AutoMaceConfig.hitRange, 2.5, 5.5, v -> AutoMaceConfig.hitRange = v.floatValue())); y += gap;
            addContent(slider(cx, y, w, "AM Min Fall", AutoMaceConfig.minFallDistance, 0.8, 5, v -> AutoMaceConfig.minFallDistance = v.floatValue())); y += gap;
            addContent(slider(cx, y, w, "AM Hit Height", AutoMaceConfig.hitHeightAboveGround, 1, 4, v -> AutoMaceConfig.hitHeightAboveGround = v.floatValue())); y += gap;
            addContent(slider(cx, y, w, "AM Rot Speed", AutoMaceConfig.rotationSpeed, 50, 130, v -> AutoMaceConfig.rotationSpeed = v.floatValue())); y += gap;
            addContent(toggle(cx, y, w, "AM Auto Wind", () -> AutoMaceConfig.autoWindCharge, v -> AutoMaceConfig.autoWindCharge = v)); y += gap;
            addContent(toggle(cx, y, w, "AM Switch Mace", () -> AutoMaceConfig.switchToMace, v -> AutoMaceConfig.switchToMace = v)); y += gap;
            addContent(toggle(cx, y, w, "AM Adaptive", () -> AutoMaceConfig.adaptive, v -> AutoMaceConfig.adaptive = v));
        } else if (category == 1) { // Movement
            addContent(toggle(cx, y, w, "Fly", () -> PoufVisualClient.flyEnabled, v -> PoufVisualClient.flyEnabled = v)); y += gap;
            addContent(toggle(cx, y, w, "Speed", () -> PoufVisualClient.speedEnabled, v -> PoufVisualClient.speedEnabled = v)); y += gap;
            addContent(toggle(cx, y, w, "NoFall", () -> PoufVisualClient.noFallEnabled, v -> PoufVisualClient.noFallEnabled = v));
        } else if (category == 2) { // Visual
            addContent(toggle(cx, y, w, "HUD", () -> PoufVisualClient.hudEnabled, v -> PoufVisualClient.hudEnabled = v)); y += gap;
            addContent(slider(cx, y, w, "Corner Radius", PoufVisualClient.cornerRadius, 0, 24, v -> PoufVisualClient.cornerRadius = v.floatValue())); y += gap;
            addContent(toggle(cx, y, w, "FullBright", () -> PoufVisualClient.fullBrightEnabled, v -> PoufVisualClient.fullBrightEnabled = v));
        } else { // Player
            addContent(toggle(cx, y, w, "KA Players", () -> KillAuraConfig.attackPlayers, v -> KillAuraConfig.attackPlayers = v)); y += gap;
            addContent(toggle(cx, y, w, "KA Mobs", () -> KillAuraConfig.attackMobs, v -> KillAuraConfig.attackMobs = v)); y += gap;
            addContent(toggle(cx, y, w, "AM Players", () -> AutoMaceConfig.attackPlayers, v -> AutoMaceConfig.attackPlayers = v)); y += gap;
            addContent(toggle(cx, y, w, "AM Mobs", () -> AutoMaceConfig.attackMobs, v -> AutoMaceConfig.attackMobs = v));
        }

        for (ClickableWidget wdg : content) addDrawableChild(wdg);
    }

    private void addContent(ClickableWidget w) { content.add(w); }

    private ButtonWidget toggle(int x, int y, int w, String name, BooleanSupplier get, Consumer<Boolean> set) {
        return ButtonWidget.builder(Text.literal(name + ": " + (get.getAsBoolean() ? "§aON" : "§cOFF")), b -> {
            boolean n = !get.getAsBoolean();
            set.accept(n);
            b.setMessage(Text.literal(name + ": " + (n ? "§aON" : "§cOFF")));
        }).dimensions(x, y, w, 20).build();
    }

    private SliderWidget slider(int x, int y, int w, String name, double cur, double min, double max, Consumer<Double> set) {
        double norm = (cur - min) / (max - min);
        return new SliderWidget(x, y, w, 20, Text.literal(name + ": " + fmt(cur)), norm) {
            @Override protected void updateMessage() {
                setMessage(Text.literal(name + ": " + fmt(min + value * (max - min))));
            }
            @Override protected void applyValue() {
                set.accept(min + value * (max - min));
            }
        };
    }

    private String fmt(double v) {
        return v == (int) v ? String.valueOf((int) v) : String.format("%.2f", v);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // фон панели
        ctx.fill(0, 0, 90, height, 0xCC12121A);
        ctx.fill(90, 0, width, height, 0xCC0A0A12);
        ctx.fill(90, 0, 91, height, 0xFF3B82F6);

        ctx.drawTextWithShadow(textRenderer, "PoufDLC", 12, 12, 0xFF60A5FA);
        ctx.drawTextWithShadow(textRenderer, cats[category], 100, 14, 0xFFE2E8F0);

        super.render(ctx, mx, my, delta);
    }

    @Override public boolean shouldPause() { return false; }
}

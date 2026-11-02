package ru.poufdlc.visual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class VisualConfigScreen extends Screen {
    private final Screen parent;

    public VisualConfigScreen(Screen parent) {
        super(Text.literal("PoufDLC Visual — Tg @PoufDLCclient"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 40;
        int gap = 24;

        this.addDrawableChild(new SliderWidget(cx - 100, y, 200, 20,
                Text.literal("Corner radius: " + (int) PoufVisualClient.cornerRadius),
                PoufVisualClient.cornerRadius / 24.0) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Corner radius: " + (int) PoufVisualClient.cornerRadius));
            }

            @Override
            protected void applyValue() {
                PoufVisualClient.cornerRadius = (float) (this.value * 24.0);
            }
        });
        y += gap;

        this.addDrawableChild(toggleButton(cx, y, "HUD", () -> PoufVisualClient.hudEnabled, v -> PoufVisualClient.hudEnabled = v));
        y += gap;
        this.addDrawableChild(toggleButton(cx, y, "Fly", () -> PoufVisualClient.flyEnabled, v -> PoufVisualClient.flyEnabled = v));
        y += gap;
        this.addDrawableChild(toggleButton(cx, y, "Speed", () -> PoufVisualClient.speedEnabled, v -> PoufVisualClient.speedEnabled = v));
        y += gap;
        this.addDrawableChild(toggleButton(cx, y, "FullBright", () -> PoufVisualClient.fullBrightEnabled, v -> PoufVisualClient.fullBrightEnabled = v));
        y += gap;
        this.addDrawableChild(toggleButton(cx, y, "NoFall", () -> PoufVisualClient.noFallEnabled, v -> PoufVisualClient.noFallEnabled = v));
        y += gap;
        this.addDrawableChild(toggleButton(cx, y, "KillAura", () -> PoufVisualClient.killAuraEnabled, v -> PoufVisualClient.killAuraEnabled = v));
        y += gap + 8;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(cx - 100, y, 200, 20).build());
    }

    private ButtonWidget toggleButton(int cx, int y, String name, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
        return ButtonWidget.builder(
                Text.literal(name + ": " + (getter.getAsBoolean() ? "ON" : "OFF")),
                b -> {
                    boolean next = !getter.getAsBoolean();
                    setter.accept(next);
                    b.setMessage(Text.literal(name + ": " + (next ? "ON" : "OFF")));
                }
        ).dimensions(cx - 100, y, 200, 20).build();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFE8E0FF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Right Shift — меню"),
                this.width / 2, 26, 0xFFAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

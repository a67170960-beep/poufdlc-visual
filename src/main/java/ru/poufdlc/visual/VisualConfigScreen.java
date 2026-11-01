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
        int y = this.height / 2 - 40;

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

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(PoufVisualClient.hudEnabled ? "HUD: ON" : "HUD: OFF"),
                b -> {
                    PoufVisualClient.hudEnabled = !PoufVisualClient.hudEnabled;
                    b.setMessage(Text.literal(PoufVisualClient.hudEnabled ? "HUD: ON" : "HUD: OFF"));
                }
        ).dimensions(cx - 100, y + 28, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(cx - 100, y + 56, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFE8E0FF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Visual only. No combat modules."),
                this.width / 2, 36, 0xFFAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

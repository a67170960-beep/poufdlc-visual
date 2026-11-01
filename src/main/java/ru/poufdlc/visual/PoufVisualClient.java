package ru.poufdlc.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PoufVisualClient implements ClientModInitializer {
    public static float cornerRadius = 8.0f;
    public static boolean hudEnabled = true;
    public static final String HUD_TITLE = "PoufDLC Beta";

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.poufdlc-visual.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.poufdlc-visual"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new VisualConfigScreen(client.currentScreen));
                }
            }
        });

        HudRenderCallback.EVENT.register(PoufVisualClient::renderHud);
    }

    private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (!hudEnabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null) return;

        int x = 6;
        int y = 6;
        int pad = 4;
        String text = HUD_TITLE;
        int tw = mc.textRenderer.getWidth(text);
        int th = mc.textRenderer.fontHeight;
        int w = tw + pad * 2;
        int h = th + pad * 2;

        // simple rounded-ish panel via layered rects (no extra deps)
        int bg = 0xAA101018;
        int border = 0xFF7B5CFF;
        fillRounded(context, x, y, x + w, y + h, (int) Math.max(1, cornerRadius / 2), bg);
        context.drawBorder(x, y, w, h, border);
        context.drawTextWithShadow(mc.textRenderer, text, x + pad, y + pad, 0xFFE8E0FF);
    }

    static void fillRounded(DrawContext ctx, int x1, int y1, int x2, int y2, int r, int color) {
        r = Math.min(r, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
        ctx.fill(x1 + r, y1, x2 - r, y2, color);
        ctx.fill(x1, y1 + r, x2, y2 - r, color);
        // corners as small blocks
        ctx.fill(x1, y1, x1 + r, y1 + r, color);
        ctx.fill(x2 - r, y1, x2, y1 + r, color);
        ctx.fill(x1, y2 - r, x1 + r, y2, color);
        ctx.fill(x2 - r, y2 - r, x2, y2, color);
    }
}

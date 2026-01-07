package moe.ytonidc.ytongame_hostingmenu.client;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.ytonidc.ytongame_hostingmenu.Ytongame_hostingmenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class MultiPlayerAdEntry extends ServerSelectionList.Entry {
    private final Minecraft minecraft;

    public MultiPlayerAdEntry(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public @NotNull Component getNarration() {
        return Component.literal("YtonGame AdEntry");
    }

    @Override
    public void render(GuiGraphics context, int index, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.blit(Ytongame_hostingmenu.HOSTING_LOGO, left, top, 0, 0.0f, 0.0f, entryHeight, entryHeight, entryHeight, entryHeight);
        context.drawString(this.minecraft.font, "如果您需要24H不间断的服务器? 点击我跳转详情!", left + 32 + 3, top + 1, 16777215, false);

        String line1 = "推荐选用昱通游戏，我们收录且支持数百种整合包一键联机（仍在更新）";
        String line2 = "致力为您提供稳定、流畅的服务器，打造优、稳、快的游戏体验";
        int textStartX = left + 32 + 3;

        renderGradientText(context, line1, textStartX, top + 12, true);
        renderGradientText(context, line2, textStartX, top + 12 + 9, false);
    }

    private void renderGradientText(GuiGraphics context, String text, int startX, int y, boolean useGreen) {
        int charX = startX;
        int textLength = text.length();

        for (int i = 0; i < textLength; i++) {
            String ch = String.valueOf(text.charAt(i));
            float progress = (float) i / (textLength - 1);
            int color = useGreen ? getGreenGradientColor(progress) : getYellowOrangeGradientColor(progress);

            context.drawString(this.minecraft.font, ch, charX, y, color, false);
            charX += this.minecraft.font.width(ch);
        }
    }

    private int getGreenGradientColor(float progress) {
        int startR = 0xAA, startG = 0xFF, startB = 0x55;
        int midR = 0x00, midG = 0xFF, midB = 0x88;
        int endR = 0x00, endG = 0xAA, endB = 0xCC;

        int r, g, b;
        if (progress < 0.5f) {
            float t = progress * 2;
            r = (int) (startR + (midR - startR) * t);
            g = (int) (startG + (midG - startG) * t);
            b = (int) (startB + (midB - startB) * t);
        } else {
            float t = (progress - 0.5f) * 2;
            r = (int) (midR + (endR - midR) * t);
            g = (int) (midG + (endG - midG) * t);
            b = (int) (midB + (endB - midB) * t);
        }

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int getYellowOrangeGradientColor(float progress) {
        int startR = 0xFF, startG = 0xFF, startB = 0x55;
        int midR = 0xFF, midG = 0xAA, midB = 0x00;
        int endR = 0xFF, endG = 0x66, endB = 0x00;

        int r, g, b;
        if (progress < 0.5f) {
            float t = progress * 2;
            r = (int) (startR + (midR - startR) * t);
            g = (int) (startG + (midG - startG) * t);
            b = (int) (startB + (midB - startB) * t);
        } else {
            float t = (progress - 0.5f) * 2;
            r = (int) (midR + (endR - midR) * t);
            g = (int) (midG + (endG - midG) * t);
            b = (int) (midB + (endB - midB) * t);
        }

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            HostingTab.shouldOpenHostingTab = true;
            CreateWorldScreen.openFresh(this.minecraft, this.minecraft.screen);
            return true;
        }
        return false;
    }
}

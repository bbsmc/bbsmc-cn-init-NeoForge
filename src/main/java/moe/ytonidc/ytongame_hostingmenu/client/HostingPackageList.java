package moe.ytonidc.ytongame_hostingmenu.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class HostingPackageList extends ObjectSelectionList<HostingPackageList.Entry> {

    public HostingPackageList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
        super(minecraft, width, height, top, itemHeight);

        for (HostingPackage pkg : HostingPackage.getAllPackages()) {
            this.addEntry(new Entry(pkg));
        }
    }

    @Override
    public int getRowWidth() {
        return this.width - 40;
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final HostingPackage pkg;

        public Entry(HostingPackage pkg) {
            this.pkg = pkg;
        }

        @Override
        public void render(GuiGraphics context, int index, int top, int left, int width, int height,
                          int mouseX, int mouseY, boolean hovering, float partialTick) {
            var font = minecraft.font;

            if (hovering) {
                context.fill(left - 2, top - 2, left + width + 2, top + height + 2, 0x80808080);
            }

            int borderColor = pkg.getColor();
            context.fill(left, top, left + 4, top + height, borderColor);

            int textLeft = left + 12;
            int line1Y = top + 4;
            int line2Y = top + 18;
            int line3Y = top + 32;

            context.drawString(font, pkg.getName(), textLeft, line1Y, pkg.getColor(), false);

            String tag = pkg.getTag();
            if (tag != null && !tag.isEmpty()) {
                int nameWidth = font.width(pkg.getName());
                int tagX = textLeft + nameWidth + 6;
                int tagY = line1Y;
                int tagWidth = font.width(tag) + 6;
                int tagHeight = 10;
                int tagBgColor = tag.equals("热销") ? 0xFFFF5555 : pkg.getColor();
                context.fill(tagX, tagY - 1, tagX + tagWidth, tagY + tagHeight, tagBgColor);
                context.drawString(font, tag, tagX + 3, tagY, 0xFFFFFFFF, false);
            }

            String priceText = "¥" + pkg.getPrice() + "/月";
            int priceWidth = font.width(priceText);
            context.drawString(font, priceText, left + width - priceWidth - 10, line1Y, 0xFFFFFF55, false);

            String cpuLabel = "CPU: ";
            context.drawString(font, cpuLabel, textLeft, line2Y, 0xFFAAAAAA, false);
            int cpuLabelWidth = font.width(cpuLabel);
            context.drawString(font, pkg.getProcessor(), textLeft + cpuLabelWidth, line2Y, 0xFFFFAA00, false);

            String memoryText = "内存: " + pkg.getMemory();
            context.drawString(font, memoryText, textLeft, line3Y, 0xFFAAAAAA, false);

            String backupText = "备份: " + pkg.getDefaultBackupSlots() + "/" + pkg.getMaxBackupSlots();
            context.drawString(font, backupText, textLeft + 80, line3Y, 0xFFAAAAAA, false);

            String storageText = "存储: " + pkg.getStorage();
            context.drawString(font, storageText, textLeft + 160, line3Y, 0xFFAAAAAA, false);

            String playersText = "推荐: " + pkg.getRecommendedPlayers();
            context.drawString(font, playersText, textLeft + 250, line3Y, 0xFFAAAAAA, false);
        }

        @Override
        public Component getNarration() {
            return Component.literal(pkg.getName() + " - ¥" + pkg.getPrice() + "/月");
        }

        public HostingPackage getPackage() {
            return pkg;
        }
    }
}

package net.bbsmc.cninit.client;

import com.google.gson.JsonObject;
import net.bbsmc.cninit.BbsmcCnInit;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalizationNoticeScreen extends Screen {

    private static final String TITLE_TEXT = "\u00a7l\u00a7eBBSMC汉化包使用须知";
    private static final String[] NOTICE_LINES = {
        "\u00a7f\u00a7l感谢您选择BBSMC汉化包，在正式使用前，请阅读以下内容。",
        "",
        "\u00a76\u00a7l一、汉化内容",
        "\u00a7fBBSMC汉化包基于AI翻译+人工精校生成，部分翻译文本可能仍带有机翻的味道，我们正在持续优化调整翻译质量。如果您发现了任何翻译不自然、不准确的地方，恳请您积极向我们反馈，我们会及时修正并重新发布更新后的汉化包。",
        "",
        "\u00a76\u00a7l二、KubeJS 翻译覆盖",
        "\u00a7f对于包含KubeJS的整合包，我们会自动提取KubeJS脚本中的文本并进行翻译覆盖。但由于该功能目前仍处于开发阶段，系统可能不够成熟，\u00a7c\u00a7l可能会导致部分合成配方缺失等问题\u00a7r\u00a7f。如果您遇到此类问题，请尽快通过以下方式联系我们，我们会在几个小时内为您解决并协助修复您客户端的问题：",
        "\u00a7f  反馈QQ群：\u00a7b\u00a7l1073724937\u00a7r\u00a7f  |  官方网站：\u00a7n\u00a7bhttps://bbsmc.net\u00a7r\u00a7f",
        ""
    };
    private static final String AGREE_TEXT = "继续";

    private final JsonObject modpackJson;
    private final List<String> languagePacks;
    private final File configFile;

    private final List<FormattedCharSequence> wrappedLines = new ArrayList<>();

    public LocalizationNoticeScreen(JsonObject modpackJson, List<String> languagePacks, File configFile) {
        super(Component.literal(TITLE_TEXT));
        this.modpackJson = modpackJson;
        this.languagePacks = languagePacks;
        this.configFile = configFile;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 120;
        int buttonHeight = 20;
        int totalWidth = buttonWidth;
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height - 40;

        this.addRenderableWidget(Button.builder(
                Component.literal(AGREE_TEXT),
                btn -> onAgree())
                .bounds(startX, buttonY, buttonWidth, buttonHeight)
                .build());

        // 预计算自动换行
        wrappedLines.clear();
        int maxWidth = this.width - 60;
        for (String line : NOTICE_LINES) {
            if (line.isEmpty()) {
                wrappedLines.add(FormattedCharSequence.EMPTY);
            } else {
                wrappedLines.addAll(this.font.split(Component.literal(line), maxWidth));
            }
        }
    }

    private void onAgree() {
        try {
            modpackJson.addProperty("user_agreement", true);
            BbsmcCnInit.writeJsonToFile(configFile, modpackJson);
            BbsmcCnInit.LOGGER.info("User agreed to localization notice, user_agreement set to true");
        } catch (Exception e) {
            BbsmcCnInit.LOGGER.error("Failed to write modpack_info.json", e);
        }

        BbsmcCnInit.markAgreed();
        this.minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);

        int textX = 30;
        int textY = 40;
        int lineHeight = 11;

        for (FormattedCharSequence line : wrappedLines) {
            if (line != FormattedCharSequence.EMPTY) {
                guiGraphics.text(this.font, line, textX, textY, 0xFFDDDDDD);
            }
            textY += lineHeight;
        }
    }

}

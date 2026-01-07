package moe.ytonidc.ytongame_hostingmenu.client;

import net.minecraft.client.Minecraft;

public class RegionDetector {

    public static boolean shouldShowAds() {
        if (!Config.isAdsEnabled()) {
            return false;
        }

        if (!Config.isChineseOnly()) {
            return true;
        }

        try {
            String mcLanguage = Minecraft.getInstance().options.languageCode;
            return "zh_cn".equalsIgnoreCase(mcLanguage);
        } catch (Exception e) {
            String systemLanguage = System.getProperty("user.language", "");
            String systemCountry = System.getProperty("user.country", "");
            return "zh".equalsIgnoreCase(systemLanguage) && "CN".equalsIgnoreCase(systemCountry);
        }
    }
}

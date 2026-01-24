package moe.ytonidc.ytongame_hostingmenu.client;

import net.minecraft.client.Minecraft;

public class RegionDetector {

    // 缓存的语言代码，用于在语言切换后保持同步
    private static String cachedLanguage = null;

    /**
     * 刷新缓存的语言代码
     * 应在语言切换后调用此方法
     */
    public static void refreshLanguage(String newLanguage) {
        cachedLanguage = newLanguage;
    }

    public static boolean shouldShowAds() {
        if (!Config.isAdsEnabled()) {
            return false;
        }

        if (!Config.isChineseOnly()) {
            return true;
        }

        // 优先使用缓存的语言代码
        if (cachedLanguage != null) {
            return "zh_cn".equalsIgnoreCase(cachedLanguage);
        }

        // 实时检测当前语言设置
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

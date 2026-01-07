package moe.ytonidc.ytongame_hostingmenu.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<String> PURCHASE_URL;
    public static final ModConfigSpec.BooleanValue ENABLE_ADS;
    public static final ModConfigSpec.BooleanValue CHINESE_ONLY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");

        PURCHASE_URL = builder
                .comment("购买链接 URL")
                .define("purchaseUrl", "https://bbsmc.net/server?aff=LaotouY");

        ENABLE_ADS = builder
                .comment("是否启用广告")
                .define("enableAds", true);

        CHINESE_ONLY = builder
                .comment("是否仅对中文用户显示")
                .define("chineseOnly", true);

        builder.pop();

        SPEC = builder.build();
    }

    public static String getPurchaseUrl() {
        return PURCHASE_URL.get();
    }

    public static boolean isAdsEnabled() {
        return ENABLE_ADS.get();
    }

    public static boolean isChineseOnly() {
        return CHINESE_ONLY.get();
    }
}

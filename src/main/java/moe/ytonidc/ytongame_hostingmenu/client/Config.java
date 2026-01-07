package moe.ytonidc.ytongame_hostingmenu.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("ytongame-hostingmenu.json");

    private static ConfigData data = new ConfigData();

    public static class ConfigData {
        public String purchaseUrl = "https://example.com/buy";
        public boolean enableAds = true;
        public boolean chineseOnly = true;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                data = GSON.fromJson(json, ConfigData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPurchaseUrl() {
        return data.purchaseUrl;
    }

    public static boolean isAdsEnabled() {
        return data.enableAds;
    }

    public static boolean isChineseOnly() {
        return data.chineseOnly;
    }
}

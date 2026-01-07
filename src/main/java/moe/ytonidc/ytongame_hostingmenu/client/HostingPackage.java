package moe.ytonidc.ytongame_hostingmenu.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.ytonidc.ytongame_hostingmenu.Ytongame_hostingmenu;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HostingPackage {
    private static final String REMOTE_JSON_URL = "https://cdn.bbsmc.net/ytonidc/hosting_packages.json";
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    private final String name;
    private final String processor;
    private final String memory;
    private final int defaultBackupSlots;
    private final int maxBackupSlots;
    private final String storage;
    private final String recommendedPlayers;
    private final int price;
    private final int color;
    private final String tag;

    private static List<HostingPackage> ALL_PACKAGES = new ArrayList<>();
    private static boolean isLoaded = false;

    public HostingPackage(String name, String processor, String memory,
                          int defaultBackupSlots, int maxBackupSlots,
                          String storage, String recommendedPlayers, int price, int color, String tag) {
        this.name = name;
        this.processor = processor;
        this.memory = memory;
        this.defaultBackupSlots = defaultBackupSlots;
        this.maxBackupSlots = maxBackupSlots;
        this.storage = storage;
        this.recommendedPlayers = recommendedPlayers;
        this.price = price;
        this.color = color;
        this.tag = tag;
    }

    public String getName() { return name; }
    public String getProcessor() { return processor; }
    public String getMemory() { return memory; }
    public int getDefaultBackupSlots() { return defaultBackupSlots; }
    public int getMaxBackupSlots() { return maxBackupSlots; }
    public String getStorage() { return storage; }
    public String getRecommendedPlayers() { return recommendedPlayers; }
    public int getPrice() { return price; }
    public int getColor() { return color; }
    public String getTag() { return tag; }

    public static List<HostingPackage> getAllPackages() {
        return ALL_PACKAGES;
    }

    public static boolean isLoaded() {
        return isLoaded;
    }

    public static void loadAsync() {
        CompletableFuture.runAsync(() -> {
            if (loadFromRemote()) {
                Ytongame_hostingmenu.LOGGER.info("Loaded {} hosting packages from remote", ALL_PACKAGES.size());
                isLoaded = true;
                return;
            }

            Ytongame_hostingmenu.LOGGER.warn("Failed to load from remote, falling back to local resources");
            loadFromResources();
            isLoaded = true;
        });
    }

    private static boolean loadFromRemote() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(REMOTE_JSON_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", "YtonGame-HostingMenu/" + Ytongame_hostingmenu.MODID);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream is = connection.getInputStream()) {
                    return loadFromStream(is);
                }
            } else {
                Ytongame_hostingmenu.LOGGER.warn("Remote JSON returned status code: {}", responseCode);
                return false;
            }
        } catch (Exception e) {
            Ytongame_hostingmenu.LOGGER.warn("Failed to load hosting packages from remote: {}", e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void loadFromResources() {
        try {
            InputStream is = HostingPackage.class.getResourceAsStream("/hosting_packages.json");
            if (is != null) {
                if (loadFromStream(is)) {
                    Ytongame_hostingmenu.LOGGER.info("Loaded {} hosting packages from resources", ALL_PACKAGES.size());
                } else {
                    loadDefaultPackages();
                }
            } else {
                Ytongame_hostingmenu.LOGGER.error("Could not find hosting_packages.json in resources");
                loadDefaultPackages();
            }
        } catch (Exception e) {
            Ytongame_hostingmenu.LOGGER.error("Failed to load hosting packages from resources", e);
            loadDefaultPackages();
        }
    }

    private static boolean loadFromStream(InputStream is) {
        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray packagesArray = root.getAsJsonArray("packages");

            List<HostingPackage> packages = new ArrayList<>();
            for (JsonElement element : packagesArray) {
                JsonObject obj = element.getAsJsonObject();

                String name = obj.get("name").getAsString();
                String processor = obj.get("processor").getAsString();
                String memory = obj.get("memory").getAsString();
                int defaultBackupSlots = obj.get("defaultBackupSlots").getAsInt();
                int maxBackupSlots = obj.get("maxBackupSlots").getAsInt();
                String storage = obj.get("storage").getAsString();
                String recommendedPlayers = obj.get("recommendedPlayers").getAsString();
                int price = obj.get("price").getAsInt();

                String colorStr = obj.get("color").getAsString();
                int color = parseColor(colorStr);

                String tag = null;
                if (obj.has("tag") && !obj.get("tag").isJsonNull()) {
                    tag = obj.get("tag").getAsString();
                }

                packages.add(new HostingPackage(name, processor, memory, defaultBackupSlots,
                        maxBackupSlots, storage, recommendedPlayers, price, color, tag));
            }

            ALL_PACKAGES = packages;
            return true;
        } catch (Exception e) {
            Ytongame_hostingmenu.LOGGER.error("Failed to parse hosting packages JSON", e);
            return false;
        }
    }

    private static int parseColor(String colorStr) {
        try {
            if (colorStr.startsWith("0x") || colorStr.startsWith("0X")) {
                return (int) Long.parseLong(colorStr.substring(2), 16);
            } else if (colorStr.startsWith("#")) {
                return (int) Long.parseLong("FF" + colorStr.substring(1), 16);
            }
            return Integer.parseInt(colorStr);
        } catch (Exception e) {
            return 0xFFFFFFFF;
        }
    }

    private static void loadDefaultPackages() {
        ALL_PACKAGES = new ArrayList<>();
        ALL_PACKAGES.add(new HostingPackage("入门型", "AMD EPYC 7R13 3.6GHz", "8G", 1, 1, "30G", "2-3人", 58, 0xFF888888, null));
        ALL_PACKAGES.add(new HostingPackage("标准型", "Intel Core I7-14700K / AMD Ryzen9 9950X", "10G", 1, 2, "30G", "4-6人", 88, 0xFF5555FF, "热销"));
        ALL_PACKAGES.add(new HostingPackage("灵活型", "Intel Core I7-14700K / AMD Ryzen9 9950X", "12G", 1, 2, "30G", "6-8人", 108, 0xFF55FF55, null));
        ALL_PACKAGES.add(new HostingPackage("悦享型", "Intel Core I7-14700K / AMD Ryzen9 9950X", "18G", 1, 3, "30G", "9-12人", 168, 0xFFAA55FF, "多人推荐"));
        ALL_PACKAGES.add(new HostingPackage("曜石型", "Intel Core I7-14700K / AMD Ryzen9 9950X", "24G", 1, 5, "30G", "12-15人", 238, 0xFFFFAA00, null));
        Ytongame_hostingmenu.LOGGER.info("Loaded default hosting packages");
    }
}

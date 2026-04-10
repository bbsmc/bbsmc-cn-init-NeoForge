package net.bbsmc.cninit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.bbsmc.cninit.client.Config;
import net.bbsmc.cninit.client.LocalizationNoticeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.api.distmarker.Dist;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mod(value = BbsmcCnInit.MODID, dist = Dist.CLIENT)
public class BbsmcCnInit {
    public static final String MODID = "bbsmc_cn_init";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new Gson();
    public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

    private static boolean configLoaded = false;
    private static boolean userAgreement = false;
    private static List<String> languagePacks = new ArrayList<>();
    private static JsonObject modpackJson = null;
    private static File configFile = null;

    public BbsmcCnInit(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modEventBus.addListener(this::onClientSetup);

        NeoForge.EVENT_BUS.register(new ClientEventHandler());
    }

    private void onClientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        // No-op after ad removal
    }

    public static void writeJsonToFile(File file, JsonObject json) throws Exception {
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            GSON_PRETTY.toJson(json, writer);
        }
    }

    public static void markAgreed() {
        userAgreement = true;
    }

    public static void setupLanguageAndPacks(Minecraft mc, List<String> packs) {
        String currentLang = mc.getLanguageManager().getSelected();
        String targetLang = "zh_cn";
        boolean languageChanged = false;
        if (!targetLang.equals(currentLang)) {
            LOGGER.info("Current language is '{}', switching to zh_cn", currentLang);
            mc.getLanguageManager().setSelected(targetLang);
            mc.options.languageCode = targetLang;
            mc.options.save();
            LOGGER.info("Language set to '{}'", targetLang);
            languageChanged = true;
        }

        boolean packsChanged = false;
        if (!packs.isEmpty()) {
            File resourcePacksDir = new File(mc.gameDirectory, "resourcepacks");
            PackRepository packRepository = mc.getResourcePackRepository();
            packRepository.reload();

            List<String> packsToEnable = new ArrayList<>();
            for (String packName : packs) {
                File packFile = new File(resourcePacksDir, packName);
                if (packFile.exists()) {
                    packsToEnable.add("file/" + packName);
                } else {
                    LOGGER.warn("Resource pack not found: {}", packName);
                }
            }

            Collection<String> selected = new ArrayList<>(packRepository.getSelectedIds());
            for (String packId : packsToEnable) {
                Pack pack = packRepository.getPack(packId);
                if (pack != null && !selected.contains(packId)) {
                    selected.add(packId);
                    packsChanged = true;
                    LOGGER.info("Auto-enabled resource pack: {}", packId);
                }
            }

            if (packsChanged) {
                packRepository.setSelected(selected);
            }
        }

        if (languageChanged || packsChanged) {
            mc.reloadResourcePacks();
        }
    }

    private static void loadConfig(Minecraft mc) {
        if (configLoaded) {
            return;
        }
        configLoaded = true;

        configFile = new File(mc.gameDirectory, "config/modpack_info.json");
        if (!configFile.exists()) {
            LOGGER.debug("modpack_info.json not found, skipping auto setup");
            userAgreement = true;
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            modpackJson = GSON.fromJson(reader, JsonObject.class);

            if (modpackJson.has("user_agreement")) {
                userAgreement = modpackJson.get("user_agreement").getAsBoolean();
            }

            JsonArray packsArray = modpackJson.getAsJsonArray("language_packs");
            if (packsArray != null) {
                for (int i = 0; i < packsArray.size(); i++) {
                    languagePacks.add(packsArray.get(i).getAsString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read modpack_info.json", e);
            userAgreement = true;
        }

    }

    public static class ClientEventHandler {
        private boolean setupDone = false;

        @SubscribeEvent
        public void onClientTick(ClientTickEvent.Post event) {
            if (setupDone) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && mc.level == null) {
                return;
            }

            setupDone = true;
            loadConfig(mc);
        }

        @SubscribeEvent
        public void onScreenOpening(ScreenEvent.Opening event) {
            if (userAgreement) {
                return;
            }

            if (!configLoaded) {
                loadConfig(Minecraft.getInstance());
            }

            if (userAgreement) {
                return;
            }

            if (event.getNewScreen() instanceof SelectWorldScreen
                    || event.getNewScreen() instanceof JoinMultiplayerScreen) {
                event.setNewScreen(new LocalizationNoticeScreen(modpackJson, languagePacks, configFile));
            }
        }
    }
}

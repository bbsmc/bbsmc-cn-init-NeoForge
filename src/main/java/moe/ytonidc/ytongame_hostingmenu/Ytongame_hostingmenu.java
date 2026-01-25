package moe.ytonidc.ytongame_hostingmenu;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import moe.ytonidc.ytongame_hostingmenu.client.Config;
import moe.ytonidc.ytongame_hostingmenu.client.HostingPackage;
import moe.ytonidc.ytongame_hostingmenu.client.RegionDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mod(Ytongame_hostingmenu.MODID)
public class Ytongame_hostingmenu {
    public static final String MODID = "ytongame_hostingmenu";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation HOSTING_LOGO = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/logo_ytongame.png");
    public static final Gson GSON = new Gson();

    public Ytongame_hostingmenu(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        HostingPackage.loadAsync();

        event.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            PackRepository packRepository = mc.getResourcePackRepository();

            File configFile = new File(mc.gameDirectory, "config/modpack_info.json");
            if (!configFile.exists()) {
                LOGGER.debug("modpack_info.json not found, skipping auto setup");
                return;
            }

            // 检查并设置语言为简体中文
            String currentLang = mc.getLanguageManager().getSelected();
            String targetLang = "zh_cn";
            if (!targetLang.equals(currentLang)) {
                LOGGER.info("Current language is '{}', switching to zh_cn", currentLang);
                mc.getLanguageManager().setSelected(targetLang);
                mc.options.languageCode = targetLang;
                mc.options.save();
                LOGGER.info("Saving language '{}' to options", targetLang);
                RegionDetector.refreshLanguage(targetLang);
                mc.reloadResourcePacks();
            }

            List<String> languagePacks = new ArrayList<>();
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);

                JsonObject resourcePackInstall = json.getAsJsonObject("resource_pack_install");
                if (resourcePackInstall != null && resourcePackInstall.has("auto_install_enabled")) {
                    boolean autoInstallEnabled = resourcePackInstall.get("auto_install_enabled").getAsBoolean();
                    if (autoInstallEnabled) {
                        LOGGER.debug("Auto install already enabled by other means, skipping");
                        return;
                    }
                }

                JsonArray packsArray = json.getAsJsonArray("language_packs");
                if (packsArray != null) {
                    for (int i = 0; i < packsArray.size(); i++) {
                        languagePacks.add(packsArray.get(i).getAsString());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read modpack_info.json", e);
                return;
            }

            if (languagePacks.isEmpty()) {
                return;
            }

            File resourcePacksDir = new File(mc.gameDirectory, "resourcepacks");
            List<String> packsToEnable = new ArrayList<>();
            for (String packName : languagePacks) {
                File packFile = new File(resourcePacksDir, packName);
                if (packFile.exists()) {
                    packsToEnable.add("file/" + packName);
                } else {
                    LOGGER.warn("Resource pack not found: {}", packName);
                }
            }

            if (packsToEnable.isEmpty()) {
                return;
            }

            packRepository.reload();

            Collection<String> selected = new ArrayList<>(packRepository.getSelectedIds());
            boolean changed = false;
            for (String packId : packsToEnable) {
                Pack pack = packRepository.getPack(packId);
                if (pack != null && !selected.contains(packId)) {
                    selected.add(packId);
                    changed = true;
                    LOGGER.info("Auto-enabled resource pack: {}", packId);
                }
            }

            if (changed) {
                packRepository.setSelected(selected);
                mc.reloadResourcePacks();
            }
        });
    }
}
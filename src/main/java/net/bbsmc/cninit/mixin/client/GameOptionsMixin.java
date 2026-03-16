package net.bbsmc.cninit.mixin.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Options;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Mixin(Options.class)
public class GameOptionsMixin {

    @Shadow
    public String languageCode;

    @Shadow
    public List<String> resourcePacks;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bbsmcOnOptionsLoaded(CallbackInfo ci) {
        try {
            File gameDir = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().toFile();
            File configFile = new File(gameDir, "config/modpack_info.json");
            if (!configFile.exists()) return;

            JsonObject config;
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                config = new Gson().fromJson(reader, JsonObject.class);
            }

            if (!"zh_cn".equals(this.languageCode)) {
                this.languageCode = "zh_cn";
            }

            JsonArray packsArray = config.getAsJsonArray("language_packs");
            if (packsArray != null) {
                File rpDir = new File(gameDir, "resourcepacks");
                for (int i = 0; i < packsArray.size(); i++) {
                    String packName = packsArray.get(i).getAsString();
                    String packId = "file/" + packName;
                    if (new File(rpDir, packName).exists()
                            && !this.resourcePacks.contains(packId)) {
                        List<String> mutablePacks = new java.util.ArrayList<>(this.resourcePacks);
                        mutablePacks.add(packId);
                        this.resourcePacks = mutablePacks;
                    }
                }
            }

            ((Options)(Object)this).save();

        } catch (Exception e) {
            LoggerFactory.getLogger("bbsmc-cn-init")
                    .warn("Failed to pre-configure options: {}", e.getMessage());
        }
    }
}

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
                List<String> mutablePacks = new java.util.ArrayList<>(this.resourcePacks);
                boolean changed = false;
                // options.resourcePacks 列表末尾 = 最高优先级（FallbackResourceManager 末尾向前查找）
                // 强制把我们的包放到列表末尾，覆盖其他第三方资源包
                for (int i = 0; i < packsArray.size(); i++) {
                    String packName = packsArray.get(i).getAsString();
                    String packId = "file/" + packName;
                    if (!new File(rpDir, packName).exists()) continue;
                    boolean removed = mutablePacks.remove(packId);
                    mutablePacks.add(packId);
                    if (!removed || mutablePacks.indexOf(packId) != this.resourcePacks.indexOf(packId)) {
                        changed = true;
                    }
                }
                if (changed) {
                    this.resourcePacks = mutablePacks;
                }
            }

            // 不调用 save()：<init> 阶段其他模组（如 dynamic_fps）在 save() 上的 Mixin
            // 可能因 ModList 未初始化而崩溃，且 Java 的 <clinit> 失败后该类永久不可用。
            // 字段修改已在内存中生效，游戏正常退出时会自动保存到 options.txt。

        } catch (Exception e) {
            LoggerFactory.getLogger("bbsmc-cn-init")
                    .warn("Failed to pre-configure options: {}", e.getMessage());
        }
    }
}

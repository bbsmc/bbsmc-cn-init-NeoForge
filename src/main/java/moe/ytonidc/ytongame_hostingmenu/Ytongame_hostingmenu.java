package moe.ytonidc.ytongame_hostingmenu;

import com.mojang.logging.LogUtils;
import moe.ytonidc.ytongame_hostingmenu.client.Config;
import moe.ytonidc.ytongame_hostingmenu.client.HostingPackage;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod(Ytongame_hostingmenu.MODID)
public class Ytongame_hostingmenu {
    public static final String MODID = "ytongame_hostingmenu";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation HOSTING_LOGO = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/logo_ytongame.png");

    public Ytongame_hostingmenu(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        HostingPackage.loadAsync();
    }
}

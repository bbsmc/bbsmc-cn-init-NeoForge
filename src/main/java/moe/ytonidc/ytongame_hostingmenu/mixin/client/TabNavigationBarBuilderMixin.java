package moe.ytonidc.ytongame_hostingmenu.mixin.client;

import moe.ytonidc.ytongame_hostingmenu.client.HostingTab;
import moe.ytonidc.ytongame_hostingmenu.client.RegionDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TabNavigationBar.Builder.class)
public class TabNavigationBarBuilderMixin {

    @Shadow @Final private TabManager tabManager;
    @Shadow @Final private List<Tab> tabs;
    @Shadow private int width;

    @Inject(method = "build", at = @At("HEAD"))
    private void onBuild(CallbackInfoReturnable<TabNavigationBar> cir) {
        if (!RegionDetector.shouldShowAds()) {
            return;
        }

        if (!(Minecraft.getInstance().screen instanceof CreateWorldScreen screen)) {
            return;
        }

        for (Tab tab : tabs) {
            if (tab instanceof HostingTab) {
                return;
            }
        }

        tabs.add(new HostingTab(screen));
    }
}

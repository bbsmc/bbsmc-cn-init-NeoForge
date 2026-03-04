package moe.ytonidc.ytongame_hostingmenu.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        builder.pop();

        SPEC = builder.build();
    }
}

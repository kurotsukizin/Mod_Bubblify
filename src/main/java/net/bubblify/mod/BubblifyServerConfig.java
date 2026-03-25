package net.bubblify.mod;

import net.minecraftforge.common.ForgeConfigSpec;

public class BubblifyServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue SHOW_CHAT;

    static {
        BUILDER.push("Config_Adm");

        SHOW_CHAT = BUILDER.comment("If set to “false,” text messages will not appear in the standard chat (only in speech bubbles).")
                .define("show_chat", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
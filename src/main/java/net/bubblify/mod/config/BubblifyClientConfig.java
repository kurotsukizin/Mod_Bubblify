package net.bubblify.mod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BubblifyClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue RED;
    public static final ForgeConfigSpec.IntValue GREEN;
    public static final ForgeConfigSpec.IntValue BLUE;
    public static final ForgeConfigSpec.BooleanValue TEXTO_BRANCO;

    static {
        BUILDER.push("Configuracoes_do_Bubble_Chat");

        RED = BUILDER.comment("Cor Vermelha do Chat")
                .defineInRange("cor_vermelha", 255, 0, 255);

        GREEN = BUILDER.comment("Cor Verde do Chat")
                .defineInRange("cor_verde", 255, 0, 255);

        BLUE = BUILDER.comment("Cor Azul do Chat")
                .defineInRange("cor_azul", 255, 0, 255);

        TEXTO_BRANCO = BUILDER.comment("Define se a cor do texto na bolha será branca (true) ou preta (false)")
                .define("texto_branco", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
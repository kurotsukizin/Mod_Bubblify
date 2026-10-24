package net.bubblify.mod.bubble;

public final class BubbleStyleDefinition {

    public String id;
    public String displayName;

    /*
     * Valores aceitos:
     * DEFAULT, FREE, VIP, PREMIUM, STAFF, ADMIN
     */
    public String category = "DEFAULT";

    /*
     * Nomes livres dos três PNGs dentro da pasta do estilo.
     */
    public String top;
    public String body;
    public String tail;

    /*
     * Medidas-fonte do estilo.
     */
    public float sourceWidth = 128.0F;

    public float topHeight = 16.0F;
    public float bodyHeight = 32.0F;

    public float tailWidth = 16.0F;
    public float tailHeight = 16.0F;

    /*
     * Fatias que ficam fixas durante a expansão horizontal.
     */
    public float leftSlice = 16.0F;
    public float rightSlice = 16.0F;

    /*
     * Área segura para texto.
     */
    public float paddingLeft = 16.0F;
    public float paddingRight = 16.0F;

    public float paddingTop = 7.0F;
    public float paddingBottom = 7.0F;

    /*
     * Ajuste vertical da bolha específica.
     */
    public float heightOffset = 2.10F;
}
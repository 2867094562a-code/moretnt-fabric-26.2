import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Creates the original 16x16 pixel textures used by More TNT.
 * Run from the project root with:
 *   java tools/GenerateTntTextures.java src/main/resources/assets/moretnt/textures/block
 */
public final class GenerateTntTextures {
    private static final int SIZE = 16;
    private static final int OUTLINE = rgb(41, 29, 28);
    private static final int RED_DARK = rgb(111, 35, 29);
    private static final int RED = rgb(184, 53, 39);
    private static final int RED_LIGHT = rgb(232, 83, 52);
    private static final int PAPER = rgb(218, 194, 139);
    private static final int PAPER_LIGHT = rgb(241, 222, 169);
    private static final int PAPER_SHADOW = rgb(173, 142, 102);
    private static final int FUSE_DARK = rgb(42, 45, 47);
    private static final int FUSE = rgb(106, 111, 109);

    private record Variant(String id, int accent, int rune) {}

    private static final Variant[] VARIANTS = {
        v("lumber_tnt", 0x7B4D2A, 0x29), v("soil_tnt", 0x8B5A35, 0x0D),
        v("stone_tnt", 0x9299A0, 0x17), v("ore_miner_tnt", 0x20D1CB, 0x31),
        v("ore_safe_tnt", 0xE6C44C, 0x16), v("crop_tnt", 0x79B83B, 0x2D),
        v("glass_tnt", 0x9FE7F6, 0x2A), v("wool_tnt", 0xF3F0E9, 0x0B),
        v("concrete_tnt", 0x8E9AA7, 0x35), v("ice_tnt", 0x79C9FF, 0x1E),
        v("nether_tnt", 0xB04442, 0x26), v("endstone_tnt", 0xD4CC78, 0x19),
        v("wood_tnt", 0xB37A3C, 0x33), v("water_tnt", 0x2D9FE8, 0x0F),
        v("lava_tnt", 0xF36A27, 0x3A), v("frost_tnt", 0xBCEBFF, 0x15),
        v("fire_tnt", 0xFFB326, 0x27), v("glow_tnt", 0xFFD95B, 0x3C),
        v("sponge_tnt", 0xE0C83B, 0x12), v("tunnel_tnt", 0x666E73, 0x2F),
        v("shaft_tnt", 0xAD7B46, 0x1B), v("quarry_tnt", 0x626D77, 0x39),
        v("surface_tnt", 0x65A943, 0x0E), v("trench_tnt", 0x8A6038, 0x36),
        v("demolition_tnt", 0x595F67, 0x23), v("mega_tnt", 0xFF7C22, 0x3D),
        v("colossal_tnt", 0xE03726, 0x3B), v("obsidian_tnt", 0x7A4FA2, 0x1D),
        v("bedrock_tnt", 0x5B5F5C, 0x2E), v("void_tnt", 0x542582, 0x34)
    };

    private GenerateTntTextures() {}

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0
            ? Path.of("src/main/resources/assets/moretnt/textures/block")
            : Path.of(args[0]);
        Files.createDirectories(output);
        for (Variant variant : VARIANTS) {
            write(output, variant.id + "_side", createSide(variant, false));
            write(output, variant.id + "_side_lit", createSide(variant, true));
            write(output, variant.id + "_top", createTop(variant, false));
            write(output, variant.id + "_top_lit", createTop(variant, true));
            write(output, variant.id + "_bottom", createBottom(variant));
        }
        System.out.println("Generated " + (VARIANTS.length * 5) + " More TNT texture files in " + output.toAbsolutePath());
    }

    private static Variant v(String id, int accent, int rune) {
        return new Variant(id, 0xFF000000 | accent, rune);
    }

    private static void write(Path directory, String name, BufferedImage image) throws IOException {
        ImageIO.write(image, "PNG", directory.resolve(name + ".png").toFile());
    }

    private static BufferedImage createSide(Variant variant, boolean lit) {
        BufferedImage image = image(lit ? RED_LIGHT : RED_DARK);
        // Dark frame, red casing, and an aged paper label are intentionally block-game-like,
        // but are fully original rather than copied from a vanilla texture.
        rect(image, 0, 0, 15, 15, OUTLINE);
        rect(image, 1, 1, 14, 3, lit ? rgb(255, 126, 71) : RED_LIGHT);
        rect(image, 1, 12, 14, 14, lit ? rgb(247, 92, 53) : RED);
        rect(image, 2, 4, 13, 11, lit ? rgb(255, 237, 186) : PAPER);
        rect(image, 2, 4, 13, 4, lit ? rgb(255, 251, 217) : PAPER_LIGHT);
        rect(image, 2, 11, 13, 11, lit ? rgb(236, 188, 126) : PAPER_SHADOW);
        for (int x = 2; x <= 13; x += 3) {
            pixel(image, x, 2, RED_DARK);
            pixel(image, x + 1, 13, RED_DARK);
        }
        drawRune(image, 5, 6, variant.rune, lit ? brighten(variant.accent, 80) : variant.accent);
        if (lit) {
            pixel(image, 3, 5, rgb(255, 255, 235));
            pixel(image, 12, 10, rgb(255, 237, 131));
        }
        return image;
    }

    private static BufferedImage createTop(Variant variant, boolean lit) {
        BufferedImage image = image(OUTLINE);
        rect(image, 1, 1, 14, 14, lit ? rgb(246, 94, 51) : RED);
        rect(image, 2, 2, 13, 4, lit ? rgb(255, 142, 71) : RED_LIGHT);
        rect(image, 2, 11, 13, 13, RED_DARK);
        // A small asymmetric fuse makes every TNT read as an explosive even in an inventory icon.
        rect(image, 6, 3, 9, 8, FUSE_DARK);
        rect(image, 7, 2, 8, 8, FUSE);
        pixel(image, 8, 3, variant.accent);
        pixel(image, 5, 8, variant.accent);
        pixel(image, 10, 8, variant.accent);
        if (lit) {
            pixel(image, 7, 1, rgb(255, 242, 141));
            pixel(image, 8, 1, rgb(255, 255, 221));
            pixel(image, 6, 2, rgb(255, 203, 67));
            pixel(image, 9, 2, rgb(255, 203, 67));
        }
        return image;
    }

    private static BufferedImage createBottom(Variant variant) {
        BufferedImage image = image(OUTLINE);
        rect(image, 1, 1, 14, 14, RED_DARK);
        rect(image, 2, 2, 13, 13, RED);
        rect(image, 3, 3, 12, 12, rgb(122, 39, 34));
        rect(image, 5, 5, 10, 10, darken(variant.accent, 75));
        rect(image, 6, 6, 9, 9, variant.accent);
        pixel(image, 7, 7, brighten(variant.accent, 50));
        return image;
    }

    private static void drawRune(BufferedImage image, int x, int y, int rune, int color) {
        int shadow = darken(color, 65);
        // Six bits draw a unique icon-like circuit/rune. Accent and pattern together distinguish all 30 variants.
        int[][] dots = {{0, 0}, {2, 0}, {4, 0}, {0, 2}, {4, 2}, {2, 4}};
        for (int bit = 0; bit < dots.length; bit++) {
            if ((rune & (1 << bit)) != 0) {
                pixel(image, x + dots[bit][0], y + dots[bit][1], color);
                pixel(image, x + dots[bit][0] + 1, y + dots[bit][1], shadow);
            }
        }
        pixel(image, x + 2, y + 2, brighten(color, 45));
        pixel(image, x + 1, y + 2, color);
        pixel(image, x + 3, y + 2, color);
        pixel(image, x + 2, y + 1, color);
        pixel(image, x + 2, y + 3, color);
    }

    private static BufferedImage image(int color) {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        rect(image, 0, 0, SIZE - 1, SIZE - 1, color);
        return image;
    }

    private static void rect(BufferedImage image, int x0, int y0, int x1, int y1, int color) {
        for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) pixel(image, x, y, color);
    }

    private static void pixel(BufferedImage image, int x, int y, int color) {
        if (x >= 0 && y >= 0 && x < SIZE && y < SIZE) image.setRGB(x, y, color);
    }

    private static int brighten(int color, int amount) {
        return rgb(Math.min(255, ((color >> 16) & 255) + amount), Math.min(255, ((color >> 8) & 255) + amount), Math.min(255, (color & 255) + amount));
    }

    private static int darken(int color, int amount) {
        return rgb(Math.max(0, ((color >> 16) & 255) - amount), Math.max(0, ((color >> 8) & 255) - amount), Math.max(0, (color & 255) - amount));
    }

    private static int rgb(int red, int green, int blue) {
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }
}

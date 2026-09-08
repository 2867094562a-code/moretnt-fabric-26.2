import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Generates the original 64x64 More TNT material set.
 *
 * <p>The large front code and top pictogram are intentionally part of every block model, so
 * inventory thumbnails no longer fall back to a generic TNT appearance.</p>
 */
public final class GenerateTntTextures {
    private static final int SIZE = 64;
    private static final int OUTLINE = rgb(29, 21, 24);
    private static final int METAL_DARK = rgb(58, 48, 51);
    private static final int RED_DARK = rgb(101, 26, 28);
    private static final int RED = rgb(185, 48, 39);
    private static final int RED_LIGHT = rgb(244, 84, 48);
    private static final int PAPER = rgb(215, 190, 132);
    private static final int PAPER_LIGHT = rgb(248, 228, 166);
    private static final int PAPER_SHADOW = rgb(144, 104, 76);
    private static final int FUSE = rgb(108, 116, 117);
    private static final int FUSE_DARK = rgb(34, 39, 42);

    private record Variant(String id, String code, int accent, int badge) {}

    private static final Variant[] VARIANTS = {
        v("lumber_tnt", "LOG", 0x72A843, 0), v("soil_tnt", "DRT", 0xA8663B, 1),
        v("stone_tnt", "STN", 0x9BA4A8, 2), v("ore_miner_tnt", "ORE", 0x39E6D6, 3),
        v("ore_safe_tnt", "SAF", 0xF2CA4E, 4), v("crop_tnt", "CRP", 0x84CB45, 5),
        v("glass_tnt", "GLS", 0xA9F0F5, 6), v("wool_tnt", "WOL", 0xF2EEE3, 7),
        v("concrete_tnt", "CON", 0xA6B2BD, 8), v("ice_tnt", "ICE", 0x7DDBFF, 9),
        v("nether_tnt", "NTH", 0xBD4B44, 10), v("endstone_tnt", "END", 0xDED27C, 11),
        v("wood_tnt", "WOD", 0xC9863C, 12), v("water_tnt", "H2O", 0x319EEB, 13),
        v("lava_tnt", "LAV", 0xF16C20, 14), v("frost_tnt", "FRZ", 0xC7F1FF, 15),
        v("fire_tnt", "FIR", 0xFFB621, 16), v("glow_tnt", "LUX", 0xFFE36A, 17),
        v("sponge_tnt", "DRY", 0xE5CE42, 18), v("tunnel_tnt", "TUN", 0x75808A, 19),
        v("shaft_tnt", "SFT", 0xB98A51, 20), v("quarry_tnt", "QRY", 0x6D7A89, 21),
        v("surface_tnt", "FLT", 0x74B64B, 22), v("trench_tnt", "TRN", 0x96633A, 23),
        v("demolition_tnt", "DMO", 0x697687, 24), v("mega_tnt", "MG1", 0xFF8022, 25),
        v("colossal_tnt", "MG2", 0xE73B25, 26), v("nuclear_tnt", "NUC", 0xFFD833, 30),
        v("super_nuclear_tnt", "SNU", 0x66F3FF, 31), v("obsidian_tnt", "OBS", 0x935AC2, 27),
        v("bedrock_tnt", "BDR", 0x6B706C, 28), v("void_tnt", "NIL", 0x7841C8, 29)
    };

    private static final String[][] BADGES = {
        {"00100","01110","00100","01110","11111"}, {"00100","01110","00100","00100","01110"},
        {"01110","11111","11111","11111","01110"}, {"00100","01110","11111","01110","00100"},
        {"11111","11011","10101","01110","00100"}, {"00100","01110","10101","00100","01110"},
        {"10101","01010","10101","01010","10101"}, {"01110","11111","11111","01110","00000"},
        {"11111","10101","11111","10101","11111"}, {"10101","01010","11111","01010","10101"},
        {"10101","01110","11111","01110","10101"}, {"11111","10001","10101","10001","11111"},
        {"11111","10001","11111","10001","11111"}, {"00100","01110","11111","11111","01110"},
        {"00100","01110","11111","00100","01110"}, {"10101","01010","11111","01010","10101"},
        {"00100","01110","11111","01110","00100"}, {"01110","10001","10101","10001","01110"},
        {"11111","10101","11111","10101","11111"}, {"11111","10001","10101","10001","11111"},
        {"00100","00100","00100","01110","11111"}, {"11111","10001","10101","10001","11111"},
        {"00000","11111","00000","11111","00000"}, {"10001","11011","11111","11011","10001"},
        {"10101","01110","00100","01110","10101"}, {"00100","01110","11111","01110","00100"},
        {"11111","11011","10101","11011","11111"}, {"11111","10001","10101","10001","11111"},
        {"11011","01110","11111","01110","11011"}, {"10001","01010","00100","01010","10001"},
        {"01110","11011","01110","11011","01110"}, {"11111","10101","01010","10101","11111"}
    };

    private GenerateTntTextures() {}

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0 ? Path.of("src/main/resources/assets/moretnt/textures/block") : Path.of(args[0]);
        Files.createDirectories(output);
        for (Variant variant : VARIANTS) {
            write(output, variant.id + "_side", createSide(variant, false));
            write(output, variant.id + "_side_lit", createSide(variant, true));
            write(output, variant.id + "_top", createTop(variant, false));
            write(output, variant.id + "_top_lit", createTop(variant, true));
            write(output, variant.id + "_bottom", createBottom(variant));
        }
        System.out.println("Generated " + (VARIANTS.length * 5) + " original 64x64 More TNT textures in " + output.toAbsolutePath());
    }

    private static Variant v(String id, String code, int accent, int badge) {
        return new Variant(id, code, 0xFF000000 | accent, badge);
    }

    private static void write(Path directory, String name, BufferedImage image) throws IOException {
        ImageIO.write(image, "PNG", directory.resolve(name + ".png").toFile());
    }

    private static BufferedImage createSide(Variant variant, boolean lit) {
        BufferedImage image = image(OUTLINE);
        rect(image, 2, 2, 61, 61, METAL_DARK);
        rect(image, 4, 4, 59, 15, lit ? brighten(RED_LIGHT, 30) : RED_LIGHT);
        rect(image, 4, 48, 59, 59, lit ? brighten(RED, 30) : RED);
        rect(image, 5, 17, 58, 46, lit ? brighten(PAPER, 20) : PAPER);
        rect(image, 5, 17, 58, 19, PAPER_LIGHT);
        rect(image, 5, 44, 58, 46, PAPER_SHADOW);
        for (int x = 8; x <= 54; x += 9) {
            rect(image, x, 7, x + 2, 9, RED_DARK);
            rect(image, x + 1, 52, x + 3, 54, RED_DARK);
        }
        drawBadge(image, variant.badge, 27, 5, 2, lit ? rgb(255, 247, 143) : variant.accent);
        drawCode(image, 9, 22, variant.code, lit ? brighten(variant.accent, 65) : darken(variant.accent, 35));
        if (variant.badge >= 30) drawHazardCorners(image, variant, lit);
        return image;
    }

    private static BufferedImage createTop(Variant variant, boolean lit) {
        BufferedImage image = image(OUTLINE);
        rect(image, 2, 2, 61, 61, lit ? brighten(RED, 26) : RED);
        rect(image, 5, 5, 58, 58, lit ? brighten(RED_LIGHT, 18) : RED_LIGHT);
        rect(image, 8, 8, 55, 55, lit ? brighten(PAPER, 18) : PAPER);
        rect(image, 10, 10, 53, 53, darken(PAPER, 24));
        drawBadge(image, variant.badge, 14, 16, 7, lit ? rgb(255, 249, 158) : variant.accent);
        rect(image, 28, 3, 35, 14, FUSE_DARK);
        rect(image, 30, 1, 33, 14, lit ? rgb(255, 240, 116) : FUSE);
        if (lit) rect(image, 28, 0, 35, 2, rgb(255, 250, 174));
        if (variant.badge >= 30) drawHazardCorners(image, variant, lit);
        return image;
    }

    private static BufferedImage createBottom(Variant variant) {
        BufferedImage image = image(OUTLINE);
        rect(image, 2, 2, 61, 61, RED_DARK);
        rect(image, 6, 6, 57, 57, darken(variant.accent, 72));
        rect(image, 12, 12, 51, 51, variant.accent);
        drawBadge(image, variant.badge, 22, 22, 4, brighten(variant.accent, 55));
        return image;
    }

    private static void drawHazardCorners(BufferedImage image, Variant variant, boolean lit) {
        int warning = variant.badge == 30 ? rgb(255, 204, 40) : rgb(102, 246, 255);
        int inner = lit ? rgb(255, 255, 236) : warning;
        for (int corner : new int[] {6, 53}) {
            rect(image, corner, 6, corner + 4, 8, warning);
            rect(image, corner, 55, corner + 4, 57, warning);
        }
        rect(image, 29, 28, 34, 33, inner);
    }

    private static void drawBadge(BufferedImage image, int badge, int x, int y, int scale, int color) {
        String[] pattern = BADGES[badge];
        int shadow = darken(color, 72);
        for (int row = 0; row < pattern.length; row++) for (int col = 0; col < pattern[row].length(); col++) if (pattern[row].charAt(col) == '1') {
            int left = x + col * scale;
            int top = y + row * scale;
            rect(image, left, top, left + scale - 1, top + scale - 1, color);
            if (scale > 1) {
                rect(image, left, top, left + scale - 1, top + 1, brighten(color, 42));
                rect(image, left, top + scale - 2, left + scale - 1, top + scale - 1, shadow);
            }
        }
    }

    private static void drawCode(BufferedImage image, int x, int y, String code, int color) {
        for (int character = 0; character < code.length(); character++) {
            String[] glyph = glyph(code.charAt(character));
            int left = x + character * 17;
            for (int row = 0; row < glyph.length; row++) for (int col = 0; col < glyph[row].length(); col++) if (glyph[row].charAt(col) == '1') {
                rect(image, left + col * 4, y + row * 4, left + col * 4 + 3, y + row * 4 + 3, color);
            }
        }
    }

    private static String[] glyph(char character) {
        return switch (character) {
            case 'A' -> new String[] {"010","101","111","101","101"}; case 'B' -> new String[] {"110","101","110","101","110"};
            case 'C' -> new String[] {"011","100","100","100","011"}; case 'D' -> new String[] {"110","101","101","101","110"};
            case 'E' -> new String[] {"111","100","110","100","111"}; case 'F' -> new String[] {"111","100","110","100","100"};
            case 'G' -> new String[] {"011","100","101","101","011"}; case 'H' -> new String[] {"101","101","111","101","101"};
            case 'I' -> new String[] {"111","010","010","010","111"}; case 'L' -> new String[] {"100","100","100","100","111"};
            case 'M' -> new String[] {"101","111","111","101","101"}; case 'N' -> new String[] {"101","111","111","111","101"};
            case 'O' -> new String[] {"010","101","101","101","010"}; case 'P' -> new String[] {"110","101","110","100","100"};
            case 'Q' -> new String[] {"010","101","101","011","001"}; case 'R' -> new String[] {"110","101","110","101","101"};
            case 'S' -> new String[] {"011","100","010","001","110"}; case 'T' -> new String[] {"111","010","010","010","010"};
            case 'U' -> new String[] {"101","101","101","101","111"}; case 'V' -> new String[] {"101","101","101","101","010"};
            case 'W' -> new String[] {"101","101","111","111","101"}; case 'X' -> new String[] {"101","101","010","101","101"};
            case 'Y' -> new String[] {"101","101","010","010","010"}; case 'Z' -> new String[] {"111","001","010","100","111"};
            case '1' -> new String[] {"010","110","010","010","111"}; case '2' -> new String[] {"110","001","010","100","111"};
            default -> new String[] {"000","000","000","000","000"};
        };
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

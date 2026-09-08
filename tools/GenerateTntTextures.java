import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Creates original 32x32 More TNT textures. Each face has a large high-contrast purpose code
 * and a matching top pictogram, so the variants stay recognisable in a 32x resource-pack setup.
 */
public final class GenerateTntTextures {
    private static final int SIZE = 32;
    private static final int OUTLINE = rgb(37, 24, 25);
    private static final int RED_DARK = rgb(112, 31, 28);
    private static final int RED = rgb(184, 52, 39);
    private static final int RED_LIGHT = rgb(238, 86, 48);
    private static final int PAPER = rgb(214, 191, 139);
    private static final int PAPER_LIGHT = rgb(245, 226, 171);
    private static final int PAPER_SHADOW = rgb(160, 124, 89);
    private static final int FUSE = rgb(91, 96, 96);
    private static final int FUSE_DARK = rgb(39, 43, 45);

    private record Variant(String id, String code, int accent, int badge) {}

    private static final Variant[] VARIANTS = {
        v("lumber_tnt", "LOG", 0x6F9D3C, 0), v("soil_tnt", "DRT", 0x9B6039, 1),
        v("stone_tnt", "STN", 0x8A939A, 2), v("ore_miner_tnt", "ORE", 0x38E0D1, 3),
        v("ore_safe_tnt", "SAF", 0xF0C84B, 4), v("crop_tnt", "CRP", 0x7DC143, 5),
        v("glass_tnt", "GLS", 0x9BE9F1, 6), v("wool_tnt", "WOL", 0xEDE9E0, 7),
        v("concrete_tnt", "CON", 0x9AA6B2, 8), v("ice_tnt", "ICE", 0x72CFFF, 9),
        v("nether_tnt", "NTH", 0xB64843, 10), v("endstone_tnt", "END", 0xD9CF76, 11),
        v("wood_tnt", "WOD", 0xBF7D35, 12), v("water_tnt", "H2O", 0x2C9BEA, 13),
        v("lava_tnt", "LAV", 0xF06A23, 14), v("frost_tnt", "FRZ", 0xB9EAFF, 15),
        v("fire_tnt", "FIR", 0xFFB525, 16), v("glow_tnt", "LUX", 0xFFE364, 17),
        v("sponge_tnt", "DRY", 0xDEC840, 18), v("tunnel_tnt", "TUN", 0x667179, 19),
        v("shaft_tnt", "SFT", 0xAF7B45, 20), v("quarry_tnt", "QRY", 0x606C78, 21),
        v("surface_tnt", "FLT", 0x69AB42, 22), v("trench_tnt", "TRN", 0x895D35, 23),
        v("demolition_tnt", "DMO", 0x5C6672, 24), v("mega_tnt", "MG1", 0xFF7922, 25),
        v("colossal_tnt", "MG2", 0xE33425, 26), v("obsidian_tnt", "OBS", 0x8754B1, 27),
        v("bedrock_tnt", "BDR", 0x5D605C, 28), v("void_tnt", "NIL", 0x6B36B8, 29)
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
        {"11011","01110","11111","01110","11011"}, {"10001","01010","00100","01010","10001"}
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
        System.out.println("Generated " + (VARIANTS.length * 5) + " original 32x32 More TNT textures in " + output.toAbsolutePath());
    }

    private static Variant v(String id, String code, int accent, int badge) {
        return new Variant(id, code, 0xFF000000 | accent, badge);
    }

    private static void write(Path directory, String name, BufferedImage image) throws IOException {
        ImageIO.write(image, "PNG", directory.resolve(name + ".png").toFile());
    }

    private static BufferedImage createSide(Variant variant, boolean lit) {
        BufferedImage image = image(OUTLINE);
        int upper = lit ? brighten(RED_LIGHT, 35) : RED_LIGHT;
        int lower = lit ? brighten(RED, 35) : RED;
        rect(image, 2, 2, 29, 6, upper);
        rect(image, 2, 25, 29, 29, lower);
        rect(image, 3, 8, 28, 23, lit ? brighten(PAPER, 22) : PAPER);
        rect(image, 3, 8, 28, 9, PAPER_LIGHT);
        rect(image, 3, 22, 28, 23, PAPER_SHADOW);
        for (int x = 4; x <= 27; x += 5) {
            rect(image, x, 3, x + 1, 4, RED_DARK);
            rect(image, x + 1, 27, x + 2, 28, RED_DARK);
        }
        drawCode(image, 5, 11, variant.code, lit ? brighten(variant.accent, 68) : darken(variant.accent, 34));
        drawBadge(image, variant.badge, 13, 2, 1, lit ? rgb(255, 241, 115) : variant.accent);
        return image;
    }

    private static BufferedImage createTop(Variant variant, boolean lit) {
        BufferedImage image = image(OUTLINE);
        rect(image, 2, 2, 29, 29, lit ? brighten(RED, 30) : RED);
        rect(image, 4, 4, 27, 27, lit ? brighten(RED_LIGHT, 20) : RED_LIGHT);
        rect(image, 6, 6, 25, 25, lit ? brighten(PAPER, 20) : PAPER);
        drawBadge(image, variant.badge, 8, 8, 3, lit ? rgb(255, 245, 134) : variant.accent);
        rect(image, 14, 2, 17, 6, FUSE_DARK);
        rect(image, 15, 1, 16, 6, lit ? rgb(255, 238, 116) : FUSE);
        if (lit) rect(image, 14, 0, 17, 1, rgb(255, 246, 157));
        return image;
    }

    private static BufferedImage createBottom(Variant variant) {
        BufferedImage image = image(OUTLINE);
        rect(image, 2, 2, 29, 29, RED_DARK);
        rect(image, 5, 5, 26, 26, darken(variant.accent, 62));
        rect(image, 8, 8, 23, 23, variant.accent);
        drawBadge(image, variant.badge, 12, 12, 2, brighten(variant.accent, 55));
        return image;
    }

    private static void drawBadge(BufferedImage image, int badge, int x, int y, int scale, int color) {
        String[] pattern = BADGES[badge];
        int shadow = darken(color, 70);
        for (int row = 0; row < pattern.length; row++) for (int col = 0; col < pattern[row].length(); col++) if (pattern[row].charAt(col) == '1') {
            rect(image, x + col * scale, y + row * scale, x + (col + 1) * scale - 1, y + (row + 1) * scale - 1, color);
            if (scale > 1) pixel(image, x + col * scale, y + row * scale, shadow);
        }
    }

    private static void drawCode(BufferedImage image, int x, int y, String code, int color) {
        for (int character = 0; character < code.length(); character++) {
            String[] glyph = glyph(code.charAt(character));
            int left = x + character * 8;
            for (int row = 0; row < glyph.length; row++) for (int col = 0; col < glyph[row].length(); col++) if (glyph[row].charAt(col) == '1') {
                rect(image, left + col * 2, y + row * 2, left + col * 2 + 1, y + row * 2 + 1, color);
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

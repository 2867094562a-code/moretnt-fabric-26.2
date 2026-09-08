import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Generates original 32x32 vanilla-style sprites for the Glowstone Light Arrow. */
public final class GenerateGlowstoneArrowTextures {
    private static final int SIZE = 32;
    private static final int CLEAR = 0x00000000;
    private static final int OUTLINE = 0xFF33291F;
    private static final int SHAFT_DARK = 0xFF6B4A2D;
    private static final int SHAFT = 0xFFC18952;
    private static final int GOLD_DARK = 0xFFB86B14;
    private static final int GOLD = 0xFFFFC844;
    private static final int GLOW = 0xFFFFF2A8;
    private static final int FEATHER_DARK = 0xFF7AA7B0;
    private static final int FEATHER = 0xFFC4F7FF;

    private GenerateGlowstoneArrowTextures() {}

    public static void main(String[] args) throws IOException {
        Path root = args.length == 0 ? Path.of("src/main/resources/assets/moretnt/textures") : Path.of(args[0]);
        Path item = root.resolve("item");
        Path entity = root.resolve("entity/projectiles");
        Files.createDirectories(item);
        Files.createDirectories(entity);
        write(item.resolve("glowstone_light_arrow.png"), itemSprite());
        write(entity.resolve("glowstone_light_arrow.png"), entitySprite());
        System.out.println("Generated 32x32 Glowstone Light Arrow item and projectile sprites.");
    }

    private static BufferedImage itemSprite() {
        BufferedImage image = blank();
        // The diagonal silhouette follows the vanilla inventory arrow, with warm glowstone accents.
        line(image, 5, 26, 25, 6, OUTLINE, 4);
        line(image, 6, 25, 25, 6, SHAFT_DARK, 2);
        line(image, 7, 24, 24, 7, SHAFT, 1);
        line(image, 22, 5, 27, 5, OUTLINE, 1);
        line(image, 24, 4, 27, 7, OUTLINE, 1);
        line(image, 24, 5, 26, 7, GOLD_DARK, 1);
        pixel(image, 25, 5, GLOW);
        pixel(image, 26, 6, GOLD);
        line(image, 3, 24, 8, 29, OUTLINE, 2);
        line(image, 4, 24, 8, 28, FEATHER_DARK, 1);
        line(image, 5, 25, 7, 27, FEATHER, 1);
        line(image, 7, 27, 10, 30, FEATHER_DARK, 2);
        line(image, 8, 27, 10, 29, FEATHER, 1);
        return image;
    }

    private static BufferedImage entitySprite() {
        BufferedImage image = blank();
        // Uses the same horizontal layout as minecraft:arrow.png so vanilla arrow geometry fits.
        rect(image, 1, 14, 22, 17, OUTLINE);
        rect(image, 3, 15, 22, 16, SHAFT_DARK);
        rect(image, 4, 15, 21, 15, SHAFT);
        rect(image, 22, 13, 28, 18, OUTLINE);
        rect(image, 23, 14, 27, 17, GOLD_DARK);
        rect(image, 24, 14, 26, 16, GOLD);
        pixel(image, 25, 15, GLOW);
        rect(image, 0, 10, 7, 13, OUTLINE);
        rect(image, 1, 11, 7, 12, FEATHER_DARK);
        rect(image, 2, 11, 6, 11, FEATHER);
        rect(image, 0, 18, 7, 21, OUTLINE);
        rect(image, 1, 19, 7, 20, FEATHER_DARK);
        rect(image, 2, 20, 6, 20, FEATHER);
        return image;
    }

    private static BufferedImage blank() {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) image.setRGB(x, y, CLEAR);
        return image;
    }

    private static void write(Path path, BufferedImage image) throws IOException {
        ImageIO.write(image, "PNG", path.toFile());
    }

    private static void line(BufferedImage image, int x0, int y0, int x1, int y1, int color, int width) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int error = dx + dy;
        while (true) {
            for (int y = -width / 2; y <= width / 2; y++) for (int x = -width / 2; x <= width / 2; x++) pixel(image, x0 + x, y0 + y, color);
            if (x0 == x1 && y0 == y1) return;
            int twice = 2 * error;
            if (twice >= dy) { error += dy; x0 += sx; }
            if (twice <= dx) { error += dx; y0 += sy; }
        }
    }

    private static void rect(BufferedImage image, int x0, int y0, int x1, int y1, int color) {
        for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) pixel(image, x, y, color);
    }

    private static void pixel(BufferedImage image, int x, int y, int color) {
        if (x >= 0 && y >= 0 && x < SIZE && y < SIZE) image.setRGB(x, y, color);
    }
}

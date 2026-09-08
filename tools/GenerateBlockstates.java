import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Regenerates physical-TNT blockstates, per-variant block models, and per-variant item models.
 * The item definition is intentionally pointed at each variant's own block model, not the old
 * generic utility_tnt model, which makes inventory/JEI thumbnails match the placed material.
 */
public final class GenerateBlockstates {
    private static final String[] IDS = {
        "lumber_tnt", "soil_tnt", "stone_tnt", "ore_miner_tnt", "ore_safe_tnt", "crop_tnt",
        "glass_tnt", "wool_tnt", "concrete_tnt", "ice_tnt", "nether_tnt", "endstone_tnt",
        "wood_tnt", "water_tnt", "lava_tnt", "frost_tnt", "fire_tnt", "glow_tnt", "sponge_tnt",
        "tunnel_tnt", "shaft_tnt", "quarry_tnt", "surface_tnt", "trench_tnt", "demolition_tnt",
        "mega_tnt", "colossal_tnt", "nuclear_tnt", "super_nuclear_tnt", "obsidian_tnt", "bedrock_tnt", "void_tnt"
    };

    private GenerateBlockstates() {}

    public static void main(String[] args) throws IOException {
        Path blockstateDirectory = args.length == 0
            ? Path.of("src/main/resources/assets/moretnt/blockstates")
            : Path.of(args[0]);
        Path assetDirectory = blockstateDirectory.getParent();
        Path modelDirectory = assetDirectory.resolve("models").resolve("block");
        Path itemDirectory = assetDirectory.resolve("items");
        Files.createDirectories(blockstateDirectory);
        Files.createDirectories(modelDirectory);
        Files.createDirectories(itemDirectory);

        for (String id : IDS) {
            Files.writeString(blockstateDirectory.resolve(id + ".json"), blockstate(id));
            Files.writeString(modelDirectory.resolve(id + ".json"), blockModel(id, false));
            Files.writeString(modelDirectory.resolve(id + "_lit.json"), blockModel(id, true));
            Files.writeString(itemDirectory.resolve(id + ".json"), itemModel(id));
        }
        System.out.println("Regenerated " + IDS.length + " More TNT blockstates, models, and item thumbnails.");
    }

    private static String blockstate(String id) {
        return """
            {
              "variants": {
                "unstable=false": { "model": "moretnt:block/%s" },
                "unstable=true": { "model": "moretnt:block/%s" }
              }
            }
            """.formatted(id, id);
    }

    private static String blockModel(String id, boolean lit) {
        String suffix = lit ? "_lit" : "";
        return """
            {
              "parent": "minecraft:block/cube_bottom_top",
              "textures": {
                "bottom": "moretnt:block/%s_bottom",
                "side": "moretnt:block/%s_side%s",
                "top": "moretnt:block/%s_top%s"
              }
            }
            """.formatted(id, id, suffix, id, suffix);
    }

    private static String itemModel(String id) {
        return """
            {
              "model": {
                "type": "minecraft:model",
                "model": "moretnt:block/%s"
              }
            }
            """.formatted(id);
    }
}

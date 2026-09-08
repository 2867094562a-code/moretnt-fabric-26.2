import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/** Regenerates More TNT blockstates for the physical primed-entity implementation. */
public final class GenerateBlockstates {
    private GenerateBlockstates() {}

    public static void main(String[] args) throws IOException {
        Path directory = args.length == 0
            ? Path.of("src/main/resources/assets/moretnt/blockstates")
            : Path.of(args[0]);
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json")).forEach(path -> {
                String name = path.getFileName().toString();
                String id = name.substring(0, name.length() - ".json".length());
                String content = """
                    {
                      "variants": {
                        "unstable=false": { "model": "moretnt:block/%s" },
                        "unstable=true": { "model": "moretnt:block/%s" }
                      }
                    }
                    """.formatted(id, id);
                try {
                    Files.writeString(path, content);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
        System.out.println("Regenerated physical-TNT blockstates in " + directory.toAbsolutePath());
    }
}

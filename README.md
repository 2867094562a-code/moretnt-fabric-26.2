# More TNT

[简体中文说明](README_zh_CN.md)

More TNT is a Fabric mod for Minecraft 26.2 that adds 32 purpose-built TNT variants. It is designed for practical work: harvesting a forest, digging a tunnel, protecting ores during demolition, draining water, or making a very large crater when that is actually what you need.

## Requirements and installation

- Minecraft `26.2`
- Fabric Loader `0.19.3` or newer
- Fabric API `0.157.0+26.2` or compatible newer build
- Java `25`

Place `moretnt-1.3.1+26.2.jar` in the instance's `mods` directory beside Fabric API, then start the game. In Creative mode, all variants and the Glowstone Light Arrow appear under the **More TNT** tab. In Survival, every variant is crafted in a normal crafting grid.

## How it works

- Ignite any variant with flint and steel, a fire charge, or a redstone signal.
- Every variant has original 64×64 side, top, and bottom textures. The side has a high-contrast purpose code (for example `STN`, `ORE`, `H2O`, `NUC`, or `SNU`) and the top has a matching pictogram. Each item model uses its own block model, so Creative/JEI thumbnails match the placed material.
- Ignition replaces the block with a real primed-TNT entity: a four-second (80-tick) fuse with vanilla gravity, bouncing, smoke, swelling, and flash animation.
- Vanilla TNT ignition routes are supported: redstone power, flint and steel, fire charges, dispenser use of those ignition items, burning projectiles, nearby fire, nearby lava, and explosion chain reactions. More TNT and vanilla TNT within a utility effect radius are also physically primed with a randomized short fuse, so selective/environment effects do not break chain reactions. Command-placed `unstable=true` variants use the vanilla unstable-break trigger.
- **Glow TNT** is the exception to ordinary hand placement: right-click throws a physical, 30-tick primed TNT charge. It follows normal TNT gravity and collision, then places exactly one glowstone at the detonation point. A command-placed Glow TNT block can still use every ordinary ignition route above.
- **Glowstone Light Arrows** fly as normal arrows from bows, crossbows, and dispensers. Their first block impact places one glowstone on the struck face and consumes the arrow.
- Selective TNT protects blocks with block entities, such as chests and machines. The ore-protection variants also recognize most modded ores whose registry path follows common patterns such as `*_ore`.
- The `doTNTExplodes` gamerule is respected. When it is disabled, More TNT cannot arm or chain-react.
- Targeted mining TNT drops normally harvested blocks. **Void TNT** deliberately destroys targets without drops. Bedrock TNT removes bedrock without dropping bedrock.

## Crafting table

Every recipe is shapeless: combine one vanilla TNT with the listed material in any crafting grid.

| Variant | Additional ingredient | Result / intended job |
|---|---|---|
| Lumber TNT | Diamond Axe | Removes logs, wood, bamboo, and leaves only. |
| Earthworks TNT | Diamond Shovel | Removes dirt, sand, gravel, clay, and mud only. |
| Stone TNT | Diamond Pickaxe | Removes common stone, deepslate, tuff, calcite, and similar rock. |
| Ore Miner TNT | Emerald | Mines ores only, including most modded ores. |
| Ore-Safe TNT | Golden Apple | Demolishes non-ore blocks while preserving ores. |
| Harvest TNT | Diamond Hoe | Harvests crops, flowers, vines, sugar cane, and cactus. |
| Glass TNT | Glass | Removes glass blocks and panes only. |
| Wool TNT | Shears | Removes wool, carpets, and beds only. |
| Concrete TNT | Concrete | Removes concrete, concrete powder, and terracotta only. |
| Icebreaker TNT | Packed Ice | Removes ice and snow only. |
| Nether Mining TNT | Netherrack | Removes netherrack, basalt, blackstone, and soul soil. |
| Endstone TNT | End Stone | Removes end stone and purpur blocks. |
| Woodwork TNT | Oak Log | Removes wooden building blocks without touching ores. |
| Water TNT | Water Bucket | Fills nearby air spaces with water sources. |
| Lava TNT | Lava Bucket | Fills nearby air spaces with lava. |
| Frost TNT | Blue Ice | Converts nearby water sources to ice. |
| Fire TNT | Fire Charge | Ignites safe nearby air spaces. |
| Glow TNT | Glowstone | Right-click throws a physical charge; its detonation places exactly one glowstone. |
| Glowstone Light Arrow | One Glow TNT + one Arrow | Shapeless recipe yields **10** arrows; a block impact places one glowstone. |
| Sponge TNT | Sponge | Drains nearby water and lava. |
| Tunnel TNT | Rail | Excavates all breakable, non-container terrain into a 3 × 3 north-south tunnel. |
| Shaft TNT | Scaffolding | Excavates all breakable, non-container terrain into a 3 × 3 vertical shaft. |
| Quarry TNT | Diamond | Mines stone and ores while preserving containers. |
| Leveling TNT | Grass Block | Detects every column's real surface and clears its topsoil. |
| Trench TNT | Stone Bricks | Digs a broad east-west trench. |
| Demolition TNT | Obsidian | General demolition that preserves ores and containers. |
| Mega TNT | Nether Star | A conventional explosion with radius 16. |
| Colossal TNT | Dragon Egg | A conventional explosion with radius 48. |
| Nuclear Charge | Four Netherite Blocks, Nether Star, two End Crystals, Colossal Charge, and Respawn Anchor | A radius-64 explosion. Uses a 3 × 3 shaped recipe. |
| Super-Nuclear Charge | Five Nuclear Charges, Nether Star, two Beacons, and Dragon Egg | A radius-96 explosion. Uses a 3 × 3 shaped recipe. |
| Obsidian TNT | Crying Obsidian | Breaks obsidian, crying obsidian, and respawn anchors. |
| Bedrock TNT | Netherite Ingot | Removes bedrock in radius 4; bedrock never drops. |
| Void TNT | Ender Pearl | Clears blocks without drops while protecting ores and containers. |

## Safety notes

1. Back up your world before using Mega TNT, Colossal TNT, Nuclear Charge, Super-Nuclear Charge, or Bedrock TNT. Their terrain changes are permanent. The two nuclear variants can also cause severe lag while their large blast area is processed.
2. Test a variant in a copy of the world first. A selective TNT evaluates block tags and registry names, so heavily modded blocks can behave differently from vanilla blocks.
3. Water, lava, fire, glowstone, frost, and sponge variants alter the environment instead of performing ordinary block explosions. Glow TNT is a thrown charge, and each Glowstone Light Arrow leaves one permanent glowstone block when it strikes a block. Keep these effects away from farms, redstone, and wooden builds unless that is intentional.
4. Tunnel TNT runs north-south and Trench TNT runs east-west; use them in the intended orientation.
5. Container protection applies to block entities. Do not treat it as a substitute for a backup when using the high-power variants.

## Visual asset source

The `docs/texture-concept-reference.png` sheet was generated as a visual direction reference. The game textures themselves are original, code-generated 64×64 PNG pixel art under `src/main/resources/assets/moretnt/textures/block/`, plus original 32×32 vanilla-style Glowstone Light Arrow item/entity sprites. The reproducible generators are `tools/GenerateTntTextures.java` and `tools/GenerateGlowstoneArrowTextures.java`.

## Development

The project uses Gradle, Fabric Loom, and Java 25. Build a distributable jar with:

```powershell
./gradlew build
```

The remapped production jar is produced under `build/libs/`.

## License

This project is released under the [MIT License](LICENSE).

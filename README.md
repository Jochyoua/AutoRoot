![AutoRoot Banner](https://jochyoua.github.io/assets/resources/AutoRoot.png)
# AutoRoot 🌱

[![GitHub](https://img.shields.io/github/license/Jochyoua/AutoRoot?style=plastic)](https://github.com/Jochyoua/AutoRoot/blob/main/LICENSE) [![GitHub last commit](https://img.shields.io/github/last-commit/Jochyoua/AutoRoot?style=plastic)](https://github.com/Jochyoua/AutoRoot/commits/) [![Github Release](https://img.shields.io/github/v/release/Jochyoua/AutoRoot?style=plastic)](https://github.com/Jochyoua/AutoRoot/releases/latest)

[![Showcase](https://img.shields.io/badge/Watch_Showcase-FF4D4D?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/watch?v=LqcHIz8dpXg)

> ### Tired of your forest depleting over time? Worried about global warming? LOOK NO FURTHER
> AutoRoot is a SpigotMC / PaperMC plugin that creates the solution for deforestation in your Minecraft server.  \
> It can also automatically plant dropped crops, saplings, and flowers for you too. :)

---

## Plugin Features & Compatibility

- [x] **Realistic Seed Drifting** - Seeds catch the breeze and blow sideways before landing, rather than just dropping
  straight down.
- [x] **Smart Claim Protection** - Respects **WorldGuard**, **Towny**, and **GriefPrevention**. \
  AutoRoot checks if the closest player has build rights, ensuring trees *never* grow inside protected areas!
- [x] **CoreProtect Tracking** - Every single sapling and crop planted by the plugin gets logged directly into *
  *CoreProtect** history under the `AutoRoot` name.
- [x] **Vegetation Bypass** - Seeds fall right through tall grass, ferns, and flowers to reach the actual soil
  underneath.
- [x] **Custom Items & Biomes** - Zero hardcoded plants. You can customize overworld trees, nether fungi, and farmland
  crops!
- [x] **Custom Commands** - Want to customize it even further? You can also execute commands whenever an item is planted!

---

## The Environmental Engine

> **AutoRoot is a small environmental engine that helps your world rebuild itself.**
>
> It constantly monitors tree canopies for leaves that can drop seeds, and lets those seeds drift naturally into the
> world, and securely plants them.
>
> * **Forests refill naturally** without manual replanting fatigue or staff intervention.
> * **Dropped crops plant in soil** to help keep rural farming regions automated and visually stunning.
> * **Natural spaces stay alive** and vibrant, remaining populated for generations.
>
> AutoRoot is a quiet, optimized background infrastructure that makes your server feel alive.

## Getting Started

1. **Download** the latest release from the [releases section](https://github.com/Jochyoua/AutoRoot/releases).
2. **Install** by dropping the downloaded `.jar` file into your server's `plugins` folder.
3. **Restart** your server to generate the default configuration.
4. **Customize** your config, then run `/autoroot reload` (Permission: `autoroot.reload`) to apply changes instantly.

> [!CAUTION]
> If you set `require_nearest_player_can_build: false` in the config, a warning message will appear in your server
> console reminding you that seeds will bypass standard land claims.
>
---

## Configuration Setup

AutoRoot is incredibly easy to configure. You can adjust drop chances, wind strengths, and allowed dirt types for every
single plant on your server.

## 🌿 Test Server — server.J5568.dev

A lightweight environment for plugin testing and world‑growth simulation.  
Players are randomly teleported on every join to ensure fresh terrain and unbiased testing conditions.


> [!IMPORTANT]  
> Click the dropdown below to see the complete, fully-commented default configuration file!

<details>
<summary><b>📄 Click to expand the FULL config.yml</b></summary>

```yaml
planting:
  delay_ticks: 40                      # Ticks a dropped item must sit on the ground before trying to plant.
  scan_interval_ticks: 20              # How frequently (in ticks) the plugin checks items on the ground.
  enable_event_listeners: false        # Use event listeners instead of a timer to track items (Can be heavier on performance).
  debug: false                         # Prints detailed planting actions to the console for troubleshooting.
  enable_particles: true               # Spawns a small green particle effect when a plant successfully roots.
  enable_sound: true                   # Plays a soft planting sound effect when a plant roots.
  require_nearest_player_can_build: true # Checks land claims (WorldGuard, Towny, etc.) to prevent griefing.
  enable_coreprotect_logging: true     # Enable coreprotect logging

falling_seeds:
  enabled: true                        # Should trees naturally dropping seeds from their leaves?
  max_leaf_scan_height: 128            # The maximum Y-level the plugin will scan for natural tree canopies.
  interval_ticks: 120                  # How frequently (in ticks) the plugin checks leaves for drops.
  natural_seed_fall_weight: 15         # The base weight that a scanned leaf will drop a seed.
  max_seed_fall_weight: 1000           # The maximum denominator for the drop calculation (Chance is 15 out of 1000).
  max_seeds_per_cycle: 32              # Hard limit on how many seeds can drop globally in a single scan cycle.
  max_chunks_per_cycle_per_player: 10  # Maximum number of chunks to scan around each online player per cycle.
  chunk_leaf_density_limit: 100        # If reached, falling seeds will stop generating in chunks that have more than 100 leaves.
  chunk_sapling_density_limit: 8        # If reached, saplings will not be planted if there are 8 saplings in a chunk.
  consume_leaf_on_seed_spawn: true     # If true, the leaf block is broken/consumed when it drops a seed.
  minimum_canopy: 2                    # Prevents trees from going completely bald by leaving a tiny bit of foliage intact.

  leaf_cache:
    dynamic_scaling_enabled: true      # Automatically adjusts memory cache lifespan based on server player count.
    base_lifespan_ms: 60000            # Standard time (in milliseconds) a scanned leaf stays in memory.
    min_lifespan_ms: 10000             # Minimum time a leaf stays in memory during heavy server load.
    max_lifespan_ms: 60000             # Maximum time a leaf stays in memory during light server load.

  wind:
    direction_x: 0.1                   # The velocity applied to falling seeds along the X axis.
    direction_z: -0.05                 # The velocity applied to falling seeds along the Z axis.
    strength: 1.0                      # Total multiplier for the wind's power.

ignore_vegetation:
  enabled: true                        # Allows falling seeds to bypass ground cover instead of getting stuck on top of it.
  list:                                # List of block tags or materials that seeds will pass straight through.
    - flowers
    - small_flowers
    - short_grass
    - tall_grass
    - fern
    - large_fern
    - dead_bush
    - sweet_berry_bush
    - red_mushroom
    - brown_mushroom

defaults:
  plant_chance: 0.75                  # Global default chance (75%) for a dropped item to successfully root.
  valid_blocks: [DIRT, GRASS_BLOCK, PODZOL, COARSE_DIRT, MUD] # Global default soil types.
  biome_whitelist: []                 # Global default biome restrictions (empty = allowed in all biomes).
  enable_falling_seeds: false         # Global default for whether plants can drop from leaves (Only works for TREES).
  destroy_item_on_failure: false      # We don't want to destroy their seeds, do we?

plantables:

  # --- CUSTOM COMMAND / GAMBLE EXAMPLE --- > COMMANDS SUPPORT PAPI PLACEHOLDERS
  # %x% - X Cords; %y% - Y Cords; %z% - Z Cords; and %world% - World name. 
  # [asPlayer] : Will cause the command to be ran as the player
  # [randomEntry] : Will cause the command to become a list of commands that are chosen randomly, split by |.
  AIR:                               # Triggers when item lands in open air
    items: [AMETHYST_SHARD]          # Dropping an amythyst shard triggers this rule
    plant_chance: 0.20               # 20% success chance
    valid_blocks: [ OBSIDIAN ]       # Require the item to be on obsidian
    commands:
      - 'minecraft:tellraw %nearest_player_name% "&e[AutoRoot] &7I hope the trade-off was worth the exchange!"'
      - '[randomEntry] minecraft:give %nearest_player_name% emerald 1 | minecraft:give %nearest_player_name% gold_ingot 1'
    destroy_item_on_failure: true    # Consume the item on failure!
    
  # --- TREES ---

  OAK_SAPLING:
    items: [OAK_SAPLING, APPLE]       # Dropping an oak sapling or apple triggers an oak tree.
    valid_blocks: []                  # Inherits the default 'valid_blocks' list from above.
    plant_chance: 0.90                # 90% chance to successfully take root.
    biome_whitelist: []               # Can grow in any biome.
    enable_falling_seeds: true        # Oak leaves will naturally drop these items.

  BIRCH_SAPLING:
    items: [BIRCH_SAPLING]            # Dropping a birch sapling triggers a birch tree.
    valid_blocks: []                  # Inherits defaults.
    plant_chance: 0.85                # 85% chance to plant.
    enable_falling_seeds: true        # Birch leaves will drop seeds naturally.

  SPRUCE_SAPLING:
    items: [SPRUCE_SAPLING]           # Dropping a spruce sapling triggers a spruce tree.
    valid_blocks: []                  # Inherits defaults.
    plant_chance: 0.85                # 85% chance to plant.
    enable_falling_seeds: true        # Spruce leaves will drop seeds naturally.

  JUNGLE_SAPLING:
    items: [JUNGLE_SAPLING, COCOA_BEANS] # Dropping a jungle sapling or cocoa beans triggers a jungle tree.
    valid_blocks: []                  # Inherits defaults.
    plant_chance: 0.80                # 80% chance to plant.
    enable_falling_seeds: true        # Jungle leaves will drop seeds naturally.

  ACACIA_SAPLING:
    items: [ACACIA_SAPLING]           # Dropping an acacia sapling triggers an acacia tree.
    valid_blocks: []                  # Inherits defaults.
    plant_chance: 0.80                # 80% chance to plant.
    enable_falling_seeds: true        # Acacia leaves will drop seeds naturally.

  DARK_OAK_SAPLING:
    items: [DARK_OAK_SAPLING]         # Dropping a dark oak sapling triggers a dark oak tree.
    valid_blocks: []                  # Inherits defaults.
    plant_chance: 0.80                # 80% chance to plant.
    enable_falling_seeds: true        # Dark oak leaves will drop seeds naturally.

  MANGROVE_PROPAGULE:
    items: [MANGROVE_PROPAGULE]       # Dropping a propagule triggers a mangrove tree.
    valid_blocks: []                  # Inherits defaults.
    plant_chance: 0.75                # 75% chance to plant.
    enable_falling_seeds: true        # Mangrove leaves will drop seeds naturally.

  CHERRY_SAPLING:
    items: [CHERRY_SAPLING]           # Dropping a cherry sapling triggers a cherry tree.
    valid_blocks: []                  # Inherits defaults.
    plant_chance: 0.85                # 85% chance to plant.
    enable_falling_seeds: true        # Cherry leaves will drop seeds naturally.

  PALE_OAK_SAPLING:
    items: [PALE_OAK_SAPLING]         # Dropping a pale oak sapling triggers a pale oak tree.
    valid_blocks: []                  # Inherits defaults.
    biome_whitelist: [PALE_GARDEN]    # Restricted to ONLY grow in the Pale Garden biome!
    plant_chance: 0.85                # 85% chance to plant.
    enable_falling_seeds: true        # Pale oak leaves will drop seeds naturally.


  # --- OVERWORLD CROPS ---

  WHEAT:
    items: [WHEAT_SEEDS]              # Wheat seeds will plant wheat crops.
    valid_blocks: [FARMLAND]          # Must land specifically on Farmland to root.
    plant_chance: 0.80                # 80% chance to plant.

  CARROTS:
    items: [CARROT]                   # Dropped carrots will plant carrot crops.
    valid_blocks: [FARMLAND]          # Must land on Farmland.
    plant_chance: 0.80                # 80% chance to plant.

  POTATOES:
    items: [POTATO]                   # Dropped potatoes will plant potato crops.
    valid_blocks: [FARMLAND]          # Must land on Farmland.
    plant_chance: 0.80                # 80% chance to plant.

  BEETROOTS:
    items: [BEETROOT_SEEDS]           # Beetroot seeds will plant beetroot crops.
    valid_blocks: [FARMLAND]          # Must land on Farmland.
    plant_chance: 0.80                # 80% chance to plant.

  MELON_STEM:
    items: [MELON_SEEDS]              # Melon seeds will plant a melon stem.
    valid_blocks: [FARMLAND]          # Must land on Farmland.
    plant_chance: 0.75                # 75% chance to plant.

  PUMPKIN_STEM:
    items: [PUMPKIN_SEEDS]            # Pumpkin seeds will plant a pumpkin stem.
    valid_blocks: [FARMLAND]          # Must land on Farmland.
    plant_chance: 0.75                # 75% chance to plant.


  # --- NETHER CROPS ---

  NETHER_WART:
    items: [NETHER_WART]              # Dropped nether wart will replant itself.
    valid_blocks: [SOUL_SAND]         # Restricted strictly to Soul Sand.
    plant_chance: 0.65                # 65% chance to plant.

  CRIMSON_FUNGUS:
    items: [CRIMSON_FUNGUS]           # Dropped crimson fungus will replant itself.
    valid_blocks: [NETHERRACK, CRIMSON_NYLIUM] # Can grow on netherrack or nylium.
    plant_chance: 0.70                # 70% chance to plant.

  WARPED_FUNGUS:
    items: [WARPED_FUNGUS]            # Dropped warped fungus will replant itself.
    valid_blocks: [NETHERRACK, WARPED_NYLIUM] # Can grow on netherrack or nylium.
    plant_chance: 0.70                # 70% chance to plant.

  CRIMSON_ROOTS:
    items: [CRIMSON_ROOTS]            # Dropped crimson roots will plant themselves.
    valid_blocks: [CRIMSON_NYLIUM]    # Strictly requires crimson nylium.
    plant_chance: 0.65                # 65% chance to plant.

  WARPED_ROOTS:
    items: [WARPED_ROOTS]             # Dropped warped roots will plant themselves.
    valid_blocks: [WARPED_NYLIUM]     # Strictly requires warped nylium.
    plant_chance: 0.65                # 65% chance to plant.


  # --- SAND PLANTS ---

  SUGAR_CANE:
    items: [SUGAR_CANE]               # Dropped sugar cane will replant itself.
    valid_blocks: [SAND, RED_SAND, DIRT, GRASS_BLOCK, PODZOL] # Expanded list of valid soils.
    plant_chance: 0.70                # 70% chance to plant (Vanilla water requirements apply).

  CACTUS:
    items: [CACTUS]                   # Dropped cactus blocks will replant themselves.
    valid_blocks: [SAND, RED_SAND]    # Can only grow on sand.
    plant_chance: 0.70                # 70% chance to plant.


  # --- BAMBOO & BERRIES ---

  BAMBOO:
    items: [BAMBOO]                   # Dropped bamboo will replant itself.
    valid_blocks: [DIRT, GRASS_BLOCK, PODZOL, SAND] # Allowed soil types.
    plant_chance: 0.75                # 75% chance to plant.

  SWEET_BERRY_BUSH:
    items: [SWEET_BERRIES]            # Dropped sweet berries will grow a bush.
    valid_blocks: [DIRT, GRASS_BLOCK, PODZOL] # Allowed soil types.
    plant_chance: 0.70                # 70% chance to plant.


  # --- FLOWERS ---

  DANDELION:
    items: [DANDELION]                # Dropped dandelions will plant themselves.
    valid_blocks: [DIRT, GRASS_BLOCK] # Restricted to basic dirt/grass.
    plant_chance: 0.60                # 60% chance to plant.

  POPPY:
    items: [POPPY]                    # Dropped poppies will plant themselves.
    valid_blocks: [DIRT, GRASS_BLOCK] # Restricted to basic dirt/grass.
    plant_chance: 0.60                # 60% chance to plant.

  BLUE_ORCHID:
    items: [BLUE_ORCHID]              # Dropped blue orchids will plant themselves.
    valid_blocks: [GRASS_BLOCK]       # Strict requirement for grass blocks only.
    plant_chance: 0.55                # 55% chance to plant.

  LILAC:
    items: [LILAC]                    # Dropped lilacs will plant a 2-tall flower.
    valid_blocks: [DIRT, GRASS_BLOCK] # Restricted to basic dirt/grass.
    plant_chance: 0.55                # 55% chance to plant.

  ROSE_BUSH:
    items: [ROSE_BUSH]                # Dropped rose bushes will plant themselves.
    valid_blocks: [DIRT, GRASS_BLOCK] # Restricted to basic dirt/grass.
    plant_chance: 0.55                # 55% chance to plant.

  PEONY:
    items: [PEONY]                    # Dropped peonies will plant themselves.
    valid_blocks: [DIRT, GRASS_BLOCK] # Restricted to basic dirt/grass.
    plant_chance: 0.55                # 55% chance to plant.
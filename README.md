# JointBlock Remastered

Forge 1.20.1 port of **JointBlock 0.8.4** (Minecraft 1.12.2), a block-assembly / rideable-machine mod.

> ⚠️ **Please read [NOTICE.md](NOTICE.md) first** — this is an *unofficial* preservation port.
> The original mod is the work of **ARUBE (oANCIENTREDo)** and remains their property.

## Status

The main mod is ported: it builds, the dedicated server and the client start, and the ported
crafting/smelting recipes (68 datapack entries, including the 1.12.2 damage-value conversions and the
NBT-copying recipes) are verified against the real `RecipeManager`. Not ported yet: the **JBRobotModel
addon** (its assets are modernized, its code is not) and datagen (the JSON is hand-written).
See [MIGRATION_PLAN.md](MIGRATION_PLAN.md) for the phase plan and
[referance/docs/MIGRATION_ASSESSMENT.md](referance/docs/MIGRATION_ASSESSMENT.md) for the
structural analysis of the original mod.

## Building

- JDK 17 required.
- `gradlew build` — output jar in `build/libs/`.
- `gradlew runClient` — dev client.

## Repository layout

| Path | Content |
|---|---|
| `src/main/java/jp/mc/ancientred/jointblock/` | Ported mod code (package names kept from the original) |
| `src/main/resources/assets/jointblock/` | Modernized assets (namespace `jointblock`) |
| `src/main/resources/data/jointblock/recipes/` | Ported crafting/smelting recipes (datapack JSON) |
| `src/main/resources/assets/jbrobot/` | JBRobotModel addon assets (ported in Phase 5) |
| `referance/` | **Git-ignored, local only.** Original jar, decompiled sources, analysis docs |
| `debug/` | **Git-ignored, local only.** Throwaway porting scratch: build/client/server logs, javac/javap scaffolding, jar caches, probes, extracted SRG sources |

`referance/` is intentionally excluded from version control: it contains the original
mod jar and decompiled sources, which are the original author's copyrighted material
and are kept locally solely as a porting reference.

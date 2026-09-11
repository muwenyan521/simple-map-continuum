# Simple Map Continuum

Simple Map Continuum is an independent multiloader and multiversion rewrite of
[Simple Map](https://github.com/muwenyan521/Simple-Map).

The rewrite targets a reusable map core with separate Fabric, Forge, and
NeoForge adapters for selected Minecraft 1.20+ version families. The project
is not affiliated with or endorsed by the original upstream author.

## Status

The rewrite is implemented as a JDK-only domain core with explicit loader
adapters. Runtime integration is kept at the platform boundary so the same
state, projection, persistence, protocol, and scheduling code is reusable
across loader targets.

## Design direction

- JDK-only map domain and algorithm core.
- Project-owned ports for world access, scheduling, persistence, networking,
  and rendering.
- Separate loader and Minecraft-version adapters.
- No Architectury API runtime dependency.
- Versioned migration readers for compatible data from the original project.

## Modules

| Module | Responsibility | Java baseline |
| --- | --- | --- |
| `core` | map state, surface/cave sampling, minimap, rendering plans, texture budgeting, waypoints, Map Book, protocol and archives | 17 |
| `platform-api` | world, render, scheduler, config, persistence and network ports plus runtime composition | 17 |
| `fabric-1.20.1` | Fabric 1.20.1 client entrypoint and adapter descriptor | 17 |
| `fabric-1.21.1` | Fabric 1.21.1 client entrypoint and adapter descriptor | 21 |
| `forge-1.20.1` | Forge 1.20.1 mod entrypoint and adapter descriptor | 17 |
| `neoforge-1.21.1` | NeoForge 1.21.1 mod entrypoint and adapter descriptor | 21 |

The loader modules intentionally contain only the narrow integration layer;
Minecraft and loader types never cross into `core` or `platform-api`.

## Verification

Run the complete non-runtime gate from the repository root:

```bash
./gradlew clean matrixCheck dependencyIsolationCheck --no-daemon
```

This compiles every declared target, executes the core and platform tests, and
runs each target self-check. Actual game startup and visual checks are kept
outside this gate.

The archive writer creates a sibling `.bak` before replacing an existing file.
Do not commit local `.omo/` research material or generated reports; they are
ignored by design and remain local recovery data.

## License

This project is released under the MIT License. See [LICENSE](LICENSE).

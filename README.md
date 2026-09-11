# Simple Map Continuum

Simple Map Continuum is an independent multiloader and multiversion rewrite of
[Simple Map](https://github.com/muwenyan521/Simple-Map).

The rewrite targets a reusable map core with separate Fabric, Forge, and
NeoForge adapters for selected Minecraft 1.20+ version families. The project
is not affiliated with or endorsed by the original upstream author.

## Status

The repository is being initialized. Architecture and compatibility decisions
are being validated before runtime implementation begins.

## Design direction

- JDK-only map domain and algorithm core.
- Project-owned ports for world access, scheduling, persistence, networking,
  and rendering.
- Separate loader and Minecraft-version adapters.
- No Architectury API runtime dependency.
- Versioned migration readers for compatible data from the original project.

## License

This project is released under the MIT License. See [LICENSE](LICENSE).

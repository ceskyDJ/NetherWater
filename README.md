# NetherWater
Simple Spigot plugin that allows players to use water buckets and ice blocks in nether worlds.

## Commands
- /nwreload or /nwr - reload plugin configuration

## Permissions
- netherwater.use.WORLD - using plugin features in some world
- netherwater.use.* - using plugin features in all worlds
- netherwater.world.bypass - turn on plugin features in disabled worlds for same group of players
- netherwater.reload - use /nwreload (or /nwr) command
- netherwater.* - every permissions of this plugin

## Default configuration
```YAML
# Allow using debug dumps. It's not recommended on production servers.
debug: false
# Worlds where the plugin is disabled
disabled-worlds:
  - nether_without_water
# Max world height (Y coordinate) to use plugin features
max-height: 999
# Min world height (Y coordinate) to use plugin features
min-height: 0
# Messages for translation
# Change only text in quotation marks ("")!
# If you need to use quotation mark in your text, use \" instead
messages:
  permissions: "You do not have permission to do that!"
  config-reload: "Nether Water configuration reloaded!"
```

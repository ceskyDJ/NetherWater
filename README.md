# NetherWater
Simple Spigot plugin that allows players to use water buckets and ice blocks in nether worlds.

This is a fork of [KlutzyBubbles/NetherWater](https://github.com/KlutzyBubbles/NetherWater) repository.

## Commands
- /nwreload or /nwr - reload plugin configuration

## Permissions
- netherwater.use.WORLD - using plugin features in some world
- netherwater.use.* - using plugin features in all worlds
- netherwater.world.bypass - turn on plugin features in disabled worlds for some players
- netherwater.spread.bypass - disable spread limits for some players - **needs to allow this feature in config file!**
- netherwater.reload - use /nwreload (or /nwr) command
- netherwater.* - every permissions of this plugin

## Download and installation
1. Go to the latest release page and download the JAR file https://github.com/ceskyDJ/NetherWater/releases/latest.
2. Put the file NetherWater-1.X.X to your server's plugins/ directory.
3. Restart your server.
4. You are done! Now you can change configuration of the plugin in plugins/NetherWater directory. If there is some problem, you can insert there default configuration from this page or create an issue.

If you don't want to find the JAR, here's the direct link: [NetherWater-1.0.7.jar](https://github.com/ceskyDJ/NetherWater/releases/download/v1.0.7/NetherWater-1.0.7.jar)

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
# Spread limits bypass for OP players or players with netherwater.spread.bypass permission
spread-bypass: false
# Messages for translation
# Change only text in quotation marks ("")!
# If you need to use quotation mark in your text, use \" instead
messages:
  permissions: "You do not have permission to do that!"
  config-reload: "Nether Water configuration reloaded!"
```

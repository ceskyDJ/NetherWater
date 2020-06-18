# NetherWater
Simple [Spigot plugin](https://www.spigotmc.org/resources/nether-water-enable-water-in-nether-worlds.79256/) that allows players to use water buckets and ice blocks in nether worlds.

This is a fork of [KlutzyBubbles/NetherWater](https://github.com/KlutzyBubbles/NetherWater) repository.

## Commands
- **/nwreload** or **/nwr** - reload plugin configuration

## Permissions
- **netherwater.use.WORLD** - using plugin features in some world
- **netherwater.use.*** - using plugin features in all worlds
- **netherwater.world.bypass** - turn on plugin features in disabled worlds for some players
- **netherwater.spread.bypass** - disable spread limits for some players - **needs to allow this feature in config file!**
- **netherwater.scooping.bypass** - allow scooping water into bucket for some players
- **netherwater.reload** - use /nwreload (or /nwr) command
- **netherwater.*** - every permissions of this plugin

## Default configuration
```YAML
#--------------------------------------------------------------------------------------------
#     _   _          _     _                      __        __          _
#    | \ | |   ___  | |_  | |__     ___   _ __    \ \      / /   __ _  | |_    ___   _ __
#    |  \| |  / _ \ | __| | '_ \   / _ \ | '__|    \ \ /\ / /   / _` | | __|  / _ \ | '__|
#    | |\  | |  __/ | |_  | | | | |  __/ | |        \ V  V /   | (_| | | |_  |  __/ | |
#    |_| \_|  \___|  \__| |_| |_|  \___| |_|         \_/\_/     \__,_|  \__|  \___| |_|
#
#--------------------------------------------------------------------------------------------

# Allow using debug dumps. It's not recommended on production servers.
debug: false
# Worlds where the plugin is disabled
disabled-worlds:
  - nether_without_water
# Max world height (Y coordinate) to use plugin features
max-height: 999
# Min world height (Y coordinate) to use plugin features
min-height: 0
# You can disable water spread behaviour by setting this to false
# OP players and others with netherwater.spread.bypass permission still can have normal water spread
# To setup this change spread-bypass to true and eventually add netherwater.spread.bypass permission for target players
spread-enabled: true
# Spread limits bypass for OP players or players with netherwater.spread.bypass permission
spread-bypass: false
# Scoping water into buckets is disabled by default
scooping-into-buckets: false
# Messages for translation
# Change only text in quotation marks ("")!
# If you need to use quotation mark in your text, use \" instead
messages:
  permissions: "You do not have permission to do that!"
  config-reload: "Nether Water configuration reloaded!"
```

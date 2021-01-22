![Nether Water](https://github.com/ceskyDJ/NetherWater/blob/master/other/logo.png "Nether Water logo")

Simple [Spigot plugin](https://www.spigotmc.org/resources/nether-water-enable-water-in-nether-worlds.79256/) that allows players to use water in nether worlds. They can use buckets, ice blocks and dispensers for spawning water.

This is a fork of [KlutzyBubbles/NetherWater](https://github.com/KlutzyBubbles/NetherWater) repository.

## Dependencies
- [WorldEdit](https://enginehub.org/worldedit/) (optional) - if you use WorldEdit, world changes are linked to this
  plugin (creating or deleting water blocks)
- [WorldGuard](https://enginehub.org/worldguard/) (optional) - if you need to integrate its protection system
- [BKCommonLib](https://www.spigotmc.org/resources/bkcommonlib.39590/) (optional) - for modifying mobs' behaviour

This is only NetherWater's direct dependencies! For the right functionality
you need some other plugins (for example WorldEdit for WorldGuard). Read dependency
information on individual plugins' pages, please.

Optional dependencies aren't needed but if you don't provide them,
some NetherWater's functionality will be turned off.

## Commands
- **/nw** or **/nw help** - show command list
- **/nw version** - show current plugin version
- **/nw check** - check for plugin updates

## Permissions
### Using the plugin
- **netherwater.use.WORLD** - using plugin features in some world
- **netherwater.use.*** - using plugin features in all worlds

### Bypasses
- **netherwater.world.bypass** - turn on plugin features in disabled worlds for some players
- **netherwater.spread.bypass** - disable spread limits for some players - **needs to allow this feature in config file!**
- **netherwater.scooping.bypass** - allow scooping water into buckets for some players
- **netherwater.disappearing.bypass** - turn off water disappearing for some players
- **netherwater.bypass** - all bypasses in one permission

### Commands
- **netherwater.command.help** - use /nw help command
- **netherwater.command.version** - use /nw version command
- **netherwater.command.check** - use /nw check command
- **netherwater.command.*** - use all the commands

### Special
- **netherwater.*** - every permission of this plugin except some bypasses
- **netherwater.op** - just like netherwater.* but includes all bypasses

## Statistics
Statistics are provided by [BStats](https://bstats.org/plugin/bukkit/Nether%20Water/8833).
![BStats statistics](https://bstats.org/signatures/bukkit/Nether%20Water.svg "BStats statistics - how many servers and players use the plugin")

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
# Infinite sources of water is disabled by default
infinite-sources: false
# Water automatically disappears after specified number of minutes
# Set this property to 0 if you want to disable this behaviour
# You need to reload or restart server to apply change of this property
water-disappearing: 30
# Damage mobs in water in nether worlds (excluding underwater mobs - https://minecraft.fandom.com/wiki/Category:Underwater_Mobs)
mob-damaging: true
# Damage players in water in nether worlds (only survival mode players without god mode enabled)
player-damaging: true
# How many health point remove while damaging mobs and players
# Mobs and players are hit every 20 ticks (usually ~ 1 second)
# For example: player has 20 health points (10 hearts)
damage-value:
  mob: 1.0
  player: 1.0
# Allow water in nether worlds to be animated with smoke effect (hot watter effect)
hot-water-animation: true
# Messages for translation
# Change only text in quotation marks ("")!
# If you need to use quotation mark in your text, use \" instead
# Use can use colors! Just write & and color code (for ex.: &a for light green)
messages:
  prefix: "&f[&cNether&bWater&f]"
  permissions: "&cYou don't have permission to do that"
  command-permissions: "&cYou don't have permission for this command"
  help.heading: "&l&aHelp page"
  help.underline: "&a=========="
  help.help: "&9%command% help &r- Show this page"
  help.version: "&9%command% version &r- Show current version of the plugin"
  help.check: "&9%command% check &r- Check for updates of the plugin"
  version: "&l&aVersion: &r%version%"
  check.old: "&eUpdater has found a new version %version%!"
  check.latest: "&aYou have the newest version of the plugin."
  check.error: "&cUpdate check hasn't been successful."
```

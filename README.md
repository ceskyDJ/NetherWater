![Nether Water](https://github.com/ceskyDJ/NetherWater/blob/master/other/logo.png "Nether Water logo")

This is a fork of [CodyScheer/NetherWater](https://github.com/CodyScheer/NetherWater) repository.

Nether Water is a [Spigot plugin](https://www.spigotmc.org/resources/nether-water-enable-water-in-nether-worlds.79256/)
allows players to use water in nether worlds. They can use buckets, ice blocks and dispensers for spawning water.
Water added by WorldEdit is fully supported, too. Plugin adds some new properties to water in nether worlds. It provides
hot water animation, water drying do to high temperatures in the nether and damaging mobs and players behaviour. Damaged
are every non-underwater mobs. Nether mobs due to their water intolerance and other high water temperature for other
ones. Players are damaged, too, of course.

All these features can be configured to your needs. You can completely disable special functionality and limitations
set by the plugin. For more information see "Permissions" and "Default configuration" sections, please.


## Support

Do you like my plugin? You can buy me a pizza or start supporting me using membership!

<a href="https://www.buymeacoffee.com/ceskyDJ"><img src="https://img.buymeacoffee.com/button-api/?text=Buy me a pizza&emoji=🍕&slug=ceskyDJ&button_colour=FFDD00&font_colour=000000&font_family=Cookie&outline_colour=000000&coffee_colour=ffffff" /></a>

## Dependencies
- [WorldEdit](https://enginehub.org/worldedit/) (optional) - if you use WorldEdit, world changes are linked to this
  plugin (creating or deleting water blocks)
- [WorldGuard](https://enginehub.org/worldguard/) (optional) - if you need to integrate its protection system
- [BKCommonLib](https://www.spigotmc.org/resources/bkcommonlib.39590/) (optional) - need for damaging mobs feature
- [Multiverse-Core](https://www.spigotmc.org/resources/multiverse-core.64450/) (optional) - worlds unloaded by
  Multiverse-Core is supported by this plugin
- [Vault](https://www.spigotmc.org/resources/vault.34315/) (optional but recommended) - for offline player permissions (for dispensers working in autoloaded chunk), better support for permissions in general

These are only NetherWater's direct dependencies! For the right functionality
you need some other plugins (for example WorldEdit for WorldGuard). Read dependency
information on individual plugins' pages (click on the plugin name to see its website), please.

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
- **netherwater.disappearing.bypass** - turn off water disappearing for some players (INCLUDES cauldron drying)
- **netherwater.drying.bypass** - turn off cauldron drying for some players (NOT water disappearing at all)
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

Never use `debug: true` when you are running plugin on a public and/or production server! Console messages are slow,
so your server will be slowed down, and it could crash in some situations (bigger WorldEdit operation etc.).

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
# To set up this change spread-bypass to true and eventually add netherwater.spread.bypass permission for target players
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
# Similar to `water-disappearing` option but for water stored in cauldrons
# This needs a water disappearing to be enabled (set to non-zero value)
# Warning: Cauldrons dry gradually (like if you grab water with glass bottles)
cauldron-drying: 0
# Damage mobs in water in nether worlds
# Aquatic mobs are excluded (https://minecraft.fandom.com/wiki/Aquatic)
mob-damaging: true
# Damage players in water in nether worlds (only survival mode players without god mode enabled)
player-damaging: true
# How much health point remove while damaging mobs and players
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

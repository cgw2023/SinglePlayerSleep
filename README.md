# SinglePlayerSleep

Due to inactivity on the plugin, I have compiled it for 1.12 and removed mcstats.

New features have now been added, and several improvements to the code. See changelog for details.
Latest download link (for MC 1.12): https://github.com/coldcode69/SinglePlayerSleep/raw/master/SinglePlayerSleep.jar

----

Original authors spigot link: https://www.spigotmc.org/resources/single-player-sleep.20173/

## Description

When a player right clicks on a bed, the plugin broadcasts `<player> is sleeping [CANCEL]`, if no one clicks on Cancel, then after 10 seconds the plugin will advance the time ahead to the next morning. Thus the day and difficulty is not affected. The plugin also checks for storms, and if the player has the permissions, it will clear them. There is also a permission for the Cancel command.

## Permissions

```
sps.hermits - Allows player to sleep when other players are online.

sps.downfall - Allows player to clear downfall.

sps.thunder - Allows player to clear thunderstorm.

sps.cancel - Allows player to cancel SPS of other players.

sps.update - Allows the player to check for updates.
    
sp.command - Allows player to use /sleep command, instead of a bed.

sps.* - Admin, grants all sps permissions.
```

If the player is the only player online, then the bed functions as normal, since the sleep function works before the 10 second delay.

\*/sleep still requires it to be night to use. ie - after 13187

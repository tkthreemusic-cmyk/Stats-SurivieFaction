# 📊 Stats-SurivieFaction

A comprehensive Minecraft 1.21 plugin for tracking and displaying player statistics with ranked scoreboards on your Survival/Faction server.

## 🎮 Features

### Ranked Leaderboards
- **Top 10 Rankings**: Live scoreboards showing the best players in each category
- **Multiple Categories**: Mob kills, PvP kills, deaths, and overall progress
- **Medal System**: Gold/Silver/Bronze medals for top 3 players
- **Real-time Updates**: Scoreboards update automatically every minute

### Statistics Tracked
| Stat | Description |
|------|-------------|
| 🧟 Mobs Killed | All hostile mob kills |
| ⚔️ Player Kills | PvP kill count |
| 💀 Deaths | Total deaths |
| ⛏️ Blocks Broken | Blocks mined |
| 🧱 Blocks Placed | Blocks placed |
| 🔨 Items Crafted | Items crafted |
| 🐄 Animals Bred | Animals bred |

## 📥 Installation

1. Download the latest release JAR from the [Releases page](https://github.com/tkthreemusic-cmyk/Stats-SurivieFaction/releases)
2. Place the JAR in your server's `plugins` folder
3. Restart your server
4. Configure in `plugins/PlayerStats/config.yml`

## Commands

### Player Commands
| Command | Description |
|---------|-------------|
| `/stats` | Open scoreboard menu |
| `/stats kills` | Show top 10 mob killers |
| `/stats playerkills` | Show top 10 PvP killers |
| `/stats deaths` | Show top 10 deaths (most to least) |
| `/stats progress` | Show top 10 progress (blocks, items, etc.) |
| `/stats <player>` | View specific player's stats |
| `/stats toggle` | Toggle/clear scoreboard |
| `/stats reload` | Reload configuration (admin) |

### Admin Commands
| Command | Description |
|---------|-------------|
| `/psta reset <player>` | Reset all stats for a player |
| `/psta reset <player> <stat>` | Reset specific stat |
| `/psta set <player> <stat> <value>` | Set a stat value |
| `/psta add <player> <stat> <value>` | Add to a stat value |

### Stat Types for Admin Commands
- `mobskilled`, `mobs`, `kills`
- `playerskilled`, `players`, `pvp`
- `deaths`
- `blocksbroken`
- `blocksplaced`
- `itemscrafted`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `playerstats.use` | Basic command usage | true |
| `playerstats.seeother` | View other players' stats | true |
| `playerstats.admin` | Admin commands | op |

## Building

```bash
cd PlayerStats
mvn clean package
```

The compiled JAR will be in `target/PlayerStats-1.0.0.jar`

## Configuration

All settings can be modified in `config.yml`:

```yaml
settings:
  auto-save-interval: 5  # minutes
  max-ranked-players: 10

tracking:
  mobs-killed: true
  players-killed: true
  deaths: true
  # ... more options
```

## Scoreboard Display

The plugin displays ranked scoreboards with medals:
- 🥇 **1st Place**: Gold (✦)
- 🥈 **2nd Place**: Silver (✦)
- 🥉 **3rd Place**: Bronze (✦)
- **4th-10th**: Gray (✦)

## API

Other plugins can access player stats:

```java
PlayerStats plugin = PlayerStats.getInstance();
StatsManager manager = plugin.getStatsManager();

// Get player stats
StatsManager.PlayerStatsData stats = manager.getPlayerStats(player.getUniqueId());

// Increment stats
manager.incrementMobsKilled(player.getUniqueId());
```

## Support

For issues or feature requests, please create an issue on the repository.
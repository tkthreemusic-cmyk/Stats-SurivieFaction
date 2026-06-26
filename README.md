# 📊 PlayerStats

Plugin Minecraft 1.21 pour afficher les statistiques des joueurs et classements directement dans le chat.

## 🎮 Fonctionnalités

### Classements dans le Chat
- **Top 10** : Classement des 10 meilleurs joueurs
- **Médaille** : 🥇 Or, 🥈 Argent, 🥉 Bronze pour le top 3
- **Tout dans le chat** : Pas de scoreboard latéral, tout s'affiche dans le chat

### Statistiques Suivies
| Statistique | Description |
|-------------|-------------|
| 🗡️ Mobs tués | Tueurs de mobs hostiles |
| ⚔️ Joueurs tués | Kills PvP |
| 💀 Morts | Total des morts |
| ⛏️ Blocs cassés | Blocs minés |
| 🧱 Blocs posés | Blocs placés |
| 🔨 Objets craftés | Objets fabricués |
| 🐄 Animaux élevés | Animaux reproduits |

## 📥 Installation

1. Télécharger le JAR depuis les [Releases](https://github.com/tkthreemusic-cmyk/Stats-SurivieFaction/releases)
2. Placer le JAR dans le dossier `plugins` du serveur
3. Redémarrer le serveur

## Commandes

| Commande | Description |
|----------|-------------|
| `/stats` | Afficher l'aide |
| `/stats kills` | Classement des kills de mobs |
| `/stats playerkills` | Classement PvP |
| `/stats deaths` | Classement des morts |
| `/stats progress` | Classement de la progression |
| `/stats <joueur>` | Voir les stats d'un joueur |

### Commandes Admin (OP)
| Commande | Description |
|----------|-------------|
| `/stats reload` | Recharger la configuration |
| `/stats reset <joueur>` | Reset les stats d'un joueur |

## Permissions

| Permission | Description | Défaut |
|------------|-------------|--------|
| `playerstats.use` | Commandes de base | true |
| `playerstats.admin` | Commandes admin | op |

## Construction

```bash
cd PlayerStats
mvn clean package
```

Le JAR sera dans `target/PlayerStats-1.1.0.jar`

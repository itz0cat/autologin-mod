# AutoLogin Mod

A client-side Fabric mod for Minecraft 1.21.11 that automatically runs `/login <password>` when prompted by auth plugins (AuthMe, etc.).

## Features

- 🔐 **Auto-login** — Detects login prompts in chat and automatically sends `/login <password>`
- 🌐 **Per-server passwords** — Set different passwords for different servers
- 🔑 **Default password** — Fallback password for servers without a specific one
- ⚙️ **Configurable triggers** — Customize which chat messages trigger the auto-login
- 💬 **Client commands** — Easy in-game commands to manage passwords

## Commands

| Command | Description |
|---|---|
| `/autologin setpass <password>` | Set password for the current server |
| `/autologin default <password>` | Set the default fallback password |
| `/autologin remove` | Remove password for the current server |
| `/autologin status` | Show current configuration status |
| `/autologin toggle` | Enable/disable auto-login |
| `/autologin delay <ms>` | Set delay before sending login (default: 500ms) |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the latest release from the [Releases](../../releases) page
4. Place the `.jar` file in your `.minecraft/mods/` folder

## Configuration

Config is stored at `.minecraft/config/autologin.json` and can be edited manually or via in-game commands.

## Building

Builds are automated via GitHub Actions. Push a tag like `v1.0.0` to trigger a release build.

## License

MIT

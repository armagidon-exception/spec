# Spec

[![Discord](https://discord.com/api/guilds/939962855476846614/widget.png)](https://discord.gg/pEGGF785zp)
[![Maven Central](https://img.shields.io/maven-metadata/v/https/repo1.maven.org/maven2/io/github/revxrsal/spec/maven-metadata.xml.svg?label=maven%20central&colorB=brightgreen)](https://search.maven.org/artifact/io.github.revxrsal/spec)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://github.com/Revxrsal/spec/actions/workflows/gradle.yml/badge.svg)](https://github.com/Revxrsal/spec/actions/workflows/gradle.yml)
[![CodeFactor](https://www.codefactor.io/repository/github/revxrsal/spec/badge)](https://www.codefactor.io/repository/github/revxrsal/spec)

A library for generating beautiful, commented, type-safe YML through interfaces

## 🚀 Features

- **Define your configs with clean interfaces** 🎨  
  Just write an interface — no messy boilerplate!

- **Default values? Easy.** ✍️  
  Simply use default methods to provide fallback values.

- **Built-in comments with `@Comment`** 💬  
  Generate beautiful, documented configuration files automatically.

- **Fully customizable keys** 🗝️  
  Use `@Key` or `@SerializedName` to control config names precisely.

- **Recursive specs support** ♻️  
  Nest specs inside arrays, lists, maps, or other specs — no limits!

- **Powerful setters support** 🛠️  
  Update values programmatically at runtime without effort.

- **Self-saving and self-reloading** 🔥  
  Call `save()` or `reload()` right from your spec — it's automatic!

- **Map access made simple** 🗺️  
  Get live `Map` views of your specs. Changes to the map = changes to the object.

- **Custom `@AsMap` support** 📜  
  Define your own methods to expose a `Map<String, Object>` view of the spec.

- **Instant resets with `@Reset`** 🔄  
  Roll back any spec instance to its default state in a single call.

- **Powered by Gson** ⚡  
  Use all Gson features: custom type adapters, fine-tuned serialization, and more.

- **Ultra lightweight** 🧹  
  Only **40 KB** in size — no bloat, no slowdown.

## ✨ Why you'll love it:
- Super clean APIs
- Zero learning curve
- Infinite flexibility with Gson
- Handles nested, complex configs effortlessly
- Blazing fast and tiny footprint

### Example

```java
@ConfigSpec(header = {
        "========================================",
        "          Server Configuration          ",
        "========================================",
        "This file controls basic server settings.",
        "Edit with care to avoid misconfiguration."
})
public interface ServerConfig {

    @Key("name") // <--- optional
    @Comment("The name that will be displayed to players.")
    default String serverName() {
        return "My Awesome Server";
    }

    @Key("max-players")
    @Comment("Maximum number of players allowed online at once.")
    default int maxPlayers() {
        return 100;
    }

    @Key("game-mode")
    @Comment({
            "Server operating mode:",
            "- SURVIVAL = Normal gameplay",
            "- CREATIVE = Build freely",
            "- ADVENTURE = Limited interactions",
            " ",
            "Default: SURVIVAL"
    })
    default Mode serverMode() {
        return Mode.SURVIVAL;
    }

    @Comment("The message shown in the multiplayer server list.")
    default String motd() {
        return "Welcome to the Adventure!";
    }

    @Key("whitelist-enabled")
    @Comment("Enable/disable whitelist mode. Only approved players can join.")
    default boolean whitelistEnabled() {
        return false;
    }

    @Save
    void save();

    @Reload
    void reload();

    enum Mode {
        SURVIVAL,
        CREATIVE,
        ADVENTURE
    }
}
```

```java
import java.nio.file.Paths;

public static void main(String[] args) {
    ServerConfig config = Specs.fromFile(ServerConfig.class, Paths.get("server.yml"));
    // The Specs class includes many more utilities. Check it out!

    // Saves the configuration to config.yml
    config.save();

    // Reloads the configuration from config.yml
    config.reload();

    System.out.println(config);
}
```

Output YML:
```yml
# ========================================
#           Server Configuration          
# ========================================
# This file controls basic server settings.
# Edit with care to avoid misconfiguration.

# Server operating mode:
# - SURVIVAL = Normal gameplay
# - CREATIVE = Build freely
# - ADVENTURE = Limited interactions
#  
# Default: SURVIVAL
game-mode: SURVIVAL

# The message shown in the multiplayer server list.
motd: Welcome to the Adventure!

# The name that will be displayed to players.
name: My Awesome Server

# Enable/disable whitelist mode. Only approved players can join.
whitelist-enabled: false

# Maximum number of players allowed online at once.
max-players: 100.0
```

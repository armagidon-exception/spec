# Spec

[![Discord](https://discord.com/api/guilds/939962855476846614/widget.png)](https://discord.gg/pEGGF785zp)
[![Maven Central](https://img.shields.io/maven-metadata/v/https/repo1.maven.org/maven2/io/github/revxrsal/spec/maven-metadata.xml.svg?label=maven%20central&colorB=brightgreen)](https://search.maven.org/artifact/io.github.revxrsal/spec)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://github.com/Revxrsal/spec/actions/workflows/gradle.yml/badge.svg)](https://github.com/Revxrsal/spec/actions/workflows/gradle.yml)
[![CodeFactor](https://www.codefactor.io/repository/github/revxrsal/spec/badge)](https://www.codefactor.io/repository/github/revxrsal/spec)

A library for generating beautiful, commented, type-safe YML through interfaces

### Features
- Create interfaces that define your configuration
- Create default values with default methods
- Supports comments for specs using `@Comment` 🔥
- Recursively use specs as arrays, lists, values of maps, etc.
- Specs support setters
- Self-reloading and self-saving 🔥
- Uses Gson under the hood for serializing and deserializing, so you can add 

### Example

```java
@ConfigSpec(header = {
        "Welcome to my retro encabulator configuration",
        " ",
        "This configuration allows you to use six hydrocoptic marzel vanes and",
        "an ambifacient lunar wane shaft to prevent unwanted side fumbling.",
        " ",
        "[Insert more unintelligible tech jargon here]"
})
public interface MyConfiguration {

    @Key("device-name") // <--- optional
    @Comment("The device name")
    default String name() {
        return "Default name";
    }

    @Comment("The arena capacity")
    int capacity();

    @Comment({
            "The arena type. Values: 'teams' or 'single'",
            " ",
            "Default value: teams"
    })
    default ArenaType type() {
        return ArenaType.SINGLE;
    }

    @Save // <--- Spec handles that! Don't worry
    void save();
    
    @Reload // <--- Spec handles that! Don't worry
    void reload();
    
    enum ArenaType {
        TEAMS,
        SINGLE
    }
}
```

```java
import java.nio.file.Paths;

public static void main(String[] args) {
    MyConfiguration config = Specs.fromFile(MyConfiguration.class, Paths.get("config.yml"));
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
# Welcome to my retro encabulator configuration
#  
# This configuration allows you to use six hydrocoptic marzel vanes and
# an ambifacient lunar wane shaft to prevent unwanted side fumbling.
#  
# [Insert more unintelligible tech jargon here]

# The device name
device-name: Default name

# The arena type. Values: 'teams' or 'single'
#  
# Default value: teams
type: SINGLE

# The arena capacity
capacity: 0.0
```

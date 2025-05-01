package revxrsal.spec.test;

import revxrsal.spec.annotation.*;

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

    // You can leave it like this with no implementation
    @Save
    void save();

    // Or write as a default function which will be called
    // after the plugin is reloaded.
    //
    // Your function can have any arguments as needed
    @Reload
    default void reload() {
        System.out.println("Configuration reloaded!");
    }

    // We can nest ConfigSpecs too!
    //
    // You can also have them inside Maps, Lists, Sets, arrays, etc.
    @Comment("The server messages")
    Messages messages();

    @Comment("Includes numerical values")
    ServerNumbers numbers();

    @ConfigSpec
    interface ServerNumbers {

        @Comment("The chunk radius")
        default int chunkRadius() {
            return 13;
        }

        // @Memoize allows us to compute certain values and cache their result
        //
        // This is very useful for heavy, repetitive computations.
        //
        // Note: @Memoize does not (yet) consider arguments when caching values.
        // Therefore, it is best to just use it to compute the parts that depend on the
        // configuration values
        //
        // Reloading, resetting, or calling a setter will re-compute all memoized values.
        //
        // See the docs of @Memoize for more info
        @Memoize
        default int chunkRadiusCubed() {
            System.out.println("Computing chunkRadius^3");
            return chunkRadius() * chunkRadius() * chunkRadius();
        }
    }

    @ConfigSpec
    interface Messages {

        @Comment("Sent when a player joins")
        default String playerJoined() {
            return "Player %player% joined the server!";
        }

        @Comment("Sent when a player leaves")
        default String playerLeft() {
            return "Player %player% left the server!";
        }
    }

    enum Mode {
        SURVIVAL,
        CREATIVE,
        ADVENTURE
    }
}

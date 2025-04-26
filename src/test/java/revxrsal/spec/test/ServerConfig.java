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

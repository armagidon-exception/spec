package revxrsal.spec.test;

import revxrsal.spec.annotation.*;

@ConfigSpec(header = {
        "Welcome to my retro encabulator configuration",
        " ",
        "This configuration allows you to use six hydrocoptic marzel vanes and",
        "an ambifacient lunar wane shaft to prevent unwanted side fumbling.",
        " ",
        "[Insert more unintelligible tech jargon here]"
})
public interface Config {

    @Comment("The name")
    default String name() {
        return "Default name";
    }

    @Comment("The time to wait before the game starts")
    default int cooldown() {
        return 20;
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

    /**
     * Saves the config to the file
     */
    @Save
    void save();

    /**
     * Reloads the config
     */
    @Reload
    void reload();

    enum ArenaType {
        TEAMS,
        SINGLE
    }
}

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

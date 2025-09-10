package revxrsal.spec;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public interface PostProcessor {

    @Nullable Consumer<Object> createReadHook(SpecProperty property);

    @Nullable Consumer<Object> createWriteHook(SpecProperty property);

}

package revxrsal.spec;

public class PostProcessorHook {

    public static void injectPostProcessor(PostProcessor processor) {
        SpecClass.postProcessors.add(processor);
    }

}

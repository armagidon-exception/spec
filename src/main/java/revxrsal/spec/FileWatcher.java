package revxrsal.spec;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileWatcher implements AutoCloseable {

    private static final WatchEvent.Kind<?>[] DEFAULT_WATCH_EVENTS = new WatchEvent.Kind[]{
        StandardWatchEventKinds.OVERFLOW,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY};

    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("FileWatcher-Thread-" + counter.get());
            return t;
        }
    };

    private final Executor taskExecutor;

    private final AtomicBoolean open = new AtomicBoolean(false);
    private final WatchService watchService;
    private final Thread executor;
    private final Map<Path, Registration> registrations = new ConcurrentHashMap<>();


    private FileWatcher(ThreadFactory threadFactory, FileSystem fileSystem, Executor taskExecutor)
        throws IOException {
        this.watchService = fileSystem.newWatchService();
        this.taskExecutor = taskExecutor;

        this.executor = threadFactory.newThread(() -> {
            while (open.get()) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    open.set(false);
                    Thread.currentThread().interrupt();
                    break;
                } catch (ClosedWatchServiceException e) {
                    break;
                }

                Path watched = (Path) key.watchable();
                var registration = registrations.get(watched);
                if (registration != null) {
                    final Set<Object> seenContexts = new HashSet<>();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (!key.isValid()) {
                            break;
                        }

                        if (!seenContexts.add(event.context())) {
                            continue;
                        }

                        registration.handle(event);
                        if (registration.hasSubscribers()) {
                            key.cancel();
                            break;
                        }
                    }

                    if (!key.reset()) {
                        registrations.remove(watched);
                    }
                }

                try {
                    Thread.sleep(20);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        executor.start();
    }

    @Override
    public void close() throws Exception {
        open.set(false);
        this.registrations.clear();
        try {
            this.executor.interrupt();
            this.executor.join();
        } catch (final InterruptedException e) {
            throw new IOException("Failed to await termination of executor thread!");
        }
    }

    public void listenToFile(Path file, Consumer<WatchEvent<?>> action) throws IOException {
        file = file.toAbsolutePath();
        if (Files.isDirectory(file)) {
            throw new IllegalArgumentException("Path " + file + " must be a file");
        }

        final Path fileName = file.getFileName();
        registration(file.getParent()).subscribe(fileName, action);
    }


    public void listenToDirectory(Path directory, Consumer<WatchEvent<?>> callback)
        throws IllegalArgumentException, IOException {
        directory = directory.toAbsolutePath();
        if (!(Files.isDirectory(directory) || !Files.exists(directory))) {
            throw new IllegalArgumentException("Path " + directory + " must be a directory");
        }

        registration(directory).subscribe(callback);
    }


    private Registration registration(final Path directory) throws IOException {
        AtomicReference<IOException> exceptionHolder = new AtomicReference<>();
        final @Nullable Registration reg = this.registrations.computeIfAbsent(directory, dir -> {
            try {
                return new Registration(dir.register(this.watchService, DEFAULT_WATCH_EVENTS),
                    this.taskExecutor);
            } catch (final IOException ex) {
                exceptionHolder.set(ex);
                return null;
            }
        });

        if (reg == null) {
            throw new IOException("While adding listener for " + directory, exceptionHolder.get());
        }
        return reg;
    }

    public static FileWatcher create() throws IOException {
        return new FileWatcher(DEFAULT_THREAD_FACTORY, FileSystems.getDefault(),
            ForkJoinPool.commonPool());
    }

    private static final class Registration {


        @Getter
        private final WatchKey key;
        private final Executor executor;


        private final Map<Path, Collection<Consumer<WatchEvent<?>>>> fileListeners = new ConcurrentHashMap<>();
        private final Collection<Consumer<WatchEvent<?>>> dirListeners = new CopyOnWriteArraySet<>();


        private Registration(WatchKey key, Executor executor) {
            this.key = key;
            this.executor = executor;
        }

        public void subscribe(Consumer<WatchEvent<?>> subscriber) {
            if (subscriber == null) {
                throw new NullPointerException("subscriber");
            }
            dirListeners.add(subscriber);
        }

        public void subscribe(Path path, Consumer<WatchEvent<?>> subscriber) {
            if (subscriber == null) {
                throw new NullPointerException("subscriber");
            }
            if (path == null) {
                throw new NullPointerException("path");
            }
            fileListeners.computeIfAbsent(path, p -> new CopyOnWriteArraySet<>()).add(subscriber);
        }

        public void handle(WatchEvent<?> event) {
            final Path file = (Path) event.context();
            var listeners = fileListeners.get(file);
            if (listeners != null) {
                listeners.forEach(l -> executor.execute(() -> l.accept(event)));
            }

            dirListeners.forEach(l -> executor.execute(() -> l.accept(event)));
        }

        public boolean hasSubscribers() {
            return fileListeners.values().stream().anyMatch(Predicate.not(Collection::isEmpty))
                || !dirListeners.isEmpty();
        }
    }
}

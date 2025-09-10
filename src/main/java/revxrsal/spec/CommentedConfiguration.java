/*
 * This file is part of Cream, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.spec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.events.*;
import revxrsal.spec.Util.PeekingIterator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.*;
import static java.util.regex.Pattern.LITERAL;

/**
 * A configuration that supports comments. Set comments with
 * {@link #setComments(Map)}
 */
public class CommentedConfiguration {

    private static final ThreadLocal<Yaml> YAML = ThreadLocal.withInitial(() -> {
        DumperOptions options = new DumperOptions();
        setProcessComments(options, false);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    });

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(SpecAdapterFactory.INSTANCE)
            .create();

    /**
     * Pattern for matching newline characters.
     */
    public static final Pattern NEW_LINE = Pattern.compile("\n", LITERAL);
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    /**
     * YAML processor instance for reading and writing YAML data.
     */
    protected final Yaml yaml;

    /**
     * A map storing comments associated with specific configuration keys.
     */
    protected final Map<String, String> configComments = new HashMap<>();

    /**
     * A map storing comments associated with specific configuration keys.
     */
    protected List<String> headers = Collections.emptyList();

    /**
     * Json instance for serializing and deserializing JSON data.
     */
    protected final Gson gson;

    /**
     * Path to the configuration socket.
     */
    protected final DataSocket socket;

    /**
     * The JSON representation of the configuration data.
     */
    protected Map<String, Object> data = new LinkedHashMap<>();

    /**
     * The array commenting style
     */
    protected final ArrayCommentStyle arrayCommentStyle;

    public CommentedConfiguration(DataSocket socket, Gson gson, ArrayCommentStyle arrayCommentStyle, Yaml yaml) {
        this.socket = socket;
        this.gson = gson;
        this.arrayCommentStyle = arrayCommentStyle;
        this.yaml = yaml;
    }

    public CommentedConfiguration(DataSocket socket, Gson gson, ArrayCommentStyle arrayCommentStyle) {
        this(socket, gson, arrayCommentStyle, YAML.get());
    }

    /**
     * Loads the content of this configuration
     */
    @SneakyThrows
    public void load() {
        try (BufferedReader reader = new BufferedReader(socket.openReader())) {
            data = yaml.load(reader);
            if (data == null)
                data = new LinkedHashMap<>();
        }
    }

    /**
     * Sets the comment of the given path.
     *
     * @param path    The comment path. Subkeys are delimited by '.', and array entries have 0
     *                as their parent.
     * @param comment The comment
     */
    public void setComment(@NotNull String path, @NotNull String comment) {
        this.configComments.put(path, comment);
    }

    /**
     * Sets the comments of this configuration file.
     *
     * @param comments The comments to set. Supports multiple lines (use \n as a spacer).
     */
    public void setComments(@NotNull Map<String, String> comments) {
        this.configComments.clear();
        this.configComments.putAll(comments);
    }

    /**
     * Saves this configuration file with comments set with {@link #setComments(Map)}.
     */
    @SneakyThrows
    public void save() {
        if (configComments.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(socket.openWriter())) {
                yaml.dump(data, writer);
            }
            return;
        }
        String simpleDump = yaml.dump(data);
        String[] split = NEW_LINE.split(simpleDump);
        List<String> lines = new ArrayList<>(split.length);
        Collections.addAll(lines, split);
        StringReader reader = new StringReader(simpleDump);
        Iterable<Event> events = yaml.parse(reader);
        handleEvents(events.iterator(), lines); // terribly inefficient way but I can't care less lol
        if (!lines.isEmpty()) {
            String first = lines.get(0);
            if (Character.isWhitespace(first.charAt(0))) {
                lines.set(0, first.substring(1));
            }
        }
        for (int i = 0; i < headers.size(); i++) {
            String l = headers.get(i);
            if (l.startsWith("#"))
                lines.add(i, "#" + l);
            else
                lines.add(i, "# " + l);
        }
        if (!headers.isEmpty()) {
            lines.add(headers.size(), "");
        }

        socket.writeStrings(lines);
    }

    /**
     * Create a config from a socket
     *
     * @param socket              The socket to load the config from.
     * @param json              The JSON instance to deserialize with
     * @param arrayCommentStyle The array commenting style. See {@link ArrayCommentStyle}.
     * @return A new instance of CommentedConfiguration
     */
    public static @NotNull CommentedConfiguration from(
            @NotNull DataSocket socket,
            @NotNull Gson json,
            @NotNull ArrayCommentStyle arrayCommentStyle
    ) {
        //Creating a blank instance of the config.
        return new CommentedConfiguration(socket, json, arrayCommentStyle);
    }

    /**
     * Create a config from a socket
     *
     * @param socket              The socket to load the config from.
     * @param arrayCommentStyle The array commenting style. See {@link ArrayCommentStyle}.
     * @return A new instance of CommentedConfiguration
     */
    public static @NotNull CommentedConfiguration from(
            @NotNull DataSocket socket,
            @NotNull ArrayCommentStyle arrayCommentStyle
    ) {
        //Creating a blank instance of the config.
        return new CommentedConfiguration(socket, GSON, arrayCommentStyle);
    }

    /**
     * Create a config from a socket
     *
     * @param socket The socket to load the config from.
     * @param gson The JSON instance to deserialize with
     * @return A new instance of CommentedConfiguration
     */
    public static @NotNull CommentedConfiguration from(
            @NotNull DataSocket socket,
            @NotNull Gson gson
    ) {
        //Creating a blank instance of the config.
        return new CommentedConfiguration(socket, gson, ArrayCommentStyle.COMMENT_FIRST_ELEMENT);
    }

    /**
     * Create a config from a socket
     *
     * @param socket The socket to load the config from.
     * @return A new instance of CommentedConfiguration
     */
    public static @NotNull CommentedConfiguration from(@NotNull DataSocket socket) {
        //Creating a blank instance of the config.
        return from(socket, GSON);
    }

    /**
     * Retrieves the value for a key and deserializes it to the specified type.
     *
     * @param key  The key to retrieve the value for.
     * @param type The type to deserialize the value into.
     * @param <T>  The type of the returned value.
     * @return The deserialized value.
     */
    public <T> T get(@NotNull String key, @NotNull Type type) {
        return fromValue(gson, data.get(key), Object.class, type);
    }


    /**
     * Deserializes the entire configuration data to the specified type.
     *
     * @param type The type to deserialize the data into.
     * @param <T>  The type of the returned value.
     * @return The deserialized data.
     */
    public <T> T getAs(@NotNull Type type) {
        return fromValue(gson, data, MAP_TYPE, type);
    }

    /**
     * Retrieves the value for a key and deserializes it to the specified class.
     *
     * @param key  The key to retrieve the value for.
     * @param type The class to deserialize the value into.
     * @param <T>  The type of the returned value.
     * @return The deserialized value.
     */
    public <T> T get(@NotNull String key, @NotNull Class<T> type) {
        return get(key, (Type) type);
    }

    /**
     * Sets a value for a key using JSON serialization.
     *
     * @param key The key to set the value for.
     * @param v   The value to set.
     */
    public void set(@NotNull String key, @Nullable Object v) {
        if (v == null)
            data.remove(key);
        else
            data.put(key, toJsonValue(gson, v, v.getClass()));
    }

    /**
     * Sets a value for a key using JSON serialization with a specific type.
     *
     * @param key  The key to set the value for.
     * @param v    The value to set.
     * @param type The type used for serialization.
     */
    public void set(@NotNull String key, @NotNull Object v, @NotNull Type type) {
        data.put(key, toJsonValue(gson, v, type));
    }

    private static Object toJsonValue(Gson gson, @NotNull Object o, @NotNull Type type) {
        String toJson = gson.toJson(o, type);
        return gson.fromJson(toJson, Object.class);
    }

    private static <T> T fromValue(Gson gson, Object o, @NotNull Type valueType, @NotNull Type javaType) {
        String toJson = gson.toJson(o, valueType);
        return gson.fromJson(toJson, javaType);
    }

    /**
     * Checks if the configuration contains a value for the given path.
     *
     * @param path The path to check.
     * @return {@code true} if the path exists, {@code false} otherwise.
     */
    public boolean contains(@NotNull String path) {
        return data.containsKey(path);
    }

    public void setHeaders(@NotNull List<String> headers) {
        this.headers = headers;
    }

    /**
     * Replaces the configuration data with the given JSON object.
     *
     * @param data The new JSON object to set.
     */
    public void setTo(@NotNull Object data, Type type) {
        Object value = toJsonValue(gson, data, type);
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("Expected data to be a map-like structure, found " + value);
        }
        //noinspection unchecked
        this.data = new LinkedHashMap<>((Map<String, Object>) value);
    }

    /**
     * Replaces the configuration data with the given JSON object.
     *
     * @param data The new JSON object to set.
     */
    public void setTo(@NotNull Object data) {
        setTo(data, data.getClass());
    }

    /**
     * Retrieves the entire configuration data as a JSON object.
     *
     * @return The configuration data.
     */
    public @UnmodifiableView Map<String, Object> getData() {
        return Collections.unmodifiableMap(data);
    }

    protected void handleEvents(Iterator<Event> eventsI, List<String> lines) {
        PeekingIterator<Event> events = PeekingIterator.from(eventsI);
        LinkedList<String> path = new LinkedList<>();
        Set<String> commentsAdded = new HashSet<>();
        boolean expectKey = true;
        boolean lastWasScalar = false;
        int offset = 0;
        while (events.hasNext()) {
            Event event = events.next();
            if (event instanceof DocumentStartEvent) {
                expectKey = true;
            }
            if (event instanceof MappingStartEvent) {
                expectKey = true;
            } else if (event instanceof MappingEndEvent) {
                path.pollLast();
                expectKey = true;
                if (events.hasNext()) {
                    Event next = events.peek();
                    if (next instanceof ScalarEvent) {
                        path.pollLast();
                    }
                }
            } else if (event instanceof ScalarEvent) {
                if (expectKey) {
                    expectKey = false;
                    if (lastWasScalar)
                        path.removeLast();
                    path.add(((ScalarEvent) event).getValue());
                } else {
                    expectKey = true;
                }
            }
            if (event instanceof SequenceStartEvent) {
                path.add(SpecClass.ARRAY_INDEX);
            } else if (event instanceof SequenceEndEvent) {
                path.pollLast();
                expectKey = true;
                if (events.hasNext()) {
                    Event next = events.peek();
                    if (next instanceof ScalarEvent) {
                        path.pollLast();
                    }
                }
            }

            lastWasScalar = event instanceof ScalarEvent;
            String commentPath = String.join(".", path);
            String comment = configComments.get(commentPath);
            if (comment != null && (commentsAdded.add(commentPath) || arrayCommentStyle == ArrayCommentStyle.COMMENT_ALL_ELEMENTS)) {
                lines.add(event.getStartMark().getLine() + (offset++), comment);
            }
        }
    }

    /**
     * Reflective access to the `setProcessComments` method in {@link DumperOptions}.
     */
    private static @Nullable Method SET_PROCESS_COMMENTS;

    static {
        try {
            // Attempt to retrieve the private `setProcessComments` method.
            SET_PROCESS_COMMENTS = DumperOptions.class.getDeclaredMethod("setProcessComments", boolean.class);
            SET_PROCESS_COMMENTS.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
            // Ignored as the method may not exist in older versions.
        }
    }

    /**
     * Sets the `processComments` flag on the given {@link DumperOptions} instance.
     *
     * @param options The {@link DumperOptions} instance.
     * @param process The value to set for `processComments`.
     */
    @SneakyThrows
    protected static void setProcessComments(@NotNull DumperOptions options, boolean process) {
        if (SET_PROCESS_COMMENTS != null)
            SET_PROCESS_COMMENTS.invoke(options, process);
    }
}

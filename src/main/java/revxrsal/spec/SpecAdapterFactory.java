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
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static revxrsal.spec.MHLookup.privateLookupIn;
import static revxrsal.spec.Specs.createDefault;
import static revxrsal.spec.Specs.isConfigSpec;

@SuppressWarnings({"unchecked"})
public final class SpecAdapterFactory implements TypeAdapterFactory {

    private static @Nullable MethodHandle CTR_CTR = null;

    static {
        try {
            MethodHandles.Lookup lookup = privateLookupIn(Gson.class);
            CTR_CTR = lookup
                    .findGetter(Gson.class, "constructorConstructor", ConstructorConstructor.class);
        } catch (Throwable ignored) {
        }
    }

    public static final SpecAdapterFactory INSTANCE = new SpecAdapterFactory();

    @Override
    @SneakyThrows
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<?> rawType = type.getRawType();
        if (!isConfigSpec(rawType)) {
            return null;
        }
        SpecClass impl = Specs.from(rawType);
        Map<String, BoundField> fieldsMap = new LinkedHashMap<>();
        for (SpecProperty value : impl.properties().values()) {
            if (value.isHandledByProxy())
                continue;
            Method getter = value.getter();
            TypeToken<?> fieldType = TypeToken.get(getter.getGenericReturnType());

            TypeAdapter<?> adapter = null;
            JsonAdapter annotation = getter.getAnnotation(JsonAdapter.class);
            if (CTR_CTR != null) {
                ConstructorConstructor constructorConstructor = (ConstructorConstructor) CTR_CTR.invoke(gson);
                if (annotation != null) {
                    adapter = getTypeAdapter(constructorConstructor, gson, fieldType, annotation);
                }
            }
            if (adapter == null)
                adapter = gson.getAdapter(fieldType);

            BoundField field = new BoundField(value.key(), adapter);
            fieldsMap.put(value.key(), field);
        }

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                out.beginObject();
                Map<String, Object> map = MapProxy.getInternalMap(value);
                for (BoundField boundField : fieldsMap.values()) {
                    out.name(boundField.name);
                    Object fieldValue = map.get(boundField.name);
                    boundField.adapter().write(out, fieldValue);
                }
                out.endObject();
            }

            @SneakyThrows
            @Override
            public T read(JsonReader in) {
                in.beginObject();
                T proxy = (T) createDefault(rawType);
                Map<String, Object> map = MapProxy.getInternalMap(proxy);
                while (in.hasNext()) {
                    String name = in.nextName();
                    BoundField field = fieldsMap.get(name);
                    if (field == null) {
                        in.skipValue();
                    } else {
                        Object readValue = field.adapter.read(in);
                        map.put(field.name, readValue);
                    }
                }
                in.endObject();
                return proxy;
            }
        };
    }

    private static class BoundField {
        private final @NotNull String name;
        private final @NotNull TypeAdapter<?> adapter;

        @SneakyThrows
        public BoundField(@NotNull String name, @NotNull TypeAdapter<?> adapter) {
            this.name = name;
            this.adapter = adapter;
        }

        public @NotNull <T> TypeAdapter<T> adapter() {
            return (TypeAdapter<T>) adapter;
        }
    }

    static TypeAdapter<?> getTypeAdapter(ConstructorConstructor constructorConstructor, Gson gson,
                                         TypeToken<?> fieldType, JsonAdapter annotation) {
        Class<?> value = annotation.value();
        if (TypeAdapter.class.isAssignableFrom(value)) {
            Class<TypeAdapter<?>> typeAdapter = (Class<TypeAdapter<?>>) value;
            return constructorConstructor.get(TypeToken.get(typeAdapter)).construct();
        }
        if (TypeAdapterFactory.class.isAssignableFrom(value)) {
            Class<TypeAdapterFactory> typeAdapterFactory = (Class<TypeAdapterFactory>) value;
            return constructorConstructor.get(TypeToken.get(typeAdapterFactory))
                    .construct()
                    .create(gson, fieldType);
        }

        throw new IllegalArgumentException("@JsonAdapter value must be TypeAdapter or TypeAdapterFactory reference.");
    }
}

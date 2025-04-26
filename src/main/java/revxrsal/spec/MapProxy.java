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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import revxrsal.spec.annotation.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static revxrsal.spec.SpecProperty.impliesSetter;
import static revxrsal.spec.SpecProperty.keyOf;
import static revxrsal.spec.Specs.createDefaultMap;
import static revxrsal.spec.Specs.isConfigSpec;

/**
 * Generates proxies that are backed by {@link Map maps}.
 */
final class MapProxy<T> implements InvocationHandler {

    @SuppressWarnings("unchecked")
    public static @NotNull <T> T generate(@NotNull Class<T> type, @NotNull Map<String, Object> map) {
        return (T) Proxy.newProxyInstance(
                MapProxy.class.getClassLoader(),
                new Class[]{type},
                new MapProxy<>(type, map)
        );
    }

    @SuppressWarnings({"unchecked"})
    @Contract("null -> fail")
    static @NotNull <T> Map<String, Object> getInternalMap(T value) {
        Objects.requireNonNull(value, "value is null!");
        if (!Proxy.isProxyClass(value.getClass())) {
            throw new IllegalArgumentException("Not a proxy instance.");
        }
        InvocationHandler handler = Proxy.getInvocationHandler(value);
        if (!(handler instanceof MapProxy)) {
            throw new IllegalArgumentException("Not a config spec");
        }
        return ((MapProxy<T>) handler).map;
    }

    private static final Method TO_STRING;
    private static final Method EQUALS;
    private static final Method HASH_CODE;

    private final Class<T> type;
    private final Map<String, Object> map;

    private final Map<Method, MethodHandle> defaultMethodHandles = new HashMap<>();

    public MapProxy(Class<T> type, Map<String, Object> map) {
        this.type = type;
        this.map = map;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.equals(TO_STRING)) {
            return generateToString();
        }
        if (method.equals(EQUALS)) {
            Object other = args[0];
            if (!Proxy.isProxyClass(other.getClass())) {
                return false;
            }
            if (isConfigSpec(other.getClass())) {
                MapProxy<?> otherHandler = (MapProxy<?>) Proxy.getInvocationHandler(args);
                return map.equals(otherHandler.map);
            }
        }
        if (method.equals(HASH_CODE)) {
            return map.hashCode();
        }
        if (method.isAnnotationPresent(IgnoreMethod.class)) {
            MethodHandle mh = defaultMethodHandles.get(method);
            if (mh == null) {
                mh = MHLookup.privateLookupIn(type)
                        .in(type)
                        .unreflectSpecial(method, type);
                defaultMethodHandles.put(method, mh);
            }
            return mh.bindTo(proxy).invokeWithArguments(args);
        }
        if (method.isAnnotationPresent(AsMap.class)) {
            AsMap asMap = method.getAnnotation(AsMap.class);
            switch (Objects.requireNonNull(asMap).value()) {
                case CLONE:
                    return new LinkedHashMap<>(map);
                case IMMUTABLE_VIEW:
                    return Collections.unmodifiableMap(map);
                case UNDERLYING_MAP:
                    return map;
            }
        }
        if (method.isAnnotationPresent(Reload.class)) {
            throw new IllegalStateException("You cannot reload this! Try to reload the top entity.");
        }
        if (method.isAnnotationPresent(Save.class)) {
            throw new IllegalStateException("You cannot save this! Try to save the top entity.");
        }
        if (method.isAnnotationPresent(Reset.class)) {
            this.map.clear();
            //noinspection unchecked
            createDefaultMap(type, (T) proxy, this.map);
            return null;
        }
        String key = keyOf(method);
        if (method.getReturnType() == Void.TYPE || impliesSetter(method)) {
            map.put(key, args[0]);
            return null;
        } else {
            return map.get(key);
        }
    }

    private String generateToString() {
        StringBuilder sb = new StringBuilder(type.getSimpleName() + "(");
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(")");
        return sb.toString();
    }

    static {
        try {
            TO_STRING = Object.class.getDeclaredMethod("toString");
            EQUALS = Object.class.getDeclaredMethod("equals", Object.class);
            HASH_CODE = Object.class.getDeclaredMethod("hashCode");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

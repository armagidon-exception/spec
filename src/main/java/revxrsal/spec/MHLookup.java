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

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.lookup;

/**
 * A utility for generating private {@link Lookup}s. These are not supported
 * natively in Java 8, so we have to use reflection hacks to simulate them.
 */
final class MHLookup {

    private static @Nullable Constructor<Lookup> constructor;
    private static @Nullable Method privateLookupIn;

    static {
        try {
            privateLookupIn = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
        } catch (NoSuchMethodException e) {
            try {
                constructor = Lookup.class.getDeclaredConstructor(Class.class);
                constructor.setAccessible(true);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private MHLookup() {
    }

    /**
     * Generates a {@link Lookup} that can access private members in the given
     * class.
     *
     * @param cl The class to access
     * @return The created {@link Lookup}
     */
    @SneakyThrows
    public static @NotNull Lookup privateLookupIn(Class<?> cl) {
        if (privateLookupIn != null) {
            return (Lookup) privateLookupIn.invoke(null, cl, lookup());
        }
        if (constructor != null) {
            return constructor.newInstance(cl);
        }
        throw new IllegalArgumentException("Failed to create a private lookup!");
    }
}

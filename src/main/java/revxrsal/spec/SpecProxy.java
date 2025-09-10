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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import revxrsal.spec.annotation.Reload;
import revxrsal.spec.annotation.Save;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
final class SpecProxy<T> implements InvocationHandler {

    public static <T> T proxy(Class<T> type, Supplier<T> supplier, Runnable onReload, Runnable onSave) {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                new SpecProxy<>(type, supplier, onReload, onSave)
        );
    }

    private final Class<?> type;
    private final Supplier<T> supplier;
    private final Runnable onReload;
    private final Runnable onSave;

    private MethodHandle reloadDef, saveDef;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Reload.class)) {
            onReload.run();
            if (method.isDefault()) {
                if (reloadDef == null) {
                    reloadDef = MHLookup.privateLookupIn(type)
                            .in(type)
                            .unreflectSpecial(method, type);
                }
                return reloadDef.bindTo(proxy).invokeWithArguments(args);
            }
            return null;
        }
        if (method.isAnnotationPresent(Save.class)) {
            onSave.run();
            if (method.isDefault()) {
                if (saveDef == null) {
                    saveDef = MHLookup.privateLookupIn(type)
                            .in(type)
                            .unreflectSpecial(method, type);
                }
                return saveDef.bindTo(proxy).invokeWithArguments(args);
            }
            return null;
        }
        T instance = supplier.get();
        return method.invoke(instance, args);
    }
}

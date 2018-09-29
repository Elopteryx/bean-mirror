package com.github.elopteryx.reflect.tests.internal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.elopteryx.reflect.BeanMirror;
import com.github.elopteryx.reflect.BeanMirrorException;
import com.github.elopteryx.reflect.internal.NULL;
import com.github.elopteryx.reflect.internal.Utils;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

class UtilsTest {

    @SuppressWarnings("unused")
    private static class ParameterTypes {

        private void empty() {}

        private void run(final int value) {}

        private void call(final Boolean value) {}

    }

    @Test
    void createUtils() {
        final var exception = assertThrows(BeanMirrorException.class,
                () -> BeanMirror.of(Utils.class, MethodHandles.lookup()).create());
        assertEquals(UnsupportedOperationException.class, exception.getCause().getCause().getClass());
    }

    @Test
    void useIsSimilarSignature() throws Exception {
        final var emptyMethod = ParameterTypes.class.getDeclaredMethod("empty");
        assertFalse(Utils.isSimilarSignature(emptyMethod, "empty_"));
        assertFalse(Utils.isSimilarSignature(emptyMethod, "empty", Integer.class));

        final var runMethod = ParameterTypes.class.getDeclaredMethod("run", int.class);
        assertTrue(Utils.isSimilarSignature(runMethod, "run", Integer.class));
        assertFalse(Utils.isSimilarSignature(runMethod, "run", Long.class));

        final var callMethod = ParameterTypes.class.getDeclaredMethod("call", Boolean.class);
        assertTrue(Utils.isSimilarSignature(callMethod, "call", Boolean.class));
        assertTrue(Utils.isSimilarSignature(callMethod, "call", boolean.class));
    }

    @Test
    void useTypes() {
        assertAll(
                () -> assertEquals(Utils.types((Object[]) null).length, 0),
                () -> assertEquals(Utils.types((Object) null)[0], NULL.class)
        );
    }

    @Test
    void useWrapper() {
        assertAll(
                () -> assertNull(Utils.wrapper(null)),
                () -> assertEquals(Utils.wrapper(Object.class), Object.class),
                () -> assertEquals(Utils.wrapper(boolean.class), Boolean.class),
                () -> assertEquals(Utils.wrapper(int.class), Integer.class),
                () -> assertEquals(Utils.wrapper(long.class), Long.class),
                () -> assertEquals(Utils.wrapper(short.class), Short.class),
                () -> assertEquals(Utils.wrapper(byte.class), Byte.class),
                () -> assertEquals(Utils.wrapper(double.class), Double.class),
                () -> assertEquals(Utils.wrapper(float.class), Float.class),
                () -> assertEquals(Utils.wrapper(char.class), Character.class),
                () -> assertEquals(Utils.wrapper(void.class), Void.class)
        );
    }
}

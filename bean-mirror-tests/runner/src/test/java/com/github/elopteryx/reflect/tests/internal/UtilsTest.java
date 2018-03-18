package com.github.elopteryx.reflect.tests.internal;

import com.github.elopteryx.reflect.internal.NULL;
import com.github.elopteryx.reflect.internal.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsTest {

    @SuppressWarnings("unused")
    private static class ParameterTypes {

        private void empty() {}

        private void run(int value) {}

        private void call(Boolean value) {}

    }

    @Test
    void useIsSimilarSignature() throws Exception {
        final var runMethod = ParameterTypes.class.getDeclaredMethod("run", int.class);
        assertTrue(Utils.isSimilarSignature(runMethod, "run", new Class[]{Integer.class}));
        final var callMethod = ParameterTypes.class.getDeclaredMethod("call", Boolean.class);
        assertTrue(Utils.isSimilarSignature(callMethod, "call", new Class[]{Boolean.class}));
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ApproximateObjectSize implements ObjectSize {

    private static Logger LOGGER = LoggerFactory.getLogger(ApproximateObjectSize.class);
    private static final int HEADER_SIZE = 8;
    private static final int ARRAY_HEADER_SIZE = 12;
    private static final int STRING_HEADER_SIZE = 32;
    private static final int REFERENCE_SIZE = 8;
    private static final int CHAR_SIZE = 2;
    private static final String PRIMITIVE_TYPE_FIELD_NAME = "TYPE";

    private static Map<Class, Integer> PRIMITIVE_SIZE = new HashMap<>();

    static {
        PRIMITIVE_SIZE.put(Boolean.TYPE, 1);
        PRIMITIVE_SIZE.put(Byte.TYPE, 1);
        PRIMITIVE_SIZE.put(Character.TYPE, 2);
        PRIMITIVE_SIZE.put(Short.TYPE, 2);
        PRIMITIVE_SIZE.put(Integer.TYPE, 4);
        PRIMITIVE_SIZE.put(Float.TYPE, 4);
        PRIMITIVE_SIZE.put(Long.TYPE, 8);
        PRIMITIVE_SIZE.put(Double.TYPE, 8);
    }

    @Override
    public long getSize(Object object) throws IllegalAccessException, NoSuchFieldException {
        Class<?> type = object.getClass();
        boolean isNumeric = Number.class.isAssignableFrom(type);
        boolean isArray = type.isArray();
        boolean isString = type == String.class;
        long size;
        if (!(isNumeric || isArray || isString)) {
            size = HEADER_SIZE;
            for (Field field : type.getDeclaredFields()) {
                size += sizeOf(field.get(object));
            }
        } else {
            size = sizeOf(object);
        }
        return align(size);
    }

    /**
     * Returns size of a given type
     *
     * @return size of the type if it's primitive, wrapper, array or {@code String}, {@code 0} otherwise.
     */
    private long sizeOf(Object object) {
        long size;
        Class<?> type = object.getClass();
        if (type.isPrimitive()) {
            size = PRIMITIVE_SIZE.get(type);
        } else if (Number.class.isAssignableFrom(type)) {
            size = HEADER_SIZE + sizeOfPrimitive((Class<Number>) type);
        } else if (type.isArray()) {
            size = ARRAY_HEADER_SIZE;
            size += REFERENCE_SIZE * getArrayLength(object);
        } else if (type == String.class) {
            size = STRING_HEADER_SIZE;
            size += CHAR_SIZE * getStringLength(object);
        } else {
            size = HEADER_SIZE;
        }
        return size;
    }

    private static int sizeOfPrimitive(Class<? extends Number> type) {
        int size = 0;
        try {
            size = PRIMITIVE_SIZE.get(type.getDeclaredField(PRIMITIVE_TYPE_FIELD_NAME).get(null));
        } catch (Exception e) {
            LOGGER.warn("Size of numeric type '{}' can't be calculated due to error:", type, e);
        }
        return size;
    }

    /**
     * Aligns a size to 8
     *
     * @param size the size that should be aligned
     * @return aligned size
     */
    private static long align(long size) {
        while (size % 8 != 0) {
            size += 1;
        }
        return size;
    }

    private static int getStringLength(Object value) {
        return ((String) value).length();
    }

    private static int getArrayLength(Object value) {
        int length = Array.getLength(value);
        int nestedLength = 0;
        for (int i = 0; i < length; i++) {
            Object next = Array.get(value, i);
            if (next.getClass().isArray()) {
                nestedLength += getArrayLength(next);
            }
        }
        return length + nestedLength;
    }

}

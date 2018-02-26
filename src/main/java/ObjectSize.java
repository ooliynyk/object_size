/**
 * Calculates the size of an object
 */
public interface ObjectSize {

    /**
     * @param object the object which size is to be calculated
     * @return the cost in bytes of a given object
     */
    long getSize(Object object) throws IllegalAccessException, NoSuchFieldException;

}
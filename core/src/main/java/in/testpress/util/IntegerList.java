package in.testpress.util;

import java.util.ArrayList;
import java.util.Collection;

public class IntegerList extends ArrayList<Integer> {

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public IntegerList() {
        super();
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public IntegerList(Collection<? extends Integer> c) {
        super(c);
    }

}

package in.testpress.util;

import java.util.ArrayList;
import java.util.Collection;

public class StringList extends ArrayList<String> {

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public StringList() {
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
    public StringList(Collection<? extends String> c) {
        super(c);
    }

}

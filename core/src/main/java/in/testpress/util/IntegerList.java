package in.testpress.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;

public class IntegerList extends ArrayList<Integer> {
    public IntegerList(@NonNull Collection<? extends Integer> c) {
        super(c);
    }

    public IntegerList() {
    }
}

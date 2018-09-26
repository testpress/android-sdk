package in.testpress.util;

import android.content.Context;
import android.support.v4.content.Loader;
import android.util.Log;

import java.util.List;

import in.testpress.core.TestpressException;

/**
 * Loader that support throwing an exception when loading in the background
 *
 * @param <D>
 */
public abstract class ThrowableLoader<D> extends AsyncLoader<D> {

    private final D data;

    private TestpressException exception;

    /**
     * Create loader for context and seeded with initial data
     *
     * @param context
     * @param data
     */
    public ThrowableLoader(final Context context, final D data) {
        super(context);

        this.data = data;
    }

    @Override
    public D loadInBackground() {
        exception = null;
        try {
            return loadData();
        } catch (final TestpressException e) {
            Log.d("ThrowableLoader", "Exception loading data");
            e.printStackTrace();
            exception = e;
            return data;
        }
    }

    /**
     * @return exception
     */
    public TestpressException getException() {
        return exception;
    }

    /**
     * Clear the stored exception and return it
     *
     * @return exception
     */
    public TestpressException clearException() {
        final TestpressException throwable = exception;
        exception = null;
        return throwable;
    }

    /**
     * Load data
     *
     * @return data
     * @throws TestpressException
     */
    public abstract D loadData() throws TestpressException;

    /**
     * return the stored exception in given loader and clear it.
     *
     * @return exception
     */
    public static <T> TestpressException getException(Loader<List<T>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<T>>) loader).clearException();
        } else {
            return null;
        }
    }
}

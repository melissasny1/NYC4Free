package com.thenyc4free.gps1.nyc4free;

/**
 * This is a useful callback mechanism to use to abstract the AsyncTasks out into a separate,
 * re-usable and testable class, yet still retain a hook back into the calling activity.
 *
 * @param <T>
 */
interface AsyncTaskCompleteListener<T> {

    /**
     * Invoked when the AsyncTask has completed its execution.
     * @param result The resulting object from the AsyncTask.
     */
    void onTaskComplete(T result);
}

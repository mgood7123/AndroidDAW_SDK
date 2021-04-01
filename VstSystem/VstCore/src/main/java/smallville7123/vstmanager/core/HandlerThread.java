package smallville7123.vstmanager.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link Thread} that has a {@link Looper}.
 * The {@link Looper} can then be used to create {@link Handler}s.
 * <p>
 * Note that just like with a regular {@link Thread}, {@link #start()} must still be called.
 */
public class HandlerThread extends Thread {
    private static final String TAG = "HandlerThread";
    int mPriority;
    int mTid = -1;
    Looper mLooper;
    @Nullable Handler mHandler;

    Vector<FutureTask<?>> futureTaskVector = new Vector<>(10);

    /** Main lock guarding all access */
    final ReentrantLock lock;

    /** Condition for waiting retrievals */
    private final Condition notEmpty;

    public HandlerThread(String name) {
        super(name);
        mPriority = Process.THREAD_PRIORITY_DEFAULT;
        lock = new ReentrantLock(true);
        notEmpty = lock.newCondition();
    }

    /**
     * Constructs a HandlerThread.
     * @param name
     * @param priority The priority to run the thread at. The value supplied must be from
     * {@link android.os.Process} and not from java.lang.Thread.
     */
    public HandlerThread(String name, int priority) {
        super(name);
        mPriority = priority;
        lock = new ReentrantLock(true);
        notEmpty = lock.newCondition();
    }

    /**
     * Call back method that can be explicitly overridden if needed to execute some
     * setup before Looper loops.
     */
    protected void onLooperPrepared() {
    }

    @Override
    public void run() {
        mTid = Process.myTid();
        Looper.prepare();
        synchronized (this) {
            mLooper = Looper.myLooper();
            notifyAll();
        }
        Process.setThreadPriority(mPriority);
        onLooperPrepared();
        Looper.loop();
        mTid = -1;
    }

    /**
     * This method returns the Looper associated with this thread. If this thread not been started
     * or for any reason isAlive() returns false, this method will return null. If this thread
     * has been started, this method will block until the looper has been initialized.
     * @return The looper.
     */
    public Looper getLooper() {
        if (!isAlive()) {
            return null;
        }

        // If the thread has been started, wait until the looper has been created.
        synchronized (this) {
            while (isAlive() && mLooper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return mLooper;
    }

    /**
     * @return a shared {@link Handler} associated with this thread, starts the thread if not
     * already started
     */
    @NonNull
    public Handler getThreadHandler() {
        if (getState() == State.NEW) start();
        if (mHandler == null) {
            mHandler = new Handler(getLooper()) {
                /**
                 * Subclasses must implement this to receive messages.
                 *
                 * @param msg
                 */
                @Override
                public void handleMessage(@NonNull Message msg) {
                    FutureTask<?> f = new FutureTask<>(() -> super.handleMessage(msg), null);
                    if (futureTaskVector.size() == futureTaskVector.capacity()) {
                        if (!futureTaskVector.isEmpty()) {
                            futureTaskVector.remove(futureTaskVector.size() - 1);
                        }
                    }
                    futureTaskVector.add(f);
                    lock.lock();
                    notEmpty.signal();
                    lock.unlock();
                    f.run();
                }

                /**
                 * Handle system messages here.
                 *
                 * @param msg
                 */
                @Override
                public void dispatchMessage(@NonNull Message msg) {
                    FutureTask<?> f = new FutureTask<>(() -> super.dispatchMessage(msg), null);
                    if (futureTaskVector.size() == futureTaskVector.capacity()) {
                        if (!futureTaskVector.isEmpty()) {
                            futureTaskVector.remove(futureTaskVector.size() - 1);
                        }
                    }
                    futureTaskVector.add(f);
                    lock.lock();
                    notEmpty.signal();
                    lock.unlock();
                    f.run();
                }
            };
        }
        return mHandler;
    }

    public void waitForOldestPost() {
        lock.lock();
        while (futureTaskVector.size() == 0)
            notEmpty.awaitUninterruptibly();
        lock.unlock();
        FutureTask<?> futureTask = futureTaskVector.lastElement();
        if (futureTask != null) {
            while(true) {
                try {
                    futureTask.get();
                    break;
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    // try again
                }
            }
        }
    }

    public void waitForCurrentPost() {
        lock.lock();
        while (futureTaskVector.size() == 0)
            notEmpty.awaitUninterruptibly();
        lock.unlock();
        FutureTask<?> futureTask = futureTaskVector.firstElement();
        if (futureTask != null) {
            while(true) {
                try {
                    futureTask.get();
                    break;
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    // try again
                }
            }
        }
    }

    /**
     * Quits the handler thread's looper.
     * <p>
     * Causes the handler thread's looper to terminate without processing any
     * more messages in the message queue.
     * </p><p>
     * Any attempt to post messages to the queue after the looper is asked to quit will fail.
     * For example, the {@link Handler#sendMessage(Message)} method will return false.
     * </p><p class="note">
     * Using this method may be unsafe because some messages may not be delivered
     * before the looper terminates.  Consider using {@link #quitSafely} instead to ensure
     * that all pending work is completed in an orderly manner.
     * </p>
     *
     * @return True if the looper looper has been asked to quit or false if the
     * thread had not yet started running.
     *
     * @see #quitSafely
     */
    public boolean quit() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quit();
            return true;
        }
        return false;
    }

    /**
     * Quits the handler thread's looper safely.
     * <p>
     * Causes the handler thread's looper to terminate as soon as all remaining messages
     * in the message queue that are already due to be delivered have been handled.
     * Pending delayed messages with due times in the future will not be delivered.
     * </p><p>
     * Any attempt to post messages to the queue after the looper is asked to quit will fail.
     * For example, the {@link Handler#sendMessage(Message)} method will return false.
     * </p><p>
     * If the thread has not been started or has finished (that is if
     * {@link #getLooper} returns null), then false is returned.
     * Otherwise the looper is asked to quit and true is returned.
     * </p>
     *
     * @return True if the looper looper has been asked to quit or false if the
     * thread had not yet started running.
     */
    public boolean quitSafely() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quitSafely();
            return true;
        }
        return false;
    }

    /**
     * Returns the identifier of this thread. See Process.myTid().
     */
    public int getThreadId() {
        return mTid;
    }
}
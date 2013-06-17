package com.taobao.android.ski.gear;

import java.io.File;
import java.io.FileFilter;
import java.lang.Thread.State;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Pattern;

import android.os.AsyncTask;
import android.util.Log;

/**
 * A thread pool executor implementation optimized for concurrently scheduling mixture of blocking and non-blocking
 * tasks on SMP system.
 *
 * <p>SaturativeExecutor ensures new thread will only be started when some of the CPU cores are not fully occupied.
 * Otherwise tasks are queued instead to reduce the unnecessary context switches of threads.
 * 
 * @author Oasis
 */
public class SaturativeExecutor extends ThreadPoolExecutor {

	private static final int MAX_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;			// In seconds
    private static final int QUEUE_CAPACITY = 1024;
	private static final Pattern PATTERN_CPU_ENTRIES = Pattern.compile("cpu[0-9]+");

	private static final boolean DEBUG = true;

    @Override public void execute(Runnable command) {
    	super.execute(new CountedTask(command));
    }

    public static final boolean installAsDefaultAsyncTaskExecutor(ThreadPoolExecutor executor) {
    	try {
			Method setter = AsyncTask.class.getMethod("setDefaultExecutor", Executor.class);
			setter.setAccessible(true);
			setter.invoke(null, executor);
			return true;
		} catch (Exception e) {
			try {
				Field field = AsyncTask.class.getDeclaredField("sDefaultExecutor");	// Honeycomb and above
				field.setAccessible(true);
				field.set(null, executor);
				return true;
			} catch (Exception ex) {
				try {
					Field field = AsyncTask.class.getDeclaredField("sExecutor");	// Old versions
					field.setAccessible(true);
					field.set(null, executor);		// Only accept ThreadPoolExecutor
					return true;
				} catch (Exception exc) {
					Log.d(TAG, "Failed to install as default executor of AsyncTask.");
					return false;
				}
			}
		}
    }

    public SaturativeExecutor() {
		this(determineBestMinPoolSize());
	}

	public SaturativeExecutor(int minPoolSize) {
		super(minPoolSize, MAX_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
				mQueue = new SaturationAwareBlockingQueue<Runnable>(QUEUE_CAPACITY), sThreadFactory,
				new ThreadPoolExecutor.CallerRunsPolicy() /* TODO: Dump queue upon overflow */);
		((SaturationAwareBlockingQueue<Runnable>) getQueue()).setExecutor(this);
	}

    protected boolean isReallyUnsaturated() {
    	if (isSaturated()) return false;
    	// Spin for sure
    	LockSupport.parkNanos(10);
    	if (isSaturated()) {
        	if (DEBUG) System.out.println("*** Saturated after spin ***");
    		return false;
    	}
    	return true;
    }

    protected boolean isSaturated() {
		int core_size = getCorePoolSize();
    	int num_running = CountedTask.mNumRunning.get();
		int num_threads = mThreads.size();		// Safe to read the size unsynchronized
    	// Quick check for saturation: less running tasks than cores or threads
		if (num_running < core_size || num_running < num_threads) {
	    	if (DEBUG) System.out.println("Status: " + num_running + " running in " + num_threads + " threads, " + mQueue.size() + " queued...");
			return true;
		}

    	int num_busy = 0, num_idle = 0;
		synchronized(mThreads) {
			Iterator<Thread> threads = mThreads.iterator();
			while(threads.hasNext()) {
				Thread thread = threads.next();
				State state = thread.getState();
				if (state == State.RUNNABLE || state == State.NEW)
					num_busy ++;
				else if (state == State.TERMINATED)
					threads.remove();
				else if (DEBUG) {
					num_idle ++;
				}
			}
		}
		if (DEBUG) System.out.println("Status: " + num_busy + " busy & " + num_idle + " idle in " + num_threads + " threads, " + mQueue.size() + " queued...");
		return num_busy >= core_size;
	}
	
	protected static void collectThread(Thread thread) {
		synchronized(mThreads) {
			mThreads.add(thread);
		}
	}

	/** Prefer the number of cores, or double the number of processors otherwise */
	private static int determineBestMinPoolSize() {
		int cores = countCpuCores();
		if (DEBUG) System.out.println("CPU has " + cores + "cores.");
		return cores > 0 ? cores : 2 * Runtime.getRuntime().availableProcessors();
	}

	/** Get the number of cores available in this device, across all processors. */
	private static int countCpuCores() {
	    try {		// Count virtual CPU devices (cores)
	        File dir = new File("/sys/devices/system/cpu/");
	        File[] cpu_entries = dir.listFiles(new FileFilter() { @Override public boolean accept(File pathname) {
	            return PATTERN_CPU_ENTRIES.matcher(pathname.getName()).matches();
	        }});
	        return cpu_entries.length;
	    } catch(Exception e) {
	        //Log.e(TAG, "Cannot detect accurate number of CPU cores", e);
	        return 0;
	    }
	}

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {

        @Override public Thread newThread(Runnable r) {
            String name = "SaturativeThread #" + mCount.getAndIncrement();
			Thread thread = new Thread(r, name);
            if (DEBUG) System.out.println("Spawning " + name);
			collectThread(thread);
			return thread;
        }

    	private final AtomicInteger mCount = new AtomicInteger(1);
    };

    private static final HashSet<Thread> mThreads = new HashSet<Thread>();
	private static SaturationAwareBlockingQueue<Runnable> mQueue;
	static final String TAG = "SatuExec";

    protected static class SaturationAwareBlockingQueue<T> extends LinkedBlockingQueue<T> {

		public SaturationAwareBlockingQueue(int capacity) { super(capacity); }

		void setExecutor(SaturativeExecutor executor) { mExecutor = executor; }

		@Override public boolean add(T e) {
			if (mExecutor.isReallyUnsaturated()) throw new IllegalStateException("Unsaturated");
    		return super.add(e);
    	}
    	
    	@Override public boolean offer(T e) {
    		return mExecutor.isReallyUnsaturated() ? false: super.offer(e);
    	}

    	@Override public void put(T e) { throw new UnsupportedOperationException(); }
    	@Override public boolean offer(T e, long timeout, TimeUnit unit) { throw new UnsupportedOperationException(); }

		private SaturativeExecutor mExecutor;
    	private static final long serialVersionUID = 1L;
    }

    protected static class CountedTask implements Runnable {

		public CountedTask(Runnable runnable) { mRunnable = runnable; }
		@Override public void run() {
			mNumRunning.incrementAndGet();
			try { mRunnable.run(); }
			finally { mNumRunning.decrementAndGet(); }
		}

		Runnable mRunnable;
		static final AtomicInteger mNumRunning = new AtomicInteger();
    }
}
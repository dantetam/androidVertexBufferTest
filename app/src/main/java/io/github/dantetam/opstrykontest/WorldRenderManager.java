package io.github.dantetam.opstrykontest;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;

import java.net.URL;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dante on 9/26/2016.
 */
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This class creates pools of background threads for downloading
 * Picasa images from the web, based on URLs retrieved from Picasa's featured images RSS feed.
 * The class is implemented as a singleton; the only way to get an WorldRenderManager instance is to
 * call {@link #getInstance}.
 * <p>
 * The class sets the pool size and cache size based on the particular operation it's performing.
 * The algorithm doesn't apply to all situations, so if you re-use the code to implement a pool
 * of threads for your own app, you will have to come up with your choices for pool size, cache
 * size, and so forth. In many cases, you'll have to set some numbers arbitrarily and then
 * measure the impact on performance.
 * <p>
 * This class actually uses two threadpools in order to limit the number of
 * simultaneous image decoding threads to the number of available processor
 * cores.
 * <p>
 * Finally, this class defines a handler that communicates back to the UI
 * thread to change the bitmap to reflect the state.
 */
@SuppressWarnings("unused")
public class WorldRenderManager {

    // Sets the size of the storage that's used to cache images
    private static final int IMAGE_CACHE_SIZE = 1024 * 1024 * 4;

    // Sets the amount of time an idle thread will wait for a task before terminating
    private static final int KEEP_ALIVE_TIME = 1;

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    // Sets the initial threadpool size to 8
    private static final int CORE_POOL_SIZE = 8;

    // Sets the maximum threadpool size to 8
    private static final int MAXIMUM_POOL_SIZE = 8;

    /**
     * NOTE: This is the number of total available cores. On current versions of
     * Android, with devices that use plug-and-play cores, this will return less
     * than the total number of cores. The total number of cores is not
     * available in current Android implementations.
     */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    /*
     * Creates a cache of byte arrays indexed by image URLs. As new items are added to the
     * cache, the oldest items are ejected and subject to garbage collection.
     */
    private final LruCache<URL, byte[]> mPhotoCache;

    // A queue of Runnables for the image download pool
    private final BlockingQueue<Runnable> mDownloadWorkQueue;

    // A queue of WorldRenderManager tasks. Tasks are handed to a ThreadPool.
    private final Queue<WorldHandlerTask> mWorldHandlerTaskWorkQueue;

    // A managed pool of background download threads
    private final ThreadPoolExecutor mDownloadThreadPool;

    // An object that manages Messages in a Thread
    private Handler mHandler;

    // A single instance of WorldRenderManager, used to implement the singleton pattern
    private static WorldRenderManager sInstance = null;

    // A static block that sets class fields
    static {

        // The time unit for "keep alive" is in seconds
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        // Creates a single static instance of WorldRenderManager
        sInstance = new WorldRenderManager();
    }
    /**
     * Constructs the work queues and thread pools used to download and decode images.
     */
    private WorldRenderManager() {

        /*
         * Creates a work queue for the pool of Thread objects used for downloading, using a linked
         * list queue that blocks when the queue is empty.
         */
        mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();

        /*
         * Creates a work queue for the set of of task objects that control downloading and
         * decoding, using a linked list queue that blocks when the queue is empty.
         */
        mWorldHandlerTaskWorkQueue = new LinkedBlockingQueue<WorldHandlerTask>();

        /*
         * Creates a new pool of Thread objects for the download work queue
         */
        mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);

        // Instantiates a new cache based on the cache size estimate
        mPhotoCache = new LruCache<URL, byte[]>(IMAGE_CACHE_SIZE) {

            /*
             * This overrides the default sizeOf() implementation to return the
             * correct size of each cache entry.
             */

            @Override
            protected int sizeOf(URL paramURL, byte[] paramArrayOfByte) {
                return paramArrayOfByte.length;
            }
        };
        /*
         * Instantiates a new anonymous Handler object and defines its
         * handleMessage() method. The Handler *must* run on the UI thread, because it moves photo
         * Bitmaps from the WorldHandlerTask object to the View object.
         * To force the Handler to run on the UI thread, it's defined as part of the WorldRenderManager
         * constructor. The constructor is invoked when the class is first referenced, and that
         * happens when the View invokes startDownload. Since the View runs on the UI Thread, so
         * does the constructor and the Handler.
         */
        mHandler = new Handler(Looper.getMainLooper()) {

            /*
             * handleMessage() defines the operations to perform when the
             * Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {

                // Gets the image task from the incoming Message object.
                //WorldHandlerTask photoTask = (WorldHandlerTask) inputMessage.obj;

                // If this input view isn't null

                /*
                 * Chooses the action to take, based on the incoming message
                 */
                super.handleMessage(inputMessage);
            }
        };
    }

    /**
     * Returns the WorldRenderManager object
     * @return The global WorldRenderManager object
     */
    public static WorldRenderManager getInstance() {
        return sInstance;
    }

    /**
     * Cancels all Threads in the ThreadPool
     */
    public static void cancelAll() {
        /*
         * Creates an array of tasks that's the same size as the task work queue
         */
        WorldHandlerTask[] taskArray = new WorldHandlerTask[sInstance.mDownloadWorkQueue.size()];

        // Populates the array with the task objects in the queue
        sInstance.mDownloadWorkQueue.toArray(taskArray);

        // Stores the array length in order to iterate over the array
        int taskArraylen = taskArray.length;

        /*
         * Locks on the singleton to ensure that other processes aren't mutating Threads, then
         * iterates over the array of tasks and interrupts the task's current Thread.
         */
        synchronized (sInstance) {

            // Iterates over the array of tasks
            for (int taskArrayIndex = 0; taskArrayIndex < taskArraylen; taskArrayIndex++) {

                // Gets the task's current thread
                Thread thread = taskArray[taskArrayIndex].mThreadThis;

                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }

    /**
     * Stops a download Thread and removes it from the threadpool
     */
    static public void removeTask(WorldHandlerTask downloaderTask) {
        /*
         * Locks on this class to ensure that other processes aren't mutating Threads.
         */
        synchronized (sInstance) {

            // Gets the Thread that the downloader task is running on
            Thread thread = downloaderTask.mThreadThis;

            // If the Thread exists, posts an interrupt to it
            if (null != thread)
                thread.interrupt();
        }
        /*
         * Removes the download Runnable from the ThreadPool. This opens a Thread in the
         * ThreadPool's work queue, allowing a task in the queue to start.
         */
        sInstance.mDownloadThreadPool.remove(downloaderTask.mRunnable);
    }

    /**
     * Starts an image download and decode
     */
    static public WorldHandlerTask startTasks() {

        /*
         * Gets a task from the pool of tasks, returning null if the pool is empty
         */
        WorldHandlerTask downloadTask = sInstance.mWorldHandlerTaskWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == downloadTask) {
            downloadTask = new WorldHandlerTask();
        }

        // Initializes the task
        downloadTask.init(WorldRenderManager.sInstance);

        /*
         * Provides the download task with the cache buffer corresponding to the URL to be
         * downloaded.
         */
        sInstance.mDownloadThreadPool.execute(downloadTask.mRunnable);

        // Returns a task object, either newly-created or one from the task pool
        return downloadTask;
    }

    /**
     * Recycles tasks by calling their internal recycle() method and then putting them back into
     * the task queue.
     * @param downloadTask The task to recycle
     */
    void recycleTask(WorldHandlerTask downloadTask) {

        // Frees up memory in the task
        downloadTask.recycle();

        // Puts the task object back into the queue for re-use.
        mWorldHandlerTaskWorkQueue.offer(downloadTask);
    }
}


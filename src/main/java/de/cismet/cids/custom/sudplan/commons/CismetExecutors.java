/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.custom.sudplan.commons;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
// TODO: scheduled thread pool executor
public final class CismetExecutors {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CismetExecutors object.
     */
    private CismetExecutors() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   nThreads  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ExecutorService newFixedThreadPool(final int nThreads) {
        return new UEHThreadPoolExecutor(
                nThreads,
                nThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                Executors.defaultThreadFactory());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nThreads       DOCUMENT ME!
     * @param   threadFactory  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ExecutorService newFixedThreadPool(final int nThreads, final ThreadFactory threadFactory) {
        return new UEHThreadPoolExecutor(
                nThreads,
                nThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ExecutorService newSingleThreadExecutor() {
        return new DelegatedExecutorService(new UEHThreadPoolExecutor(
                    1,
                    1,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    Executors.defaultThreadFactory()));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   threadFactory  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ExecutorService newSingleThreadExecutor(final ThreadFactory threadFactory) {
        return new DelegatedExecutorService(new UEHThreadPoolExecutor(
                    1,
                    1,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    threadFactory));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ExecutorService newCachedThreadPool() {
        return new UEHThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                Executors.defaultThreadFactory());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   threadFactory  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ExecutorService newCachedThreadPool(final ThreadFactory threadFactory) {
        return new UEHThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                threadFactory);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class DelegatedExecutorService extends AbstractExecutorService {

        //~ Instance fields ----------------------------------------------------

        private final ExecutorService e;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DelegatedExecutorService object.
         *
         * @param  executor  DOCUMENT ME!
         */
        DelegatedExecutorService(final ExecutorService executor) {
            e = executor;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  command  DOCUMENT ME!
         */
        @Override
        public void execute(final Runnable command) {
            e.execute(command);
        }

        @Override
        public void shutdown() {
            e.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return e.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return e.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return e.isTerminated();
        }

        @Override
        public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
            return e.awaitTermination(timeout, unit);
        }

        @Override
        public Future<?> submit(final Runnable task) {
            return e.submit(task);
        }

        @Override
        public <T> Future<T> submit(final Callable<T> task) {
            return e.submit(task);
        }

        @Override
        public <T> Future<T> submit(final Runnable task, final T result) {
            return e.submit(task, result);
        }

        @Override
        public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> arg0) throws InterruptedException {
            return e.invokeAll(arg0);
        }

        @Override
        public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> arg0,
                final long arg1,
                final TimeUnit arg2) throws InterruptedException {
            return e.invokeAll(arg0, arg1, arg2);
        }

        @Override
        public <T> T invokeAny(final Collection<? extends Callable<T>> arg0) throws InterruptedException,
            ExecutionException {
            return e.invokeAny(arg0);
        }

        @Override
        public <T> T invokeAny(final Collection<? extends Callable<T>> arg0, final long arg1, final TimeUnit arg2)
                throws InterruptedException, ExecutionException, TimeoutException {
            return e.invokeAny(arg0, arg1, arg2);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class UEHThreadPoolExecutor extends ThreadPoolExecutor {

        //~ Instance fields ----------------------------------------------------

        private final transient Map<Runnable, Thread> runnableToThreadMap;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new UncaughtExcHandlerUsingThreadPool object.
         *
         * @param  corePoolSize     DOCUMENT ME!
         * @param  maximumPoolSize  DOCUMENT ME!
         * @param  keepAliveTime    DOCUMENT ME!
         * @param  unit             DOCUMENT ME!
         * @param  workQueue        DOCUMENT ME!
         * @param  threadFactory    DOCUMENT ME!
         */
        public UEHThreadPoolExecutor(final int corePoolSize,
                final int maximumPoolSize,
                final long keepAliveTime,
                final TimeUnit unit,
                final BlockingQueue<Runnable> workQueue,
                final ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);

            this.runnableToThreadMap = new HashMap<Runnable, Thread>();
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected void beforeExecute(final Thread t, final Runnable r) {
            super.beforeExecute(t, r);

            runnableToThreadMap.put(r, t);
        }

        @Override
        protected void afterExecute(final Runnable r, final Throwable t) {
            super.afterExecute(r, t);

            final Thread thread = runnableToThreadMap.remove(r);

            assert thread != null : "expected associated thread"; // NOI18N

            if ((t == null) && (r instanceof Future)) {
                Throwable thrown = null;
                try {
                    ((Future)r).get();
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException ex) {
                    thrown = ex.getCause();
                } catch (final Throwable tw) {
                    thrown = tw;
                }

                if (thrown != null) {
                    final Thread.UncaughtExceptionHandler handler = thread.getUncaughtExceptionHandler();
                    if (handler == null) {
                        final Thread.UncaughtExceptionHandler groupHandler = thread.getThreadGroup();
                        if (groupHandler == null) {
                            final Thread.UncaughtExceptionHandler defHandler = Thread
                                        .getDefaultUncaughtExceptionHandler();
                            if (defHandler != null) {
                                defHandler.uncaughtException(thread, thrown);
                            }
                        } else {
                            groupHandler.uncaughtException(thread, thrown);
                        }
                    } else {
                        handler.uncaughtException(thread, thrown);
                    }
                }
            }
        }
    }
}

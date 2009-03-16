package org.axiom.service;

import org.apache.camel.Consume;
import org.apache.camel.Header;
import static org.apache.commons.lang.StringUtils.*;
import org.axiom.integration.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Exposes an API for a channel used to indicate
 * shutdown/termination of an axiom control channel context.
 * <p>
 * A {@link ShutdownChannel} is always associated with an instance
 * of a {@link ControlChannel}, the lifecycle of which is bound to
 * the termination channel exposed via this object.
 * </p>
 */
public class ShutdownChannel {

    private static final int INITIAL_COUNT = 1;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final CountDownLatch latch;

    public ShutdownChannel() {
        this(new CountDownLatch(INITIAL_COUNT));
    }

    public ShutdownChannel(final CountDownLatch latch) {
        this.latch = latch;
    }

    /**
     * Callback/API method used internally by the managed (camel) control channels.
     * <b>Application code should not call this method under any circumstances.</b>
     * @param signal A signal header injected by the underlying (camel) message exchange.
     */
    @Consume(uri= Environment.TERMINATION_CHANNEL)
    public void onShutdown(@Header(name=Environment.SIGNAL) final String signal) {
        log.debug("Received {} signal.", signal);
        if (equalsIgnoreCase(Environment.SIG_TERMINATE, signal)) {
            latch.countDown();
        }
    }

    /**
     * Queries the status of this channel.
     * @return {@code true} if shutdown has already completed, otherwise {@code false}.
     */
    public boolean isShutdown() {
        return waitShutdown(0l);
    }

    /**
     * Blocks the calling thread until shutdown has completed, or the supplied
     * {@code timeout} has been exceeded.
     * @param timeout The time delay in milliseconds to block before aborting the wait and returning
     * @return {@code true} if {@link ShutdownChannel#isShutdown()} became true before the specified timeout was
     * exceeded, otherwise {@code false}.
     * @exception LifecycleException thrown if the calling thread is interrupted whilst waiting
     */
    public boolean waitShutdown(final long timeout) {
        try {
            log.info("Entering wait shutdown ({}ms timeout).", timeout);
            final boolean wasShutdown = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (!wasShutdown) {
                log.info("Wait Shutdown timed out after {}ms.", timeout);
            }
            return wasShutdown;
        } catch (InterruptedException e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Blocks the calling thread indefinitely until shutdown is complete.
     * @exception LifecycleException thrown if the calling thread is interrupted whilst waiting
     */
    public void waitShutdown() {
        try {
            log.info("Entering wait shutdown.");
            latch.await();
        } catch (InterruptedException e) {
            throw new LifecycleException(e.getLocalizedMessage(), e);
        }
    }
}

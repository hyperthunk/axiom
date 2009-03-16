package org.axiom.service;

import jdave.Block;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.apache.commons.lang.RandomStringUtils;
import org.axiom.SpecSupport;
import org.axiom.integration.Environment;
import static org.axiom.util.CollectionUtils.*;
import org.axiom.util.Computation;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(JDaveRunner.class)
public class ShutdownChannelSpec extends Specification<ShutdownChannel> {

    public class WhenConsumingTerminationSignals extends SpecSupport {

        @SuppressWarnings({"unchecked"})
        private CountDownLatch latch = new CountDownLatch(1);
        private ShutdownChannel channel;

        public ShutdownChannel create() {
            return channel = new ShutdownChannel(latch);
        }

        public void itShouldDecrementTheLatchIfTheTerminationSignalIsPassed() {
            channel.onShutdown(Environment.SIG_TERMINATE);
            specify(latch.getCount(), equal(0l));
        }

        public void itShouldNotDecrementTheLatchIfAnotherSignalIsPassed() {
            for (final String randomSignal : generateRandomSignals()) {
                channel.onShutdown(randomSignal);
                specify(latch.getCount(), equal(1l));
            }
        }

        public void itShouldWaitOnTheLatchWithTheSpecifiedTimeout() throws InterruptedException {
            latch = mock(CountDownLatch.class);
            channel = new ShutdownChannel(latch);

            final long timeout = 1000l;

            one(latch).await(timeout, TimeUnit.MILLISECONDS);
            will(returnValue(true));
            checking(this);

            specify(channel.waitShutdown(timeout), equal(true));
        }

        public void itShouldWaitOnTheLatchWithZeroTimeoutWhenNoneIsSpecified() throws InterruptedException {
            latch = mock(CountDownLatch.class);
            channel = new ShutdownChannel(latch);

            one(latch).await();
            checking(this);

            channel.waitShutdown();
        }

        public void itShouldQueryTheLatchOnDemand() throws InterruptedException {
            latch = mock(CountDownLatch.class);
            channel = new ShutdownChannel(latch);

            stubWaitZero();

            specify(channel.isShutdown(), equal(false));
        }

        public void itShouldWrapInterruptsInRuntimeExceptions() throws InterruptedException {
            latch = mock(CountDownLatch.class);
            channel = new ShutdownChannel(latch);

            @SuppressWarnings({"ThrowableInstanceNeverThrown"})
            final InterruptedException ex = new InterruptedException();

            allowing(latch).await(0l, TimeUnit.MILLISECONDS);
            will(throwException(ex));
            allowing(latch).await();
            will(throwException(ex));
            checking(this);

            specify(new Block() {
                @Override public void run() throws Throwable {
                    channel.waitShutdown(0l);
                }
            }, should.raise(LifecycleException.class));

            specify(new Block() {
                @Override public void run() throws Throwable {
                    channel.waitShutdown();
                }
            }, should.raise(LifecycleException.class));
        }

        private void stubWaitZero() throws InterruptedException {
            one(latch).await(0, TimeUnit.MILLISECONDS);
            will(returnValue(false));
            checking(this);
        }

        private Iterable<? extends String> generateRandomSignals() {
            return map(range(1, 100), new Computation<Integer, String>() {
                @Override public String apply(final Integer integer) {
                    return RandomStringUtils.random(integer, true, true);
                }
            });
        }
    }

}

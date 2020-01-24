package camel.kafka.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * this is used in embedded tests to evaluate errors
 * 
 */
public class EvaluateExceptionProcessor implements Processor, EvaluateException {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluateExceptionProcessor.class);

    private String lastBody;
    private Throwable lastException;
    private AtomicInteger numExceptions = new AtomicInteger(0);
    
    private CountDownLatch latchOne;
    private CountDownLatch latchTwo;
    private CountDownLatch latchThree;

    @Override
    public void process(Exchange exchange) throws Exception {
        numExceptions.incrementAndGet();
        this.lastBody = exchange.getMessage().getBody(String.class);
        this.lastException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        
        LOGGER.info("an exception was thrown {} with body {}", lastException.getMessage(), lastBody);
        
        if (latchOne != null) {
            latchOne.countDown();
        }
        
        if (latchTwo != null) {
            latchTwo.countDown();
        }
        
        if (latchThree != null) {
            latchThree.countDown();
        }
    }
    
    public void setCountDownOne(CountDownLatch latch) {
        this.latchOne = latch;
    }

    public void setCountDownTwo(CountDownLatch latch) {
        this.latchTwo = latch;
    }

    public void setCountDownThree(CountDownLatch latch) {
        this.latchThree = latch;
    }
    
    @Override
    public String getMessageBody() {
        return lastBody;
    }

    @Override
    public Throwable getException() {
        return lastException;
    }

    @Override
    public int numberOfExceptions() {
        return numExceptions.get();
    }

    
}

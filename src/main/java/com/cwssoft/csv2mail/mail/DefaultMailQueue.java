package com.cwssoft.csv2mail.mail;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author csyperski
 */
public class DefaultMailQueue implements MailQueue {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMailQueue.class);

    public static final int DEFAULT_MAX_QUEUE_SIZE = 50000;
   
    private final BlockingQueue<MailQueueTask> queue;

    public DefaultMailQueue() {
        this(DEFAULT_MAX_QUEUE_SIZE);
    }

    public DefaultMailQueue(int queueSize) {
        this.queue = new LinkedBlockingQueue(queueSize);
    }

    @Override
    public void addTask(MailQueueTask task) {
        synchronized (this) {
            logger.debug("Attempting to add email job: {}", task);
            if (task != null) {
                this.queue.add(task);
                logger.info("Enqueueing email task {}; new queue size: {}", task, this.queue.size());
            }
        }
    }

    /**
     * @return the queue
     */
    @Override
    public BlockingQueue<MailQueueTask> getQueue() {
        return queue;
    }

}

package com.cwssoft.csv2mail.mail;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author csyperski
 */
public class PriorityMailQueue implements MailQueue {

    protected static final Logger logger = LoggerFactory.getLogger(PriorityMailQueue.class);

    public static final int DEFAULT_INITIAL_SIZE = 11;

    private final Comparator<MailQueueTask> comparator = (MailQueueTask o1, MailQueueTask o2) -> {
        if (o1 == null && o2 == null) {
            return 0;
        }
        
        if (o1 == null) {
            return 1;
        }
        
        if (o2 == null) {
            return -1;
        }
        
        if (o1.getPriority() < o2.getPriority()) {
            return -1;
        } else if (o2.getPriority() > o1.getPriority()) {
            return 1;
        }
        return 0;
    };

    // this field holds the jobs to process
    // it just holds the customer id to process
    // it is blocking so the jobs should take this into account.
    private final PriorityBlockingQueue<MailQueueTask> queue;

    public PriorityMailQueue() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public PriorityMailQueue(int queueSize) {
        this.queue = new PriorityBlockingQueue(queueSize, comparator);
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

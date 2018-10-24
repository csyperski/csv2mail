package com.cwssoft.csv2mail.mail;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author csyperski
 */
public interface MailQueue extends Serializable {

    void addTask(MailQueueTask task);

    /**
     * @return the queue
     */
    BlockingQueue<MailQueueTask> getQueue();
    
}

package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BookPublishRequestManager {
    private Queue<BookPublishRequest> q;

    @Inject
    public BookPublishRequestManager() {
        // need to modify this when using threads later
        // linked lists are not thread safe!!
        // Anything that is not thread safe allows anything to access the linked list eg all
        // at once. leads to inconsistencies.
        // thread-safe = only one thread can access the object at a time.
        this.q = new ConcurrentLinkedQueue<>();
    }

    public void addBookPublishRequest(BookPublishRequest r) {

        q.offer(r);
    }

    public BookPublishRequest getBookPublishRequest() {
        //if (q == null || q.isEmpty()) {
        //    return null;
        //}
        return q.poll();
    }
}

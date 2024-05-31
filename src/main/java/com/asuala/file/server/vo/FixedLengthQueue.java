package com.asuala.file.server.vo;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FixedLengthQueue<T> {
    private int maxSize;
    private int size;
    private WebSocketSession[] queue;
    private Lock lock;

    public static FixedLengthQueue instance() {
        return new FixedLengthQueue<WebSocketSession>(200);
    }

    private FixedLengthQueue(int maxSize) {
        this.maxSize = maxSize;
        this.size = 0;
        this.queue = new WebSocketSession[maxSize];
        this.lock = new ReentrantLock();
    }

//    public static FixedLengthQueue getInstance() {
//        return instance;
//    }

    public void add(WebSocketSession element) {
        lock.lock();
        try {
            if (size == maxSize) {
                // 队列已满，移除最早添加的元素
                remove(0);
            }
            queue[size] = element;
            size++;
        } finally {
            lock.unlock();
        }
    }

    public void remove(int index) {
        lock.lock();
        try {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Invalid index");
            }
            for (int i = index; i < size - 1; i++) {
                queue[i] = queue[i + 1];
            }
            queue[size - 1] = null;
            size--;
        } finally {
            lock.unlock();
        }
    }

    public T get(int index) {
        lock.lock();
        try {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Invalid index");
            }
            return (T) queue[index];
        } finally {
            lock.unlock();
        }
    }

    public WebSocketSession[] list() {
        return queue;
    }

    public int size() {
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            for (WebSocketSession webSocketSession : queue) {
                webSocketSession.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
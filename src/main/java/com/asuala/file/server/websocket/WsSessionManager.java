package com.asuala.file.server.websocket;

import com.asuala.file.server.vo.FixedLengthQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author buhao
 * @version WsSessionManager.java, v 0.1 2019-10-22 10:24 buhao
 */
@Slf4j
public class WsSessionManager {
    /**
     * 保存连接 session 的地方
     */
    private static final ConcurrentHashMap<String, FixedLengthQueue> SESSION_POOL = new ConcurrentHashMap<>();

    /**
     * 添加 session
     *
     * @param key
     */
    public static void add(String key, WebSocketSession session) {
        // 添加 session
        if (SESSION_POOL.contains(key)) {
            SESSION_POOL.get(key).add(session);
        } else {
            FixedLengthQueue queue = FixedLengthQueue.instance();
            queue.add(session);
            SESSION_POOL.put(key, queue);
        }
    }

    /**
     * 删除 session,会返回删除的 session
     *
     * @param key
     * @return
     */
    public static FixedLengthQueue remove(String key) {
        // 删除 session
        return SESSION_POOL.remove(key);
    }

    /**
     * 删除并同步关闭连接
     *
     * @param key
     */
    public static void removeAndClose(String key) {
        FixedLengthQueue session = remove(key);
        if (session != null) {
            // 关闭连接
            session.clear();
        }
    }

    /**
     * 获得 session
     *
     * @param key
     * @return
     */
    public static FixedLengthQueue get(String key) {
        // 获得 session
        return SESSION_POOL.get(key);
    }
}
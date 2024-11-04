package qihang.service.impl;

import qihang.exception.SseException;
import qihang.service.SseService;
import qihang.session.SseSession;
import qihang.task.HeartBeatTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @description: TODO
 * @author: zhqihang
 * @date: 2024/11/04
 */
@Service

public class SseServiceImpl implements SseService {
    private static final Logger logger = LoggerFactory.getLogger(SseServiceImpl.class);

    /**
     * 发送心跳线程池
     */
    private static ScheduledExecutorService heartbeatExecutors = Executors.newScheduledThreadPool(2);

    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    /**
     * 新建连接
     *
     * @param clientId 客户端ID
     * @return
     */
    @Override
    public SseEmitter start(String clientId) {
        // 默认30秒超时,设置为0L则永不超时
        SseEmitter emitter = new SseEmitter(30_000L);

        // 加入Map中
        sseEmitters.put(clientId, emitter);

        logger.info("MSG: SseConnect | EmitterHash: {} | ID: {} | Date: {}", emitter.hashCode(), clientId, new Date());
        SseSession.add(clientId, emitter);
        final ScheduledFuture<?> future = heartbeatExecutors.scheduleAtFixedRate(new HeartBeatTask(clientId), 0, 10, TimeUnit.SECONDS);
        emitter.onCompletion(() -> {
            logger.info("MSG: SseConnectCompletion | EmitterHash: {} |ID: {} | Date: {}", emitter.hashCode(), clientId, new Date());
            SseSession.onCompletion(clientId, future);
        });
        emitter.onTimeout(() -> {
            logger.error("MSG: SseConnectTimeout | EmitterHash: {} |ID: {} | Date: {}", emitter.hashCode(), clientId, new Date());
            SseSession.onError(clientId, new SseException("TimeOut(clientId: " + clientId + ")"));
        });
        emitter.onError(t -> {
            logger.error("MSG: SseConnectError | EmitterHash: {} |ID: {} | Date: {}", emitter.hashCode(), clientId, new Date());
            SseSession.onError(clientId, new SseException("Error(clientId: " + clientId + ")"));
        });
        return emitter;
    }

    /**
     * 发送数据
     *
     * @param clientId 客户端ID
     * @return
     */
    @Override
    public String send(String clientId) {
        if (SseSession.send(clientId, System.currentTimeMillis())) {
            return "Succeed!";
        }
        return "error";

    }

    /**
     * 关闭连接
     *
     * @param clientId 客户端ID
     * @return
     */
    @Override
    public String close(String clientId) {
        logger.info("MSG: SseConnectClose | ID: {} | Date: {}", clientId, new Date());
        if (SseSession.del(clientId)) return "Succeed!";
        return "Error!";
    }

    /**
     * 发送消息
     * @param clientId
     * @param message
     * @return
     */
    @Override
    public String sendMessage(String clientId, String message) {
        SseEmitter emitter = sseEmitters.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(message);
                return "消息发送至客户端: " + clientId;
            } catch (IOException e) {
                emitter.completeWithError(e);
                sseEmitters.remove(clientId);
                return "消息发送至客户端: " + clientId + "，发送失败";
            }
        }
        return "没有对应的客户端连接: " + clientId;
    }
}

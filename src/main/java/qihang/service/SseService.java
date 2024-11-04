package qihang.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @description: SSE服务接口
 * @author: zhqihang
 * @date: 2024/11/04
 */
public interface SseService {

    /**
     * 新建连接
     *
     * @param clientId 客户端ID
     * @return
     */
    SseEmitter start(String clientId);

    /**
     * 发送数据
     *
     * @param clientId 客户端ID
     * @return
     */
    String send(String clientId);

    /**
     * 关闭连接
     *
     * @param clientId 客户端ID
     * @return
     */
    String close(String clientId);

    /**
     * 发送消息
     * @param clientId
     * @param message
     * @return
     */
    String sendMessage(String clientId, String message);
}

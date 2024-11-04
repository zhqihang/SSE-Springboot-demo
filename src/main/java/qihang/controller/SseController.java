package qihang.controller;

import qihang.service.SseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @description: SSE测试控制器
 * @author: zhqihang
 * @date: 2024/11/04
 */
@RestController
@RequestMapping("sse")
@RequiredArgsConstructor
public class SseController {

    private static final Logger logger = LoggerFactory.getLogger(SseController.class);

    private final SseService sseService;

    @RequestMapping("start")
    public SseEmitter start(@RequestParam String clientId) {
        return sseService.start(clientId);
    }

    /**
     * 将SseEmitter对象设置成完成
     *
     * @param clientId
     * @return
     */
    @RequestMapping("/end")
    public String close(String clientId) {
        return sseService.close(clientId);
    }


    /**
     * 向特定客户端发送消息
     *
     * @param clientId 客户端ID
     * @param message  消息内容
     * @return 发送状态
     */
    @PostMapping("/send")
    public String sendMessage(@RequestParam String clientId, @RequestParam String message) {
        // logger.info("Sending message to client {}: {}", clientId, message);
        return sseService.sendMessage(clientId, message);
    }
}

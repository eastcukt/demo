package com.east.demo.service.sse;

import cn.hutool.core.util.IdUtil;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SSEService {

    private final OkHttpClient okHttpClient;
    private EventSource eventSource;

    private static final Map<String,SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public SSEService(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public SseEmitter crateSse(String uid) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.onCompletion(() -> {
            log.info("[{}]结束链接" , uid);
            sseEmitterMap.remove(uid);
        });
        sseEmitter.onTimeout(() -> {
            log.info("[{}]链接超时",uid);
        });
        sseEmitter.onError(throwable -> {
            try{
                log.info("[{}]链接异常，{}",uid,throwable.toString());
                sseEmitter.send(SseEmitter.event()
                        .id(uid)
                        .name("发生异常")
                        .data("发生异常请重试")
                        .reconnectTime(3000));
                sseEmitterMap.put(uid,sseEmitter);
            }catch (IOException e){
                e.printStackTrace();
            }
        });
        try{
            sseEmitter.send(SseEmitter.event().reconnectTime(5000));
        }catch (IOException e){
            e.printStackTrace();
        }
        sseEmitterMap.put(uid,sseEmitter);
        log.info("[{}]创建sse连接成功!",uid);
        return sseEmitter;
    }

    public void sendTest() {
        SseEmitter sseEmitter = sseEmitterMap.get("1");
        if(sseEmitter == null){
            log.info("[{}]sse连接不存在", "1");
        }
        for (int i = 0; i < 10; i++) {
            String uuid = IdUtil.fastSimpleUUID();

            try {
                Thread.sleep(1000L);
                sseEmitter.send(SseEmitter.event().id("1").reconnectTime(60000).data(uuid));
                log.info("用户{},消息ID：{}，推送成功：{}", "1", i, uuid);
            } catch (Exception e) {
                sseEmitterMap.remove("1");
                log.info("用户{},消息ID：{}，推送失败：{}", "1", i, uuid);
                sseEmitter.complete();
            }
        }
        sseEmitter.complete();
    }

    public String connectToSse() {
        String sseServerUrl = "http://127.0.0.1:8089/sse/createSse?uid=1"; // 替换为实际的SSE服务URL

        Request request = new Request.Builder()
                .url(sseServerUrl)
                .build();

        EventSourceListener eventSourceListener = new EventSourceListener() {
            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                System.out.println("SSE连接已打开");
            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                System.out.println("接收到事件: " + (type != null ? type : "message"));
                System.out.println("事件ID: " + id);
                System.out.println("事件数据: " + data);

                // 根据事件类型进行不同处理
                if ("update".equals(type)) {
                    handleUpdateEvent(data);
                } else if ("notification".equals(type)) {
                    handleNotificationEvent(data);
                }
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                System.out.println("SSE连接已关闭");
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                System.err.println("SSE连接失败: " + (t != null ? t.getMessage() : "未知错误"));
            }
        };

        eventSource = EventSources.createFactory(okHttpClient)
                .newEventSource(request, eventSourceListener);

        return "已开始连接到SSE流";
    }

    private void handleUpdateEvent(String data) {
        // 处理更新类型的事件
        System.out.println("处理更新事件: " + data);
    }

    private void handleNotificationEvent(String data) {
        // 处理通知类型的事件
        System.out.println("处理通知事件: " + data);
    }

}
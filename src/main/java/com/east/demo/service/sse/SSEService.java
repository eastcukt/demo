package com.east.demo.service.sse;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SSEService {
    private static final Map<String,SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
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

    public boolean sendMessage(String uid,String messageId,String message){
        if(StrUtil.isEmpty(message)){
            log.info("[{}]参数异常，msg为空",uid);
            return false;
        }
        SseEmitter sseEmitter = sseEmitterMap.get(uid);
        if(sseEmitter == null){
            log.info("[{}]sse连接不存在",uid);
            return  false;
        }
        try{
            sseEmitter.send(SseEmitter.event().id(messageId).reconnectTime(60000).data(message));
            log.info("用户{},消息ID：{}，推送成功：{}",uid,messageId,message);
            return true;
        }catch (IOException e){
            sseEmitterMap.remove(uid);
            log.info("用户{},消息ID：{}，消息推送失败：{}",uid,messageId,message);
            sseEmitter.complete();
            return false;
        }
    }

    public void closeSse(String uid){
        if(sseEmitterMap.containsKey(uid)){
            SseEmitter sseEmitter = sseEmitterMap.get(uid);
            sseEmitter.complete();
            sseEmitterMap.remove(uid);
        }else {
            log.info("用户{}连接已关闭",uid);
        }
    }

}
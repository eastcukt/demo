package com.east.demo.controller.sse;

import cn.hutool.core.util.IdUtil;
import com.east.demo.service.sse.SSEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/**
 * 参考：https://www.cnblogs.com/tm2015/p/18525323
 */
@Controller
@RequestMapping("sse")
public class SseController {
    @Autowired
    private SSEService sseService;


    @GetMapping("createSse")
    @CrossOrigin
    public SseEmitter createSse(String uid)
    {
        return sseService.crateSse(uid);
    }

    @GetMapping("sendMsg")
    @ResponseBody
    @CrossOrigin
    public String sseChat(String uid){
        for (int i = 0; i < 10; i++) {
            sseService.sendMessage(uid,"消息"+i,IdUtil.fastUUID().replace("-",""));
        }
        return "OK";
    }


    @GetMapping("closeSse")
    @CrossOrigin
    public void closeSse(String uid){
        sseService.closeSse(uid);
    }
}
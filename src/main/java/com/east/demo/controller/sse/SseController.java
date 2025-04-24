package com.east.demo.controller.sse;

import com.east.demo.service.sse.SSEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
        sseService.sendTest();
        return "OK";
    }

    @GetMapping("/connect-sse")
    public String connectToSse() {
        sseService.connectToSse();
        return "OK";
    }
}
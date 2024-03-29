package com.github.balchua.temporaldemosink.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sink/api/v1/")
@Slf4j
public class SinkController {

    @Value("${delay}")
    private int delay;


    @GetMapping("finalAction")
    public String finalAction() {
        log.info("calling the final Action");

        sleep(delay);
        return "finalAction done";
    }

    private void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            log.warn("Nothing to worry about");
        }
    }
}

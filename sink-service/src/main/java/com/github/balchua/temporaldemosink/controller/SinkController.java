package com.github.balchua.temporaldemosink.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sink/api/v1/")
@Slf4j
public class SinkController {


    @GetMapping("finalAction")
    public String finalAction() {
        log.info("calling the final Action");

        return "finalAction done";
    }
}

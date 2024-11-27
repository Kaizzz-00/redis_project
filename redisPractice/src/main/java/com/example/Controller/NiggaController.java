package com.example.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kyle.zheng
 * @date 2024/11/27
 */
@RestController
public class NiggaController {
    @GetMapping("/")
    public String index() {
        return "Hello World";
    }
}

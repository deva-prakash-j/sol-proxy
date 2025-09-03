package com.proxy.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proxy.client.SolscanClient;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/proxy")
@RequiredArgsConstructor
public class ProxyController {

    private final SolscanClient solscanClient;

    @GetMapping("/token/holders")
    public String getTokenHolders(@RequestParam String address,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String fromAmount,
            @RequestParam(required = false) String toAmount) {
        return solscanClient.getTokenHolders(address, page, pageSize, fromAmount, toAmount);
    }
    
}

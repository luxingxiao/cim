package com.biyao.test.controller;

import com.crossoverjie.cim.common.enums.StatusEnum;
import com.crossoverjie.cim.common.res.BaseResponse;
import com.crossoverjie.cim.common.res.NULLBody;
import com.crossoverjie.cim.route.api.vo.res.RegisterInfoResVO;
import com.crossoverjie.cim.route.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/by")
public class AccountsInitController {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountsInitController.class);
    private final RedisTemplate redisTemplate;
    private final AccountService accountService;

    public AccountsInitController(RedisTemplate redisTemplate, AccountService accountService) {
        this.redisTemplate = redisTemplate;
        this.accountService = accountService;
    }

    @PostMapping("/initAccounts")
    public BaseResponse<NULLBody> initAccounts() throws Exception {
        BaseResponse<NULLBody> res = new BaseResponse();

        LOGGER.info("开始初始化账户");

        for (int i = 100000; i < 200001; i++) {
            long userId = i;
            RegisterInfoResVO info = new RegisterInfoResVO(userId, "test" + i);
            info = accountService.register(info);
            LOGGER.info("用户{}注册完成", info.getUserName());
        }

        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }
}

package com.jordan.ban.market;

import com.jordan.ban.domain.AccountDto;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class TradeContext {

    private Map<String, AccountDto> accountDtoMap = new ConcurrentHashMap<>();


    public void putAccount(String key, AccountDto account) {
        accountDtoMap.put(key, account);
    }

    public AccountDto getAccount(String key) {
        return accountDtoMap.get(key);
    }

    public void removeAccount(String key) {
        this.accountDtoMap.remove(key);
    }

    public void clear(){
        this.accountDtoMap.clear();
    }
}

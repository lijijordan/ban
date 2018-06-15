package com.jordan.ban.service;


import com.jordan.ban.dao.PlatformRepository;
import com.jordan.ban.entity.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlatformService {


    @Autowired
    private PlatformRepository platformRepository;

    public Platform createPlatform(String name) {
        Platform platform = this.platformRepository.findByName(name);
        if (platform != null) {
            return platform;
        } else {
            platform = Platform.builder().name(name).build();
            return this.platformRepository.save(platform);
        }
    }
    

}

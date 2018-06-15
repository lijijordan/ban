package com.jordan.ban.dao;

import com.jordan.ban.entity.Order;
import com.jordan.ban.entity.Platform;
import org.springframework.data.repository.CrudRepository;

public interface PlatformRepository extends CrudRepository<Platform, Long> {

    Platform findByName(String name);
}

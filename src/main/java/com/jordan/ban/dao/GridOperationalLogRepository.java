package com.jordan.ban.dao;

import com.jordan.ban.entity.GridOperationalLog;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GridOperationalLogRepository extends CrudRepository<GridOperationalLog, Long> {

    public List<GridOperationalLog> findAllByGridId(Long gridId);

}

package com.jordan.ban.service;

import com.jordan.ban.dao.GridRepository;
import com.jordan.ban.entity.Grid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.jordan.ban.common.Constant.ETH_USDT;

@Service
public class AdminService {

    @Autowired
    private GridRepository gridRepository;

    public List<Grid> listGrids() {
        return this.gridRepository.findAllBySymbol(ETH_USDT);
    }
}

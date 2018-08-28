package com.jordan.ban.service;

import com.jordan.ban.dao.GridOperationalLogRepository;
import com.jordan.ban.dao.GridRepository;
import com.jordan.ban.dao.WareHouseRepository;
import com.jordan.ban.domain.out.GridDto;
import com.jordan.ban.entity.Grid;
import com.jordan.ban.entity.GridOperationalLog;
import com.jordan.ban.entity.WareHouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.jordan.ban.common.Constant.ETH_USDT;

@Service
public class AdminService {

    @Autowired
    private GridRepository gridRepository;

    @Autowired
    private WareHouseRepository wareHouseRepository;


    @Autowired
    private GridOperationalLogRepository gridOperationalLogRepository;

    public List<Grid> listGrids() {
        return this.gridRepository.findAllBySymbol(ETH_USDT);
    }


    public List<WareHouse> wareHouses(Long gridId) {
        return this.wareHouseRepository.findAllByGridId(gridId);
    }

    public List<GridOperationalLog> gridOperationalLogs(Long gridId) {
        return this.gridOperationalLogRepository.findAllByGridId(gridId);
    }


    public GridDto fetchGridData(Long gridId) {
        return GridDto.builder().gridId(gridId).grids(listGrids())
                .gridOperationalLogs(gridOperationalLogs(gridId)).wareHouses(wareHouses(gridId)).build();
    }
}

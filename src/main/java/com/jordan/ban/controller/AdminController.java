package com.jordan.ban.controller;


import com.jordan.ban.domain.out.GridDto;
import com.jordan.ban.entity.Grid;
import com.jordan.ban.entity.GridOperationalLog;
import com.jordan.ban.entity.WareHouse;
import com.jordan.ban.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;


    @GetMapping("/index/grid/{gridId}")
    @ResponseBody
    public GridDto index(@PathVariable Long gridId) {
        return this.adminService.fetchGridData(gridId);
    }


    @GetMapping("/grids")
    @ResponseBody
    public List<Grid> grids() {
        return this.adminService.listGrids();
    }


    @GetMapping("/warehouse/{gridId}")
    @ResponseBody
    public List<WareHouse> warehouse(Long gridId) {
        return this.adminService.wareHouses(gridId);
    }

    @GetMapping("/gridOperationalLog/{gridId}")
    @ResponseBody
    public List<GridOperationalLog> gridOperationalLogs(Long gridId) {
        return this.adminService.gridOperationalLogs(gridId);
    }
}

package com.jordan.ban.domain.out;

import com.jordan.ban.entity.Grid;
import com.jordan.ban.entity.GridOperationalLog;
import com.jordan.ban.entity.WareHouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridDto {

    private Long gridId;

    private List<WareHouse> wareHouses;

    private List<GridOperationalLog> gridOperationalLogs;

    private List<Grid> grids;
}

package com.jordan.ban.domain;


import com.jordan.ban.entity.Grid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridMatch {

    private Grid grid;

    private double matchResult;

    private boolean isMatch;
}

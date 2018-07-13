package com.jordan.ban.domain.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Greeting {

    private double moveMetrics;
    private double moveBackMetrics;

    private double upMax;

    private double downMax;

    private float avgFloatPercent;

}
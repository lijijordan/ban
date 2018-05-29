package com.jordan.ban.domain;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BuyStack {

    private List<Sell> buyList;
}

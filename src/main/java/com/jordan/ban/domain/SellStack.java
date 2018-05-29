package com.jordan.ban.domain;


import lombok.Data;

import java.lang.ref.PhantomReference;
import java.util.Date;
import java.util.List;

@Data
public class SellStack {

    private List<Sell> sellList;

}

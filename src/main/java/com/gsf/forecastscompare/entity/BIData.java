package com.gsf.forecastscompare.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class BIData {

    private long id;
    private Date date;
    private Double value;
    private Double forecastValue;


}

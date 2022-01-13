package com.gsf.forecastscompare.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DataResult {

    private String dateFormatted;
    private Double rawValue;
    private Double biValue;
    private Double gmValue;
    private Double gmValuePE;
    private Double biValuePE;
    private Double gmValueMape;
    private Double biValueMape;


}

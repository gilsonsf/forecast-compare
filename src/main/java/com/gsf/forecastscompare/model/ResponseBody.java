package com.gsf.forecastscompare.model;

import java.util.ArrayList;
import java.util.List;

public class ResponseBody {

    List<List<Object>> chartDataPrediction = new ArrayList<>();
    List<List<Object>> chartDataPE = new ArrayList<>();

    private String gmMapeResult;
    private String biMapeResult;

    public String getGmMapeResult() {
        return gmMapeResult;
    }

    public void setGmMapeResult(String gmMapeResult) {
        this.gmMapeResult = gmMapeResult;
    }

    public String getBiMapeResult() {
        return biMapeResult;
    }

    public void setBiMapeResult(String biMapeResult) {
        this.biMapeResult = biMapeResult;
    }

    public List<List<Object>> getChartDataPrediction() {
        return chartDataPrediction;
    }

    public List<List<Object>> getChartDataPE() {
        return chartDataPE;
    }

    public void setChartDataPE(List<List<Object>> chartDataPE) {
        this.chartDataPE = chartDataPE;
    }

    public void setChartDataPrediction(List<List<Object>> chartDataPrediction) {
        this.chartDataPrediction = chartDataPrediction;
    }
}

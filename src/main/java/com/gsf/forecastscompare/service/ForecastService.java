package com.gsf.forecastscompare.service;

import com.gsf.forecastscompare.entity.BIData;
import com.gsf.forecastscompare.entity.DataResult;
import com.gsf.forecastscompare.entity.RawData;
import com.gsf.forecastscompare.utils.DateFormatter;
import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastService {

    public List<RawData> readExcel(MultipartFile file) throws IOException {

        List result = new ArrayList<RawData>();
        Map<Integer, List<String>> data = new HashMap<>();
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        int count = 1;
        for (Row row : sheet) {
            data.put(count, new ArrayList<String>());
            for (Cell cell : row) {
                String[] value = cell.getRichStringCellValue().getString().split(";");
                RawData dt = new RawData(count, DateFormatter.formatterRawFileDate(value[0]), Double.parseDouble(value[1]));
                result.add(dt);
            }
            count++;
        }
        if (workbook != null){
            workbook.close();
        }
        return result;
    }

    public List<RawData> readCSV(MultipartFile file) throws IOException {

        List result = new ArrayList<RawData>();
        Map<Integer, List<String>> data = new HashMap<>();
        int count = 1;

        CSVParser parser = CSVParser.parse(file.getInputStream(), Charset.defaultCharset(), CSVFormat.DEFAULT);
        List<CSVRecord> list = parser.getRecords();
        for (CSVRecord record : list) {
            data.put(count, new ArrayList<String>());

            for (String str : record) {
                String[] value = str.split(";");
                RawData dt = new RawData(count, DateFormatter.formatterRawFileDate(value[0]), Double.parseDouble(value[1]));
                result.add(dt);
            }
            count++;
        }
        parser.close();

        return result;
    }

    public List<BIData> readCsvBI(MultipartFile file) throws IOException {

        List result = new ArrayList<BIData>();
        Map<Integer, List<String>> data = new HashMap<>();
        int count = 0;

        CSVParser parser = CSVParser.parse(file.getInputStream(), Charset.defaultCharset(), CSVFormat.DEFAULT);
        List<CSVRecord> list = parser.getRecords();
        for (CSVRecord record : list) {
            //skip first line
            if (count == 0) {
                count++;
                continue;
            }
            data.put(count, new ArrayList<String>());

            if (!"".equalsIgnoreCase(record.get(2))) {
                BIData dt = new BIData(count, DateFormatter.formatterBIFileDate(record.get(0)),
                        Double.parseDouble(record.get(1)),
                        Double.parseDouble(record.get(2)));
                result.add(dt);
            }

            count++;
        }
        parser.close();

        return result;
    }

    public void execMatLab()  {
//        MatlabEngine matEng = null;
//        try {
//            matEng = MatlabEngine.startMatlab();
//            matEng.eval("syms x;", null, null);
//            matEng.eval("f = sin(x)/x", null, null);
//            StringWriter output = new StringWriter();
//            matEng.eval("limit(f,x,0)", output, null);
//            System.out.println(output.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        double[] A = {1.5, 2.1, 3.3, 4.6, 5.7};
        int m = 30;
        int n = A.length;
        double[] B = cumsum(A);


        double[] C = new double[]{A.length};

        for (int i = 1; i<n; i++) {
            C[i] = (B[i] + B[i - 1])/2;
        }
        //C[0] = [];

//        Construct the data matrix
//        B = [-C;ones(1,n-1)];
//        Y = A; Y(1) = []; Y = Y';

        //B = {-C; ones(1, n-1)};


    }

    // method to find cumulative sum of array
    public double[] cumsum(double[] numbers) {
        // variable
        int sum = 0;

        // traverse through the array
        for (int i = 0; i < numbers.length; i++) {
            sum += numbers[i]; // find sum
            numbers[i] = sum; // replace
        }

        // return
        return numbers;
    }

  public static void main(String[] args) {
      new ForecastService().execMatLab();
  }

    public List<DataResult> createList(List<RawData> listRawData, List<BIData> listBI, List<RawData> listGM) {
        List<DataResult> result = new ArrayList<>();

    listBI.forEach(
        value -> {
          DataResult dataResult = new DataResult();

          dataResult.setDateFormatted(DateFormatter.dateFormatted(value.getDate()));
          dataResult.setBiValue(value.getForecastValue());
          dataResult.setGmValue(value.getForecastValue() + 6);

          RawData rawData = getRawValueFromDate(listRawData, value.getDate());

          dataResult.setRawValue(rawData.getValue());

          Double peBI = (value.getForecastValue() - rawData.getValue()) / value.getForecastValue();
          dataResult.setBiValuePE(peBI);

          Double mapeBI = Math.abs((rawData.getValue() - value.getForecastValue()) / rawData.getValue());
          dataResult.setBiValueMape(mapeBI);


          Double peGM = ((value.getForecastValue() - rawData.getValue()) / value.getForecastValue()) + 6;
          dataResult.setGmValuePE(peGM);

          Double mapeGE = Math.abs((rawData.getValue() - value.getForecastValue()) / rawData.getValue() + 6);
          dataResult.setGmValueMape(mapeGE);

          result.add(dataResult);
        });

    return result;
    }

    private RawData getRawValueFromDate(List<RawData> listRawData, Date date) {
        return listRawData.stream()
                .filter(value -> DateFormatter.dateFormatted(value.getDate())
                        .equals(DateFormatter.dateFormatted(date)))
                .findFirst()
                .orElse(null);

    }

    public String calculeMape(List<DataResult> listResult, String option) {

        Double sum = 0d;
        if("bi".equalsIgnoreCase(option)) {
            for (DataResult r : listResult) {
                sum = sum+r.getBiValueMape();
            }
        } else {
            for (DataResult r : listResult) {
                sum = sum+r.getGmValueMape();
            }
        }

        if (sum < 10) {
            return sum + " High forecasting";
        } else if (sum >=10 && sum < 20) {
            return sum + " Good forecasting";
        } else if (sum >=20 && sum <= 50) {
            return sum + " Reasonable forecasting";
        } else {
            return sum + " Weak forecasting";
        }
    }

}

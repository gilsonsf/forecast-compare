package com.gsf.forecastscompare.service;

import com.gsf.forecastscompare.entity.BIData;
import com.gsf.forecastscompare.entity.DataResult;
import com.gsf.forecastscompare.entity.RawData;
import com.gsf.forecastscompare.utils.DateFormatter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ejml.simple.SimpleMatrix;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.*;

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
                RawData dt = new RawData(count, DateFormatter.formatterRawFileDate(value[0]), Double.parseDouble(value[1]), null);
                result.add(dt);
            }
            count++;
        }
        if (workbook != null) {
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
                RawData dt = new RawData(count, DateFormatter.formatterRawFileDate(value[0]), Double.parseDouble(value[1]), null);
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

    public List<RawData> executaPrevisaoGM(List<RawData> h1) {
        List<RawData> result = new ArrayList<>();
        double[] data = new double[h1.size()];
        int i = 0;
        for (RawData r : h1) {
            result.add(r);
            data[i] = r.getValue();
            i++;
        }

        double[] previsoes = executePrevisaoGM(data);


        i=0;
        for (RawData r : result) {
            result.get(i).setForecastValue(previsoes[i]);
            i++;
        }

        return result;
    }

    private double[] executePrevisaoGM(double[] data) {
        double[][] h1 = new double[][]{data};
        double a, b;

        SimpleMatrix A = new SimpleMatrix(h1).transpose();

        int m = 30;
        int n = A.getDDRM().getData().length;
        double[] B_ = cumsum(A.getDDRM().getData());

        double[] C_ = new double[n];

        for (int i = 1; i < n; i++) { //primeira posição é zero
            C_[i] = (B_[i] + B_[i - 1]) / 2;
        }

        C_ = removeElement(C_, 0);

        double[][] B1 = new double[2][C_.length];

        for (int j = 0; j < C_.length; j++) {
            B1[0][j] = C_[j] * -1;
            B1[1][j] = 1;
        }

        SimpleMatrix B = new SimpleMatrix(B1);

        double[] Y_data = removeElement(data, 0);
        SimpleMatrix Y = new SimpleMatrix(new double[][]{Y_data}).transpose();

        SimpleMatrix c = calculate(B, Y);
        a = c.get(0,0);
        b = c.get(0,1);
//        a = -0.00052;
//        b = 4.329608647;

        double[] F = new double[n + m];
        double[] A_data = A.getDDRM().getData();
        F[0] = A_data[0];

        for (int i = 0; i < n + m; i++) {
            double value = (A_data[0] - (b / a)) / Math.exp(a * (i)) + b / a;
            F[i] = formatCasasDecimais(value);
        }

        double[] G = new double[m];
        G[0] = A_data[0];

        for (int i = 1; i < m; i++) {
            double value = F[i] - F[i - 1];
            G[i] = formatCasasDecimais(value);
        }

//      disp('The forecast data is:');
        for (double value : G) {
            System.out.println(value);
        }



//      TESTE ULTIMOS 31
        int m_test = m+1;
        double[] G_TESTE = new double[m_test];
        int start = A_data.length - m_test;
        G_TESTE[0] = A_data[start];


        for (int i = 1; i < m_test; i++) {
            double value = F[start + i] - F[(start + i) - 1];
            G_TESTE[i] = formatCasasDecimais(value);
        }

        for (double value : G_TESTE) {
            System.out.println(value);
        }

        //teste 92 itens
        double[] G_92 = new double[n];
        G_92[0] = A_data[0];

        for (int i = 1; i < n; i++) {
            double value = F[i] - F[i - 1];
            G_92[i] = formatCasasDecimais(value);
        }

        for (double value : G_92) {
            System.out.println(value);
        }

        return G_92;
    }

    public static double[] cumsum(double[] numbers) {
        double sum = 0;
        double[] result = new double[numbers.length];

        for (int i = 0; i < numbers.length; i++) {
            sum += numbers[i];
            result[i] = sum;
        }
        return result;
    }

    private static SimpleMatrix calculate(SimpleMatrix B, SimpleMatrix Y) {

        SimpleMatrix B2 = B.copy();
        B2 = B2.transpose();

        SimpleMatrix c = B.mult(B2).invert().mult(B).mult(Y);
        c = c.transpose();

        //c.print();

        return c;
    }


    public static void main(String[] args) {
        double[] data = new double[]{2.54, 2.45, 2.23, 15.65, 2.22, 1.87, 5.63,
                2.38, 2.49, 3.76, 2.39, 6.55,
                2.42, 2.51, 2.63, 2.43, 4, 11.12, 2.91, 2.25,
                5.29, 3.38, 3.04, 3.39, 2.66, 2.8, 2.8, 8.26,
                3.46, 2.38, 3.26, 3.29, 2.61, 4.55, 2.62, 2.94,
                11.89, 4.88, 3.34, 2.86, 2.98, 3.41, 3.11, 30.3,
                2.74, 4.07, 2.59, 3.37, 2.68, 4.36, 2.51, 5.27,
                2.97, 4.62, 2.41, 6.85, 4.57, 4.07,
                6.36, 6.74, 2.27, 9.5, 6.93, 2.93, 4.74, 2.62,
                3.59, 6.37, 2.93, 3.62, 16.49, 5.85,
                2.36, 3.66, 4.81, 3.63, 3.17, 3.18,
                3.8, 3.44, 2.78, 3.5, 3.03, 2.98, 3.21,
                3.48, 2.74, 3.55, 3.8, 3.29, 7.57, 4.13};

        new ForecastService().executePrevisaoGM(data);

    }

    private static double formatCasasDecimais(double value) {

        DecimalFormat df = new DecimalFormat("#.#########");

        Double aDouble = Double.valueOf(df.format(value)
                .replace(",", "."));

        return aDouble.doubleValue();

    }

    public List<DataResult> createList(List<RawData> listRawData, List<BIData> listBI, List<RawData> listGM) {
        List<DataResult> result = new ArrayList<>();

        listBI.forEach(
                biData -> {
                    DataResult dataResult = new DataResult();

                    dataResult.setDateFormatted(DateFormatter.dateFormatted(biData.getDate()));
                    dataResult.setBiValue(biData.getForecastValue());

                    RawData gmData = getRawValueFromDate(listGM, biData.getDate());
                    dataResult.setGmValue(gmData.getForecastValue());

                    RawData rawData = getRawValueFromDate(listRawData, biData.getDate());

                    dataResult.setRawValue(rawData.getValue());

                    Double peBI = (biData.getForecastValue() - rawData.getValue()) / biData.getForecastValue();
                    dataResult.setBiValuePE(peBI);

                    Double mapeBI = Math.abs((rawData.getValue() - biData.getForecastValue()) / rawData.getValue());
                    dataResult.setBiValueMape(mapeBI);


                    Double peGM = ((gmData.getForecastValue() - rawData.getValue()) / gmData.getForecastValue());
                    dataResult.setGmValuePE(peGM);

                    Double mapeGE = Math.abs((rawData.getValue() - gmData.getForecastValue()) / rawData.getValue());
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
        if ("bi".equalsIgnoreCase(option)) {
            for (DataResult r : listResult) {
                sum = sum + r.getBiValueMape();
            }
        } else {
            for (DataResult r : listResult) {
                sum = sum + r.getGmValueMape();
            }
        }

        if (sum < 10) {
            return sum + " High forecasting";
        } else if (sum >= 10 && sum < 20) {
            return sum + " Good forecasting";
        } else if (sum >= 20 && sum <= 50) {
            return sum + " Reasonable forecasting";
        } else {
            return sum + " Weak forecasting";
        }
    }

    public static double[] removeElement(double[] arr, int index) {

        if (arr == null || index < 0 || index >= arr.length) {
            return arr;
        }

        double[] anotherArray = new double[arr.length - 1];

        for (int i = 0, k = 0; i < arr.length; i++) {
            if (i == index) {
                continue;
            }
            anotherArray[k++] = arr[i];
        }
        return anotherArray;
    }

}

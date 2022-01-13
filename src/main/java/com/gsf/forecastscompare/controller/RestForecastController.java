package com.gsf.forecastscompare.controller;

import com.gsf.forecastscompare.entity.BIData;
import com.gsf.forecastscompare.entity.DataResult;
import com.gsf.forecastscompare.entity.RawData;
import com.gsf.forecastscompare.model.AjaxResponseBody;
import com.gsf.forecastscompare.model.ResponseBody;
import com.gsf.forecastscompare.model.SearchCriteria;
import com.gsf.forecastscompare.model.User;
import com.gsf.forecastscompare.service.ForecastService;
import com.gsf.forecastscompare.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RestForecastController {

    private final Logger logger = LoggerFactory.getLogger(RestForecastController.class);

    @Autowired
    private ForecastService forecastService;

    private  List<RawData> listRawData;
    private  List<BIData> listBI;
    private  List<RawData> listGM;
    private  List<DataResult> listResult;

    private final String UPLOAD_DIR = "./uploads/";

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/api/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return new ResponseEntity("please select a file!", HttpStatus.OK);
        }

        // normalize the file path
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // save the file on the local file system
        try {

            //forecastService.execMatLab();
            //this.list = forecastService.readExcel(file);
            this.listRawData = forecastService.readCSV(file);
            System.out.println(listRawData);

            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return success response
        return new ResponseEntity("You successfully uploaded " +
                file.getOriginalFilename(), new HttpHeaders(), HttpStatus.OK);

    }

    @PostMapping("/api/upload/bi")
    public ResponseEntity<?> uploadFileBI(
            @RequestParam("fileBI") MultipartFile file) {

        if (file.isEmpty()) {
            return new ResponseEntity("please select a file!", HttpStatus.OK);
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {

            this.listBI = forecastService.readCsvBI(file);

            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity("You successfully uploaded BI file" +
                file.getOriginalFilename(), new HttpHeaders(), HttpStatus.OK);

    }

    @PostMapping("/api/search")
    public ResponseEntity<?> getSearchResultViaAjax(@RequestBody SearchCriteria search, Errors errors) {

        AjaxResponseBody result = new AjaxResponseBody();

        //If error, just return a 400 bad request, along with the error message
        if (errors.hasErrors()) {

            result.setMsg(errors.getAllErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(",")));
            return ResponseEntity.badRequest().body(result);

        }

        List<User> users = userService.findByUserNameOrEmail(search.getUsername());
        if (users.isEmpty()) {
            result.setMsg("no user found!");
        } else {
            result.setMsg("success");
        }
        result.setResult(users);

        return ResponseEntity.ok(result);

    }

    @PostMapping("/api/chart")
    public  ResponseEntity<?> loadChart(@RequestBody SearchCriteria search, Errors errors) {
        ResponseBody responseBody = new ResponseBody();
        List<List<Object>> chartDataPrediction = getChartDataPrediction();
        List<List<Object>> chartDataPE = getChartDataPE();

        responseBody.setChartDataPrediction(chartDataPrediction);
        responseBody.setChartDataPE(chartDataPE);

        String biMapeResult = forecastService.calculeMape(listResult, "bi");
        String gmMapeResult = forecastService.calculeMape(listResult, "gm");

        responseBody.setBiMapeResult("Valor BI " + biMapeResult);
        responseBody.setGmMapeResult("Valor GM " + gmMapeResult);

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body("erros encontrados");
        }

//        return ResponseEntity.ok(chartDataPrediction);
        return ResponseEntity.ok(responseBody);
    }

    private List<List<Object>> getChartDataPrediction() {

        List<List<Object>> result = new ArrayList<>();

        listResult = forecastService.createList(listRawData, listBI, listGM);

        listResult.forEach( rd -> result.add(
                List.of(rd.getDateFormatted(), rd.getRawValue(), rd.getGmValue(), rd.getBiValue())));

        return result;
    }

    private List<List<Object>> getChartDataPE() {

        List<List<Object>> result = new ArrayList<>();

        listResult.forEach( rd -> result.add(
                List.of(rd.getDateFormatted(), rd.getGmValuePE(), rd.getBiValuePE())));

        return result;
    }



}

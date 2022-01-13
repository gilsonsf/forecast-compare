google.charts.load('current', {'packages':['line']});

$(document).ready(function () {

    $("#search-form").submit(function (event) {

        //stop submit the form, we will post it manually.
        event.preventDefault();

        fire_ajax_submit_search();

    });

});

function fire_ajax_submit_search() {

    var search = {}
    search["username"] = $("#username").val();
    //search["email"] = $("#email").val();

    $("#btn-search").prop("disabled", true);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/search",
        data: JSON.stringify(search),
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function (data) {

            var json = "<h4>Ajax Response</h4><pre>"
                + JSON.stringify(data, null, 4) + "</pre>";
            $('#feedback').html(json);

            console.log("SUCCESS : ", data);
            $("#btn-search").prop("disabled", false);

        },
        error: function (e) {

            var json = "<h4>Ajax Response</h4><pre>"
                + e.responseText + "</pre>";
            $('#feedback').html(json);

            console.log("ERROR : ", e);
            $("#btn-search").prop("disabled", false);

        }
    });

}

$(document).ready(function () {

    $("#btnSubmit").click(function (event) {

        //stop submit the form, we will post it manually.
        event.preventDefault();

        fire_ajax_submit_upload();

    });

});

$(document).ready(function () {

    $("#btnSubmitBI").click(function (event) {

        //stop submit the form, we will post it manually.
        event.preventDefault();

        fire_ajax_submit_uploadBI();

    });

});


function fire_ajax_submit_upload() {

    // Get form
    var form = $('#fileUploadForm')[0];

    var data = new FormData(form);

    //data.append("CustomField", "This is some extra data, testing");

    $("#btnSubmit").prop("disabled", true);

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "/api/upload",
        data: data,
        //http://api.jquery.com/jQuery.ajax/
        //https://developer.mozilla.org/en-US/docs/Web/API/FormData/Using_FormData_Objects
        processData: false, //prevent jQuery from automatically transforming the data into a query string
        contentType: false,
        cache: false,
        timeout: 600000,
        success: function (data) {

            $("#result").text(data);
            console.log("SUCCESS : ", data);
            $("#btnSubmit").prop("disabled", false);

        },
        error: function (e) {

            $("#result").text(e.responseText);
            console.log("ERROR : ", e);
            $("#btnSubmit").prop("disabled", false);

        }
    });

}

function fire_ajax_submit_uploadBI() {

    var form = $('#fileUploadFormBI')[0];
    var data = new FormData(form);
    $("#btnSubmitBI").prop("disabled", true);

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "/api/upload/bi",
        data: data,
        processData: false, //prevent jQuery from automatically transforming the data into a query string
        contentType: false,
        cache: false,
        timeout: 600000,
        success: function (data) {
            $("#resultBI").text(data);
            console.log("SUCCESS : ", data);
            $("#btnSubmitBI").prop("disabled", false);
        },
        error: function (e) {
            $("#resultBI").text(e.responseText);
            console.log("ERROR : ", e);
            $("#btnSubmitBI").prop("disabled", false);
        }
    });
}


$(document).ready(function () {

    $("#chart-form").submit(function (event) {

        //stop submit the form, we will post it manually.
        event.preventDefault();

        fire_ajax_submit_chart();

    });

});

function fire_ajax_submit_chart() {

    var search = {}
    search["username"] = $("#username").val();

    $("#btn-chart").prop("disabled", true);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/chart",
        data: JSON.stringify(search),
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function (data) {
            drawChart(data.chartDataPrediction);
            drawChartPE(data.chartDataPE);
            $("#btn-chart").prop("disabled", false);
        },
        error: function (e) {

            var json = "<h4>Ajax Response</h4><pre>"
                + e.responseText + "</pre>";
            $('#feedback').html(json);

            console.log("ERROR : ", e);
            $("#btn-search").prop("disabled", false);

        }
    });

}

function drawChart(values) {

      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Data');
      data.addColumn('number', 'Real');
      data.addColumn('number', 'Previsão GM');
      data.addColumn('number', 'Previsão BI');

      Object.keys(values).forEach(function(key) {
                data.addRow(values[key]);
            });

      var options = {
        chart: {
          title: 'Gráfico representando comportamento dos dados',
          subtitle: 'Servidor 4.'
        },
        width: 900,
        height: 500
      };

      var chart = new google.charts.Line(document.getElementById('linechart_material'));

      chart.draw(data, google.charts.Line.convertOptions(options));
}

function drawChartPE(values) {

      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Data');
      data.addColumn('number', 'PE GM');
      data.addColumn('number', 'PE BI');

      Object.keys(values).forEach(function(key) {
                data.addRow(values[key]);
            });

      var options = {
        chart: {
          title: 'Comparação entre PE GM x PE BI'
        },
        width: 900,
        height: 500
      };

      var chart = new google.charts.Line(document.getElementById('linechart_material_pe'));

      chart.draw(data, google.charts.Line.convertOptions(options));
}



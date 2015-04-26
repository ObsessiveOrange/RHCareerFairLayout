var globalData;
$(document).ready(function() {
    $("#uploadForm").submit(function(e) {
        e.preventDefault();
        // var formData = new FormData();
        // $.each($('#file')[0].files, function(i, file) {
        //     formData.append('file-' + i, file);
        // });
        // $.ajax({
        //     url: '/api/users/admin?method=test', //Server script to process data
        //     type: 'POST',
        //     xhr: function() { // Custom XMLHttpRequest
        //         var myXhr = $.ajaxSettings.xhr();
        //         if (myXhr.upload) { // Check if upload property exists
        //             myXhr.upload.addEventListener('progress', progressHandlingFunction, false); // For handling the progress of the upload
        //         }
        //         return myXhr;
        //     },
        //     //Ajax events
        //     // beforeSend: beforeSendHandler,
        //     success: function(data) {
        //         globalData = data;
        //         console.log(data);
        //     },
        //     headers: {
        //         "authUser": "bennydictwong",
        //     },
        //     // error: errorHandler,
        //     // Form data
        //     data: formData,
        //     //Options to tell jQuery not to process data or worry about content-type.
        //     cache: false,
        //     contentType: false,
        //     processData: false
        // });
        var form = $("#uploadForm");
        var formdata = false;
        if (window.FormData) {
            formdata = new FormData(form[0]);
        }
        var formAction = form.attr('action');
        $.ajax({
            url: '/api/users/admin?method=test',
            data: formdata ? formdata : form.serialize(),
            cache: false,
            contentType: false,
            processData: false,
            type: 'POST',
            success: function(data, textStatus, jqXHR) {
                // Callback code
                globalData = data;
                console.log(data);
            },
            error: function(jqXHR, textStatus, errorThrown ) {
                console.log("failed");
                console.log(textStatus);
                console.log(errorThrown);
            },
        });
    });
});
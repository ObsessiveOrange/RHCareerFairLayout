var globalData;
$(document).ready(function() {
    $("#uploadForm").submit(function(e) {
        e.preventDefault();
        var formData = new FormData($('#uploadForm')[0]);
        $.ajax({
            url: '/api/users/admin?method=test', //Server script to process data
            type: 'POST',
            xhr: function() { // Custom XMLHttpRequest
                var myXhr = $.ajaxSettings.xhr();
                if (myXhr.upload) { // Check if upload property exists
                    myXhr.upload.addEventListener('progress', progressHandlingFunction, false); // For handling the progress of the upload
                }
                return myXhr;
            },
            //Ajax events
            // beforeSend: beforeSendHandler,
            success: function(data) {
                globalData = data;
                console.log(data);
            },
            headers: {
                "authUser": "bennydictwong",
            },
            // error: errorHandler,
            // Form data
            data: formData,
            //Options to tell jQuery not to process data or worry about content-type.
            cache: false,
            contentType: false,
            processData: false
        });
    });
});
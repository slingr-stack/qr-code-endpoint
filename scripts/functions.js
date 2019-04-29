/////////////////////
// Public API
/////////////////////

endpoint.generateQRCode = function (data, options, callbackData, callbacks) {
    var config = {
        data: data,
        options: options
    };
    return endpoint._generateQRCode(config, callbackData, callbacks);
};

endpoint.parseQRCode = function (fileId, callbackData, callbacks) {
    var options = {
        fileId: fileId
    };
    return endpoint._parseQRCode(options, callbackData, callbacks);
};

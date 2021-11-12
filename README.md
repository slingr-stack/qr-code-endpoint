---
title: QR Code
keywords: 
last_updated: April 25, 2018
tags: []
summary: "Detailed description of QR Code endpoint."
---

## Overview

The QR Code endpoint allows to create and parse QR codes. Some of the features supported by this endpoint are:

- Generate an image (JPG or PNG) with the QR code for a given string 
- Parse QR codes given an image or PDF file

## Quick start

You can generate a code like this:

```js
app.endpoints.qrCode.generateQRCode(
  "Text to convert in QR code",
  { width: 512, height: 512 }, 
  { record: record }, 
  {
    'qrResponse': function(res, callbackData) {
      var data = res.data;
      if(data && data.status == "ok") {
        var document = callbackData.record;
        document.field('myQRFile').val({
          id: data.file.fileId,
          name: data.file.fileName,
          contentType: data.file.contentType
        });
        sys.data.save(document);
      }
    }
  }
);
```

You can parse a QR code like this:

```js
app.endpoints.qrCode.parseQRCode(
  record.field('myPdfFile').id(), 
  { 'record': record }, 
  {
    'qrResponse': function(res, callbackData) {
        var data = res.data;
        if(data && data.status == "ok" && data.codes){
          for(var i in data.codes){
            sys.logs.debug(">>> Code: " + data.codes[i] + "\n\n<<<<");
          }
        }
    }
  }
);
```

## Configuration

This endpoint does not have any specific configuration.

## Javascript API

### Generate QR code

```js
app.endpoints.qrCode.generateQRCode(text, options, callbackData, callbacks);
```

Where:

- `text`: the information to encode in the QR file.
- `options`: a map with the following options:
  - `width`: the width in pixels of the image to be generated (optional). By default `250`.
  - `height`: height in pixels of the image image be generated (optional). By default `250`.
  - `color`: hex color of QR code (optional). By default `#000000`.
  - `fileType`: image type to be generated (optional). Allowed values are `jpg` and `png`. By default `png`.
- `callbackData`: information that will be passed to the callbacks in the second parameter.
- `callbacks`: a map with the callback `qrResponse` to receive the file with the QR code.

Sample:

```javascript
var record = sys.data.findById('company', '5ab0041fea30451201647e2b');
app.endpoints.qrCode.generateQRCode("Text to convert in QR code", { 
    width: 350, 
    height: 350, 
    color: '#137335', 
    fileType: 'jpg'
  }, 
  { record: record }, 
  {
    'qrResponse': function(res, resData) {
      var data = res.data;
      if(data && data.status == "ok") {
        var document = resData.record;
        document.field('myQRFile').val({
          id: data.file.fileId,
          name: data.file.fileName,
          contentType: data.file.contentType
        });
        sys.data.save(document);
      }
    }
  }
);
```

### Parse QR code

```js
var resp = app.endpoints.qrCode.parseQRCode(fileId);
```

Where:

- `fileId`: is the file ID in the SLINGR app that contains the QR code. It could be a JPG, PNG or PDF file.

It will return an array with the information of all QR codes found in the file.

Sample: 

```javascript
var record = sys.data.findById('company', '5ab0041fea30451201647e2b');

app.endpoints.qrCode.parseQRCode(
  record.field('myQRFile').id(), 
  { 'record': record }, 
  {
    'qrResponse': function(res, resData) {
        var data = res.data;
        if(data && data.status == "ok") {
            if(data.codes){
              for(var i in data.codes){
                sys.logs.debug(">>> Code: " + data.codes[i] + "\n\n<<<<");
              }
            }
            
            var document = resData.record;
            document.field("codes").val(data.codes);
        }
    }
  }
);

```

## About SLINGR

SLINGR is a low-code rapid application development platform that accelerates development, with robust architecture for integrations and executing custom workflows and automation.

[More info about SLINGR](https://slingr.io)

## License

This endpoint is licensed under the Apache License 2.0. See the `LICENSE` file for more details.

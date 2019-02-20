require('../node_modules/font-awesome/css/font-awesome.min.css');
require('../node_modules/bootstrap/dist/css/bootstrap.css');
require('../node_modules/animate.css/animate.css');
require('../bower_components/angular-notify/dist/angular-notify.css');
require('../sass/camera-style.scss');

window.$ = window.jQuery = require('jquery');
require("bootstrap");
require("angular");
require('qrcode-reader');
//require('tracking');
require('webrtc-adapter');

// define angular module/app
var app = angular.module('exeo_camera', [require("angular-cookies"), require("angular-animate"), require("../bower_components/angular-notify/dist/angular-notify.js") && "cgNotify", require("angular-bootstrap")])
.controller('CameraController', /*@ngInject*/ function($scope, ngNotify) {
    ngNotify.config({position: "right"});

    var comp1;
    initCanvas(800, 600);
    qrcode.callback = function (str) {
        if(str) {
            if(comp1 && comp1 != str) {
                $.post("/api/pair", {comp1: comp1, comp2: str});
                comp1 = undefined;
                ngNotify.set('Computers Paired');
            } else {
                comp1 = str;
                ngNotify.set('Computer Scanned, please scan the second computer now');
            }
            console.log(str);
        }
    };
    setwebcam();

    //$scope.pair = function() {
    //};
});

var qrcode = new QrCode();

var v, gCtx, gCanvas, tracker;

function setwebcam() {
    navigator.mediaDevices.getUserMedia({video: { facingMode: "environment" }, audio: false}).then(success).catch(error);
}

function captureToCanvas() {
    try {
        gCtx.clearRect(0, 0, canvas.width, canvas.height);
        var w = v.clientWidth;
        var h = v.clientHeight;
        gCtx.drawImage(v, 0, 0, w, h, 0, 0, 800, 600);//, w/5,h/5,w*3/5,h*3/5, 0,0,800,600);
        //tracking.track('#qr-canvas', tracker);
        qrcode.decode();
    } catch(e){
        console.log(e);
    }
    setTimeout(captureToCanvas, 500);
}

function error(err) {
    console.error(err);
}

function success(stream) {
    v.src = window.URL.createObjectURL(stream);
    setTimeout(captureToCanvas, 500);
}

function drawRect(rect, context) {
    context.strokeStyle = "#00ff00";
    context.strokeRect(rect.x, rect.y, rect.width, rect.height);
    context.font = '11px Helvetica';
    context.fillStyle = "#fff";
    context.fillText('x: ' + rect.x + 'px', rect.x + rect.width + 5, rect.y + 11);
    context.fillText('y: ' + rect.y + 'px', rect.x + rect.width + 5, rect.y + 22);
}

function areaRect(rect) {
    return rect.width * rect.height;
}

function threshold(pixels, threshold) {
    var d = pixels.data;
    for (var i=0; i<d.length; i+=4) {
        var r = d[i];
        var g = d[i+1];
        var b = d[i+2];
        var v = (0.2126*r + 0.7152*g + 0.0722*b >= threshold) ? 255 : 0;
        d[i] = d[i+1] = d[i+2] = v;
    }
    return pixels;
}

function initCanvas(w,h)  {
    v=document.getElementById("v");
    gCanvas = canvas = document.getElementById("qr-canvas");
    gCanvas.style.width = w + "px";
    gCanvas.style.height = h + "px";
    gCanvas.width = w;
    gCanvas.height = h;
    gCtx = context = gCanvas.getContext("2d");
    gCtx.clearRect(0, 0, w, h);

    /*tracking.ColorTracker.registerColor('white', function(r, g, b) {
        return r > 240 && g > 240 && b > 240;
    });
    tracker = new tracking.ColorTracker(['white']);
    tracker.on('track', function(event) {
        var cRect;
        event.data.forEach(function(rect) {
            if(rect.width - rect.height < 0.1 * rect.width && (!cRect || areaRect(rect) > areaRect(cRect))) {
                cRect = rect;
            }
        });
        if(cRect) {
            console.log(cRect);
            drawRect(cRect, context);
            var imgData = context.getImageData(cRect.x, cRect.y, cRect.width, cRect.height);
            //threshold(imgData, 200);
            document.getElementById("qr-sub-canvas").getContext("2d").putImageData(imgData, 0, 0);
            qrcode.decode(imgData);
        }
    });*/
}

require('../node_modules/font-awesome/css/font-awesome.min.css');
require('../node_modules/bootstrap/dist/css/bootstrap.css');
require('../node_modules/animate.css/animate.css');
require('../bower_components/angular-notify/dist/angular-notify.css');
require('../sass/app-style.scss');

window.$ = window.jQuery = require('jquery');
require("bootstrap");
require("webrtc-adapter");
require("q");
require("script-loader!../node_modules/qrcode-generator/qrcode.js");
require("script-loader!noty");
require("angular");

let constants = require("../js/utils/constants.js");
require("../js/utils/java-constants.js");

//noinspection JSUnresolvedVariable
window.requestFileSystem = window.requestFileSystem || window.webkitRequestFileSystem;

// define angular module/app
let app;
let routes = [];

//noinspection SpellCheckingInspection
function addDirective(drctv) {
    app.directive(drctv.name, drctv.directive);
}
//noinspection SpellCheckingInspection
function addService(srvc) {
    app.factory(srvc.name, srvc.service);
}
function addController(ctrl) {
    app.controller(ctrl.name, ctrl.controller);
    if(ctrl.route) {
        routes.push(ctrl.route);
    }
}

//noinspection SpellCheckingInspection
app = angular.module('exeo', [require("angular-route"),
    require("angular-cookies"),
    require("angular-animate"),
    require("angular-moment") && "angularMoment",
    require("angular-loading-bar") && "angular-loading-bar",
    require("angular-bootstrap"),
    require("angular-qrcode") && "monospaced.qrcode",
    require("ngstorage") && "ngStorage",
    require("script-loader!../node_modules/angular-filesize-filter/angular-filesize-filter") && "ngFilesizeFilter"]);

addDirective(require("../js/drctvs/FilePickerDrctv.js"));
addDirective(require("../js/drctvs/TimeDrctv.js"));
addService(require("../js/srvcs/WebsockSrvc.js"));
addService(require("../js/srvcs/AccountSrvc.js"));
addService(require("../js/srvcs/ConnectSrvc.js"));
addController(require("../js/ctrls/NavCtrl.js"));
addController(require("../js/ctrls/SidebarCtrl.js"));
addController(require("../js/ctrls/HomeCtrl.js"));
addController(require("../js/ctrls/QRCtrl.js"));
addController(require("../js/ctrls/SettingsCtrl.js"));
addController(require("../js/ctrls/VidCtrl.js"));
addController(require("../js/ctrls/FileSendModalCtrl.js"));
addController(require("../js/ctrls/FileReceiveModalCtrl.js"));

app.constant('iceServers', constants.iceServers)
    .constant('aHrefSanitizationWhitelist', constants.aHrefSanitizationWhitelist)
    .constant('moment', require("moment"))
    .constant('noty', noty)
    .constant('SockJS', require("sockjs-client"))
    .config(/*@ngInject*/ ($compileProvider, aHrefSanitizationWhitelist) => {
        $compileProvider.aHrefSanitizationWhitelist(aHrefSanitizationWhitelist);
    }).config(/*@ngInject*/ (cfpLoadingBarProvider) => {
        cfpLoadingBarProvider.includeSpinner = false;
    }).config(/*@ngInject*/ ($routeProvider) => {
        //add all the routes imported above with addController() that say they are routes
        for(let i = 0; i < routes.length; i++) {
            $routeProvider.when(routes[i].path, routes[i].action);
        }
        //add home as the default route
        $routeProvider.otherwise({
            redirectTo: '/home',
            activetab: 'home'
        });
    }).run(/*@ngInject*/ (connectUtils) => {
        connectUtils.init();
    });


require('../sass/index-style.scss');

window.$ = window.jQuery = require('jquery');
require("bootstrap");
require("angular");
require("script-loader!noty");

// define angular module/app
let app = angular.module('exeo_index', [require("angular-cookies"), require("angular-animate"), require("angular-bootstrap"),
                                        require("ngstorage") && "ngStorage"])
.constant('noty', noty)
.controller('IndexController', /*@ngInject*/ ($scope, $uibModal, $sessionStorage, $cookies, noty) => {
    $scope.showLogin = !$cookies.get("exeo-token");

    $scope.begin = () => {
        //$('#login-modal').modal();
        $uibModal.open({templateUrl: 'loginModal.html', controller: 'LoginModalController', windowClass: 'loginmodal'});
    };

    if($sessionStorage['exeo-just-logged-out']) {
        delete $sessionStorage['exeo-just-logged-out'];
        noty({text: 'You have been logged out.', type: 'success', timeout: 20000});
    }
}).controller('LoginModalController', /*@ngInject*/ ($scope, $http, $cookies, $uibModalInstance) => {
    $scope.formDataLog = {};
    $scope.formDataReg = {};

    $scope.regFailedMsg = "";

    $scope.hideLoginAlert = () => {
        $scope.loginFailed = false;
    };
    $scope.hideRegisterAlert = () => {
        $scope.registerFailed = false;
    };
    $scope.closeDialog = () => {
        $uibModalInstance.close();
    };

    function goodLogin(token) {
        $cookies.put('exeo-token', token, {secure: true});
        window.location.href = "/app";
    }

    function pulse(element) {
        element.removeClass('pulse').addClass('pulse').one('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', () =>{
            $(this).removeClass('pulse');
        });
    }

    let procAjaxLog = (response) => {
        let data = response.data;
        if (data.login) {
            $scope.loginFailed = false;
            goodLogin(data.token);
        } else {
            if($scope.loginFailed) {
                pulse($('#loginFailed'));
            }
            $scope.loginFailed = true;
        }
    };

    let procAjaxReg = (response) => {
        let data = response.data;
        if (data.register) {
            $scope.registerFailed = false;
            goodLogin(data.token);
        } else {
            if(data.errorCode == 103) {
                $scope.regFailedMsg = "The email address provided is invalid.";
            } else if(data.errorCode == 110) {
                $scope.regFailedMsg = "The given email address is already registered.";
            } else {
                $scope.regFailedMsg = "";
            }

            if($scope.registerFailed) {
                pulse($('#registerFailed'));
            }
            $scope.registerFailed = true;
        }
    };

    $scope.processLogin = () => {
        $http.post('/api/login', $scope.formDataLog).then(procAjaxLog, procAjaxLog);
    };

    $scope.processRegister = () => {
        $http.post('/api/register', $scope.formDataReg).then(procAjaxReg, procAjaxReg);
    };
});

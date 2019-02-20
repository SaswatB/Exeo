/**
 * Created by Saswat on 7/17/2016.
 */
define({
    name: "HomeCtrl"
    , route: {
        path: '/home'
        , action: {
            templateUrl: 'partials/pages/home.html',
            controller: 'HomeCtrl',
            activetab: 'home'
        }
    }, controller: /*@ngInject*/ function($rootScope, $scope, $uibModal, websock) {
        $scope.sendFile = function (device) {
            $uibModal.open({
                backdrop: "static",
                templateUrl: 'partials/modals/sendFileModal.html',
                controller: 'FileSendModalCtrl',
                resolve: {device: device}
            });
        };
    }
});
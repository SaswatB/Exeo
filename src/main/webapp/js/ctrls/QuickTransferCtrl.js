/**
 * Created by Saswat on 1/4/2017.
 */
define({
    name: "QuickTransferCtrl"
    , route: {
        path: '/transfer'
        , action: {
            templateUrl: 'partials/pages/quick/transfer.html',
            controller: 'QuickTransferCtrl',
            activetab: 'transfer'
        }
    }, controller: /*@ngInject*/ function($rootScope, $scope, $uibModal, connectUtils, noty) {
        $scope.sendFile = (device) => {
            $uibModal.open({
                backdrop: "static",
                templateUrl: 'partials/modals/sendFileModal.html',
                controller: 'FileSendModalCtrl',
                resolve: {device: device}
            });
        };

        $scope.requestPair = function () {
            connectUtils.requestPair($scope.requestedPairCode);
        };
    }
});
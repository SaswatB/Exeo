/**
 * Controller for pairing view
 * Created by Saswat on 7/17/2016.
 */
define({
    name: "QRCtrl"
    , route: {
        path: '/qrpair'
        , action: {
            templateUrl: 'partials/pages/qrpair.html',
            controller: 'QRCtrl',
            activetab: 'qrpair'
        }
    }, controller: /*@ngInject*/ function($rootScope, $scope, $document, connectUtils) {
        $scope.requestedPairCode = "";

        $document.ready(function(){
            updateQRPairCode();
        });

        function updateQRPairCode() {
            if ($rootScope.qrPairId) {
                $("#newCodeCol").height($("#myCodeCol").height());
            }
        }

        $rootScope.$watch("qrPairId", updateQRPairCode);
        updateQRPairCode();

        $("#newCodeCol").height($("#myCodeCol").height());

        $scope.requestPair = function () {
            connectUtils.requestPair($scope.requestedPairCode);
        };
    }
});

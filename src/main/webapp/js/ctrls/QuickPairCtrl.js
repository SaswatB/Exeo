/**
 * Created by Saswat on 1/4/2017.
 */
define({
    name: "QuickPairCtrl"
    , route: {
        path: '/pair'
        , action: {
            templateUrl: 'partials/pages/quick/pair.html',
            controller: 'QuickPairCtrl',
            activetab: 'pair'
        }
    }, controller: /*@ngInject*/ function($rootScope, $scope, connectUtils) {

        $scope.requestPair = function () {
            connectUtils.requestPair($scope.requestedPairCode);
        };

    }
});
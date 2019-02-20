/**
 * Created by Saswat on 7/19/2016.
 */
define({
    name: "SettingsCtrl"
    , route: {
        path: '/settings'
        , action: {
            templateUrl: 'partials/pages/settings.html',
            controller: 'SettingsCtrl',
            activetab: 'settings'
        }
    }, controller: /*@ngInject*/ function($rootScope, $scope, $uibModal, websock) {
        
    }
});
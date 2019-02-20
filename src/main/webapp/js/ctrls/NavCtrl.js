/**
 * Controller for app navigation, and general app management
 * Created by Saswat on 7/17/2016.
 */

define({
    name: "NavCtrl",
    controller: /*@ngInject*/ ($rootScope, $scope, connectUtils) => {
        //ngNotify.config({position: "top"});
        $scope.timeFormat = 'lll';//'M/d/yy h:mm:ss a';
    }
});

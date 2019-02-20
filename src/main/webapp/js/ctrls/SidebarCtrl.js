/**
 * Created by Saswat on 7/17/2016.
 */
define({
    name: "SidebarCtrl"
    , controller: /*@ngInject*/ function($scope, $route, accountUtils) {
        $scope.route = $route;

        $scope.logout = () => {
            accountUtils.logout();
        };
    }
});
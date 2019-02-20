/**
 * Created by Saswat on 1/3/2017.
 */
define({
    name: "QuickNavCtrl",
    controller: /*@ngInject*/ function($rootScope, $scope, $location, $cookies, $uibModal) {
        $scope.timeFormat = 'lll';

        $rootScope.$on('devicePaired', () => {
            $location.path("/transfer");

            $("#loading-overlay").fadeOut("fast");
        });

        if(!$cookies.get("hide-tutorial")) {
            $uibModal.open({
                templateUrl: 'partials/modals/tutorial.html',
                controller: ($scope, $uibModalInstance) => {
                    $scope.hideTutorial = false;
                    $scope.close = () => {
                        //set a cookie to hide this in the future if the user doesn't want to see this tutorial again
                        if($scope.hideTutorial) {
                            let expiry = new Date();
                            let time = expiry.getTime();
                            time += 24*60*60*1000*365;//1 year expiry
                            expiry.setTime(time);
                            $cookies.put('hide-tutorial', "true", {secure: true, expires: expiry});
                        }
                        $uibModalInstance.close();
                    };
                }
            });
        }
    }
});
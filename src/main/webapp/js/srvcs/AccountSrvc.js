/**
 * Created by Saswat on 12/27/2016.
 */


define({
    name: "accountUtils"
    , service: /*@ngInject*/ ($cookies, $sessionStorage, websock) => {
        let accountUtils = {};

        accountUtils.logout = () => {
            //logout of websocket session
            websock.logout();

            //remove cookies
            $cookies.remove('exeo-token');
            $cookies.remove('exeo-device-token');

            //create a temporary session variable so the home page can confirm logout
            $sessionStorage['exeo-just-logged-out'] = "true";

            window.location.href = "/";
        };

        return accountUtils;
}});
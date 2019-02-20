/**
 *
 * Created by Saswat on 1/22/2017.
 */

define({
    name: "connectUtils"
    , service: /*@ngInject*/ ($rootScope, $uibModal, $cookies, websock, noty) => {
        let connectUtils = {};
        let quickMode;

        //todo validate all the destructured parameters

        /**
         * Initializes an app mode websock connection
         */
        connectUtils.init = () => {
            quickMode = false;

            let token = $cookies.get('exeo-token');
            let device_token = $cookies.get('exeo-device-token');

            //if there's no user token, they didn't login
            if(!token) {
                window.location.href = "/";
                return;
            }

            websock.on("init", ({user}= {}) => {
                $rootScope.userName = user.firstname + " " + user.lastname;
            });

            let registerDeviceModal = null;
            websock.on("register-device", ({existingDevices={}, error=""} = {}) => {
                //the server wants to register this device
                $uibModal.open({
                    backdrop: "static",
                    keyboard: false,
                    templateUrl: 'partials/modals/deviceNameModal.html',
                    controller: ($scope, $uibModalInstance, accountUtils) => {
                        if(registerDeviceModal) {
                            registerDeviceModal.close();
                        }
                        registerDeviceModal = $uibModalInstance;

                        if(error === "too-short") {
                            $scope.errorAlertMsg = "Device name must be at least 5 characters long";
                        } else if (error === "bad-chars") {
                            $scope.errorAlertMsg = "Device name must only contain letters and numbers";
                        } else if (error === "bad-id") {
                            $scope.errorAlertMsg = "There was an error while attempting to associate this device, please try again";
                        }
                        $scope.existingDevices = existingDevices;
                        $scope.registerNewDevice = () => {
                            websock.sendEvent(WSEName.REGISTER_DEVICE,
                                {[WSQ_REGISTER_DEVICE_MESSAGE_NAME]: $scope.name});//TODO validation
                            $uibModalInstance.close();
                        };
                        $scope.registerExistingDevice = () => {
                            websock.sendEvent(WSEName.REGISTER_DEVICE,
                                {["id"]: $scope.id});//TODO validation
                            $uibModalInstance.close();
                        };
                        $scope.logout = () => {
                            accountUtils.logout();
                        };
                    }
                });
            });
            websock.on("device-token", ({entry} = {}) => {//the server has issued a new device token
                $cookies.put('exeo-device-token', device_token = entry);
            });
            websock.on("device-name", ({name} = {}) => {//the server has given this device's name
                $rootScope.device_name = name;
                $rootScope.$digest();
            });

            initCommon(() => {websock.login(token, device_token);});
        };

        /**
         * Initializes a quick mode websock connection
         */
        connectUtils.initQuick = () => {
            quickMode = true;

            initCommon(() => {websock.quickMode();});
        };

        function initCommon(connectAction) {
            $rootScope.devices = [];
            $rootScope.channels = {};

            $rootScope.wsConnected = false;

            $rootScope.$on('websockOpened', () => {
                $rootScope.wsConnected = true;
                $rootScope.$apply();

                connectAction();

                $("#loading-overlay").fadeOut("fast");
            });
            $rootScope.$on('websockClosed', () => {
                $rootScope.wsConnected = false;
                $rootScope.$apply();
            });

            //validates that this webapp has the proper protocol version
            websock.on("init", ({server}= {}) => {
                //noinspection JSUnresolvedVariable
                if(server.protocolVersion != PROTOCOL_VERSION) {
                    window.location.reload();
                }
            });

            websock.on("pair-id", ({code} = {}) => {//the server has assigned a pair id
                $rootScope.pairId = code;
                $rootScope.$digest();
            });
            websock.on("qr-pair-id", ({code} = {}) => {//the server has assigned a qr pair id
                $rootScope.qrPairId = code;
                $rootScope.$digest();
            });
            websock.on("pair-request", ({name, requesterPairCode, code} = {}) => {//the server has delegated a pair request from another device
                noty({text: 'Pair request received from device '+(name ? "\""+name+"\"" : " with pair code "+requesterPairCode),
                    buttons: [
                        {addClass: 'btn btn-primary', text: 'Accept', onClick: ($noty) => {
                            $rootScope.pair(code);
                            $noty.close();
                        }},
                        {addClass: 'btn btn-danger', text: 'Cancel', onClick: ($noty) => {$noty.close();}}
                    ]});
            });
            websock.on("pair-complete", ({channel} = {}) => {//the server has completed a pair request from another device
                let obj = {channel: channel};
                $rootScope.devices.push(obj);
                $rootScope.channels[channel] = obj;
                websock.sendChannelEvent(channel, "introduce", {deviceName: $rootScope.device_name ? $rootScope.device_name : "Temporary Device"});//todo fix introduce on quick mode?
                $rootScope.$digest();
                noty({text: 'Paired Successfully', timeout: 10000});
                $rootScope.$broadcast('devicePaired');
            });

            websock.onChannelEvent("introduce", (channel, {deviceName} = {}) => {
                $rootScope.channels[channel].name = deviceName;
                $rootScope.$digest();
            });
            websock.onChannelEvent("sending-file", (channel, {file} = {}) => {
                $uibModal.open({
                    backdrop: "static",
                    templateUrl: 'partials/modals/receiveFileModal.html',
                    controller: 'FileReceiveModalCtrl',
                    resolve: {device: $rootScope.channels[channel], file: file}
                });
            });

            $rootScope.pair = (code) => {
                websock.sendEvent(WSEName.PAIR_ACCEPT,
                    {[WSQ_PAIR_ACCEPT_MESSAGE_PAIRREQUESTID]: code});
            };

            //noinspection SpellCheckingInspection
            websock.connect('/api/wsignaler/');
        }

        connectUtils.requestPair = (requestedPairCode) => {
            if ($rootScope.pairId == requestedPairCode) {
                noty({text: 'Device cannot pair with itself', type: 'error'});
                return;
            }
            websock.sendEvent(WSEName.PAIR_REQUEST,
                {[WSQ_PAIR_REQUEST_MESSAGE_CODE]: requestedPairCode});
            noty({text: 'Pair Request Sent', timeout: 10000});
        };

        return connectUtils;
    }});
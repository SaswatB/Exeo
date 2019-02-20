/**
 * Controller for the file send dialog
 * Created by Saswat on 7/17/2016.
 */
define({
    name: "FileSendModalCtrl"
    , controller: /*@ngInject*/ function($rootScope, $scope, $uibModalInstance, websock, device, iceServers, noty) {
        $scope.sendingData = false;
        $scope.fileSize = 1;
        $scope.progress = 0;
        $scope.file = undefined;

        let file;

        $scope.send = function () {
            file = $scope.file;

            // Handle undefined and 0 size files.
            if (!file || file.size === 0) {
                //todo handle
                return;
            }

            $scope.fileSize = file.size;

            websock.sendChannelEvent(device.channel, "sending-file", {file: {
                name: file.name,
                type: file.type,
                size: file.size
            }});
        };

        $scope.cancel = function () {
            $uibModalInstance.close();
            if($scope.sendingData) {
                websock.sendChannelEvent(device.channel, "file-send-cancel");
            }
        };

        let pc;//peer connection
        let sendChannel;
        let dialogClosed = false;
        let lastProgressUpdate = 0;

        $uibModalInstance.closed.then(() => {
            dialogClosed = true;
            if(pc) {
                pc.close();
                pc = null;
            }
        });

        //todo these event handlers live after dialog close, somehow erase them
        websock.onChannelEvent("file-accept", (channel) => {
            if(channel != device.channel || dialogClosed) return;

            $scope.sendingData = true;
            $scope.$digest();

            //have a check to make sure the transfer started
            setTimeout(() => {
                if(lastProgressUpdate === 0) {
                    $uibModalInstance.close();
                    noty({text: 'Could not start transfer', type: 'error'});
                }
            }, 20000);
            call();
        });

        websock.onChannelEvent("file-reject", (channel) => {
            if(channel != device.channel || dialogClosed) return;

            $uibModalInstance.close();
            noty({text: 'File transfer offer rejected'});

        });

        websock.onChannelEvent("file-receive-cancel", (channel) => {
            if(channel != device.channel || dialogClosed) return;

            $uibModalInstance.close();
            noty({text: 'Transfer canceled', type: 'error'});
        });

        websock.onChannelEvent("rdesc", (channel, {desc} = {}) => {
            if(channel != device.channel || dialogClosed) return;

            pc.setRemoteDescription(new RTCSessionDescription(desc), () => {
            }, function (err) {//onSetSessionDescriptionError
                console.log('Failed to set session description: ' + err.toString());
            });
        });

        websock.onChannelEvent("rice", (channel, {candidate} = {}) => {
            if(channel != device.channel || dialogClosed) return;

            pc.addIceCandidate(new RTCIceCandidate(candidate), () => {
                console.log('remote addIceCandidate success');
            }, function (err) {
                console.log('remote failed to add ICE Candidate: ' + err.toString());
            });
        });

        websock.onChannelEvent("file-progress-update", (channel, {progress} = {}) => {
            if(channel != device.channel || dialogClosed) return;

            lastProgressUpdate = new Date().getTime();
            $scope.progress = progress;
            $scope.$digest();
        });

        websock.onChannelEvent("file-send-finished", (channel) => {
            if(channel != device.channel || dialogClosed) return;

            if (sendChannel)sendChannel.close();
            pc.close();
            pc = null;

            $uibModalInstance.close();
            $scope.sendingData = false;
            noty({text: 'Transfer Complete'});
            $scope.$digest();
        });

        function sendData() {
            const chunkSize = 16384;
            let offset = 0;
            let reader = new window.FileReader();
            let sliceFile = () => {
                reader.readAsArrayBuffer(file.slice(offset, offset + chunkSize));
            };
            reader.onload = (() => {
                return (e) => {
                    sendChannel.send(e.target.result);
                    if (file.size > offset + e.target.result.byteLength) {
                        offset += e.target.result.byteLength;
                        sliceFile();
                    }
                };
            })(file);
            sliceFile();

            //transfer watchdog, cancels transfer after 10 seconds of no activity
            let watcher = () => {
                if(!dialogClosed && $scope.progress < $scope.fileSize) {
                    if(new Date().getTime() - lastProgressUpdate > 10000) {
                        noty({text: 'Transfer failed, could not send data', type: 'error'});
                        $uibModalInstance.close();
                    } else {
                        setTimeout(watcher, 5000);
                    }
                }
            };
            setTimeout(watcher, 5000);
        }

        function call() {
            pc = new RTCPeerConnection(iceServers);

            //setup data channel
            //noinspection JSUnresolvedFunction
            sendChannel = pc.createDataChannel('sendDataChannel');
            sendChannel.binaryType = 'arraybuffer';

            sendChannel.onopen = sendChannel.onclose = () => {
                if (sendChannel.readyState === 'open') {
                    sendData();
                }
            };

            //setup ice
            pc.onicecandidate = (event) => {//onIceCandidate
                if (event.candidate) {
                    websock.sendChannelEvent(device.channel, "ice", {candidate: event.candidate});
                }
            };
            pc.oniceconnectionstatechange = (e) => {
                console.log(e);
            };
            pc.createOffer((desc) => {//onCreateOfferSuccess
                pc.setLocalDescription(desc, () => {
                }, () => {//onSetSessionDescriptionError
                    console.log('Failed to set session description: ' + error.toString());
                });

                websock.sendChannelEvent(device.channel, "desc", {desc: desc});
                console.log(desc);
            }, (error) => {//onCreateSessionDescriptionError
                console.log('Failed to create session description: ' + error.toString());
            });
        }
    }
});
/**
 * Controller for the file receive dialog
 * Created by Saswat on 7/17/2016.
 */
define({
    name: "FileReceiveModalCtrl"
    , controller: /*@ngInject*/ function($rootScope, $scope, $uibModalInstance, websock, device, file, iceServers, noty) {
        $scope.receivingData = false;
        $scope.downloadHref = '';
        $scope.downloadName = '';
        $scope.fileSize = file.size;
        $scope.fileName = file.name;
        $scope.deviceName = device.name;
        $scope.progress = 0;

        let pc;//peer connection
        let receiveChannel;
        let receivedSize = 0;
        window.receiveBuffer = [];//todo investigate why this is global
        let dialogClosed = false;
        let lastDataReceived = 0;

        $uibModalInstance.closed.then(() => {
            dialogClosed = true;
            if(pc) {
                pc.close();
                pc = null;
            }
        });

        $scope.accept = () => {
            websock.sendChannelEvent(device.channel, "file-accept");
            $scope.receivingData = true;

            let lastProgress = 0;
            let progressUpdate = () => {
                if(receivedSize > lastProgress) {
                    websock.sendChannelEvent(device.channel, "file-progress-update", {progress: receivedSize});
                    lastProgress = receivedSize;
                }
                if($scope.fileSize > receivedSize && !dialogClosed) {
                    setTimeout(progressUpdate, 500);
                }
            };
            progressUpdate();

            //have a check to make sure the transfer started
            setTimeout(() => {
                if(lastDataReceived === 0) {
                    $uibModalInstance.close();
                    noty({text: 'Could not start transfer', type: 'error'});
                }
            }, 20000);
        };

        $scope.reject = () => {
            websock.sendChannelEvent(device.channel, "file-reject");
            $uibModalInstance.close();
        };

        $scope.cancel = () => {
            websock.sendChannelEvent(device.channel, "file-receive-cancel");
            $uibModalInstance.close();
            noty({text: 'Transfer canceled', timeout: 10000});
        };

        //todo these event handlers live after dialog close, somehow erase them
        websock.onChannelEvent("desc", (channel, {desc} = {}) => {
            if(channel != device.channel || dialogClosed) return;

            rcall(desc);
        });

        websock.onChannelEvent("ice", (channel, {candidate} = {}) => {
            if(channel != device.channel || dialogClosed) return;

            pc.addIceCandidate(new RTCIceCandidate(candidate), function () {
                console.log('remote addIceCandidate success');
            }, function (err) {
                console.log('remote failed to add ICE Candidate: ' + err.toString());
            });
        });

        websock.onChannelEvent("file-send-cancel", (channel) => {
            if(channel != device.channel || dialogClosed) return;

            $uibModalInstance.close();
            noty({text: 'Transfer canceled', type: 'error'});
        });

        function receiveData(event) {
            $scope.progress = (receivedSize += event.data.byteLength);
            lastDataReceived = new Date().getTime();

            receiveBuffer.push(event.data);

            if (receivedSize === file.size && !dialogClosed) {
                let a = document.createElement("a");
                document.body.appendChild(a);
                a.style = "display: none";
                //noinspection JSUnresolvedFunction
                let blob = new Blob(receiveBuffer, {type: "octet/stream"}), url = window.URL.createObjectURL(blob);
                a.href = url;
                a.download = file.name;
                a.click();
                //noinspection JSUnresolvedFunction
                window.URL.revokeObjectURL(url);

                noty({text: 'Transfer Complete'});
                websock.sendChannelEvent(device.channel, "file-send-finished");
                $uibModalInstance.close();
            }
        }

        function rcall(desc) {
            pc = new RTCPeerConnection(iceServers);

            pc.ondatachannel = function (event) {
                receiveChannel = event.channel;
                receiveChannel.binaryType = 'arraybuffer';
                receiveChannel.onmessage = receiveData;
                //todo handle
                //receiveChannel.onopen = onReceiveChannelStateChange;
                //receiveChannel.onclose = onReceiveChannelStateChange;

                receivedSize = 0;

                //transfer watchdog, cancels transfer after 10 seconds of no activity
                let watcher = () => {
                    if(!dialogClosed && $scope.progress < $scope.fileSize) {
                        if(new Date().getTime() - lastDataReceived > 10000) {
                            noty({text: 'Transfer failed, could not receive data', type: 'error'});
                            $uibModalInstance.close();
                        } else {
                            setTimeout(watcher, 5000);
                        }
                    }
                };
                setTimeout(watcher, 5000);
            };

            pc.onicecandidate = function (event) {//onIceCandidate
                if (event.candidate) {
                    websock.sendChannelEvent(device.channel, "rice", {candidate: event.candidate});
                }
            };
            pc.oniceconnectionstatechange = function (e) {
                console.log(e);
            };
            pc.setRemoteDescription(new RTCSessionDescription(desc), function () {

                pc.createAnswer(function (rdesc) {//onCreateAnswerSuccess
                    pc.setLocalDescription(rdesc, function () {
                    }, function (error) {//onSetSessionDescriptionError
                        console.log('Failed to set session description: ' + error.toString());
                    });

                    websock.sendChannelEvent(device.channel, "rdesc", {desc: rdesc});
                    console.log(rdesc);
                }, function (error) {//onCreateSessionDescriptionError
                    console.log('Failed to create session description: ' + error.toString());
                });

            }, function (error) {//onSetSessionDescriptionError
                console.log('Failed to set session description: ' + error.toString());
            });
        }
    }
});
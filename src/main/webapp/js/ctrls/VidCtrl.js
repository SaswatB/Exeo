/**
 * Created by Saswat on 7/17/2016.
 */

define({
    name: "VidCtrl"
    , route: {
        path: '/video'
        , action: {
            templateUrl: 'partials/pages/video.html',
            controller: 'VidCtrl',
            activetab: 'video'
        }
    }, controller: /*@ngInject*/ function($rootScope, $scope, websock) {
        //vidSetup($scope, websock);
    }
});


/*function vidSetup($scope, websock) {

 var others = {};

 $scope.$on('websockOpened', function(){
 websock.subscribe(discussid);
 websock.messageEveryone(discussid, "find", "");
 });

 var pwdDialog = null;
 $scope.$on('websockMessaged', function(event, msg){
 if(msg.subject == "roomfilled") {
 BootstrapDialog.show({
 type: BootstrapDialog.TYPE_DANGER,
 title: 'Discussion Full',
 message: 'This discussion is current full.<br>Try creating a new public discussion or scheduling a private discussion',
 buttons: [],
 closable: false
 });
 } else if(msg.subject == "password") {
 pwdDialog = BootstrapDialog.show({
 closable: false,
 title: 'Password required',
 message: '<div class="form-group"><div class="input-group">' +
 '<div class="input-group-addon"><i class="fa fa-lock"></i></div>' +
 '<input type="password" class="form-control" id="password" name="password" placeholder="Password" required>' +
 '</div></div>',
 buttons: [
 {
 label: 'Submit',
 cssClass: 'btn-primary',
 action: function(dialogItself){
 websock.messageServer(discussid, , "password", $("#password").val());
 }
 }]
 });
 } else if(msg.subject == "passwordIncorrect") {
 BootstrapDialog.show({
 title: 'Incorrect Password',
 message: 'The password you entered is incorrect',
 buttons: [
 {
 label: 'Ok',
 cssClass: 'btn-primary',
 autospin: true,
 action: function(dialogItself){
 dialogItself.close();
 }
 }]
 });
 } else if(msg.subject == "passwordCleared") {
 if(pwdDialog) {
 websock.subscribe(discussid);
 websock.messageEveryone(discussid, "find", "");
 pwdDialog.close();
 pwdDialog = null;
 }
 } else if(msg.subject == "find" || msg.subject == "exist") {
 //the discovery process, new users issue a "find" command to everyone and every other user responds to that user with "exist"
 if(msg.subject == "find") {
 websock.messageUser(msg.fromUser, discussid, "exist", "");
 }
 others[msg.fromUser] = {pc1:null, pc2: null, elm: Math.floor(Math.random()*16777215).toString(16)};
 call(msg.fromUser);
 } else if(msg.subject == "left") {
 $('#remoteVideo'+others[msg.fromUser].elm+"Container").remove();
 delete others[msg.fromUser];
 } else if(msg.subject == "desc") {
 rcall(msg.fromUser, JSON.parse(msg.message));
 } else if(msg.subject == "ice") {
 others[msg.fromUser].pc2.addIceCandidate(new RTCIceCandidate(JSON.parse(msg.message)), function() {
 console.log('remote addIceCandidate success');
 }, function(err) {
 console.log('remote failed to add ICE Candidate: ' + err.toString());
 });
 } else if(msg.subject == "rdesc") {
 others[msg.fromUser].pc1.setRemoteDescription(new RTCSessionDescription(JSON.parse(msg.message)), function() {}, onSetSessionDescriptionError);
 } else if(msg.subject == "rice") {
 others[msg.fromUser].pc1.addIceCandidate(new RTCIceCandidate(JSON.parse(msg.message)), function() {
 console.log('remote addIceCandidate success');
 }, function(err) {
 console.log('remote failed to add ICE Candidate: ' + err.toString());
 });
 }
 });


 $scope.callDisabled = true;
 $scope.hangupDisabled = true;

 var servers = {'iceServers': [{'urls': 'stun:stun.services.mozilla.com'}, {'urls': 'stun:stun.l.google.com:19302'}]};;
 var sdpConstraints = {'mandatory': {'OfferToReceiveAudio': true, 'OfferToReceiveVideo': true}};



 function call(user) {
 others[user].pc1 = new RTCPeerConnection(servers);
 others[user].pc1.onicecandidate = function(e) {
 onIceCandidate(user, e);
 };
 others[user].pc1.oniceconnectionstatechange = function(e) {
 console.log(e);
 };
 others[user].pc1.addStream(localStream);
 others[user].pc1.createOffer(function(desc) {onCreateOfferSuccess(user, others[user].pc1, desc)}, onCreateSessionDescriptionError);
 }

 function rcall(user, desc) {
 others[user].pc2 = new RTCPeerConnection(servers);
 others[user].pc2.onaddstream = function(e) {//TODO check potential xss
 var userVideoID = "remoteVideo"+others[user].elm;
 $("#video-row").append("<div id=\""+userVideoID+"Container\" class=\"col-md-3 video-screen\"><video id=\""+userVideoID+"\" autoplay></video><!--div style=\"position: absolute; bottom: 0px;\"><button ng-click=\"expand($event)\">Expand</button></div--></div>");
 attachMediaStream(document.getElementById(userVideoID), e.stream);
 others[user].pc2.onremovestream = function(e) {
 console.log(e);
 $('#'+userVideoID+"Container").remove();
 };
 others[user].pc2.onsignalingstatechange = function(e) {
 console.log(e);
 };
 };
 others[user].pc2.onicecandidate = function(e) {
 onIceCandidateR(user, e);
 };
 others[user].pc2.oniceconnectionstatechange = function(e) {
 console.log(e);
 };
 others[user].pc2.setRemoteDescription(new RTCSessionDescription(desc), function() {}, onSetSessionDescriptionError);
 others[user].pc2.createAnswer(function(desc) {onCreateAnswerSuccess(user, others[user].pc2, desc)}, onCreateSessionDescriptionError, sdpConstraints);
 }

 function onIceCandidate(user, event) {
 if (event.candidate) {
 websock.messageUser(user, discussid, "ice", JSON.stringify(event.candidate));
 }
 }

 function onIceCandidateR(user, event) {
 if (event.candidate) {
 websock.messageUser(user, discussid, "rice", JSON.stringify(event.candidate));
 }
 }

 function onCreateSessionDescriptionError(error) {
 console.log('Failed to create session description: ' + error.toString());
 }

 function onSetSessionDescriptionError(error) {
 console.log('Failed to set session description: ' + error.toString());
 }

 function onCreateOfferSuccess(user, pc, desc) {
 pc.setLocalDescription(desc, function() {}, onSetSessionDescriptionError);

 websock.messageUser(user, discussid, "desc", JSON.stringify(desc));
 console.log(desc);
 }

 function onCreateAnswerSuccess(user, pc, desc) {
 pc.setLocalDescription(desc, function() {}, onSetSessionDescriptionError);

 websock.messageUser(user, discussid, "rdesc", JSON.stringify(desc));
 console.log(desc);
 }


 }*/

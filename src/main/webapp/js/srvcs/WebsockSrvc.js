/**
 * Websock library to interface with web sockets
 * Created by Saswat on 1/6/2016.
 */

define({
    name: "websock"
    , service: /*@ngInject*/ ($rootScope, SockJS) => {
        let lastHeartbeat;
        let lastPing;
        let sock;
        let url;
        let websock = {};
        let eventCallbacks = {};
        let eventChannelCallbacks = {};
        let reconnectTime = 0;

        websock.connect = (connect_url) => {//todo implement reconnect
            url = connect_url;
            sock = new SockJS(url);
            sock.onopen = on_open;
            sock.onmessage = on_message;
            sock.onclose = on_close;
        };

        websock.disconnect = () => {
            if(sock) {
                url = null;
                sock.close();
                sock = null;
            }
        };

        websock.login = (userToken, deviceToken) => {
            let message = {};
            message[WSQ_LOGIN_MESSAGE_USER_TOKEN] = userToken;
            message[WSQ_LOGIN_MESSAGE_DEVICE_TOKEN] = deviceToken;
            websock.message({name:WSEName.LOGIN, data: message});
        };

        websock.logout = () => {
            websock.message({name:WSEName.LOGOUT, data:{}});
            url = null; //prevents reconnect when the server disconnects
        };

        websock.quickMode = () => {
            websock.message({name: WSEName.QUICKMODE});
        };

        websock.message = (obj) => {
            if(!sock) return;
            console.log("sent", obj);
            sock.send(JSON.stringify(obj));
        };

        websock.sendEvent = (name, data) => {
            websock.message({name:name, data:data});
        };

        websock.sendChannelEvent = (channel, action, message = {}) => {
            websock.sendEvent("message", {action: action, channel: channel, message: message});
        };

        websock.testConnection = () => {
            if(!sock) return;
            sock.send("ping");
            setTimeout(() => {
                if(new Date().getTime() - lastPing > 1000) {
                    sock.close();
                }
            }, 750);
        };

        websock.on = (name, callback) => {
            eventCallbacks[name] = eventCallbacks[name] || [];
            eventCallbacks[name].push(callback);
        };

        websock.onChannelEvent = (name, callback) => {
            eventChannelCallbacks[name] = eventChannelCallbacks[name] || [];
            eventChannelCallbacks[name].push(callback);
        };

        function on_message(e) {
            if(!sock) return;
            if(e.data == "heartbeat") {
                lastHeartbeat = new Date().getTime();
                sock.send("heartbeat");
                setTimeout(() => {
                    if(sock && new Date().getTime() - lastHeartbeat > 30000) {
                        sock.close();
                        sock = null;
                    }
                }, 30200);
            } else if(e.data == "ping") {
                sock.send("pong");
            } else if(e.data == "pong") {
                lastPing = new Date().getTime();
            } else {
                let msg = JSON.parse(e.data);
                console.log('message', msg);
                if("name" in msg && "data" in msg && "action" in msg.data && "message" in msg.data &&
                        msg.name.startsWith("channel-") && msg.data.action in eventChannelCallbacks) {
                    let channel = msg.name.substring("channel-".length);
                    for(let callback of eventChannelCallbacks[msg.data.action]) {
                        callback(channel, msg.data.message);
                    }
                } else if("name" in msg && "data" in msg && msg.name in eventCallbacks) {
                    for(let callback of eventCallbacks[msg.name]) {
                        callback(msg.data);
                    }
                } else {
                    console.log("error, unhandled message: ", msg);
                    //$rootScope.$broadcast('websockMessaged', msg);
                }
            }
        }
        function on_open() {
            if(sock.readyState === SockJS.OPEN) {
                reconnectTime = 0;
                $rootScope.$broadcast('websockOpened');
            } else {
                //sometimes the sockjs connection isn't ready immediately
                //so we wait until it is
                setTimeout(on_open, 1);
            }
        }
        function on_close() {
            $rootScope.$broadcast('websockClosed');
            reconnect();
        }

        function reconnect() {
            if(url) {
                setTimeout(() => {
                    websock.connect(url);
                }, reconnectTime);
                //exponential backoff to prevent the system from connecting too many times
                reconnectTime *= 1.5;
                //max reconnect time = 10 seconds
                if(reconnectTime > 10000) {
                    reconnectTime = 10000;
                } else if(reconnectTime <= 0) {
                    reconnectTime = 300;
                }
            }
        }

        function updateOnlineStatus() {
            const condition = navigator.onLine ? "online" : "offline";
            console.log("network "+condition);
            websock.testConnection();
        }
        window.addEventListener('online',  updateOnlineStatus);
        window.addEventListener('offline', updateOnlineStatus);

        return websock;
}});

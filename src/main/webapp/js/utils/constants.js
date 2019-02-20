/**
 * Created by Saswat on 10/25/2016.
 */
define({
    //todo run own stun/turn server
    iceServers: {'iceServers': [{'urls': 'stun:stun.services.mozilla.com'}, {'urls': 'stun:stun.l.google.com:19302'}]}
    , aHrefSanitizationWhitelist: /^\s*(https?|ftp|mailto|tel|file|blob|filesystem):/
});
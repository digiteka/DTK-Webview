/* Override share button click */
if (navigator.share == null) {
  navigator.share = (param) => {
     Android.androidShare(param.title, param.text, param.url);
  };
};

/* Inject css to disable tap highlight */
var node = document.createElement('style');
node.type = 'text/css';
node.innerHTML = '* { -webkit-tap-highlight-color: rgba(0,0,0,0); }';
document.head.appendChild(node);

/* Listen for postMessage events */
window.addEventListener('message', function(event) {
    if (event.data.videofeed === true && event.data.action === 'close') {

        console.log('Message received from webview:', event);
        Android.videoFeedClose();
    }
});
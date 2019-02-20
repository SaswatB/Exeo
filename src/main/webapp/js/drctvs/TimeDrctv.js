/**
 * Directive to show the current time for an element
 * Created by Saswat on 12/27/2016.
 */

define({
    name: "currentTime"
    , directive: /*@ngInject*/ ($interval, moment) => {
        // return the directive link function. (compile function not needed)
        return (scope, element, attrs) => {
            let format,  // date format
                stopTime; // so that we can cancel the time updates

            // used to update the UI
            function updateTime() {
                let str = moment().format(format);
                if(str != element.text()) {
                    element.text(str);
                }
            }

            // watch the expression, and update the UI on change.
            scope.$watch(attrs.currentTime, (value) => {
                format = value;
                updateTime();
            });

            stopTime = $interval(updateTime, 1000);

            // listen on DOM destroy (removal) event, and cancel the next UI update
            // to prevent updating time after the DOM element was removed.
            element.on('$destroy', () => {
                $interval.cancel(stopTime);
            });
        };
    }});
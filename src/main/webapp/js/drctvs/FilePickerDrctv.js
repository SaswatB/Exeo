/**
 * Directive to pick files nicely
 * Created by Saswat on 12/27/2016.
 */

define({
    name: "fileInput"
    , directive: /*@ngInject*/ ($interval, moment) => {
        // return the directive link function. (compile function not needed)
        return {
            restrict: 'E',
            scope: {
                ngModel:'='
            },
            template: //todo fix keyboard flow here
            `<label class="fileInput btn btn-default" tabIndex="1">`+
                `<input type="file" tabIndex="-1"/>`+
                `<span>Choose a file</span>`+
            `</label>`,
            link: (scope, element) => {
                let $input = element.find("input"),
                    $label = element.find("label");

                $input.on('change', (e) => {
                    let fileName = "";

                    if($input[0].files && $input[0].files.length > 1) {
                        fileName = $input[0].files.length + " Files Selected";
                    } else if(e.target.value) {
                        fileName = e.target.value.split('\\').pop();
                    }

                    if(fileName) {
                        $label.find('span').text(fileName);
                        scope.ngModel = $input[0].files[0];
                    } else {
                        $label.find('span').text("Choose a file");
                        scope.ngModel = undefined;
                    }
                });
            }
        };
    }});
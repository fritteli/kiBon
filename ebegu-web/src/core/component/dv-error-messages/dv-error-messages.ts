import {IComponentOptions} from 'angular';
let errTemplate =  require('./dv-error-messages.html');

export class DvErrorMessagesComponentConfig implements IComponentOptions {
    transclude = false;
    bindings: any = {
        errorObject: '<for'
    };
    template = errTemplate;
    controller = DvErrorMessages;
    controllerAs = 'vm';
}

export class DvErrorMessages {
}




'use strict';

javascript:(function () {
    let checkExistFunc = (selector, index) => {
        let checkExist = setInterval(() => {
            if (document.querySelectorAll(selector)[index] !== undefined) {
                document.querySelectorAll(selector)[index].click();
                console.log(`${selector} Exists!`);
                clearInterval(checkExist);
            }
        }, 1000);
    };

    /* For React 16, because React onChange event is synthetical，and use descriptor to intercept value, so we need to
set native input value here.
    */
    let setNativeInputValue = (ele, value) => {
        const nativeInputValueSetter = (Object)
            .getOwnPropertyDescriptor((window).HTMLInputElement.prototype, 'value')
            .set;
        nativeInputValueSetter.call(ele, value);
    };

    let setValueAndDispatchFunc = (ele, value, event) => {
        let ele = document.querySelector(ele);
        ele.value = value;
        if (event === 'input') {
            setNativeInputValue(ele, value);
        }
        ele.dispatchEvent(new Event(event, {bubbles: true}));
    };

    setValueAndDispatchFunc('input[name=make]', "9", 'blur');
    setValueAndDispatchFunc('input[name=model]', "1626", 'blur');
    setValueAndDispatchFunc('select[name=pricefrom]', "1000", 'change');

    let pic = document.querySelector('input[name=pic]');
    pic.click();

    checkExistFunc('button', 0);

    let powerFrom = 'input[name=powerfrom]';
    setValueAndDispatchFunc(powerFrom, "1000", 'input');
    powerFrom.dispatchEvent(new Event('blur', {bubbles: true}));

    let J = document.querySelector('input[name=J]');
    J.click();
})();




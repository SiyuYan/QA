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
    checkExistFunc('button', 0);
    checkExistFunc('img[aria-label=\'image\']', 0);
    checkExistFunc('a[aria-label=\'cars of dealers\']', 1);
})();

'use strict';

javascript:(function () {
    let changeValue = (selector, text) => {
        let sort = document.querySelector(selector);
        Array.prototype.slice.call(sort.options).map(item => {
            if (item.text === text) {
                sort.value = item.value
            }
        });
        sort.dispatchEvent(new Event('change', {bubbles: true}));
    };
    changeValue('select[name=search-result-sort]', 'Preis aufsteigend');
})();

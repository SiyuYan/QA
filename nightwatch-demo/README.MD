# Nightwatch
This is just a simple Demo about how to use Nightwatch to do web UI test.

## download dependency
```
npm install
```

## install reporter
```
npm install handlebars
```

## run with report
```
nightwatch tests/Amazon --reporter html-reporter.js
```

## run without report
```
nightwatch tests/Amazon
or
npm test tests/Amazon
```

## parallel running
```
nightwatch tests/GoogleTest --env default,firefox --reporter html-reporter.js
```
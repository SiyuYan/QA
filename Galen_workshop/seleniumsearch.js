driver.findElement(By.cssSelector("#address-service")).sendKeys("46 St Kilda Rd, St Kilda, VIC 3182");
console.log("***********************");
function pageIsLoaded() {
    return driver.findElement(By.cssSelector('button.property-search-submit')) != null;
}
console.log("***********************");
driver.findElement(By.cssSelector('button.property-search-submit')).click();
console.log("***********************");

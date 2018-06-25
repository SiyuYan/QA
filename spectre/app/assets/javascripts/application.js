// This is a manifest file that'll be compiled into application.js, which will include all the files
// listed below.
//
// Any JavaScript/Coffee file within this directory, lib/assets/javascripts, vendor/assets/javascripts,
// or any plugin's vendor/assets/javascripts directory can be referenced here using a relative path.
//
// It's not advisable to add code directly here, but if you do, it'll appear at the bottom of the
// compiled file.
//
// Read Sprockets README (https://github.com/rails/sprockets#sprockets-directives) for details
// about supported directives.
//
//= require jquery
//= require jquery_ujs
//= require_tree .

$(document).on('ready page:load', function () {

  $('.filters').each(function() {
    var form = $(this).find('form');
    var select = form.find('select');
    select.on('change', function() {
      form.submit();
    });
  });

  $('img.lazy').lazyload();

  // TODO: how is the JS being sturctured in this project?
  // ajax ui changes 'SET AS BASELINE' button
  $('.edit_test').on('ajax:success', function (e, data, status, xhr) {
    var button = $(this).find("input[type='submit']");
    var row = $(this).parents('tr:first');
    button.hide();
    row.find('.label--fail').removeClass('label--fail').addClass('label--pass').text('Pass');
  }).on('ajax:error', function (e, xhr, status, error) {
    var button = $(this).find('input[type="submit"]');
    button.prop('value', 'Error! Try again?');
  });

  new AnchorScroll();
});

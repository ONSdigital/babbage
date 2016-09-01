$(function() {
    $('.js-feedback-form').on("submit",function(e) {
        e.preventDefault(); // cancel the actual submit

        var $this = $(this),
            msg = 'Thank you for your feedback',
            msgHTML = $('<p class="margin-top--0">' + msg + '</p>');

        // insert success message and hide form
        msgHTML.insertAfter($this);
        $this.hide();


    });
});

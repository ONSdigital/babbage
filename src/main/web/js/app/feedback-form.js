$(function() {
    $('.js-feedback-form').on("submit",function(e) {
        e.preventDefault(); // cancel the actual submit

        var $form = $(this),
            msg = 'Thank you for your feedback',
            msgHTML = $('<p class="margin-top--0">' + msg + '</p>'),
            data = [],
            dataStr = '';

        // collect values and add to data array
        var found = $('input[name=found]:checked', '.js-feedback-form').val();
        if(found){found = "found=" + found; data.push(found)}

        var understood = found = $('input[name=understood]:checked', '.js-feedback-form').val();
        if(understood){understood = "understood=" + understood; data.push(understood)}

        var email = $('#email').val();
        if(email){email = "email=" + email; data.push(email)}

        var feedback = $('#feedback').val();
        if(feedback){feedback = "feedback=" + feedback; data.push(feedback)}

        // create data string
        for (var i = 0; i < data.length; i++) {
            // if value exists, add value to data string
            if(data[i]) {dataStr += data[i];}
            // if not last in array add ampersand
            if(i < (data.length - 1)) {dataStr += '&';}
        }

        // send to server
        $.ajax({
            url: '/feedback',
            type: "post",
            data: dataStr
        }).done(function() {
            msgHTML.insertAfter($form);
            $form.hide();
        });

    });
});

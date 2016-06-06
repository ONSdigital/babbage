$(function () {
    if ($('body').hasClass('compendium_landing_page')) {
        $('.js-print-chapters').click(function (e) {
            e.preventDefault();
            addLoadingOverlay();

            var $chapters =  $('.chapter'),
            chapterLength = $chapters.length;

            $chapters.each(function (index) {
                // Synchronously adds div with id to get around Ajax working asynchronously
                $('main').append("<div id='compendium-print" + index + "'></div>");

                var url = $(this).attr('href');

                // Set what content from each page we want to retrieve for printing
                var childIntro = ('.page-intro');
                var childContent = ('.page-content');

                // Get chapter content
                $.get(url, function (response) {
                    // Remove noscript tags around images, they break the charts when requested
                    var html = response.replace(/<\/noscript>/g, '').replace(/<noscript>/g, '');

                    // Add in print page breaks before each chapter and add to compendium landing page
                    var $response = $(html);
                    $response.find(childIntro).addClass('print--break-before').appendTo('#compendium-print' + index);
                    $response.find(childContent).appendTo('#compendium-print' + index);

                    chaptersComplete(index)
                });
            });

            // Tally number of chapters complete and print window when done
            function chaptersComplete(index) {
                if (index+1 == chapterLength) {
                    var $imgs = $('img');
                    console.log($imgs);
                    // Only open print window once all images are loaded
                    $imgs.on('load', function() {
                        window.print();
                        location.reload();
                    });
                }
            }
        });
    }
});
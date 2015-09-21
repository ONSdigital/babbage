//progressive enhancement (jQuery)

$(function() {
// jQuery(window).load(function() {

    var browserNotSupported = (function () {
        var div = document.createElement('DIV');
        // http://msdn.microsoft.com/en-us/library/ms537512(v=vs.85).aspx
        div.innerHTML = '<!--[if lte IE 7]><I></I><![endif]-->';
        return div.getElementsByTagName('I').length > 0;
    }());


    if (browserNotSupported) {
        setTimeout(function() {
            $('#loading-overlay').fadeOut(300);
        }, 500);
    } else {
        jsEnhance();
    }



    function jsEnhance() {
        $('.js-enhance--show').show();
        $('.js-enhance--hide').hide();
        $('.nojs-hidden').removeClass('nojs-hidden');

        //The order of these functions being called is important...
        jsEnhanceULNavToSelectNav();
        jsEnhanceClickableDiv();
        jsEnhanceLinechart();
        jsEnhanceSparkline();
        jsEnhancePrint();
        jsEnhanceNumberSeparator();
        jsEnhanceMarkdownCharts();

        jsEnhancePrintCompendium();
        jsEnhanceBoxHeight();
        jsEnhanceBoxHeightResize();
        //jsEnhanceTriggerAnalyticsEvent();
        jsEnhanceDownloadAnalytics();
        jsEnhanceAnchorAnalytics();
        jsEnhanceExternalLinks();

        jsEnhanceTableOfContents();
        jsEnhanceScrollToSection();

        jsEnhanceMobileTables();

        // prototypeModalButtons();

        // setTimeout(function() {
        //     jsEnhanceIframedTables();
        //     jsEnhanceMobileTables();
        // }, 400);

        // set jsEnhanced cookie for server side optimisations.
        var expires = new Date();
        expires.setDate(expires.getDate() + (10*365)); // 10 years
        document.cookie='jsEnhanced=true;expires=' + expires.toUTCString() + ';path=/';


        setTimeout(function() {
            $('#loading-overlay').fadeOut(300);
        }, 500);
    }

    function jsEnhanceULNavToSelectNav() {
        $('.js-enhance--ul-to-select').each(function() {
            var labeltext = $('p:first', this).text();
            var selectoptions = $('ul:first li a', this);


            //IE9 dosent like this...
            // var label = $('<label>', {
            //     class: 'definition-emphasis',
            //     text: labeltext
            // });
            var label = $(document.createElement('label'));
            label.attr('class', 'definition-emphasis');
            label.attr('text', labeltext);



            //$(document.createElement('select')) is faster
            // var newselect = $('<select>', {
            //     class: 'field field--spaced'
            // });
            var newselect = $(document.createElement('select'));
            newselect.attr('class', 'field field--spaced max-width');

            // convert to lower case and remove colon from end of string
            labeltext = labeltext.toLowerCase().substring(0, labeltext.length - 1);

            newselect.append($('<option>', {
                value: '',
                text: 'Select a  ' + labeltext
            }));

            newselect.change(function() {
                var location = $(this).find('option:selected').val();
                if (location) {
                    window.location = location;
                }
            });

            $.each(selectoptions, function(i, item) {
                newselect.append($('<option>', {
                    value: $(this).attr('href'),
                    text: $(this).text()
                }));
            });

            label.append(newselect);

            $(this).html(label);

        });
    }

    function jsEnhanceClickableDiv() {
      // get any content with clickable-wrap class
      var clickableDiv = $('.clickable-wrap');
      var hoverHashTable = {};
      hoverHashTable['tiles__item'] = ['tiles__item--hover'];
      hoverHashTable['tiles__item--list-type'] = ['tiles__item--list-type-hover'];
      hoverHashTable['tiles__item--list-type-simple'] = ['tiles__item--list-type-simple-hover'];
      hoverHashTable['tiles__item--nav-type-fixed'] = ['tiles__item--nav-type-fixed-hover'];
      hoverHashTable['tiles__content'] = ['tiles__content--hover'];
      hoverHashTable['sparkline-holder'] = ['sparkline-holder--hover'];
      hoverHashTable['image-holder'] = ['image-holder--hover'];
      hoverHashTable['tiles__image--headline-sparkline'] = ['tiles__image--headline-sparkline-hover'];
      hoverHashTable['tiles__title-dt'] = ['tiles__title-dt--hover'];
      hoverHashTable['tiles__title-h3'] = ['tiles__title-h3--hover'];
      hoverHashTable['tiles__title-h2--home'] = ['tiles__title-h2--home-hover'];
      hoverHashTable['tiles__title-h3--nav'] = ['tiles__title-h3--nav-hover'];
      hoverHashTable['tiles__title-h4'] = ['tiles__title-h4--hover'];
      hoverHashTable['tiles__content--nav'] = ['tiles__content--nav-hover'];
      hoverHashTable['tiles__read-more'] = ['tiles__read-more--hover'];



      // on click grab the first link of the content and go there
      $(clickableDiv).click(function() {
        var link = $('a:first', this).attr('href');
        window.location = link;
      });

      // on hover change the cursor so that it looks clickable
      $(clickableDiv).css({
          'cursor': 'pointer'
      });

      // change the background colour on hover
      $(clickableDiv).hover(function() {
        var elem = $(this);
        $.each(hoverHashTable, function(className, hoverClassName) {
            // $(elem).find('.'+className).css('background-color', 'red');
            $(elem).find('.'+className).addClass(hoverClassName[0]);
        });
      }, function() {
        var elem = $(this);
        $.each(hoverHashTable, function(className, hoverClassName) {
            $(elem).find('.'+hoverClassName[0]).removeClass(hoverClassName[0]);
        });
      });

      // check if there's a nav--block-landing to remove :after class
    //   if ($('.nav--block-landing')) {
    //       $(clickableDiv).hover(function() {
    //         $('.nav--block-landing').addClass('nav--block-landing--remove');
    //       }, function() {
    //         $('.nav--block-landing').removeClass('nav--block-landing--remove');
    //       });
    //     }
    }


    function jsEnhanceLinechart() {

        var chartContainer = $(".linechart");
        if (!chartContainer.length) {
            return;
        }

         chartContainer.each(function() {
            var $this = $(this);
            var uri = $this.data('uri');
            $this.empty();
            $.getJSON(uri+'/data', function(timeseries) {
                renderLineChart(timeseries);
            }).fail(function(d, textStatus, error) {
                // console.error("Failed reading timseries, status: " + textStatus + ", error: " + error)
            });
        });
    }

     function jsEnhanceSparkline() {

        var chartContainer = $(".sparkline");
        if (!chartContainer.length) {
            return;
        }
        chartContainer.each(function() {
            var $this = $(this);
            var uri = $this.data('uri');
            $this.empty();
            $.getJSON(uri+'/data?series', function(timeseries) {
                // console.log("Successfuly read timseries data");
                renderSparkline(timeseries, $this);
            }).fail(function(d, textStatus, error) {
                // console.error("Failed reading timseries, status: " + textStatus + ", error: " + error)
            });
        });
    }

    function stripTrailingSlash(str) {
        if(str.substr(-1) === '/') {
            return str.substr(0, str.length - 1);
        }
        return str;
    }

    function jsEnhanceMarkdownCharts() {

        Highcharts.setOptions({
            lang: {
                thousandsSep: ','
            }
        });

        var chartContainer = $(".markdown-chart");
        if (!chartContainer.length) {
            return;
        }

        chartContainer.each(function() {
            var $this = $(this);
            var id = $this.attr('id');
            var uri = $this.data('uri');
            $this.empty();

            if (uri.indexOf('/') !== 0) {
                uri = '/' + uri;
            }

            renderChartForUri(uri, id);
        });
    }

    function jsEnhancePrint() {
    $('#jsEnhancePrint').click(function()
        {
            window.print();
            return false;
        });
    }


    function jsEnhanceNumberSeparator() {
      // Adapted from http://stackoverflow.com/questions/14075014/jquery-function-to-to-format-number-with-commas-and-decimal
      $( ".stat__figure-enhance" ).each(function( index ) {
        //console.log( index + ": " + $( this ).text() );
        var number = $( this ).text();
        var n= number.toString().split(".");
        //Comma-fies the first part
        n[0] = n[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        // //Combines the two sections
        $( this ).text(n.join("."));
      });
    }

    function jsEnhancePrintCompendium() {
        $('#jsEnhancePrintCompendium').click(function(e) {
            addLoadingOverlay();

            $('.chapter').each(function(index) {
                // Synchronously adds div with id to get around Ajax working asynchronously
                $('main').append("<div id='compendium-print" + index + "'></div>")

                var url = $(this).attr('href');

                // Set what content from each page we want to retrieve for printing
                var childIntro = ('.page-intro');
                var childContent = ('.page-content');

                $.get(url, function(data){
                    $(data).find(childIntro).addClass('print--break-before').appendTo('#compendium-print' + index);
                    $(data).find(childContent).appendTo('#compendium-print' + index);
                })


                e.preventDefault();

            });

            $(document).ajaxStop(function() {
                window.print();
                location.reload();
            });
        });
    }

    //Set adjacent boxes to same height (eg data and headlines on T3). Flexbox not suitable for this case.
    function jsEnhanceBoxHeight() {
        if ($(window).width() > 608) {
            var highestBox = 0;
            $('.equal-height').each(function(){

                if($(this).height() > highestBox) {
                   highestBox = $(this).height();
               }
            });

            $('.equal-height').height(highestBox);
        }
    }

    //Resets the box heights on resize
    function jsEnhanceBoxHeightResize() {
        $( window ).resize(function() {
            $('.equal-height').height('auto');
            jsEnhanceBoxHeight();
        });
    }

    function jsEnhanceIframedTables() {
        $('iframe').each(function(i) {
            // markdown-table-container
            if($(this).contents().find('div').hasClass('markdown-table-container')) {
                // console.log('iframe with table found');
                $(this).contents().find('iframe').remove();
                $(this).contents().find('script').remove();
                $(this).contents().find('*').css('width','auto');
                $(this).contents().find('*').css('height','auto');

                // var iframedtable =  $(this).contents().find('table');

                var iframecontent = $(this).contents().find('body').html();

                // $('<div class="table-wrapper">' + iframecontent + '</div>').insertAfter($(this));
                $(iframecontent).insertAfter($(this));
                $(this).remove();
             }
        });
    }

    function jsEnhanceMobileTables() {
        //<span class=" icon-table" role="presentation"></span>
        // $('markdown-table-container').addClass('table-holder-mobile');

        $('<button class="btn btn--mobile-table-show">View table</button>').insertAfter($('.markdown-table-wrap'));
        $('<button class="btn btn--mobile-table-hide">Close table</button>').insertAfter($('.markdown-table-wrap table'));

        $('.btn--mobile-table-show').click(function(e) {
            // console.log($(this).closest('.markdown-table-container').find('.markdown-table-wrap'));
            $(this).closest('.markdown-table-container').find('.markdown-table-wrap').show();
        });

        $('.btn--mobile-table-hide').click(function(e) {
            // console.log($(this).closest('.markdown-table-container').find('.markdown-table-wrap'));
            // $(this).closest('.markdown-table-wrap').hide();
            $(this).closest('.markdown-table-wrap').css('display', '');
        });
    }

    // Trigger Google Analytic pageview event
    function jsEnhanceTriggerAnalyticsEvent(page) {
        if (typeof ga != "undefined") {
            ga('send', 'pageview', {
                'page': page
            });
        }
    }

    //Track file downloads in analytics
    function jsEnhanceDownloadAnalytics() {
        //Track generated file downloads (eg chart xlsx download)
        $('.download-analytics').click(function(){
            var downloadType = $(this).parent().attr('action');
            var path = $('#pagePath').text();
            var downloadTitle = $('#title').text();
            var downloadFormat = $(this).attr('value');

            if(downloadType == '/file') {
                var downloadType = '/download';
                var downloadFormat = 'xls';
            }

            // Charts don't contain file type information so 'png' hardcoded
            if(downloadType == '/chartimage') {
                downloadFormat = 'png';
            }

            var page = downloadType + ('?uri=') + path + ('/') + downloadTitle + '.' + downloadFormat;

            jsEnhanceTriggerAnalyticsEvent(page);
        });

        //Track uploaded file downloads
        $('.file-download-analytics').click(function(){
            var fileName = $(this).attr('href').split('=')[1];
            var page = '/download?' + fileName ;

            jsEnhanceTriggerAnalyticsEvent(page);
        });

        //Track click on 'print full report' link
        $('.print-analytics').click(function(){
        	var path = $('#pagePath').text();
        	var page = '/print?uri=' + path;

        	jsEnhanceTriggerAnalyticsEvent(page);
        });
    }

    function jsEnhanceAnchorAnalytics(){
        //Trigger analytics pageview on click of any # anchor
        $("a[href*='#']").click(function(e){
            var hash = $(this).attr('href');
            var page = window.location.pathname + hash;
            jsEnhanceTriggerAnalyticsEvent(page);
        });
    }

    function jsEnhanceExternalLinks() {
        function eachAnchor(excludedString) {
            //Add icon to links outside of ons.gov.uk domain
            $(excludedString).each(function(){
                if (this.host !== location.host && this.host.indexOf('ons.gov.uk') == -1) {
                    $(this).attr('target', '_blank');
                }
            });
        }
        eachAnchor('a[href^="http://"]');
        eachAnchor('a[href^="https://"]');
    }


    function jsEnhanceTableOfContents() {
        if($('body').contents().find('*').hasClass('page-content__main-content')) {

            //remove html and body height 100% to allow jquery scroll functions to work properly
            $('html, body').css('height', 'auto');


            //insert sticky wrapper
            var tocStickyWrap = $('<div class="table-of-contents--sticky__wrap print--hide"><div class="wrapper">');
            $(tocStickyWrap).insertAfter($('#toc'));
            $('.table-of-contents--sticky__wrap .wrapper').append('<h2 class="table-of-contents--sticky__heading">Table of contents</h2>');


            //create select list of sections
            var tocSelectList = $('<select class="table-of-contents--sticky__select ">');

            $(tocSelectList).append($('<option/>', {
                    value: '',
                    text : '-- Select a section --'
                }));

            $('#toc li a').each(function(i){
                i = i + 1;
                var text = i + '. ' + $(this).text();
                var href = $(this).attr('href');
                $(tocSelectList).append($('<option/>', {
                    value: href,
                    text : text
                }));
            });


            //add toc select to sticky wrapper
            $('.table-of-contents--sticky__wrap .wrapper').append(tocSelectList);

            $('.table-of-contents--sticky__select').change(function() {
                var location = $(this).find('option:selected').val();
                if (location) {
                    // expands section if accordion
                    var section = $(location);
                    if (section.hasClass('is-collapsed')) {
                      section.removeClass('is-collapsed').addClass('is-expanded');
                    }

                    var functionTrigger = true;

                    //animates scroll and offsets page to counteract sticky nav
                    $('html, body').animate({ scrollTop: $(location).offset().top - 105}, 1000, function(){
                        //stops function running twice - once for 'html' and another for 'body'
                        if(functionTrigger) {
                            //adds location hash to url without causing page to jump to it - credit to http://lea.verou.me/2011/05/change-url-hash-without-page-jump/
                            if (history.pushState) {
                                history.pushState(null, null, location);
                            }
                            //TODO Add hash to window.location in IE8-9 (don't support history.pushState)
                            // else {
                            //     window.location.hash = location;
                            //     $('html, body').scrollTop( $(location.hash).offset().top - 60 );
                            // }

                            var page = window.location.pathname + location;
                            jsEnhanceTriggerAnalyticsEvent(page);
                            functionTrigger = false;
                        }
                    });
                }
            });




            // sticky toc function that evaluates scroll position and activates the sticky toc as appropriate
            function stickyTOC() {
                var contentStart = $('.page-content__main-content').offset().top;
                var scrollTop = $(window).scrollTop();
                // console.log(scrollTop);
                if (scrollTop > contentStart) {
                    $('#toc').addClass('table-of-contents-ordered-list-hide');
                    // $('#toc').removeClass('table-of-contents-ordered-list');
                    $('.page-content__main-content').css('padding-top','96px');
                    $('.table-of-contents--sticky__wrap').show();
                } else {
                    // $('#toc').addClass('table-of-contents-ordered-list');
                    $('#toc').removeClass('table-of-contents-ordered-list-hide');
                    $('.page-content__main-content').css('padding-top','0');
                    $('.table-of-contents--sticky__wrap').hide();
                }
            }

            stickyTOC();
            $(window).scroll(function() {
                stickyTOC();
                // console.log($(window).scrollTop());
            });
        }
    }

    function jsEnhanceScrollToSection() {

        //Offsets page to make room for sticky nav if arrive on page directly at section
        $(window).load(function(){
            if (location.hash && $('.page-content__main-content').length > 0) {
                var contentStart = $('.page-content__main-content').offset().top;
                var scrollTop = $(window).scrollTop();

                if (scrollTop > contentStart) {
                    //console.log('scrollTop = ' + scrollTop + ' contentStart = ' + contentStart)
                    $(document).scrollTop( $(location.hash).offset().top - 60 );
                }
            }
        });

        //Animate scroll to anchor on same page
        $('.jsEnhanceAnimateScroll').click(function(e) {
            e.preventDefault();

            var target = this.hash;

            $('html, body').animate({scrollTop: $(target).offset().top}, 1000, function(){
                location.hash = target;

                //TODO Fix root cause of IE offsetting. Temporary fix:
                //$('html, body').scrollTop( $(location.hash).offset().top - 60 );
            });
        });
    }
});

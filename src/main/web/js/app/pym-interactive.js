
$(function() {
    $('div.pym-interactive').each(function(index, element) {
        var pymParent = new pym.Parent($(element).attr('id'), $(element).data('url'));
        pymParent.onMessage('height', function(height) {
            addIframeHeightToEmbedCode($(this), height);
        });
    });
});

function addIframeHeightToEmbedCode(container, height) {
    var interactiveId = container.attr('id');
    var input = document.getElementById("embed-" + interactiveId);
    input.value = buildEmbedCode(input.value, height);
}

function buildEmbedCode(embedCode, height) {
    // replace any existing height attributes caused by multiple
    // re-sizes when child page uses JS to hide on page elements
    if (embedCode.indexOf("height") !== -1) {
        return embedCode.replace(/height=(\"|')[^(\"|')]*(\"|') /, 'height="' + height + 'px" ')
    }

    return embedCode.substr(0, 7) + ' height="' + height + 'px" ' + embedCode.substr(8, embedCode.length);
}

var USERNAME = null;
var STARS_PER_PAGE = 5;

var currentStarsPage = 1;

function getStarsPanel() {
    return $('.panel-stars');
}

function getStarsFooter() {
    return getStarsPanel().find('.panel-footer');
}

function loadStars(increment) {
    // Request user data
    $.ajax({
        url: 'api/users/' + USERNAME,
        dataType: 'json',
        success: function(userData) {
            var allStars = userData.starred;
            var start = (currentStarsPage - 1) * STARS_PER_PAGE + STARS_PER_PAGE * increment;
            var end = Math.min(start + STARS_PER_PAGE, allStars.length + 1);
            var newStars = allStars.slice(start, end);
            var tbody = getStarsPanel().find('.panel-body').find('tbody');
            var content = '';
            var count = 0;

            for (var i in newStars) {
                if (!newStars.hasOwnProperty(i)) {
                    continue;
                }
                var star = newStars[i];

                // Request project data for this Star
                $.ajax({
                    url: 'api/project?pluginId=' + star,
                    dataType: 'json',
                    success: function(projectData) {
                        var href = projectData.href;
                        var slug = href.substr(href.lastIndexOf('/') + 1, href.length);
                        var category = projectData.category;

                        // Append project contents to result
                        content +=
                            '<tr>'
                            + '<td>'
                            +   '<a href="' + href + '">' + projectData.owner + '/<strong>' + slug + '</strong></a>'
                            +   '<div class="pull-right">'
                            +     '<span class="minor">' + projectData.recommended.version + '</span> '
                            +     '<i title="' + category.title + '" class="fa ' + category.icon + '"></i>'
                            +   '</div>'
                            + '</td>';

                        if (++count == newStars.length) {
                            // Done loading, set the table to the result
                            tbody.html(content);
                            currentStarsPage += increment;
                            var footer = getStarsFooter();
                            var prev = footer.find('.prev');

                            // Check if there is a last page
                            if (currentStarsPage > 1) {
                                prev.show();
                            } else {
                                prev.hide();
                            }

                            // Check if there is a next page
                            var next = footer.find('.next');
                            if (end < allStars.length) {
                                next.show();
                            } else {
                                next.hide();
                            }
                        }
                    }
                });
            }
        }
    });
}

$(function() {
    var footer = getStarsFooter();
    footer.find('.next').click(function() { loadStars(1); });
    footer.find('.prev').click(function() { loadStars(-1); });
    $('.alert').fadeIn('slow');
});

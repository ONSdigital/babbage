/**
 * Created by crispin on 21/10/2015.
 */

function initialiseTable() {
    var $table = $('.js-table-sort'),
        $tableHeaders = $table.find('thead th'),
        $tableBody = $table.find('tbody');

    // Wrap table headers in a button
    $tableHeaders.each(function (index) {
        var $this = $(this),
            headerText = $this.text(),
            btnClasses = (index == 1)? 'class="text-right"' : ''; // right align the 'value' header
        $this.wrapInner('<button ' + btnClasses + ' aria-label="Sort table by ' + headerText + '" aria-controls="table-tbody">');
    });

    // Add aria-controls destination id to table body
    $tableBody.attr('id', 'table-tbody').attr('');
}
initialiseTable();

function triggerSort(array, column, frequency, decimalPlaces) {

    // If frequency is 'months' then replace month values with actual numbers (stops alphabetical ordering breaking order)
    if (frequency == "months") {
        $.each(array, function (index) {
            //Select the date value of each object
            var date = array[index].date;

            //Replace month string with equivalent number
            array[index].date = date.replace('JAN', '01').replace('FEB', '02').replace('MAR', '03').replace('APR', '04').replace('MAY', '05').replace('JUN', '06').replace('JUL', '07').replace('AUG', '08').replace('SEP', '09').replace('OCT', '10').replace('NOV', '11').replace('DEC', '12');

        });

        //Sort array of table data
        array.sort(sortTable(column));

        // Now months have been sorted correct switch numbers back to month
        $.each(array, function (index) {
            var date = array[index].date;
            var year = date.substr(0, date.indexOf(' '));
            var month = date.substr(date.indexOf(' ') + 1);

            //Revert number back to month string
            array[index].date = year + " " + month.replace('01', 'JAN').replace('02', 'FEB').replace('03', 'MAR').replace('04', 'APR').replace('05', 'MAY').replace('06', 'JUN').replace('07', 'JUL').replace('08', 'AUG').replace('09', 'SEP').replace('10', 'OCT').replace('11', 'NOV').replace('12', 'DEC');

        });

    } else {
        //If not months then just sort array as normal
        array.sort(sortTable(column));

    }

    //Swaps whether inverse is true or not, so it always does the opposite from the previous click
    inverse = !inverse;

    //Rebuild the table with sorted array
    buildTable(array, decimalPlaces);
}

//Sorts table contents. Argument 'column' defines which column is being sorted
function sortTable(column) {
    return function (a, b) {
        var aData = a[column];
        var bData = b[column];

        //If inverted it sorts the opposite direction
        if (inverse == true) {
            return ((aData > bData) ? -1 : ((aData < bData) ? 1 : 0));
        } else {
            return ((aData < bData) ? -1 : ((aData > bData) ? 1 : 0));
        }
    }
}

//Get the data array and build the table body from it - TODO Should reuse renderTable function in linechart.js if possible
function buildTable(array, decimalPlaces) {
    
    var tbody = $('.js-table-sort').find('tbody');
    $(tbody).empty();

    for (var i = 0; i < array.length; i++) {
        var current = array[i];
        var currentValue = current.value;
        var tr = $(document.createElement('tr')).addClass('table__row');
        if (decimalPlaces > 0) { // Fixing sort removing '.0' from numbers
             currentValue = currentValue.toFixed(decimalPlaces);
        }
        tbody.append(tr);
        tr.append('<th scope="row" class="js-table-sort__data text-left">' + current.date + '</th>');
        tr.append('<td class="js-table-sort__data text-right">' + currentValue + '</td>');
    }
}

//Set the sort styling - ie the arrow is attached to the correct header and in the correction direction
function sortMarkup(type) {

    //Find table headers
    var tableHeaders = $('.js-table-sort thead').find('.js-table-sort__header');

    //If 'reset' passed to function then it'll return the styling to default
    if (type == 'reset') {

        //Find default header to reset to
        var defaultTableHeader = $(tableHeaders).filter('th:contains("Period")');

        //Reset arrow to appear on 'period' and show as ascending;
        $(defaultTableHeader).attr('aria-sort', 'ascending').attr('aria-pressed', 'true');

    } else {

        //Instead of reset, column name is passed into function and that column has sorted styling added to it
        var column = type,
            sortOrder;

        //If inverse then toggle whether asc or desc class is added
        if (inverse === true) {
            //sortOrder = 'sorted-asc';
            sortOrder = 'descending';
        } else if (inverse === false) {
            //sortOrder = 'sorted-desc';
            sortOrder = 'ascending';
        }

        //Remove existing aria-sort attributes from headers
        $(tableHeaders).each(function () {
            var $this = $(this);

            if ($this.is(column)) {
                $this.attr('aria-sort', sortOrder)
                    .find('button')
                    .attr('aria-pressed', 'true');
            } else {
                $this.removeAttr('aria-sort')
                    .find('button')
                    .attr('aria-pressed', 'false');
            }
        });
    }
}

//TEST TO SHOW WHAT DATA IS BEING PUSHED AT EACH CHART/TABLE CHANGE

//var callCount = 0;
//
//function logArray(array) {
//	callCount++;
//	console.log("/** (" + callCount + ") START GETTING DATA **/");
//	$.each(array, function (index){
//		console.log(array[index]);
//	})
//	console.log("/** (" + callCount + ") FINISHED GETTING DATA **/");
//}

//END OF TEST CODE



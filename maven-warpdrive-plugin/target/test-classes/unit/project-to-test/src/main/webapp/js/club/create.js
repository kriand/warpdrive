$(document).ready(function() {

    var geocoder = new google.maps.Geocoder();
    var latitude = $("input[name='latitude']").val();
    var longitude = $("input[name='longitude']").val();
    var latlng = new google.maps.LatLng(latitude, longitude)
    var myOptions = {zoom: 1,
        center: latlng,
        mapTypeId: google.maps.MapTypeId.HYBRID,
        mapTypeControl: true,
        scaleControl: true
    };
    var map = new google.maps.Map(document.getElementById("map"), myOptions);

    var image = '/images/flag.png';
    var marker = new google.maps.Marker({
        position:   latlng,
        map: map,      
        clickable: false
    });

    google.maps.event.addListener(map, 'click', function(event) {                    
        marker.setPosition(event.latLng);
        $("input[name='latitude']").val(event.latLng.lat());
        $("input[name='longitude']").val(event.latLng.lng());
        map.setCenter(event.latLng);

    });
    
        
    $("select#country\\.id").change(function(event) {
        $.getJSON("/country/jsonGetCode/" + $("select#country\\.id").val(),
                function(data) {
                    if (geocoder) {
                        geocoder.geocode({ 'address': data.code}, function(results, status) {
                            if (status == google.maps.GeocoderStatus.OK) {
                                if (status != google.maps.GeocoderStatus.ZERO_RESULTS) {
                                    map.setCenter(results[0].geometry.location);
                                    map.fitBounds(results[0].geometry.bounds);
                                }
                            }
                        });
                    }
                });
    });


    $("#countrySelector").change(function(event) {
        $.getJSON("/club/jsonListByCountry/" + $("#countrySelector").val(),
                function(data) {
                    $("#clubSelector").attr('disabled', false).empty().append("<option>" + strSelect + "</option>")
                    $.each(data, function() {
                        $("#clubSelector").append("<option value='" + this.id + "'>" + this.name + "</option>");
                    });                   
                });
    });

    $("#clubSelector").change(function(event) {
        window.location = "/course/create?club.id=" + $(this).val();
    });


});
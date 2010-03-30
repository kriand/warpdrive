$(document).ready(function(){

    var clubLocation = new GPoint(longitude, latitude);
	var map = new GMap(document.getElementById("map"));
	map.addControl(new GOverviewMapControl());
	map.addControl(new GSmallMapControl());
	map.addOverlay(new GMarker(clubLocation));								   
	map.centerAndZoom(clubLocation, 3);
	  
 });
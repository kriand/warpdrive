$(document).ready(function(){

    $("a.sizeInMem").click(function(event){
     $.get("/admin/ajaxSizeInMem/" + $(this).attr('id'), function(data) {
        alert(data);
     });
   });
 });
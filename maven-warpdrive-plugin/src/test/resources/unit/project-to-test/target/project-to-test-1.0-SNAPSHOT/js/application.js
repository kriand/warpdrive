$(document).ready(function(){
   $(".initiallyDisabled").attr("disabled", true);           
   $("div.message").highlightFade('yellow');
   $("div.error").highlightFade('yellow');
   $('.submitBtn').hover(
		function(){ $(this).addClass('submitBtnHover'); },
		function(){ $(this).removeClass('submitBtnHover');}
	);
    $('.submitBtn').click(
		function(){ $(this).closest("form").submit(); }
	);
    setTimeout('removeMessage()', 3000)

    $(".tooltip").tooltip({
        showURL: false
    });
});

function removeMessage() {
    $("div.message").slideUp();
}



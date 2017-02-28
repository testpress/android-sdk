// Set click listeners to the images in the html content to display the images as zoomable
var images = document.getElementsByTagName("img");
for (i = 0; i < images.length; i++) {
   images[i].onclick = (
       function() {
           var src = images[i].src;
           return function() {
               ImageHandler.onClickImage(src);
           }
       }
   )();
}
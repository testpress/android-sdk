// Set click listeners to the images in the html content to display the images as zoomable
var images = document.getElementsByTagName("img");
for (i = 0; i < images.length; i++) {
    var src = images[i].src;
        // Check if the image has the "no-click-listener" class
        if (!images[i].classList.contains("no-click-listener")) {
            images[i].setAttribute('onclick', 'ImageHandler.onClickImage(src)');
        }
}
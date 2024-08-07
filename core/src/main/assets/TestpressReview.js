
function onClickBookmarkButton() {
    BookmarkListener.onClickBookmark();
}

function onClickReportButton() {
    ReportListener.onClickReport();
}

function onClickPreviewFile(url) {
    FileListener.onClickPreviewFile(url);
}

function updateBookmarkButtonState(bookmarked) {
    var bookmarkImage = getElement("bookmark-image");
    var bookmarkText = getElement("bookmark-text");
    if (bookmarked) {
        bookmarkImage.src = "testpress_remove_bookmark.svg"
        bookmarkText.innerHTML = "Remove Bookmark"
    } else {
        bookmarkImage.src = "testpress_bookmark.svg"
        bookmarkText.innerHTML = "Bookmark this"
    }
    currentBookmarkState = bookmarked;
}

function displayBookmarkButton() {
    var bookmarkButton = getElement("bookmark-button");
    bookmarkButton.style.display = "flex";
}

function hideBookmarkButton() {
    var bookmarkButton = getElement("bookmark-button");
    bookmarkButton.style.display = "none";
}

function getElement(className) {
    return document.getElementsByClassName(className)[0];
}

function addLogo(logoUrl) {
    var imageContainer = `<div class='logo' id='logo' style='max-width:150px;padding: 20px;display:none;'>
            <img src='${logoUrl}'>
            </div>`;
    document.body.innerHTML =  imageContainer + document.body.innerHTML
}

function showLogo() {
    var logo = getElement("logo");
    logo.style.display = "block";
}

function hideLogo() {
    var logo = getElement("logo");
    logo.style.display = "none";
}

function addWatermark(logoUrl) {
    document.body.pseudoStyle("before", "background-image", `url(${logoUrl})`);
    document.body.classList.add("watermark");
}

function stopAudio(){
  audios=document.querySelectorAll('audio');
  for(i=0;i<audios.length;i++){
   audios[i].pause();
  }
}
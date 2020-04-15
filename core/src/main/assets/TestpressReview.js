
function onClickBookmarkButton() {
    BookmarkListener.onClickBookmark();
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

function showLogo(logoUrl) {
    var imageContainer = `<div class='logo' id='logo' style='max-width:150px;padding: 20px;display:none;'>
            <img src='${logoUrl}'>
            </div>`;
    document.body.innerHTML =  imageContainer + document.body.innerHTML
    var logo = getElement("logo");
    logo.style.display = "block";
}

function hideLogo() {
    var logo = getElement("logo");
    logo.style.display = "none";
}
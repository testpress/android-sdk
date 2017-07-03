// Render MathJax into elements

const renderOptions = {
    delimiters: [
        // Display elements as inline instead of displaying in separate line(Disabled display mode)
        {left: "$$", right: "$$", display: false},
        {left: "\\[", right: "\\]", display: false},
        {left: "\\(", right: "\\)", display: false},
    ],
};

var mathElements = document.getElementsByClassName('math-tex');
for (var i = 0; i < mathElements.length; i++) {
   renderMathInElement(mathElements.item(i), renderOptions);
}
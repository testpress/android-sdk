/* Javascript to handle the selection of options in test engine */

// Handle radio button

var selectedRadioOption;

function initRadioGroup(selectedRadioOptionId) {
    if (selectedRadioOptionId != null) {
        setRadioButtonState(document.getElementById(selectedRadioOptionId), true);
    }
}

function onRadioOptionClick(clickedOption) {
    if (selectedRadioOption == clickedOption) {
        setRadioButtonState(clickedOption, false);
        selectedRadioOption = null;
        OptionsSelectionListener.onCheckedChange(clickedOption.id, false, true);
    } else {
        if (selectedRadioOption) {
            setRadioButtonState(selectedRadioOption, false);
        }
        setRadioButtonState(clickedOption, true);
        OptionsSelectionListener.onCheckedChange(clickedOption.id, true, true);
    }
}

function setRadioButtonState(option, check) {
    radioButton = getWidget(option);
    if (check) {
        if (radioButton.className.match(/(?:^|\s)icon-radio-unchecked(?!\S)/)) {
            radioButton.className = radioButton.className.replace( /(?:^|\s)icon-radio-unchecked(?!\S)/g , '' );
        }
        radioButton.className += " icon-radio-checked2";
        setSelectedOptionBackground(option);
        selectedRadioOption = option;
    } else {
        if (radioButton.className.match(/(?:^|\s)icon-radio-checked2(?!\S)/)) {
            radioButton.className = radioButton.className.replace( /(?:^|\s)icon-radio-checked2(?!\S)/g , '' );
        }
        radioButton.className += " icon-radio-unchecked";
        removeBackground(option);
    }
}

// Handle check box

function initCheckBoxGroup(selectedCheckBoxOptionIds) {
    if (selectedCheckBoxOptionIds != null) {
        for (i = 0; i < selectedCheckBoxOptionIds.length; i++) {
            var optionItem = document.getElementById(selectedCheckBoxOptionIds[i]);
            setCheckboxState(optionItem, true);
        }
    }
}

function onCheckBoxOptionClick(option) {
    if (isCheckedOption(option)) {
        setCheckboxState(option, false);
        OptionsSelectionListener.onCheckedChange(option.id, false, false);
    } else {
        setCheckboxState(option, true);
        OptionsSelectionListener.onCheckedChange(option.id, true, false);
    }
}

function reviewButtonClick(button) {
    OptionsSelectionListener.onMarkStateChange();
    if(button.innerHTML == "MARKED") {
        button.innerHTML = "MARK FOR LATER";
        button.className = "unmark-button";
    } else {
        button.innerHTML = "MARKED";
        button.className = "mark-button";
    }
}

function onFileUploadClick(button) {
    OptionsSelectionListener.onFileUploadClick();
}

function onClearUploadsClick(button) {
    OptionsSelectionListener.onClearUploadsClick();
}

function setCheckboxState(option, check) {
    checkBox = getWidget(option);
    if (check) {
        if (checkBox.className.match(/(?:^|\s)icon-checkbox-unchecked(?!\S)/)) {
            checkBox.className = checkBox.className.replace( /(?:^|\s)icon-checkbox-unchecked(?!\S)/g , '' );
        }
        checkBox.className += " icon-checkbox-checked";
        setSelectedOptionBackground(option);
    } else {
        if (checkBox.className.match(/(?:^|\s)icon-checkbox-checked(?!\S)/)) {
            checkBox.className = checkBox.className.replace( /(?:^|\s)icon-checkbox-checked(?!\S)/g , '' );
        }
        checkBox.className += " icon-checkbox-unchecked";
        removeBackground(option);
    }
}

function onValueChange(element) {
   OptionsSelectionListener.onTextValueChange(element.value)
}

// Common functions

function getWidget(layout) {
    return document.getElementsByName(layout.id)[0];
}

function isCheckedOption(option) {
    return option.className.match(/(?:^|\s)option-item-selected(?!\S)/);
}

function setSelectedOptionBackground(layout) {
    layout.className += " option-item-selected";
}

function removeBackground(layout) {
    layout.className = layout.className.replace( /(?:^|\s)option-item-selected(?!\S)/g , '' );
}

function addWatermark(logoUrl) {
    document.body.pseudoStyle("before", "background-image", `url(${logoUrl})`);
    document.body.classList.add("watermark");
}

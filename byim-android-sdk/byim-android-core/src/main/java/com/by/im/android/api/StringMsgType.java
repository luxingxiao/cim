package com.by.im.android.api;

public enum StringMsgType {
    EMOJI("emoji", "表情"),
    TEXT("text", "普通文本"),
    LINK("link", "链接")
    ;

    private String code;
    private String description;
    private StringMsgType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}

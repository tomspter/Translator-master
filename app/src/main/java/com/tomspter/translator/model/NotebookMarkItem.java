package com.tomspter.translator.model;


public class NotebookMarkItem {

    // 原文
    private String input;
    // 译文
    private String output;

    public NotebookMarkItem(String input, String output) {
        this.input = input;
        this.output = output;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }
}

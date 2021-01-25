
package com.sprd.validationtools;

public class TestItem {
    private int indexInAll;
    private int result;

    public TestItem(int indexInAll) {
        this.indexInAll = indexInAll;
    }

    public Class getTestClass() {
        return Const.ALL_TEST_ITEM[indexInAll];
    }

    public int getTestTitle() {
        return Const.ALL_TEST_ITEM_STRID[indexInAll];
    }

    public String getTestname() {
        return Const.ALL_TEST_ITEM_NAME[indexInAll];
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }
}

package com.vish.firebasetest;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Date;

/**
 * Represents an expense (or income). Each object created
 * using the contructor has a unique #id object.
 * Created by vishy on 18/4/16.
 */
public class Expense {
    private Category category;
    private float amount;
    private String date;
    private String uid;
    private String id;

    /**
     * empty constructor, needed by firebase for deserialization.
     * https://www.firebase.com/docs/android/guide/retrieving-data.html
     */
    public Expense() {

    }

    /**
     *
     * Create an expense.
     * @param date
     * @param category category the user belongs to. a Category object.
     * @param uid user's firebase UID.
     * @param amount set the expense amount to < 0 to classify this as an Expense. Positive numbers
     *               are classified as income.
     */
    public Expense(String date, Category category, String uid, float amount) {
        this.date = date;
        this.category = category;
        this.uid = uid;
        this.amount = amount;
        this.id = RandomStringUtils.randomAlphabetic(10);
    }
}

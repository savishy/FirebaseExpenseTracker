package com.vish.firebasetest;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/**
 * Represents an Expense or Income Category.
 * Each object created using the constructor has an N-digit unique ID.
 * Created by vishy on 18/4/16.
 */
public class Category {
    private String id;
    private String name;
    private boolean expenseCategory;

    /**
     * empty constructor, needed by Firebase.
     * https://www.firebase.com/docs/android/guide/retrieving-data.html
     */
    public Category() {
    }

    public Category(String name, boolean expenseCategory) {
        this.id = RandomStringUtils.randomAlphabetic(10);
        this.name = name;
        this.expenseCategory = expenseCategory;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public boolean getExpenseCategory() {
        return expenseCategory;
    }
}

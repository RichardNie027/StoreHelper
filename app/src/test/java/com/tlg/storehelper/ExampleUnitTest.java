package com.tlg.storehelper;

import android.content.ContentValues;

import com.nec.utils.SQLiteUtil;
import com.tlg.storehelper.dao.Inventory;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        Inventory inventory = new Inventory(0L, "704", new Date(), 1, "940301", "A123456-789", new Date(), new Date());
        ContentValues cv = SQLiteUtil.toContentValues(inventory);
        assertEquals("940301", cv.getAsString("username"));
    }
}
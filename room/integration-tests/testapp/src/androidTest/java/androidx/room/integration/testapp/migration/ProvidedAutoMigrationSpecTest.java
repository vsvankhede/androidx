/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.room.integration.testapp.migration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.ColumnInfo;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ProvidedAutoMigrationSpec;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Test custom database migrations.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProvidedAutoMigrationSpecTest {
    private static final String TEST_DB = "auto-migration-test";
    private ProvidedAutoMigrationDb.MyProvidedAutoMigration mProvidedSpec =
            new ProvidedAutoMigrationDb.MyProvidedAutoMigration("Hi");

    @Rule
    public MigrationTestHelper helper;

    public ProvidedAutoMigrationSpecTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                ProvidedAutoMigrationDb.class.getCanonicalName());
    }

    @Database(
            version = ProvidedAutoMigrationDb.LATEST_VERSION,
            entities = {
                    ProvidedAutoMigrationDb.Entity1.class,
                    ProvidedAutoMigrationDb.Entity2.class
            },
            autoMigrations = {
                    @AutoMigration(
                            from = 1, to = 2, spec =
                            ProvidedAutoMigrationDb.MyProvidedAutoMigration.class
                    )
            },
            exportSchema = true
    )
    public abstract static class ProvidedAutoMigrationDb extends RoomDatabase {
        static final int LATEST_VERSION = 2;

        /**
         * No change between versions.
         */
        @Entity
        static class Entity1 {
            public static final String TABLE_NAME = "Entity1";
            @PrimaryKey
            public int id;
            public String name;
            @ColumnInfo(defaultValue = "1")
            public int addedInV1;
        }

        /**
         * A new table added.
         */
        @Entity
        static class Entity2 {
            public static final String TABLE_NAME = "Entity2";
            @PrimaryKey
            public int id;
            public String name;
            @ColumnInfo(defaultValue = "1")
            public int addedInV1;
            @ColumnInfo(defaultValue = "2")
            public int addedInV2;
        }

        @ProvidedAutoMigrationSpec
        static class MyProvidedAutoMigration implements AutoMigrationSpec {
            private final String mPrefString;
            public boolean mOnPostMigrateCalled = false;

            MyProvidedAutoMigration(String prefString) {
                this.mPrefString = prefString;
            }

            @Override
            public void onPostMigrate(@NonNull SupportSQLiteDatabase db) {
                mOnPostMigrateCalled = true;
            }
        }
    }

    // Run this to create the very 1st version of the db.
    public void createFirstVersion() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 2);
        db.close();
    }

    @Test
    public void testOnPostMigrate() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);
        ProvidedAutoMigrationDb autoMigrationDbV2 = getLatestDb();
        helper.runMigrationsAndValidate(
                TEST_DB,
                2,
                true
        );
        assertThat(mProvidedSpec.mOnPostMigrateCalled, is(true));
    }

    /**
     * Verifies that the user defined migration is selected over using an autoMigration.
     */
    @Test
    public void testNoSpecProvidedInConfig() {
        try {
            ProvidedAutoMigrationDb autoMigrationDbV2 = Room.databaseBuilder(
                    InstrumentationRegistry.getInstrumentation().getTargetContext(),
                    ProvidedAutoMigrationDb.class, TEST_DB).build();
        } catch (IllegalArgumentException exception) {
            assertThat(
                    exception.getMessage(),
                    containsString(
                            "A required auto migration spec (androidx.room.integration.testapp"
                                    + ".migration.ProvidedAutoMigrationSpecTest"
                                    + ".ProvidedAutoMigrationDb.MyProvidedAutoMigration) is "
                                    + "missing in the database configuration."
                    )
            );
        }
    }

    private ProvidedAutoMigrationDb getLatestDb() {
        ProvidedAutoMigrationDb db = Room.databaseBuilder(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ProvidedAutoMigrationDb.class, TEST_DB)
                .addAutoMigrationSpec(mProvidedSpec)
                .build();
        db.getOpenHelper().getWritableDatabase(); // trigger open
        helper.closeWhenFinished(db);
        return db;
    }
}

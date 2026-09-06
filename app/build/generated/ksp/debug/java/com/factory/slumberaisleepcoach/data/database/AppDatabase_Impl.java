package com.factory.slumberaisleepcoach.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile SleepSessionDao _sleepSessionDao;

  private volatile SnoringRecordDao _snoringRecordDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `sleep_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `durationMs` INTEGER NOT NULL, `qualityScore` INTEGER NOT NULL, `deepSleepMs` INTEGER NOT NULL, `lightSleepMs` INTEGER NOT NULL, `remSleepMs` INTEGER NOT NULL, `awakeMs` INTEGER NOT NULL, `snoringEvents` INTEGER NOT NULL, `snoringDurationMs` INTEGER NOT NULL, `notes` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `snoring_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `maxAmplitude` REAL NOT NULL, `avgAmplitude` REAL NOT NULL, FOREIGN KEY(`sessionId`) REFERENCES `sleep_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_snoring_records_sessionId` ON `snoring_records` (`sessionId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'daff1684b5853f9cbe726a2908f824a2')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `sleep_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `snoring_records`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSleepSessions = new HashMap<String, TableInfo.Column>(12);
        _columnsSleepSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("endTime", new TableInfo.Column("endTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("qualityScore", new TableInfo.Column("qualityScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("deepSleepMs", new TableInfo.Column("deepSleepMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("lightSleepMs", new TableInfo.Column("lightSleepMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("remSleepMs", new TableInfo.Column("remSleepMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("awakeMs", new TableInfo.Column("awakeMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("snoringEvents", new TableInfo.Column("snoringEvents", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("snoringDurationMs", new TableInfo.Column("snoringDurationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepSessions.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSleepSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSleepSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSleepSessions = new TableInfo("sleep_sessions", _columnsSleepSessions, _foreignKeysSleepSessions, _indicesSleepSessions);
        final TableInfo _existingSleepSessions = TableInfo.read(db, "sleep_sessions");
        if (!_infoSleepSessions.equals(_existingSleepSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "sleep_sessions(com.factory.slumberaisleepcoach.data.entities.SleepSession).\n"
                  + " Expected:\n" + _infoSleepSessions + "\n"
                  + " Found:\n" + _existingSleepSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsSnoringRecords = new HashMap<String, TableInfo.Column>(6);
        _columnsSnoringRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnoringRecords.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnoringRecords.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnoringRecords.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnoringRecords.put("maxAmplitude", new TableInfo.Column("maxAmplitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSnoringRecords.put("avgAmplitude", new TableInfo.Column("avgAmplitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSnoringRecords = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysSnoringRecords.add(new TableInfo.ForeignKey("sleep_sessions", "CASCADE", "NO ACTION", Arrays.asList("sessionId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSnoringRecords = new HashSet<TableInfo.Index>(1);
        _indicesSnoringRecords.add(new TableInfo.Index("index_snoring_records_sessionId", false, Arrays.asList("sessionId"), Arrays.asList("ASC")));
        final TableInfo _infoSnoringRecords = new TableInfo("snoring_records", _columnsSnoringRecords, _foreignKeysSnoringRecords, _indicesSnoringRecords);
        final TableInfo _existingSnoringRecords = TableInfo.read(db, "snoring_records");
        if (!_infoSnoringRecords.equals(_existingSnoringRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "snoring_records(com.factory.slumberaisleepcoach.data.entities.SnoringRecord).\n"
                  + " Expected:\n" + _infoSnoringRecords + "\n"
                  + " Found:\n" + _existingSnoringRecords);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "daff1684b5853f9cbe726a2908f824a2", "f4493e0be0442d2bd74e5681d01cfd6e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "sleep_sessions","snoring_records");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `sleep_sessions`");
      _db.execSQL("DELETE FROM `snoring_records`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SleepSessionDao.class, SleepSessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SnoringRecordDao.class, SnoringRecordDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SleepSessionDao sleepSessionDao() {
    if (_sleepSessionDao != null) {
      return _sleepSessionDao;
    } else {
      synchronized(this) {
        if(_sleepSessionDao == null) {
          _sleepSessionDao = new SleepSessionDao_Impl(this);
        }
        return _sleepSessionDao;
      }
    }
  }

  @Override
  public SnoringRecordDao snoringRecordDao() {
    if (_snoringRecordDao != null) {
      return _snoringRecordDao;
    } else {
      synchronized(this) {
        if(_snoringRecordDao == null) {
          _snoringRecordDao = new SnoringRecordDao_Impl(this);
        }
        return _snoringRecordDao;
      }
    }
  }
}

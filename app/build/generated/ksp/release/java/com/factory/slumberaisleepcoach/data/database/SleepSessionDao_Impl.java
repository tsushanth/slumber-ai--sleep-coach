package com.factory.slumberaisleepcoach.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.factory.slumberaisleepcoach.data.entities.SleepSession;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SleepSessionDao_Impl implements SleepSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SleepSession> __insertionAdapterOfSleepSession;

  private final EntityDeletionOrUpdateAdapter<SleepSession> __deletionAdapterOfSleepSession;

  private final EntityDeletionOrUpdateAdapter<SleepSession> __updateAdapterOfSleepSession;

  public SleepSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSleepSession = new EntityInsertionAdapter<SleepSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sleep_sessions` (`id`,`startTime`,`endTime`,`durationMs`,`qualityScore`,`deepSleepMs`,`lightSleepMs`,`remSleepMs`,`awakeMs`,`snoringEvents`,`snoringDurationMs`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SleepSession entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getStartTime());
        if (entity.getEndTime() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEndTime());
        }
        statement.bindLong(4, entity.getDurationMs());
        statement.bindLong(5, entity.getQualityScore());
        statement.bindLong(6, entity.getDeepSleepMs());
        statement.bindLong(7, entity.getLightSleepMs());
        statement.bindLong(8, entity.getRemSleepMs());
        statement.bindLong(9, entity.getAwakeMs());
        statement.bindLong(10, entity.getSnoringEvents());
        statement.bindLong(11, entity.getSnoringDurationMs());
        statement.bindString(12, entity.getNotes());
      }
    };
    this.__deletionAdapterOfSleepSession = new EntityDeletionOrUpdateAdapter<SleepSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `sleep_sessions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SleepSession entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSleepSession = new EntityDeletionOrUpdateAdapter<SleepSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sleep_sessions` SET `id` = ?,`startTime` = ?,`endTime` = ?,`durationMs` = ?,`qualityScore` = ?,`deepSleepMs` = ?,`lightSleepMs` = ?,`remSleepMs` = ?,`awakeMs` = ?,`snoringEvents` = ?,`snoringDurationMs` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SleepSession entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getStartTime());
        if (entity.getEndTime() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEndTime());
        }
        statement.bindLong(4, entity.getDurationMs());
        statement.bindLong(5, entity.getQualityScore());
        statement.bindLong(6, entity.getDeepSleepMs());
        statement.bindLong(7, entity.getLightSleepMs());
        statement.bindLong(8, entity.getRemSleepMs());
        statement.bindLong(9, entity.getAwakeMs());
        statement.bindLong(10, entity.getSnoringEvents());
        statement.bindLong(11, entity.getSnoringDurationMs());
        statement.bindString(12, entity.getNotes());
        statement.bindLong(13, entity.getId());
      }
    };
  }

  @Override
  public Object insertSession(final SleepSession session,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSleepSession.insertAndReturnId(session);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSession(final SleepSession session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSleepSession.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSession(final SleepSession session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSleepSession.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SleepSession>> getAllSessions() {
    final String _sql = "SELECT * FROM sleep_sessions ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sleep_sessions"}, new Callable<List<SleepSession>>() {
      @Override
      @NonNull
      public List<SleepSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfQualityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "qualityScore");
          final int _cursorIndexOfDeepSleepMs = CursorUtil.getColumnIndexOrThrow(_cursor, "deepSleepMs");
          final int _cursorIndexOfLightSleepMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lightSleepMs");
          final int _cursorIndexOfRemSleepMs = CursorUtil.getColumnIndexOrThrow(_cursor, "remSleepMs");
          final int _cursorIndexOfAwakeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "awakeMs");
          final int _cursorIndexOfSnoringEvents = CursorUtil.getColumnIndexOrThrow(_cursor, "snoringEvents");
          final int _cursorIndexOfSnoringDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "snoringDurationMs");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<SleepSession> _result = new ArrayList<SleepSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SleepSession _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final int _tmpQualityScore;
            _tmpQualityScore = _cursor.getInt(_cursorIndexOfQualityScore);
            final long _tmpDeepSleepMs;
            _tmpDeepSleepMs = _cursor.getLong(_cursorIndexOfDeepSleepMs);
            final long _tmpLightSleepMs;
            _tmpLightSleepMs = _cursor.getLong(_cursorIndexOfLightSleepMs);
            final long _tmpRemSleepMs;
            _tmpRemSleepMs = _cursor.getLong(_cursorIndexOfRemSleepMs);
            final long _tmpAwakeMs;
            _tmpAwakeMs = _cursor.getLong(_cursorIndexOfAwakeMs);
            final int _tmpSnoringEvents;
            _tmpSnoringEvents = _cursor.getInt(_cursorIndexOfSnoringEvents);
            final long _tmpSnoringDurationMs;
            _tmpSnoringDurationMs = _cursor.getLong(_cursorIndexOfSnoringDurationMs);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new SleepSession(_tmpId,_tmpStartTime,_tmpEndTime,_tmpDurationMs,_tmpQualityScore,_tmpDeepSleepMs,_tmpLightSleepMs,_tmpRemSleepMs,_tmpAwakeMs,_tmpSnoringEvents,_tmpSnoringDurationMs,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SleepSession>> getRecentSessions() {
    final String _sql = "SELECT * FROM sleep_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC LIMIT 7";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sleep_sessions"}, new Callable<List<SleepSession>>() {
      @Override
      @NonNull
      public List<SleepSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfQualityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "qualityScore");
          final int _cursorIndexOfDeepSleepMs = CursorUtil.getColumnIndexOrThrow(_cursor, "deepSleepMs");
          final int _cursorIndexOfLightSleepMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lightSleepMs");
          final int _cursorIndexOfRemSleepMs = CursorUtil.getColumnIndexOrThrow(_cursor, "remSleepMs");
          final int _cursorIndexOfAwakeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "awakeMs");
          final int _cursorIndexOfSnoringEvents = CursorUtil.getColumnIndexOrThrow(_cursor, "snoringEvents");
          final int _cursorIndexOfSnoringDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "snoringDurationMs");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<SleepSession> _result = new ArrayList<SleepSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SleepSession _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final int _tmpQualityScore;
            _tmpQualityScore = _cursor.getInt(_cursorIndexOfQualityScore);
            final long _tmpDeepSleepMs;
            _tmpDeepSleepMs = _cursor.getLong(_cursorIndexOfDeepSleepMs);
            final long _tmpLightSleepMs;
            _tmpLightSleepMs = _cursor.getLong(_cursorIndexOfLightSleepMs);
            final long _tmpRemSleepMs;
            _tmpRemSleepMs = _cursor.getLong(_cursorIndexOfRemSleepMs);
            final long _tmpAwakeMs;
            _tmpAwakeMs = _cursor.getLong(_cursorIndexOfAwakeMs);
            final int _tmpSnoringEvents;
            _tmpSnoringEvents = _cursor.getInt(_cursorIndexOfSnoringEvents);
            final long _tmpSnoringDurationMs;
            _tmpSnoringDurationMs = _cursor.getLong(_cursorIndexOfSnoringDurationMs);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new SleepSession(_tmpId,_tmpStartTime,_tmpEndTime,_tmpDurationMs,_tmpQualityScore,_tmpDeepSleepMs,_tmpLightSleepMs,_tmpRemSleepMs,_tmpAwakeMs,_tmpSnoringEvents,_tmpSnoringDurationMs,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSessionById(final long id,
      final Continuation<? super SleepSession> $completion) {
    final String _sql = "SELECT * FROM sleep_sessions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SleepSession>() {
      @Override
      @Nullable
      public SleepSession call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfQualityScore = CursorUtil.getColumnIndexOrThrow(_cursor, "qualityScore");
          final int _cursorIndexOfDeepSleepMs = CursorUtil.getColumnIndexOrThrow(_cursor, "deepSleepMs");
          final int _cursorIndexOfLightSleepMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lightSleepMs");
          final int _cursorIndexOfRemSleepMs = CursorUtil.getColumnIndexOrThrow(_cursor, "remSleepMs");
          final int _cursorIndexOfAwakeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "awakeMs");
          final int _cursorIndexOfSnoringEvents = CursorUtil.getColumnIndexOrThrow(_cursor, "snoringEvents");
          final int _cursorIndexOfSnoringDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "snoringDurationMs");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final SleepSession _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final int _tmpQualityScore;
            _tmpQualityScore = _cursor.getInt(_cursorIndexOfQualityScore);
            final long _tmpDeepSleepMs;
            _tmpDeepSleepMs = _cursor.getLong(_cursorIndexOfDeepSleepMs);
            final long _tmpLightSleepMs;
            _tmpLightSleepMs = _cursor.getLong(_cursorIndexOfLightSleepMs);
            final long _tmpRemSleepMs;
            _tmpRemSleepMs = _cursor.getLong(_cursorIndexOfRemSleepMs);
            final long _tmpAwakeMs;
            _tmpAwakeMs = _cursor.getLong(_cursorIndexOfAwakeMs);
            final int _tmpSnoringEvents;
            _tmpSnoringEvents = _cursor.getInt(_cursorIndexOfSnoringEvents);
            final long _tmpSnoringDurationMs;
            _tmpSnoringDurationMs = _cursor.getLong(_cursorIndexOfSnoringDurationMs);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _result = new SleepSession(_tmpId,_tmpStartTime,_tmpEndTime,_tmpDurationMs,_tmpQualityScore,_tmpDeepSleepMs,_tmpLightSleepMs,_tmpRemSleepMs,_tmpAwakeMs,_tmpSnoringEvents,_tmpSnoringDurationMs,_tmpNotes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Long> getAverageSleepDuration() {
    final String _sql = "SELECT AVG(durationMs) FROM sleep_sessions WHERE endTime IS NOT NULL AND durationMs > 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sleep_sessions"}, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Float> getAverageQuality() {
    final String _sql = "SELECT AVG(qualityScore) FROM sleep_sessions WHERE endTime IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sleep_sessions"}, new Callable<Float>() {
      @Override
      @Nullable
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if (_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getTotalSessionCount() {
    final String _sql = "SELECT COUNT(*) FROM sleep_sessions WHERE endTime IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sleep_sessions"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getTotalSnoringEvents() {
    final String _sql = "SELECT SUM(snoringEvents) FROM sleep_sessions WHERE endTime IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sleep_sessions"}, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

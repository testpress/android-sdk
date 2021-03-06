package in.testpress.models.greendao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.SqlUtils;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "PRICE".
*/
public class PriceDao extends AbstractDao<Price, Long> {

    public static final String TABLENAME = "PRICE";

    /**
     * Properties of entity Price.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property Name = new Property(1, String.class, "name", false, "NAME");
        public final static Property Price = new Property(2, String.class, "price", false, "PRICE");
        public final static Property Validity = new Property(3, Integer.class, "validity", false, "VALIDITY");
        public final static Property Start_date = new Property(4, String.class, "start_date", false, "START_DATE");
        public final static Property End_date = new Property(5, String.class, "end_date", false, "END_DATE");
        public final static Property ProductId = new Property(6, Long.class, "productId", false, "PRODUCT_ID");
    }

    private DaoSession daoSession;


    public PriceDao(DaoConfig config) {
        super(config);
    }
    
    public PriceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PRICE\" (" + //
                "\"ID\" INTEGER PRIMARY KEY ," + // 0: id
                "\"NAME\" TEXT," + // 1: name
                "\"PRICE\" TEXT," + // 2: price
                "\"VALIDITY\" INTEGER," + // 3: validity
                "\"START_DATE\" TEXT," + // 4: start_date
                "\"END_DATE\" TEXT," + // 5: end_date
                "\"PRODUCT_ID\" INTEGER);"); // 6: productId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PRICE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Price entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
 
        String price = entity.getPrice();
        if (price != null) {
            stmt.bindString(3, price);
        }
 
        Integer validity = entity.getValidity();
        if (validity != null) {
            stmt.bindLong(4, validity);
        }
 
        String start_date = entity.getStart_date();
        if (start_date != null) {
            stmt.bindString(5, start_date);
        }
 
        String end_date = entity.getEnd_date();
        if (end_date != null) {
            stmt.bindString(6, end_date);
        }
 
        Long productId = entity.getProductId();
        if (productId != null) {
            stmt.bindLong(7, productId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Price entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
 
        String price = entity.getPrice();
        if (price != null) {
            stmt.bindString(3, price);
        }
 
        Integer validity = entity.getValidity();
        if (validity != null) {
            stmt.bindLong(4, validity);
        }
 
        String start_date = entity.getStart_date();
        if (start_date != null) {
            stmt.bindString(5, start_date);
        }
 
        String end_date = entity.getEnd_date();
        if (end_date != null) {
            stmt.bindString(6, end_date);
        }
 
        Long productId = entity.getProductId();
        if (productId != null) {
            stmt.bindLong(7, productId);
        }
    }

    @Override
    protected final void attachEntity(Price entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Price readEntity(Cursor cursor, int offset) {
        Price entity = new Price( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // name
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // price
            cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3), // validity
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // start_date
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // end_date
            cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6) // productId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Price entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setPrice(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setValidity(cursor.isNull(offset + 3) ? null : cursor.getInt(offset + 3));
        entity.setStart_date(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setEnd_date(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setProductId(cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Price entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Price entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Price entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getProductDao().getAllColumns());
            builder.append(" FROM PRICE T");
            builder.append(" LEFT JOIN PRODUCT T0 ON T.\"PRODUCT_ID\"=T0.\"ID\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected Price loadCurrentDeep(Cursor cursor, boolean lock) {
        Price entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Product product = loadCurrentOther(daoSession.getProductDao(), cursor, offset);
        entity.setProduct(product);

        return entity;    
    }

    public Price loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<Price> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<Price> list = new ArrayList<Price>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<Price> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<Price> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}

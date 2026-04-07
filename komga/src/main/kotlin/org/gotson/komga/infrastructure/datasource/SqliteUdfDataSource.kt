package org.gotson.komga.infrastructure.datasource

import org.sqlite.SQLiteConnection
import org.sqlite.SQLiteDataSource
import java.sql.Connection

class SqliteUdfDataSource : SQLiteDataSource() {
    companion object {
        // These constants are maintained for backward compatibility
        // In a future version, they should be replaced with DatabaseUdfProvider
        const val UDF_STRIP_ACCENTS = "UDF_STRIP_ACCENTS"
        const val COLLATION_UNICODE_3 = "COLLATION_UNICODE_3"
    }
    
    private val udfProvider = SqliteUdfProvider()

    override fun getConnection(): Connection = super.getConnection().also { 
        udfProvider.initializeConnection(it as SQLiteConnection) 
    }

    override fun getConnection(
        username: String?,
        password: String?,
    ): SQLiteConnection = super.getConnection(username, password).also { 
        udfProvider.initializeConnection(it) 
    }
}

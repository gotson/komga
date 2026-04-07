package org.gotson.komga.infrastructure.datasource

import org.jooq.Field
import org.jooq.impl.DSL

interface DatabaseUdfProvider {
    val udfStripAccentsName: String
    val collationUnicode3Name: String
    
    fun Field<String>.udfStripAccents(): Field<String>
    fun Field<String>.collateUnicode3(): Field<String>
    
    fun initializeConnection(connection: Any)
}
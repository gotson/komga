package org.gotson.komga.domain.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import java.net.URL
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class Book(
    @Id
    @GeneratedValue
    val id: Long? = null,

    val name: String,
    val url: URL,
    val updated: LocalDateTime,

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonManagedReference
    var serie: Serie? = null
) {
    override fun toString(): String {
        return "Book((id=$id, name=$name, url=$url)"
    }
}
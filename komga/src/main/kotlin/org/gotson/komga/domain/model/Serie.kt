package org.gotson.komga.domain.model

import com.fasterxml.jackson.annotation.JsonBackReference
import java.net.URL
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class Serie(
    @Id
    @GeneratedValue
    val id: Long? = null,

    val name: String,
    val url: URL,
    val updated: LocalDateTime,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, mappedBy = "serie")
    @JsonBackReference
    val books: List<Book> = emptyList()
) {
    override fun toString(): String {
        return "Serie(id=$id, name=$name, url=$url)"
    }
}
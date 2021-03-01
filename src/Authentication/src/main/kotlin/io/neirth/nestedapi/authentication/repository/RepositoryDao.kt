package io.neirth.nestedapi.authentication.repository

import java.io.Closeable

interface RepositoryDao<T> : Closeable {
    fun insert(entity: T) : T
    fun update(entity: T) : T
    fun remove(entity: T)

    fun findAll() : List<T>
    fun findById(idEntity: Long) : T
}
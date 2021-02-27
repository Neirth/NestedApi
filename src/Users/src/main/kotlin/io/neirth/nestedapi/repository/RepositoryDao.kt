package io.neirth.nestedapi.repository

interface RepositoryDao<T> {
    fun insert(entity: T) : T
    fun update(entity: T) : T
    fun remove(entity: T) : Unit

    fun findAll() : List<T>
    fun findById(idEntity: Long) : T

    fun insertOrUpdate(entity: T) : T
}
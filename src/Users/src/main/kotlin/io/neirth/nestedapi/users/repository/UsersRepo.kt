/*
 * MIT License
 *
 * Copyright (c) 2021 NestedApi Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.neirth.nestedapi.users.repository

import javax.persistence.EntityManager
import io.neirth.nestedapi.users.domain.User
import java.sql.SQLDataException
import javax.enterprise.context.RequestScoped
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

@RequestScoped
class UsersRepo(private val entityManager: EntityManager): RepositoryDao<User> {
    /**
     * Method to insert persist data into the database
     *
     * @param entity Entity to persist
     * @return The entity persisted
     */
    @Transactional
    override fun insert(entity: User): User {
        entityManager.persist(entity)
        return entity
    }

    /**
     * Method to update data into the database
     *
     * @param entity Entity to persist
     * @return The entity persisted
     */
    @Transactional
    override fun update(entity: User): User {
        val entityAux : User = entityManager.find(User::class.java, entity.id)

        entityManager.transaction.begin()

        entityAux.address = entity.address
        entityAux.addressInformation = entity.addressInformation
        entityAux.birthday = entity.birthday
        entityAux.country = entity.country
        entityAux.email = entity.email
        entityAux.id = entity.id
        entityAux.name = entity.name
        entityAux.surname = entity.surname
        entityAux.telephone = entity.telephone

        entityManager.transaction.commit()

        return entityAux
    }

    /**
     * Method for remove entities from database
     *
     * @param entity The entity to remove
     */
    @Transactional
    override fun remove(entity: User) {
        entityManager.remove(if (entityManager.contains(entity)) entity else entityManager.merge(entity))
    }

    /**
     * Method for dump all records from database
     *
     * @return List of entities
     */
    override fun findAll(): List<User> {
       return entityManager.createQuery("from User", User::class.java).resultList.filterIsInstance<User>()
    }

    /**
     * Method for find the entity by user id in database
     *
     * @param idEntity The entity id searched
     * @return Entity found
     */
    override fun findById(idEntity: Long): User? {
        val result: List<User> = entityManager.createQuery("from User where id = :idEntity", User::class.java)
                                              .setParameter("idEntity", idEntity).resultList

        if (result.isNotEmpty()) {
            return result[0]
        } else {
            return null
        }
    }

    /**
     * Method for close the connection
     */
    override fun close() {
        entityManager.close()
    }
}
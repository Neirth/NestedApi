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
package io.neirth.nestedapi.authentication.repository

import io.neirth.nestedapi.authentication.domain.Credential
import javax.enterprise.context.RequestScoped
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.transaction.UserTransaction

@RequestScoped
class CredentialsRepo(private val entityManager: EntityManager): RepositoryDao<Credential> {
    /**
     * Method to insert persist data into the database
     *
     * @param entity Entity to persist
     * @return The entity persisted
     */
    @Transactional
    override fun insert(entity: Credential): Credential {
        // Call to persist data function
        entityManager.persist(entity)

        // Return the persisted data
        return entity
    }

    /**
     * Method to update data into the database
     *
     * @param entity Entity to persist
     * @return The entity persisted
     */
    @Transactional
    override fun update(entity: Credential): Credential {
        // Find the entity instance inside in database
        val entityAux : Credential = entityManager.find(Credential::class.java, entity.userId)

        // Set the new values
        entityAux.userId = entity.userId
        entityAux.password = entity.password

        // Return the changed object
        return entityAux
    }

    /**
     * Method for remove entities from database
     *
     * @param entity The entity to remove
     */
    @Transactional
    override fun remove(entity: Credential) {
        entityManager.remove(if (entityManager.contains(entity)) entity else entityManager.merge(entity))
    }

    /**
     * Method for dump all records from database
     *
     * @return List of entities
     */
    override fun findAll(): List<Credential> {
        return entityManager.createQuery("from Credential", Credential::class.java).resultList.filterIsInstance<Credential>()
    }

    /**
     * Method for find the entity by user id in database
     *
     * @param idEntity The entity id searched
     * @return List of entities
     */
    fun findByUserId(idEntity: Long): Credential? {
        // We create the query and run it
        val result: List<Credential> = entityManager.createQuery("from Credential where userId = :idEntity", Credential::class.java)
                                                    .setParameter("idEntity", idEntity).resultList

        // Check the result and send null if is empty
        return if (result.isNotEmpty()) {
            result[0]
        } else {
            null
        }
    }

    /**
     * Method for find the entity by username in database
     *
     * @param idEntity The entity id searched
     * @return List of entities
     */
    fun findByUsername(idEntity: String): Credential? {
        // We create the query and run it
        val result: List<Credential> = entityManager.createQuery("from Credential where username = :idEntity", Credential::class.java)
                                                    .setParameter("idEntity", idEntity).resultList

        // Check the result and send null if is empty
        return if (result.isNotEmpty()) {
            result[0]
        } else {
            null
        }
    }

    /**
     * Method for close the connection
     */
    override fun close() {
        entityManager.close()
    }
}
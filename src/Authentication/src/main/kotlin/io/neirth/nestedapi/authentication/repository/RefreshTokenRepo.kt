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

import io.neirth.nestedapi.authentication.domain.RefreshToken
import javax.enterprise.context.RequestScoped
import javax.persistence.EntityManager
import javax.transaction.Transactional

@RequestScoped
class RefreshTokenRepo(private val entityManager: EntityManager) : RepositoryDao<RefreshToken> {
    /**
     * Method to insert persist data into the database
     *
     * @param entity Entity to persist
     * @return The entity persisted
     */
    @Transactional
    override fun insert(entity: RefreshToken): RefreshToken {
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
    override fun update(entity: RefreshToken): RefreshToken {
        // Find the entity instance inside in database
        val entityAux: RefreshToken = entityManager.find(RefreshToken::class.java, entity.userId)

        // Set the new values
        entityAux.refreshToken = entity.refreshToken
        entityAux.userId = entity.userId
        entityAux.validFrom = entity.validFrom

        // Return the changed object
        return entityAux
    }

    /**
     * Method for remove entities from database
     *
     * @param entity The entity to remove
     */
    @Transactional
    override fun remove(entity: RefreshToken) {
        entityManager.remove(if (entityManager.contains(entity)) entity else entityManager.merge(entity))
    }

    /**
     * Method for dump all records from database
     *
     * @return List of entities
     */
    override fun findAll(): List<RefreshToken> {
        return entityManager.createQuery("from RefreshToken", RefreshToken::class.java).resultList.filterIsInstance<RefreshToken>()
    }

    /**
     * Method for find the entity by user id in database
     *
     * @param idEntity The entity id searched
     * @return List of entities
     */
    fun findByUserId(idEntity: Long): List<RefreshToken>? {
        // We create the query and run it
        val result: List<RefreshToken> = entityManager.createQuery("from RefreshToken where userId = :idEntity", RefreshToken::class.java)
                                                      .setParameter("idEntity", idEntity).resultList

        // Check the result and send null if is empty
        return if (result.isNotEmpty()) {
            result
        } else {
            null
        }
    }

    /**
     * Method for find the entity by refresh token in database
     *
     * @param idEntity The entity id searched
     * @return List of entities
     */
    fun findByRefreshToken(idEntity: String): RefreshToken? {
        // We create the query and run it
        val result: List<RefreshToken> = entityManager.createQuery("from RefreshToken where refreshToken = :idEntity", RefreshToken::class.java)
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
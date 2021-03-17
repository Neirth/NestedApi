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
import java.sql.SQLDataException
import javax.enterprise.context.RequestScoped
import javax.persistence.EntityManager
import javax.transaction.Transactional

@RequestScoped
class RefreshTokenRepo(private val entityManager: EntityManager) : RepositoryDao<RefreshToken> {
    @Transactional
    override fun insert(entity: RefreshToken): RefreshToken {
        entityManager.persist(entity)

        return entity
    }

    @Transactional
    override fun update(entity: RefreshToken): RefreshToken {
        val entityAux: RefreshToken = entityManager.find(RefreshToken::class.java, entity.userId)

        entityManager.transaction.begin()

        entityAux.refreshToken = entity.refreshToken
        entityAux.userId = entity.userId
        entityAux.validFrom = entity.validFrom

        entityManager.transaction.commit()

        return entityAux
    }

    @Transactional
    override fun remove(entity: RefreshToken) {
        entityManager.remove(if (entityManager.contains(entity)) entity else entityManager.merge(entity))
    }

    override fun findAll(): List<RefreshToken> {
        return entityManager.createQuery("from RefreshToken", RefreshToken::class.java).resultList.filterIsInstance<RefreshToken>()
    }

    fun findByUserId(idEntity: Long): List<RefreshToken>? {
        val result: List<RefreshToken> = entityManager.createQuery("from RefreshToken where userId = :idEntity", RefreshToken::class.java)
                                                      .setParameter("idEntity", idEntity).resultList

        return if (result.isNotEmpty()) {
            result
        } else {
            null
        }
    }

    fun findByRefreshToken(idEntity: String): RefreshToken? {
        val result: List<RefreshToken> = entityManager.createQuery("from RefreshToken where refreshToken = :idEntity", RefreshToken::class.java)
                                                      .setParameter("idEntity", idEntity).resultList

        return if (result.isNotEmpty()) {
            result[0]
        } else {
            null
        }
    }

    override fun close() {
        entityManager.close()
    }
}
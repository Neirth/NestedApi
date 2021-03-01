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
import io.neirth.nestedapi.authentication.domain.RefreshToken
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.persistence.EntityManager

@ApplicationScoped
class CredentialsRepo: RepositoryDao<Credential> {
    @Inject
    internal lateinit var entityManager: EntityManager

    override fun insert(entity: Credential): Credential {
        entityManager.persist(entity)

        return entity
    }

    override fun update(entity: Credential): Credential {
        val entityAux : Credential = entityManager.find(Credential::class.java, entity.userId)

        entityManager.transaction.begin()

        entityAux.userId = entity.userId
        entityAux.password = entity.password

        entityManager.transaction.commit()

        return entityAux
    }

    override fun remove(entity: Credential) {
        entityManager.remove(entity)
    }

    override fun findAll(): List<Credential> {
        return entityManager.createQuery("from Credentials").resultList.filterIsInstance<Credential>()
    }

    override fun findById(idEntity: Long): Credential {
        return entityManager.createQuery("from Credentials where userId = :idEntity")
                            .setParameter("idEntity", idEntity).resultList[0] as Credential
    }

    fun findByUsername(idEntity: String): Credential {
        return entityManager.createQuery("from Credentials where username = :idEntity")
                            .setParameter("idEntity", idEntity).resultList[0] as Credential
    }

    override fun close() {
        entityManager.close()
    }
}
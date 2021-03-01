package io.neirth.nestedapi.authentication.repository

import io.neirth.nestedapi.authentication.domain.RefreshToken
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.persistence.EntityManager

@ApplicationScoped
class RefreshTokenRepo : RepositoryDao<RefreshToken> {
    @Inject
    internal lateinit var entityManager: EntityManager

    override fun insert(entity: RefreshToken): RefreshToken {
        entityManager.persist(entity)

        return entity
    }

    override fun update(entity: RefreshToken): RefreshToken {
        val entityAux : RefreshToken = entityManager.find(RefreshToken::class.java, entity.userId)

        entityManager.transaction.begin()

        entityAux.refreshToken = entity.refreshToken
        entityAux.userId = entity.userId
        entityAux.validFrom = entity.validFrom

        entityManager.transaction.commit()

        return entityAux
    }

    override fun remove(entity: RefreshToken) {
        entityManager.remove(entity)
    }

    override fun findAll(): List<RefreshToken> {
        return entityManager.createQuery("from RefreshTokens").resultList.filterIsInstance<RefreshToken>()
    }

    override fun findById(idEntity: Long): RefreshToken {
        return entityManager.createQuery("from RefreshTokens where userId = :idEntity")
                            .setParameter("idEntity", idEntity).resultList[0] as RefreshToken
    }

    override fun close() {
        entityManager.close()
    }
}
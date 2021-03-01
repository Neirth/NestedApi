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

    override fun close() {
        entityManager.close()
    }
}
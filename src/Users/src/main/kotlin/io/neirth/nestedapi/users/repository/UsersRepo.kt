package io.neirth.nestedapi.users.repository

import io.neirth.nestedapi.users.domain.User
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.persistence.EntityManager

@ApplicationScoped
class UsersRepo : RepositoryDao<User> {
    @Inject
    internal lateinit var entityManager: EntityManager

    override fun insert(entity: User): User {
        entityManager.persist(entity)
        return entity
    }

    override fun update(entity: User): User {
        entityManager.find(User::class.java, entity.id)

        return entity
    }

    override fun remove(entity: User) {
        entityManager.remove(entity)
    }

    override fun findAll(): List<User> {
       return entityManager.createQuery("from users").resultList.filterIsInstance<User>()
    }

    override fun findById(idEntity: Long): User {
        return entityManager.createQuery("from users where id = :idEntity")
                            .setParameter("idEntity", idEntity).resultList[0] as User
    }


    override fun close() {
        entityManager.close()
    }
}
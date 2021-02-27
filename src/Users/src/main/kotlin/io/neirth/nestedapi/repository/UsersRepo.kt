package io.neirth.nestedapi.repository

import io.neirth.nestedapi.domain.User
import io.neirth.nestedapi.util.sessionFactory
import org.hibernate.Session

class UsersRepo : RepositoryDao<User> {
    private val session: Session = sessionFactory().openSession()

    override fun insert(entity: User): User {
        session.save(entity)

        return entity
    }

    override fun update(entity: User): User {
        session.update(entity)

        return entity
    }

    override fun remove(entity: User) {
        session.remove(entity)
    }

    override fun findAll(): List<User> {
       return session.createQuery("from users")
                     .list().filterIsInstance<User>()
    }

    override fun findById(idEntity: Long): User {
        return session.createQuery("from users where id = :idEntity")
                      .setParameter("idEntity", idEntity).uniqueResult() as User
    }

    override fun insertOrUpdate(entity: User): User {
        session.saveOrUpdate(entity)

        return entity
    }
}
package io.neirth.nestedapi.util

import org.hibernate.LazyInitializationException
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration


private var sessionFactory: SessionFactory? = null

fun sessionFactory() : SessionFactory {
    if (sessionFactory == null) {
        throw LazyInitializationException("[!] No could initialize a session factory")
    }

    return sessionFactory as SessionFactory
}

fun sessionFactory(cfg: Configuration) : SessionFactory {
    if (sessionFactory == null) {
        sessionFactory = cfg.buildSessionFactory()
    }

    return sessionFactory as SessionFactory
}

fun closeSession(): Unit {
    sessionFactory?.close()
}

fun processJwtToken(jwtToken : String) : Map<String, Any?> {
    TODO("Builds the function")
}

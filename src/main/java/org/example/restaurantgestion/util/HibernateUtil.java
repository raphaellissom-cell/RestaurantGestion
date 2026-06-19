package org.example.restaurantgestion.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Singleton helper to obtain a JPA {@link EntityManager}.
 * The persistence unit is defined in {@code src/main/resources/META-INF/persistence.xml}.
 */
public class HibernateUtil {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("restaurantPU");

    private HibernateUtil() {}

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void shutdown() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}

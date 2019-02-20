package com.hstar.exeo.server.repos;

import com.hstar.exeo.objects.db.ExeoUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Access to rdbms user objects
 * Created by Saswat on 7/31/2015.
 */
@Repository
public interface ExeoUserRepository extends CrudRepository<ExeoUser, Long> {

    ExeoUser findByEmailIgnoreCase(String email);

    ExeoUser findByUuidIgnoreCase(String uuid);

}

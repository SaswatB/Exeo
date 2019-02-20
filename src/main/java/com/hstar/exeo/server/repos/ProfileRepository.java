package com.hstar.exeo.server.repos;

import com.hstar.exeo.objects.db.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Access to rdbms profile objects
 * Created by Saswat on 8/10/2015.
 */
@Repository
public interface ProfileRepository extends CrudRepository<Profile, Long> {

    Profile findByUserId(long user_id);

}

package com.hstar.exeo.server.repos;

import com.hstar.exeo.objects.db.Device;
import com.hstar.exeo.objects.db.ExeoUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Access to rdbms device objects
 * Created by Saswat on 8/16/2015.
 */
@Repository
public interface DeviceRepository extends CrudRepository<Device, Long> {

    List<Device> findByUser(ExeoUser user);
    Device findByUserAndToken(ExeoUser user, String token);
    Device findByPublicId(String publicId);
}

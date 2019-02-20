package com.hstar.exeo.server.repos;

import com.hstar.exeo.objects.db.Device;
import com.hstar.exeo.objects.db.DevicePair;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Access to rdbms pair objects
 * Created by Saswat on 1/27/2017.
 */
@Repository
public interface DevicePairRepository extends CrudRepository<DevicePair, Long> {

    List<DevicePair> findByDevice1(Device device1);
    List<DevicePair> findByDevice2(Device device2);

    default List<DevicePair> getAllDevicePairs(Device device) {
        ArrayList<DevicePair> devices = new ArrayList<>();
        devices.addAll(findByDevice1(device));
        devices.addAll(findByDevice2(device));
        return devices;
    }

}

package com.hstar.exeo.objects.db;

import javax.persistence.*;

/**
 * Representation of a device in the rdbms
 * Created by Saswat on 1/27/2017.
 */
@Entity
@Table(name = "device_pairs")
public class DevicePair {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "device_pair_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "device_id_1")
    private Device device1;

    @ManyToOne
    @JoinColumn(name = "device_id_2")
    private Device device2;

    protected DevicePair() {}

    public DevicePair(Device device1, Device device2) {
        this.device1 = device1;
        this.device2 = device2;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Device getDevice1() {
        return device1;
    }

    public void setDevice1(Device device1) {
        this.device1 = device1;
    }

    public Device getDevice2() {
        return device2;
    }

    public void setDevice2(Device device2) {
        this.device2 = device2;
    }
}

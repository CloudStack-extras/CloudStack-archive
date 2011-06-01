package com.cloud.network.dao;

import java.util.List;

import junit.framework.TestCase;

import com.cloud.utils.Pair;
import com.cloud.utils.component.ComponentLocator;


public class IPAddressDaoImplTest extends TestCase{
    public void testJoin() {
        
        IPAddressDaoImpl ipDao = ComponentLocator.inject(IPAddressDaoImpl.class);
        
        List<Pair<String, String>> result = ipDao.findAllElasticIpsForElasticIpVm(5);
        for (Pair<String, String> r: result) {
            System.out.println("Public: " + r.first() + " Guest:" + r.second());
        }
    }
}

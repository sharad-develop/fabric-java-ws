package com.acme.controller;

import com.acme.model.AcmeCar;
import com.acme.model.AcmeUser;
import com.acme.service.CarService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rest API. localhost:8080/car/*
 */
@RequestMapping("/car")
@RestController
public class CarController {

    private static final Logger log = Logger.getLogger(CarController.class);

    @Autowired
    CarService carService;


    @RequestMapping(value = "/enroll/admin", method = RequestMethod.POST)
    public void enrollAdmin(@RequestParam String name, @RequestParam String secret) throws Exception {
        AcmeUser admin = carService.getOrCreateAdmin(name, secret);
        log.info(admin);
    }

    @RequestMapping(value = "/enroll/user", method = RequestMethod.POST)
    public void enrollUser(@RequestParam String adminName, @RequestParam String userName) throws Exception {
        AcmeUser user = carService.getOrCreateUser(adminName, userName);
        log.info(user);
    }

    @RequestMapping(value = "/query/all", method = RequestMethod.GET)
    public @ResponseBody List<AcmeCar> queryAllFabCars() throws Exception {
        return carService.queryAllFabcars();

    }

    @RequestMapping(value = "/query/{key}", method = RequestMethod.GET)
    public @ResponseBody List<AcmeCar> queryFabCar(@PathVariable String key) throws Exception {
        return carService.queryFabcar(key);

    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@RequestBody AcmeCar car) throws Exception {
       return carService.createCar(car);

    }
}

package com.carservice.vehicles.service;

import com.carservice.vehicles.client.maps.MapsClient;
import com.carservice.vehicles.client.prices.PriceClient;
import com.carservice.vehicles.domain.car.Car;
import com.carservice.vehicles.domain.Location;
import com.carservice.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final PriceClient priceClient;
    private final MapsClient mapsClient;


    public CarService(CarRepository repository, PriceClient priceClient, MapsClient mapsClient) {
        this.repository = repository;
        this.priceClient = priceClient;
        this.mapsClient = mapsClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Optional<Car> carOptional = repository.findById(id);
        if (carOptional.isPresent()){
            throw new CarNotFoundException();
        }
        Car car = carOptional.get();
        String price = priceClient.getPrice(id);
        car.setPrice(price);
        Location location = mapsClient.getAddress(car.getLocation());
        car.setLocation(location);
        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Optional<Car> carOptional = repository.findById(id);
        if (carOptional.isPresent()){
            throw new CarNotFoundException();
        }
        Car car = carOptional.get();
        repository.delete(car);
    }
}

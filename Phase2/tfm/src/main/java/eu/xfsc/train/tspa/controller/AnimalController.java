package eu.xfsc.train.tspa.controller;

import eu.xfsc.train.tspa.model.Animal;
import eu.xfsc.train.tspa.services.AnimalService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ttfm/api/v1/regitrust/trustlist/animals")
public class AnimalController {

    private static final Logger log = LoggerFactory.getLogger(TrustFrameWorkPublishController.class);

    @Autowired
    private AnimalService animalService;

    @PostMapping
    public ResponseEntity<Animal> addAnimal(@RequestBody Animal animal) {
        log.info("Adding animal: {}", animal);  
        log.info("just testing speed once again");
        Animal savedAnimal = animalService.addAnimal(animal);
        return new ResponseEntity<>(savedAnimal, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<Animal>> getAllAnimals() {
        log.info("just testing speed once again from the GET");
        log.debug("Getting all animals...");
        List<Animal> animals = animalService.getAllAnimals();
        log.debug("animals: {}", animals);
        return new ResponseEntity<>(animals, HttpStatus.OK);
    }
}
package eu.xfsc.train.tspa.services;

import eu.xfsc.train.tspa.model.Animal;
import eu.xfsc.train.tspa.controller.TrustFrameWorkPublishController;
import eu.xfsc.train.tspa.interfaces.IAnimalRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimalService {

    private static final Logger log = LoggerFactory.getLogger(TrustFrameWorkPublishController.class);

    @Autowired
    private IAnimalRepository animalRepository;

    public Animal addAnimal(Animal animal) {
        log.info("Attempting to save animal: {}", animal);
        try {
            Animal savedAnimal = animalRepository.save(animal);
            log.info("Successfully saved animal: {}", savedAnimal);
            return savedAnimal;
        } catch (DataAccessException e) {
            log.error("Error saving animal to database", e);
            throw e;
        }
    }

    public List<Animal> getAllAnimals() {
        log.info("Attempting to retrieve all animals");
        try {
            List<Animal> animals = animalRepository.findAll();
            log.info("Retrieved {} animals", animals.size());
            return animals;
        } catch (DataAccessException e) {
            log.error("Error retrieving animals from database", e);
            throw e;
        }
    }
}
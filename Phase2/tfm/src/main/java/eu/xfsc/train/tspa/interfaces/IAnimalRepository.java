package eu.xfsc.train.tspa.interfaces;
import eu.xfsc.train.tspa.model.Animal;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IAnimalRepository extends MongoRepository<Animal, String> {
    
}

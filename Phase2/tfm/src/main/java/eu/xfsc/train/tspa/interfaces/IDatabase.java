package eu.xfsc.train.tspa.interfaces;
import eu.xfsc.train.tspa.model.trustlist.TrustServiceStatusList;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IDatabase extends MongoRepository<TrustServiceStatusList, String> {
    
}

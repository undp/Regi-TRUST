package eu.xfsc.train.tspa.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "testCollection")
public class Animal {
    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("species")
    private String species;

    // Default constructor
    public Animal() {}

    // Constructor with fields
    public Animal(String name, String species) {
        this.name = name;
        this.species = species;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "Animal{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", species='" + species + '\'' +
                '}';
    }
}

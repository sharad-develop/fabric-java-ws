package com.acme.model;


/**
 * Acme car model
 */
public class AcmeCar {

    private String key;
    private String colour;
    private String make;
    private String model;
    private String owner;

    public AcmeCar() {
        this(null, null, null, null, null);
    }

    public AcmeCar(String colour, String make, String model, String owner) {
        this(null, colour, make, model, owner);
    }

    public AcmeCar(String key, String colour, String make, String model, String owner) {
        this.key = key;
        this.colour = colour;
        this.make = make;
        this.model = model;
        this.owner = owner;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AcmeCar car = (AcmeCar) o;

        if (key != null ? !key.equals(car.key) : car.key != null) return false;
        if (colour != null ? !colour.equals(car.colour) : car.colour != null) return false;
        if (make != null ? !make.equals(car.make) : car.make != null) return false;
        if (model != null ? !model.equals(car.model) : car.model != null) return false;
        return owner != null ? owner.equals(car.owner) : car.owner == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (colour != null ? colour.hashCode() : 0);
        result = 31 * result + (make != null ? make.hashCode() : 0);
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AcmeCar{" +
                "key='" + key + '\'' +
                ", colour='" + colour + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}

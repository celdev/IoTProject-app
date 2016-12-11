package iotproj.iotproject.model;

public abstract class Retrievable {

    private int id;
    private RetrievableType retrievableType;

    public Retrievable(RetrievableType retrievableType, String line) throws IncorrectRetrievableException{
        this.retrievableType = retrievableType;
        parseValues(line);
    }

    abstract void parseValues(String line);

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Retrievable that = (Retrievable) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}

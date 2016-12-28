package iotproj.iotproject.model;

/** This class will function as a super class to the classes
 *  that will represent the different kinds of objects the
 *  web server has information about
 *
 *  Currently these are
 *      Devices             i.e. Switches
 *      Sensors             i.e. Temperature sensors
 *      Condition Threads        Threads that are running on the server
 *                               and will be executed when the condition is met,
 *                               and preform an action (i.e. turn on the lights)
 *  All Retrievable have an ID and a type
 *  if the type and ID are equal the objects should be treated as equal
 *  (they represent the same physical object (or ConditionThread))
 *
 *  The values of the Retrievable are parsed in the parseValues-method which are implemented
 *  in the subclasses
 * */
public abstract class Retrievable {

    private int id;
    private RetrievableType retrievableType;

    public Retrievable(RetrievableType retrievableType, String line) throws IncorrectRetrievableException{
        this.retrievableType = retrievableType;
        try {
            parseValues(line);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IncorrectRetrievableException();
        }
    }

    abstract void parseValues(String line) throws Exception;

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

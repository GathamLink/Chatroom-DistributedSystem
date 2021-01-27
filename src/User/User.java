package User;

/**
 * This Class is to store a based information for a user
 */
public class User {

    private int id;//The id for user
    private String ip;//The ip for user
    private String name;//The name for a user

    /**
     * The constructor for this class to initialize the data
     * @param id    The id for user
     * @param ip    The ip for user
     * @param name  The name for a user
     */
    public User(int id, String ip, String name) {
        this.id = id;
        this.ip = ip;
        this.name = name;
    }

    /**
     * The method to return user's id
     * @return  The id for user
     */
    public int getId() {
        return id;
    }

    /**
     * The method to return user's ip
     * @return  The ip for user
     */
    public String getIp() {
        return ip;
    }

    /**
     * The method to return user's name
     * @return  The name for a user
     */
    public String getName() {
        return name;
    }

    /**
     * ToString method to print out the information of a user in a rule
     * @return  Information of a user
     */
    public String toString() {
        return id + "|" + ip + "|" + name;
    }

}

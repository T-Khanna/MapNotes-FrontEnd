package mapnotes.mapnotes.data_classes;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Thomas on 28/12/2017.
 */
public class UserTest {

    @Test
    public void fromJSON() throws Exception {
        String name = "test_name";
        int id = 1;
        String email = "test@email.com";
        JSONObject JSONObj = new JSONObject();
        JSONObj.put("Name", name);
        JSONObj.put("Id", id);
        JSONObj.put("Email", email);
        User user = new User(JSONObj);
        User otherUser = new User(name, email, id);
        if (!user.equals(otherUser)) throw new Exception();
    }
}
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by garvitab on 04-02-2017.
 */
public class CountTotalQueries {

    public static void main(String[] args) {
        int count=0;
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader("timestamps.csv"));
            String line = buffReader.readLine();
            int i=1;
            while (line != null) {
                JSONObject fullObject = new JSONObject(line);
                JSONObject query = fullObject.getJSONObject("Query");
                count+=query.getInt("min");
//                System.out.println("Line "+i+", queries: "+count);
                line=buffReader.readLine();
                i++;
            }
            System.out.println("Total number of queries published : "+count);
            System.out.println("Total number of queries published : "+i);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

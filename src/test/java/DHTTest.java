import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


// This class tests three different possible "DHT" (even though its technically a simple lookup table) problems.
// Problem one, Testing connections have actually occured i.e Alice and Bob have not just DC'd when they want to connect.
// Problem two, Actually testing a username exists on the lookup table before someone calls themself it. 
// Problem three, Fixing any potential merge issues i.e. two users choose the same name at the same time (basically a race condition)


public class DHTTest {

    private DHT dht;

    @BeforeEach
    void setup() {
        dht = new DHT();
    }
    
    @Test
    void testName(){

    }
}

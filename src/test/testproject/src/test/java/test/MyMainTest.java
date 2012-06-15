package test;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MyMainTest {
    MyMain myMain;
    
    @Before
    public void setUp() throws Exception
    {
            myMain = new MyMain();
    }
    
    @Test
    public void returnHelloReturnsHello() {
        assertEquals("Not hello", "Hello", myMain.returnHello());
    }
    
    @Test
    public void returnHelloReturnsPsycho() {
        assertEquals("Not hello #1234#", "Psycho", myMain.returnHello());
    }

}

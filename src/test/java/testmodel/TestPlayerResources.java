package testmodel;

import org.junit.Before;
import org.junit.Test;

import it.polimi.LM39.exception.NotEnoughResourcesException;
import it.polimi.LM39.model.PlayerResources;
import junit.framework.TestCase;
/**
 * test player resources class
 *
 */
public class TestPlayerResources extends TestCase{

	PlayerResources testResources;
	
	@Before
	public void setUp(){
		testResources = new PlayerResources();
	}
	/**
	 * test resources get and set with a valid value
	 * @throws NotEnoughResourcesException
	 */
	@Test
	public void testResources() throws NotEnoughResourcesException{
		Integer testValue = 3;
		testResources.setCoins(testValue);
		assertEquals(testResources.getCoins(),testValue);
		testResources.setServants(testValue);
		assertEquals(testResources.getServants(),testValue);
		testResources.setWoods(testValue);
		assertEquals(testResources.getWoods(),testValue);
		testResources.setStones(testValue);
		assertEquals(testResources.getStones(),testValue);
		testResources.setCouncil(testValue);
		assertEquals(testResources.getCouncil(),testValue);
	}
	/**
	 * test get and set resources with an invalid value to test the exception
	 */
	@Test
	public void testResourcesException() {
		Integer negativeValue = -1;
	    try {
	        testResources.setCoins(negativeValue);
	        fail("Expected a NotEnoughResourcesException");
	    } catch (NotEnoughResourcesException NotEnoughResourcesException) {
	        assertTrue(("Not enough coins!").equals(NotEnoughResourcesException.getMessage()));
	    }
	    try {
	        testResources.setWoods(negativeValue);
	        fail("Expected a NotEnoughResourcesException");
	    } catch (NotEnoughResourcesException NotEnoughResourcesException) {
	        assertTrue(("Not enough woods!").equals(NotEnoughResourcesException.getMessage()));
	    }
	    try {
	        testResources.setStones(negativeValue);
	        fail("Expected a NotEnoughResourcesException");
	    } catch (NotEnoughResourcesException NotEnoughResourcesException) {
	        assertTrue(("Not enough stones!").equals(NotEnoughResourcesException.getMessage()));
	    }
	    try {
	        testResources.setServants(negativeValue);
	        fail("Expected a NotEnoughResourcesException");
	    } catch (NotEnoughResourcesException NotEnoughResourcesException) {
	        assertTrue(("Not enough servants!").equals(NotEnoughResourcesException.getMessage()));
	    }
	}
}
